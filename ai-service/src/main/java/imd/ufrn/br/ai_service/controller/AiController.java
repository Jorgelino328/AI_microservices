package imd.ufrn.br.ai_service.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiController {

    private final ChatClient chatClient;

    public AiController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/ai/generate")
    public String generate(@RequestParam(defaultValue = "Tell me a joke") String prompt) {
        return chatClient.prompt().user(prompt).call().content();
    }
}