# Desafio_intelbras_IA


🎯 Objetivo
Desenvolver um Agente de IA Consultivo capaz de auxiliar clientes e parceiros da Intelbras na busca por soluções em segurança eletrônica.

O agente deve fornecer informações precisas e direcionadas, utilizando como base o portfólio de produtos do site oficial da Intelbras e os cursos de capacitação disponíveis no portal Intelbras Itec.

✨ Tecnologias
Linguagem de Programação: Java 25

Modelo de IA/Framework:  Gemini, Java (Spring Boot)

Serviço: Aplicação backend rodando em Java.

⚙️ Endpoint de Interação
O agente será acessível através de um endpoint HTTP POST.

Método: POST

URL: http://localhost:8080/chatbot

Corpo da Requisição (JSON Exemplo):

JSON

{
"pergunta": "Quero aprender a configurar câmeras de segurança e escolher o melhor gravador de vídeo da Intelbras."
}

🧠 Escopo de Conhecimento (Fontes de Dados)
O agente deve ser treinado ou ter acesso às informações das seguintes fontes para formular suas respostas:

Site Oficial da Intelbras (Portfólio de Segurança Eletrônica):

Detalhes sobre categorias de produtos (Câmeras IP, Multi HD, Wi-Fi, Gravadores DVR/NVR, Alarmes, Fechaduras Digitais, etc.).

Características e benefícios dos produtos (exemplo: Câmera IM7+ 3MP, Linha Mibo, séries de gravadores, tecnologias como Full Color e Inteligência Artificial - IA).

Sugestões de produtos para diferentes ambientes (residencial, pequenas e médias empresas, condomínios).

Portal de Cursos Intelbras Itec (Cursos de Segurança Eletrônica):

Identificação de cursos relevantes para o tema (CFTV IP, CFTV Multi HD, Configuração de Gravadores DVR/NVR, Dominando IP Utility, Instalação, etc.).

Tipo de curso (Online, Presencial, Carga Horária, Nível, Público-alvo, Gratuidade/Custo - se disponível).

Fornecimento do título do curso e a indicação de onde encontrá-lo (Itec).
