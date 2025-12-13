package imd.ufrn.br.integrator_service.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class IntegratorController {

    private static final Logger logger = LoggerFactory.getLogger(IntegratorController.class);

    private final WebClient.Builder internalClientBuilder;
    private final WebClient weatherClient;

    public IntegratorController(WebClient.Builder internalClientBuilder) {
        this.internalClientBuilder = internalClientBuilder;
        this.weatherClient = WebClient.builder()
                .baseUrl("https://api.open-meteo.com")
                .build();
    }

    @GetMapping("/integrate/weather")
    @CircuitBreaker(name = "externalService", fallbackMethod = "fallbackResponse")
    public Mono<String> getExternalData(@RequestParam(defaultValue = "-5.79") double lat,
                                        @RequestParam(defaultValue = "-35.21") double lon) {
        logger.info("Calling External API (OpenMeteo)...");
        return weatherClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", lat)
                        .queryParam("longitude", lon)
                        .queryParam("current_weather", "true")
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping("/integrate/travel-agent")
    @CircuitBreaker(name = "orchestrator", fallbackMethod = "fallbackTravelAgent")
    public Mono<String> travelAgent(@RequestParam(defaultValue = "Natal") String city,
                                    @RequestParam(defaultValue = "-5.79") double lat,
                                    @RequestParam(defaultValue = "-35.21") double lon) {

        logger.info("Starting Travel Agent Orchestration for {}", city);

        return getExternalData(lat, lon).flatMap(weatherJson -> {
                    String prompt = "Atue como um guia de viagem. O clima atual em " + city + " é: " + weatherJson +
                            ". Com base nisso, sugira 1 atividade incrível para um turista. Seja conciso.";

                    return internalClientBuilder.build()
                            .get()
                            .uri("http://AI-SERVICE/ai/generate?prompt={prompt}", prompt)
                            .retrieve()
                            .bodyToMono(String.class);
                })
                .flatMap(aiResponse -> {
                    return internalClientBuilder.build()
                            .post()
                            .uri("http://SERVERLESS-SERVICE/uppercase")
                            .bodyValue("SUGESTÃO DE VIAGEM: " + aiResponse)
                            .retrieve()
                            .bodyToMono(String.class);
                });
    }

    public Mono<String> fallbackResponse(double lat, double lon, Throwable t) {
        return Mono.just("{\"error\": \"Serviço de Clima Indisponível\", \"details\": \"" + t.getMessage() + "\"}");
    }

    public Mono<String> fallbackTravelAgent(String city, double lat, double lon, Throwable t) {
        logger.error("Orchestration failed: {}", t.getMessage());
        return Mono.just("{\"fallback\": true, \"message\": \"O Agente de Viagens está offline.\", \"error\": \"" + t.getMessage() + "\"}");
    }
}