package com.desafio.intelbras;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/intelbras")
public class IntelbrasController {

    private final IntelbrasGeminiService geminiService;

    public IntelbrasController(IntelbrasGeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping
    public ResponseEntity<String> processarPergunta(@RequestBody PromptRequest request) {
        String resposta = geminiService.processarPergunta(request.pergunta());
        return ResponseEntity.ok(resposta);
    }
}
