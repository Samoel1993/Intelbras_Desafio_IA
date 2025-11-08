import streamlit as st
import requests
import json 

# (Bloco de CSS removido, conforme solicitado)

def sua_funcao_rag_intelbras(pergunta_usuario):
    """
    Esta função agora CHAMA o seu backend Java Spring Boot.
    (Versão 12 - Lendo a 'explicacao' consultiva)
    """
    
    URL_BACKEND = "http://localhost:8080/api/intelbras" 
    payload = {"pergunta": pergunta_usuario} 
    
    try:
        response = requests.post(URL_BACKEND, json=payload, timeout=30)
        response_text = response.text.strip()
        
        # Trecho de Limpeza de JSON (do Gemini)
        print(f"DEBUG: Resposta Bruta do Backend:\n{response_text}") 

        cleaned_text = response_text
        if cleaned_text.startswith("```json"):
            cleaned_text = cleaned_text.split("```json\n", 1)[-1].rsplit("\n```", 1)[0]
        elif cleaned_text.startswith("```"):
            cleaned_text = cleaned_text.split("```\n", 1)[-1].rsplit("\n```", 1)[0]
        
        print(f"DEBUG: Resposta Após Limpeza:\n{cleaned_text}") 
        
        data = json.loads(cleaned_text)

        if "erro" in data:
            return f"**Ocorreu um erro no Backend:**\n`{data['erro']}`"

        # --- AQUI ESTÁ A MUDANÇA ---
        # Agora nós lemos o campo 'explicacao' primeiro
        explicacao = data.get("explicacao", "").strip()
        
        produtos_md = ""
        produtos = data.get("produtos", [])
        if produtos:
            produtos_md = "### Produtos Recomendados\n"
            for prod in produtos:
                descricao_prod = prod.get('descricao', 'Sem descrição.').strip() 
                produtos_md += f"**{prod.get('nome')}**\n"
                produtos_md += f"* {descricao_prod}\n\n" 
        
        cursos_md = ""
        cursos = data.get("cursos", [])
        if cursos:
            cursos_md = "### Cursos Sugeridos (Itec)\n"
            for curso in cursos:
                descricao_curso = curso.get('descricao', 'Sem descrição.').strip()
                cursos_md += f"**{curso.get('nome')}**\n"
                cursos_md += f"* {descricao_curso}\n\n"
        
        manual_md = ""
        manual_obj = data.get("manual", {})
        passos = manual_obj.get("passos", [])
        
        if passos: 
            produto_manual = manual_obj.get("produto", "do produto")
            manual_md = f"### Passo a Passo: Instalação {produto_manual}\n"
            for i, passo in enumerate(passos):
                manual_md += f"**{i+1}.** {passo.strip()}\n" 
            manual_md += "\n"


        # --- Montagem Final da Resposta (COM A EXPLICAÇÃO) ---
        resposta_formatada_linhas = []
        
        # Adiciona a explicação no topo
        if explicacao:
            resposta_formatada_linhas.append(explicacao)
        
        if produtos_md:
            resposta_formatada_linhas.append(produtos_md)
            
        if cursos_md:
            resposta_formatada_linhas.append(cursos_md)
            
        if manual_md:
            resposta_formatada_linhas.append(manual_md)
        
        if not resposta_formatada_linhas:
            return "Não encontrei produtos, cursos ou manuais para sua solicitação. Tente reformular sua pergunta."

        # Junta tudo com quebras de linha
        resposta_formatada = "\n\n".join(resposta_formatada_linhas)

        return resposta_formatada

    except requests.exceptions.ConnectionError:
        return "**Erro de Conexão!** 🔌\n\nNão consegui falar com o backend. Você tem certeza que o servidor Java (Spring Boot) está rodando no `http://localhost:8080`?"
    except json.JSONDecodeError:
        return f"**Erro de Decodificação!** 😵‍💫\n\O backend não retornou um JSON válido (mesmo após a limpeza).\n\nA resposta (após a limpeza) foi:\n\n`{cleaned_text}`"
    except Exception as e:
        return f"Ocorreu um erro inesperado no frontend: {e}"

# --- O RESTO DO CÓDIGO (AVATAR, ETC) CONTINUA O MESMO ---

# (Lembre-se de ter a pasta 'img' com 'intelbras.jpg')
BOT_LOGO = "img/intelbras.jpg" 

# Configuração da Página do Streamlit
st.set_page_config(page_title="Consultor IA Intelbras", page_icon=BOT_LOGO)
st.title("🤖 Carinha")
st.caption("HackAIthon - Equipe: Cara") 

# Inicializa o histórico do chat na sessão
if "messages" not in st.session_state:
    st.session_state.messages = [
        {"role": "assistant", "content": "Olá! Sou o consultor de IA da Intelbras. Como posso ajudar você a encontrar a solução de segurança ideal?"}
    ]

# Exibe as mensagens do histórico
for message in st.session_state.messages:
    # Escolhe o avatar com base na role
    if message["role"] == "assistant":
        with st.chat_message("assistant", avatar=BOT_LOGO):
            st.markdown(message["content"])
    else:
        with st.chat_message("user"):
            st.markdown(message["content"])

# Pega a nova pergunta do usuário
if prompt := st.chat_input("Ex: 'Qual alarme ideal para uma loja pequena?'"):
    
    st.session_state.messages.append({"role": "user", "content": prompt})
    with st.chat_message("user"):
        st.markdown(prompt)

    # Adiciona o avatar nas novas respostas também
    with st.chat_message("assistant", avatar=BOT_LOGO):
        with st.spinner("Analisando o portfólio Intelbras (consultando o backend Java)..."):
            response = sua_funcao_rag_intelbras(prompt)
            st.markdown(response, unsafe_allow_html=True) 
    
    st.session_state.messages.append({"role": "assistant", "content": response})