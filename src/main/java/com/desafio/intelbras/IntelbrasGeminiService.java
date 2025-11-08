package com.desafio.intelbras;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class IntelbrasGeminiService {

    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public IntelbrasGeminiService(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://generativelanguage.googleapis.com/v1beta").build();
    }

    public String processarPergunta(String pergunta) {
        try {
            String prompt = """
                    Você é um assistente técnico da Intelbras.

                    Funções:
                    1️⃣ Se a pergunta for sobre um problema, recomende produtos da Intelbras que solucionem o caso.
                        - Pesquise no site oficial: https://www.intelbras.com/pt-br
                        - Traga até 5 produtos com: nome e breve descrição.

                    2️⃣ Se a pergunta envolver cursos, busque treinamentos no portal ITEC:
                        https://intec.intelbras.com.br/
                        - Traga até 5 cursos com nome e breve descrição.

                    3️⃣ Se a pergunta for "como montar/configurar/instalar algo da Intelbras",
                        acesse os manuais oficiais (https://www.intelbras.com/pt-br/ajuda/manuais)
                        e descreva o passo a passo de instalação.

                    4️⃣ Retorne SOMENTE JSON válido, neste formato:
                    {
                      "produtos": [
                        {"nome": "string", "descricao": "string"}
                      ],
                      "cursos": [
                        {"nome": "string", "descricao": "string"}
                      ],
                      "manual": {
                        "produto": "string",
                        "passos": ["passo 1", "passo 2", "..."]
                      }
                    }

                    Pergunta do usuário: %s
                    """.formatted(pergunta);

            String requestBody = """
                {
                  "contents": [
                    {
                      "parts": [
                        {"text": "%s"}
                      ]
                    }
                  ]
                }
                """.formatted(prompt.replace("\"", "'"));

            String response = webClient.post()
                    .uri("/models/gemini-2.0-flash:generateContent?key=" + geminiApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return extrairTexto(response);

        } catch (WebClientResponseException e) {
            return "{\"erro\": \"Falha ao processar: " + e.getStatusCode() + " - " +
                    e.getResponseBodyAsString().replace("\"", "'") + "\"}";
        } catch (Exception e) {
            return "{\"erro\": \"Falha ao processar: " + e.getMessage().replace("\"", "'") + "\"}";
        }
    }

    private String extrairTexto(String resposta) {
        try {
            JsonNode root = mapper.readTree(resposta);
            JsonNode candidates = root.path("candidates").get(0);
            String texto = candidates.path("content").path("parts").get(0).path("text").asText();
            return texto.replace("```json", "").replace("```", "").trim();
        } catch (Exception e) {
            return "{\"erro\": \"Falha ao interpretar resposta: " +
                    e.getMessage().replace("\"", "'") + "\"}";
        }
    }
}
