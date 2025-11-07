package com.desafio.intelbras;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class IntelbrasChatGPTService {

    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${openai.api.key}")
    private String openAiApiKey;

    public IntelbrasChatGPTService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.openai.com/v1")
                .build();
    }

    public String processarPergunta(String pergunta) {
        try {
            String prompt = """
                    Você é um assistente técnico da Intelbras.

                    Funções:
                    1️⃣ Se a pergunta for sobre um problema, recomende produtos da Intelbras que solucionem o caso.
                        - Pesquise no site oficial: https://www.intelbras.com/pt-br
                        - Traga até 5 produtos com: nome, descrição e link real.
                        - Verifique se os links são válidos (começam com https://www.intelbras.com/pt-br/).

                    2️⃣ Se a pergunta envolver cursos, busque treinamentos no portal ITEC:
                        https://intec.intelbras.com.br/
                        - Traga até 5 cursos com nome, descrição e link real.

                    3️⃣ Se a pergunta for "como montar/configurar/instalar algo da Intelbras",
                        acesse os manuais oficiais (https://www.intelbras.com/pt-br/ajuda/manuais)
                        e extraia o passo a passo de instalação.

                    4️⃣ Retorne SOMENTE JSON válido, neste formato:
                    {
                      "produtos": [
                        {"nome": "string", "descricao": "string", "link": "https://..."}
                      ],
                      "cursos": [
                        {"nome": "string", "descricao": "string", "link": "https://..."}
                      ],
                      "manual": {
                        "produto": "string",
                        "passos": ["passo 1", "passo 2", "..."]
                      }
                    }

                    Pergunta do usuário: %s
                    """.formatted(pergunta);

            ObjectNode messageSystem = mapper.createObjectNode();
            messageSystem.put("role", "system");
            messageSystem.put("content", "Você é um assistente técnico especializado em produtos Intelbras.");

            ObjectNode messageUser = mapper.createObjectNode();
            messageUser.put("role", "user");
            messageUser.put("content", prompt);

            ArrayNode messages = mapper.createArrayNode();
            messages.add(messageSystem);
            messages.add(messageUser);

            ObjectNode body = mapper.createObjectNode();
            body.put("model", "gpt-4o-mini");
            body.set("messages", messages);
            body.put("temperature", 0.7);

            String requestBody = mapper.writeValueAsString(body);

            // 🚀 Envia a requisição
            String resposta = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return extrairJsonLimpo(resposta);

        } catch (WebClientResponseException e) {
            return "{\"erro\": \"Falha ao processar: " + e.getStatusCode() + " - " + e.getResponseBodyAsString().replace("\"", "'") + "\"}";
        } catch (Exception e) {
            return "{\"erro\": \"Falha ao processar: " + e.getMessage().replace("\"", "'") + "\"}";
        }
    }

    private String extrairJsonLimpo(String resposta) {
        try {
            JsonNode root = mapper.readTree(resposta);
            JsonNode content = root.at("/choices/0/message/content");

            if (content.isMissingNode()) {
                return "{\"erro\": \"Resposta inesperada do modelo.\"}";
            }

            String texto = content.asText()
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            texto = texto.replaceAll("(?s).*?(\\{.*\\}).*", "$1");

            mapper.readTree(texto);
            return texto;

        } catch (Exception e) {
            return "{\"erro\": \"Falha ao processar resposta: " + e.getMessage().replace("\"", "'") + "\"}";
        }
    }
}
