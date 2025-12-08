package imd.ufrn.br.integrator_service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
public class IntegratorController {

    private final WebClient.Builder webClientBuilder;
    private final CircuitBreaker circuitBreaker;

    public IntegratorController(
            WebClient.Builder webClientBuilder,
            CircuitBreakerFactory circuitBreakerFactory) {

        this.webClientBuilder = webClientBuilder;
        // Create a Circuit Breaker instance named 'aiService'
        this.circuitBreaker = circuitBreakerFactory.create("aiService");
    }

    @GetMapping("/integrate/ai")
    public String integrateAI(@RequestParam(defaultValue = "Tell me a fun fact") String prompt) {

        // Use the circuit breaker wrapper around the WebClient call
        return circuitBreaker.run(
                () -> callAiService(prompt),
                throwable -> fallbackMethod(prompt, throwable)
        );
    }

    private String callAiService(String prompt) {
        return webClientBuilder.build()
                // Use the service ID from Eureka (lb://AI-SERVICE)
                .get().uri("lb://AI-SERVICE/ai/generate?prompt={prompt}", prompt)
                .retrieve()
                .bodyToMono(String.class)
                .block(); // Block is acceptable in a simple demo controller
    }

    private String fallbackMethod(String prompt, Throwable t) {
        System.err.println("Circuit Breaker OPEN or call failed: " + t.getMessage());
        return "FALLBACK RESPONSE: The AI Service is currently unavailable. " +
                "Please try again later. (Error: " + t.getClass().getSimpleName() + ")";
    }
}