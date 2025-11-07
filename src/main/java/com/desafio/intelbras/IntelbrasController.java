package com.desafio.intelbras;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chatbot")
public class IntelbrasController {

    private final IntelbrasChatGPTService chatService;

    public IntelbrasController(IntelbrasChatGPTService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public String conversar(@RequestBody String pergunta) {
        return chatService.processarPergunta(pergunta);
    }
}
