# 📦 Sistema de Controle de Estoque (StockOnYou)

Este é um sistema completo para controle de estoque, desenvolvido com uma arquitetura desacoplada utilizando **Java/Spring Boot** no backend e **Angular** no frontend. A autenticação e o gerenciamento de identidade são feitos via **Keycloak**, e a infraestrutura local é gerenciada com **Docker**.

---

## 🛠️ Tecnologias Utilizadas

* **Backend:** Java, Spring Boot, Spring Data JPA, REST API.
* **Frontend:** Angular, TypeScript, HTML, CSS/SASS, `keycloak-js`.
* **Autenticação:** Keycloak (v26)
* **Banco de Dados:** PostgreSQL (v16)
* **Infraestrutura:** Docker & Docker Compose.

---

## 📂 Estrutura do Repositório

O projeto utiliza o modelo de *monorepo*, centralizando o código e a infraestrutura:

```text
sistema-controle-estoque/
├── backend/                  # API Rest (Java / Spring Boot)
├── frontend/                 # Interface Web (Angular)
└── infra/                    # Configurações de Infraestrutura (Docker Compose)
```

---

## 🚀 Como Executar o Projeto Localmente

### 1. Pré-requisitos
Antes de começar, você precisará ter instalado em sua máquina:
* **Docker & Docker Compose**
* **Java JDK 17+**
* **Node.js** & **Angular CLI**

---

### 🐳 2. Subindo a Infraestrutura (Banco de Dados e Keycloak)

1. Navegue até a pasta de infraestrutura:
   ```bash
   cd infra
   ```
2. Inicialize os containers:
   ```bash
   docker compose up -d
   ```

* **Painel do Keycloak:** [http://localhost:8091](http://localhost:8091)
* **Usuário Admin:** `admin` | **Senha:** `admin`

---

### 🔑 3. Configurando o Keycloak para a Aplicação

Siga estes passos dentro do painel do Keycloak para liberar o acesso dos sistemas:

#### Passo 1: Criar o Realm
1. No menu superior esquerdo, clique na lista de Realms (**Master**) e clique em **Create Realm**.
2. Nome do Realm: `stockonyou-realm`
3. Clique em **Save**.

#### Passo 2: Criar o Client do Frontend (Angular)
1. No menu lateral, acesse **Clients** > **Create client**.
2. **Client type:** `OpenID Connect` | **Client ID:** `stockonyou-frontend` (Avançar).
3. Ative a opção **Standard flow** e garanta que **Client authentication** está desativado (`Off`).
4. Na tela seguinte, configure os acessos:
   * **Root URL:** `http://localhost:4200`
   * **Valid redirect URIs:** `http://localhost:4200/*`
   * **Web origins:** `http://localhost:4200` *(Evita erros de CORS)*
5. Clique em **Save**.

#### Passo 3: Criar o Client do Backend (Spring Boot)
1. Vá em **Clients** > **Create client**.
2. **Client type:** `OpenID Connect` | **Client ID:** `stockonyou-backend` (Avançar).
3. Ative a opção **Client authentication** mudando para `On` *(Torna o cliente confidencial)*.
4. Salve o cliente. Acesse a aba **Credentials** e copie o valor do campo **Client secret**.

#### Passo 4: Criar um Usuário de Teste
1. No menu lateral, clique em **Users** > **Add user**.
2. Preencha o **Username** (ex: `usuario.teste`) e salve.
3. Acesse a aba **Credentials** deste usuário, clique em **Set password**, defina uma senha e desmarque a opção **Temporary**.

---

### ☕ 4. Como rodar o Backend

1. Abra um terminal na pasta raiz e navegue até o backend:
   ```bash
   cd backend
   ```
2. No seu arquivo `application.properties` (ou `.yml`), insira as configurações de validação de token do Keycloak:
   ```properties
   spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8091/realms/stockonyou-realm
   ```
3. Execute a aplicação:
   ```bash
   ./mvnw spring-boot:run
   ```
A API estará disponível em `http://localhost:8080`.

---

### 🅰️ 5. Como rodar o Frontend

O frontend gerencia o fluxo de login usando a biblioteca oficial `keycloak-js`.

1. Abra outro terminal e navegue até a pasta do frontend:
   ```bash
   cd frontend/stockonyou-frontend
   ```
2. Instale as dependências do projeto:
   ```bash
   npm install
   ```
3. Garanta que as configurações do Keycloak mapeadas no seu `main.ts` ou arquivo de inicialização de ambiente batem com os seguintes parâmetros:
   ```typescript
   import Keycloak from 'keycloak-js';

   const keycloak = new Keycloak({
     url: 'http://localhost:8091',
     realm: 'stockonyou-realm',
     clientId: 'stockonyou-frontend'
   });

   keycloak.init({ onLoad: 'login-required' }).then(authenticated => {
     if (authenticated) {
       // Inicializa o app Angular aqui
     }
   });
   ```
4. Inicie o servidor do Angular:
   ```bash
   ng serve
   ```
O frontend estará disponível no seu navegador em `http://localhost:4200`.

---

## ✒️ Autores

* **Seu Nome** - *Desenvolvimento Completo* - [@seu-usuario-github](https://github.com)
