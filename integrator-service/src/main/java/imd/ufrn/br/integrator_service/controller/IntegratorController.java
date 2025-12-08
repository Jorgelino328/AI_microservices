package imd.ufrn.br.integrator_service.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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
    private final WebClient webClient;

    // We inject a standard WebClient builder (configured in Main class)
    public IntegratorController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.open-meteo.com").build();
    }

    @GetMapping("/integrate/weather")
    @CircuitBreaker(name = "externalService", fallbackMethod = "fallbackResponse")
    @Retry(name = "externalService") // Retry failed requests automatically
    public Mono<String> getExternalData(@RequestParam(defaultValue = "-5.79") double lat,
                                        @RequestParam(defaultValue = "-35.21") double lon) {

        logger.info("Calling External API (OpenMeteo) for Lat: {}, Lon: {}", lat, lon);

        // PRODUCTION IMPLEMENTATION: Calls the real public API
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", lat)
                        .queryParam("longitude", lon)
                        .queryParam("current_weather", "true")
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }

    // Fallback method for Resilience (Circuit Breaker Open or Timeout)
    public Mono<String> fallbackResponse(double lat, double lon, Throwable t) {
        logger.error("External System Failed: {}", t.getMessage());
        return Mono.just("{\"fallback\": true, \"message\": \"External System (OpenMeteo) is unavailable.\", \"reason\": \"" + t.getMessage() + "\"}");
    }
}