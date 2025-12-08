package imd.ufrn.br.integrator_service.controller;

import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
public class IntegratorController {

    private final WebClient webClient; // Standard WebClient (not load balanced)
    private final CircuitBreaker circuitBreaker;

    public IntegratorController(WebClient.Builder webClientBuilder, CircuitBreakerFactory circuitBreakerFactory) {
        // We use a standard WebClient for external calls (no @LoadBalanced)
        this.webClient = WebClient.builder().baseUrl("https://api.open-meteo.com").build();
        this.circuitBreaker = circuitBreakerFactory.create("externalService");
    }

    @GetMapping("/integrate/weather")
    public String getExternalData(@RequestParam(defaultValue = "-5.79") double lat,
                                  @RequestParam(defaultValue = "-35.21") double lon) { // Default: Natal, RN

        return circuitBreaker.run(
                () -> callThirdPartyApi(lat, lon),
                throwable -> fallbackMethod(throwable)
        );
    }

    private String callThirdPartyApi(double lat, double lon) {
        // Calls the Real Third-Party Weather API
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", lat)
                        .queryParam("longitude", lon)
                        .queryParam("current_weather", "true")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private String fallbackMethod(Throwable t) {
        // This executes when the External API is down or slow (Circuit Breaker Open)
        return "{\"fallback\": true, \"message\": \"External System Unavailable. Returning cached/default data.\", \"error\": \"" + t.getMessage() + "\"}";
    }
}