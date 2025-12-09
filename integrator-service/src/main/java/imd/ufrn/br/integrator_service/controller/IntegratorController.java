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

    // 1. The Internal Client (Load Balanced) - Injected by Spring
    private final WebClient.Builder internalClientBuilder;

    // 2. The External Client (Direct) - Created manually
    private final WebClient weatherClient;

    public IntegratorController(WebClient.Builder internalClientBuilder) {
        this.internalClientBuilder = internalClientBuilder;

        // Create a NEW, standard builder for external calls to avoid LoadBalancer issues
        this.weatherClient = WebClient.builder()
                .baseUrl("https://api.open-meteo.com")
                .build();
    }

    // --- EXTERNAL CALL (Weather) ---
    @GetMapping("/integrate/weather")
    @CircuitBreaker(name = "externalService", fallbackMethod = "fallbackResponse")
    public Mono<String> getExternalData(@RequestParam(defaultValue = "-5.79") double lat,
                                        @RequestParam(defaultValue = "-35.21") double lon) {
        logger.info("Calling External API (OpenMeteo)...");
        // Use the 'weatherClient' (Direct connection)
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

    // --- ORCHESTRATION (Travel Agent) ---
    @GetMapping("/integrate/travel-agent")
    @CircuitBreaker(name = "orchestrator", fallbackMethod = "fallbackTravelAgent")
    public Mono<String> travelAgent(@RequestParam(defaultValue = "Natal") String city,
                                    @RequestParam(defaultValue = "-5.79") double lat,
                                    @RequestParam(defaultValue = "-35.21") double lon) {

        logger.info("Starting Travel Agent Orchestration for {}", city);

        // Step 1: Get Weather (Uses External Client)
        return getExternalData(lat, lon).flatMap(weatherJson -> {

                    // Step 2: Call AI Service (Uses Internal Client via Eureka)
                    // FIX: Prompt in Portuguese
                    String prompt = "Atue como um guia de viagem. O clima atual em " + city + " é: " + weatherJson +
                            ". Com base nisso, sugira 1 atividade incrível para um turista. Seja conciso.";

                    return internalClientBuilder.build()
                            .get()
                            // FIX: Uses UPPERCASE ID 'AI-SERVICE'
                            .uri("http://AI-SERVICE/ai/generate?prompt={prompt}", prompt)
                            .retrieve()
                            .bodyToMono(String.class);
                })
                .flatMap(aiResponse -> {
                    // Step 3: Call Serverless Service (Uses Internal Client via Eureka)
                    // FIX: Uses UPPERCASE ID 'SERVERLESS-SERVICE'
                    return internalClientBuilder.build()
                            .post()
                            .uri("http://SERVERLESS-SERVICE/uppercase")
                            .bodyValue("SUGESTÃO DE VIAGEM: " + aiResponse)
                            .retrieve()
                            .bodyToMono(String.class);
                });
    }

    // Fallback for Weather API
    public Mono<String> fallbackResponse(double lat, double lon, Throwable t) {
        return Mono.just("{\"error\": \"Serviço de Clima Indisponível\", \"details\": \"" + t.getMessage() + "\"}");
    }

    // Fallback for Travel Agent
    public Mono<String> fallbackTravelAgent(String city, double lat, double lon, Throwable t) {
        logger.error("Orchestration failed: {}", t.getMessage());
        return Mono.just("{\"fallback\": true, \"message\": \"O Agente de Viagens está offline.\", \"error\": \"" + t.getMessage() + "\"}");
    }
}