# 💰 Gerenciador de Orçamento Inteligente com IA Generativa (Spring AI + Groq)

Este é um projeto de controle de orçamento inteligente construído em **Spring Boot 4.1.0**, **Java 25**, **MySQL** e integrado com **Spring AI**. Ele utiliza modelos de Inteligência Artificial Generativa hospedados na **Groq** para transcrição de áudio (usando Whisper) e orquestração de comandos financeiros (usando Llama 3.3 com Function Calling).

---

## 🚀 Passo a Passo: Como Usar o Projeto

Siga os passos abaixo para configurar e executar a aplicação em seu ambiente local:

### 1. Pré-requisitos

Certifique-se de ter instalado em sua máquina:

* **Java 25** ou superior.
* **MySQL Server** (rodando localmente ou em contêiner).
* Um cliente HTTP (como Postman, Insomnia) ou utilize o próprio **Swagger UI** do projeto.

### 2. Configuração do Banco de Dados

O banco de dados configurado por padrão é o MySQL com o esquema `budget_ia_db`.

* Certifique-se de que o servidor MySQL está rodando na porta `3306`.
* A string de conexão já possui a diretiva `createDatabaseIfNotExist=true` em [application.properties](file:///D:/Dev/projeto_copiloto/budgeting_2026/src/main/resources/application.properties), o que criará o banco automaticamente se o usuário tiver as permissões necessárias.
* Se as suas credenciais locais do MySQL forem diferentes de `root` / `mf459@`, altere-as em [application.properties](file:///D:/Dev/projeto_copiloto/budgeting_2026/src/main/resources/application.properties):

  ```properties
  spring.datasource.username=seu_usuario
  spring.datasource.password=sua_senha
  ```

### 3. Configuração da API Key da Groq

Este projeto utiliza a API da Groq para processar os modelos de linguagem e transcrição de voz.

1. Obtenha uma chave de API na plataforma da [Groq](https://console.groq.com/).
2. Insira sua chave no arquivo [application-local.properties](file:///D:/Dev/projeto_copiloto/budgeting_2026/src/main/resources/application-local.properties):

   ```properties
   spring.ai.openai.api-key=SUA_CHAVE_AQUI
   ```

   *(Nota: O projeto também suporta ler essa chave a partir de um arquivo `.env` na raiz por meio do dotenv).*

### 4. Executando a Aplicação

Execute o seguinte comando no diretório raiz do projeto para compilar e iniciar o servidor:

```bash
./gradlew bootRun
```

O servidor iniciará por padrão na porta `8080`.

### 5. Acessando a Interface de Testes (Swagger UI)

Após o boot bem-sucedido, acesse o painel Swagger para testar os novos endpoints GET, PUT, POST e DELETE de maneira interativa:
👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

### 6. Acessando o Painel Web (Interface Gráfica Premium)

O projeto agora possui um painel web completo e moderno com gráficos interativos e assistente de voz em tempo real:
👉 **[http://localhost:8080/](http://localhost:8080/)**

---

## 💻 Endpoints REST (CRUD de Transações)

Expondo operações diretas para gerenciar o orçamento via Swagger ou requisições REST tradicionais:

* **`GET /api/transactions`**: Lista todas as transações (filtra automaticamente pelo usuário logado caso autenticado).
* **`POST /api/transactions`**: Cria uma transação manualmente informando valor, categoria e tipo (`RECEITA`/`DESPESA`).
* **`PUT /api/transactions/{id}`**: Atualiza uma transação existente pelo seu ID.
* **`DELETE /api/transactions/{id}`**: Remove uma transação pelo seu ID.

---

## 🎙️ Como Testar o Assistente de Voz

O principal endpoint do projeto é o `/api/assistant/voice`, que processa áudios com comandos financeiros.

### Exemplo de Comando por Voz

Grave um áudio curto dizendo algo como:

> *"Registrar uma despesa de 50 reais em alimentação"* ou *"Recebi um salário de 3000 reais na categoria renda"*

### Enviando a requisição usando `curl`

```bash
curl -X POST http://localhost:8080/api/assistant/voice \
  -H "Content-Type: multipart/form-data" \
  -F "audio=@caminho/para/seu/arquivo.ogg"
```

### O que acontece nos bastidores?

1. O áudio é enviado ao modelo **Whisper (whisper-large-v3)** da Groq para ser transcrito em texto.
2. O texto transcrito é enviado ao LLM **Llama 3.3 (llama-3.3-70b-versatile)**.
3. O LLM analisa o comando e decide acionar a ferramenta `registrarTransacao` (definida em [TransactionTools.java](file:///D:/Dev/projeto_copiloto/budgeting_2026/src/main/java/br/com/budgeting/ia/tools/TransactionTools.java)).
4. A transação é persistida no banco MySQL e um resumo da ação é retornado.

---

## 🤖 Aceitando Melhorias usando IA Generativa

Este projeto foi desenhado para ser estendido utilizando o poder da IA Generativa (GenAI). Abaixo estão formas sugeridas de usar a IA para implementar melhorias ou como sugerir novos recursos:

### 💡 Ideias de Melhorias que você pode implementar com IA

1. **Ferramentas de Leitura e Consulta (Read Tools)**:

   * **Problema atual**: O agente só consegue *escrever* (registrar) transações, mas não consegue consultar gastos passados.
   * **Melhoria com IA**: Peça ao seu assistente de IA (como ChatGPT, Gemini ou Claude) para gerar um método anotado com `@Tool` em [TransactionTools.java](file:///D:/Dev/projeto_copiloto/budgeting_2026/src/main/java/br/com/budgeting/ia/tools/TransactionTools.java) que liste despesas por categoria ou período.
   * *Exemplo de Prompt*:
     > *"Crie um método Java com Spring AI anotado com @Tool para consultar o total de transações de uma determinada categoria em um repositório Spring Data JPA."*
     >
2. **Chat Interativo por Texto**:

   * **Problema atual**: A comunicação é restrita a arquivos de áudio multipart.
   * **Melhoria com IA**: Adicione um endpoint `POST /api/assistant/chat` que aceita mensagens de texto normais e usa o mesmo `ChatClient`, mas adiciona um histórico de conversa (Chat Memory) para discussões continuadas.
   * *Exemplo de Prompt*:
     > *"Como posso configurar o Spring AI ChatClient para armazenar e ler o histórico de conversa (InMemoryChatMemory) em um Controller REST do Spring Boot?"*
     >
3. **Validação e Correção de Categorias**:
   * **Implementação Realizada**: O system prompt do `AssistantAgent` foi atualizado para orientar a IA a normalizar categorias sinônimas automaticamente antes de persistir.
   * **Como funciona**: Termos como "comida", "lanche", "restaurante" são salvos como `ALIMENTAÇÃO`. Foram adicionados mapeamentos para `ROUPAS` (ex: "camiseta", "sapato"), `INVESTIMENTOS` (ex: "ações", "poupança", "tesouro"), `TRANSPORTE` (ex: "gasolina", "uber") e `LAZER` (ex: "cinema", "show").

### 🛠️ Como Contribuir com código assistido por IA

Ao enviar um Pull Request ou sugerir alterações:

1. **Documente os Prompts**: Sempre que usar IA para gerar uma nova funcionalidade (como uma nova `@Tool`), documente qual prompt gerou o código ou explique as instruções passadas ao modelo no corpo do PR.
2. **Testes Unitários Automatizados por IA**: Use IAs geradoras de código para estruturar testes mockados com mockito (como feito em [AssistantAgentTest.java](file:///D:/Dev/projeto_copiloto/budgeting_2026/src/test/java/br/com/budgeting/ia/AssistantAgentTest.java)) garantindo a resiliência das novas ferramentas.

---

## 🛠️ Histórico de Atualizações do Projeto (Passo a Passo)

Abaixo está o registro cronológico e detalhado de como este projeto foi evoluído e refatorado:

### 1. Camada de Banco de Dados e Serviços

* **Repositório**: Atualizamos o [TransactionRepository.java](file:///D:/Dev/projeto_copiloto/budgeting_2026/src/main/java/br/com/budgeting/repository/TransactionRepository.java) adicionando suporte para ordenar as transações por data de forma decrescente com o método `findByUsuarioOrderByDataDesc`.
* **Serviço**: Ampliamos o [TransactionService.java](file:///D:/Dev/projeto_copiloto/budgeting_2026/src/main/java/br/com/budgeting/service/TransactionService.java) com a implementação completa das funções CRUD (`buscarTodos`, `buscarPorId`, `atualizar` e `deletar`).

### 2. Endpoints REST (CRUD) e Segurança

* **TransactionController**: Criamos o [TransactionController.java](file:///D:/Dev/projeto_copiloto/budgeting_2026/src/main/java/br/com/budgeting/controller/TransactionController.java) contendo mapeamentos REST explícitos (`GET`, `POST`, `PUT`, `DELETE`) para expor o gerenciamento de transações no Swagger.
* **Filtros por Usuário**: Vinculamos a lógica de usuário às transações (`pedro` como padrão do MockSecurityContext ou o usuário autenticado na sessão).
* **Segurança**: Ajustamos o [SecurityConfig.java](file:///D:/Dev/projeto_copiloto/budgeting_2026/src/main/java/br/com/budgeting/config/SecurityConfig.java) liberando rotas públicas para a nova interface estática e os endpoints de transações (`/api/transactions/**`), eliminando bloqueios de CORS e autenticação nos testes locais.

### 3. Painel Web Gráfico (Front-End)

* **Estrutura**: Criamos a interface single-page em [index.html](file:///D:/Dev/projeto_copiloto/budgeting_2026/src/main/resources/static/index.html) dividida em painel de assistente, cartões de somatórios financeiros, gráficos visuais e tabela de listagem de transações com modais de edição.
* **Componentes JS**: Desenvolvemos o [index.js](file:///D:/Dev/projeto_copiloto/budgeting_2026/src/main/resources/static/index.js) com:
  - Captura e gravação de áudio através da API `MediaRecorder`.
  - Plotagem de gráficos de despesa por categoria e histórico comparativo usando a biblioteca **Chart.js**.
  - Operações dinâmicas de salvamento manual, edição e exclusão de transações.

### 4. Layout Centralizado e Responsivo

* **Visual Premium**: Refatoramos o estilo em [index.css](file:///D:/Dev/projeto_copiloto/budgeting_2026/src/main/resources/static/index.css) adicionando propriedades flexbox no `body` e largura de `1200px` no container, centralizando o painel perfeitamente na viewport do navegador com sombras brilhantes e fundo translúcido (*glassmorphic*).

### 5. Entrada Alternativa por Texto no Swagger

* **Text Input**: Adicionamos o método `processarComandoDeTexto` em [AssistantAgent.java](file:///D:/Dev/projeto_copiloto/budgeting_2026/src/main/java/br/com/budgeting/ia/AssistantAgent.java) e expusemos o endpoint `POST /api/assistant/text` em [AssistantController.java](file:///D:/Dev/projeto_copiloto/budgeting_2026/src/main/java/br/com/budgeting/controller/AssistantController.java).
* **Facilidade de Teste**: Habilitamos o teste das habilidades da IA diretamente no Swagger sem a necessidade de upload de arquivos de áudio físicos.

### 6. Rodapé de Autoria

* **Créditos**: Acrescentamos uma barra de rodapé elegante ao final do painsel com o nome do autor **Pedro Zeferino da Silva** acompanhado de um ícone de coração animado com pulsar contínuo em CSS.

### 7. Suíte de Testes e Qualidade de Código
* **Testes de Rota**: Criamos o [TransactionControllerTest.java](file:///D:/Dev/projeto_copiloto/budgeting_2026/src/test/java/br/com/budgeting/controller/TransactionControllerTest.java) cobrindo todos os cenários do novo controller.
* **Ajuste de Jackson**: Registramos o módulo `JavaTimeModule` no `ObjectMapper` de testes para garantir a correta serialização de campos do tipo `LocalDateTime`.
* **Sucesso na Compilação**: Rodamos a build com `./gradlew test` garantindo que todos os testes passem com sucesso sem regressão no projeto.

### 8. Validação e Normalização de Categorias com IA
* **Refatoração do Prompt**: Atualizamos o prompt de sistema do `AssistantAgent` para instruir o LLM a mapear e padronizar categorias sinônimas inseridas via voz ou texto para termos consistentes em caixa alta (ex: `ALIMENTAÇÃO`, `ROUPAS`, `INVESTIMENTOS`, `TRANSPORTE`, `LAZER`). Isso garante que os dados permaneçam estruturados e organizados no banco de dados e simplifica a geração dos gráficos de pizza.
