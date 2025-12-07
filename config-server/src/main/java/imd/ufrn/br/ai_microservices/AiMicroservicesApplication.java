package imd.ufrn.br.ai_microservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class AiMicroservicesApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiMicroservicesApplication.class, args);
    }

}