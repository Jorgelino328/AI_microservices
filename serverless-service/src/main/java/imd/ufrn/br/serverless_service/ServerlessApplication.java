package imd.ufrn.br.serverless_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.util.function.Function;

@SpringBootApplication
public class ServerlessApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerlessApplication.class, args);
    }

    // This Bean IS the Serverless Function
    // It maps automatically to the endpoint /uppercase
    @Bean
    public Function<String, String> uppercase() {
        return message -> {
            System.out.println("Processing: " + message);
            return "Serverless Response: " + message.toUpperCase();
        };
    }
}