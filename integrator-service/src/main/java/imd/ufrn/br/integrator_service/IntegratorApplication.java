package imd.ufrn.br.integrator_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@EnableDiscoveryClient
public class IntegratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegratorApplication.class, args);
    }

    // Bean for load-balanced WebClient
    @Bean
    @LoadBalanced // Use LoadBalancer (Ribbon/LoadBalancerClient) to find "AI-SERVICE" in Eureka
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
}