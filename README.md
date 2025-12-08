# Arquitetura de Microsserviços com IA

Este projeto implementa uma arquitetura moderna de microsserviços utilizando Spring Cloud, com integração de serviços de IA, rastreamento distribuído e padrões de resiliência.

## Visão Geral

O projeto consiste em 6 microsserviços integrados que demonstram uma arquitetura completa e escalável:

- **Config Server**: Servidor de configuração centralizada
- **Service Discovery (Eureka)**: Registro e descoberta de serviços
- **API Gateway**: Gateway de API com roteamento inteligente
- **AI Service**: Serviço de Inteligência Artificial (integração com OpenAI)
- **Integrator Service**: Serviço de integração com APIs externas (Open-Meteo)
- **Serverless Service**: Função serverless para transformação de texto

## Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 4.0.0**
- **Spring Cloud 2025.1.0**
- **Spring AI 1.0.0-M1** (integração com OpenAI)
- **Netflix Eureka** (Service Discovery)
- **Spring Cloud Gateway** (API Gateway)
- **Spring Cloud Config** (Configuração centralizada)
- **Resilience4j** (Circuit Breaker e Retry)
- **Zipkin** (Rastreamento distribuído)
- **Maven** (Gerenciamento de dependências)

## Pré-requisitos

- Java 17 ou superior
- Maven 3.6+
- Conta OpenAI com API Key
- Git

## Configuração

### 1. Clonar o Repositório

```bash
git clone https://github.com/Jorgelino328/AI_microservices.git
cd AI_microservices
```

### 2. Configurar API Key da OpenAI

O AI Service requer uma API Key da OpenAI para funcionar. Siga os passos:

1. **Navegue até o diretório do AI Service**

2. **Renomeie o arquivo `.env.example` para `.env`**

3. **Edite o arquivo `.env` e adicione sua API Key da OpenAI**

## Como Executar

#### 1. Config Server (primeiro)
```bash
cd config-server
mvn spring-boot:run
```
#### 2. Service Discovery (segundo)
```bash
cd service-discovery
mvn spring-boot:run
```
#### 3. API Gateway (terceiro)
```bash
cd api-gateway
mvn spring-boot:run
```
#### 4. Serviços de Negócio (podem ser iniciados em paralelo)

**AI Service:**
```bash
cd ai-service
mvn spring-boot:run
```
**Integrator Service:**
```bash
cd integrator-service
mvn spring-boot:run
```
**Serverless Service:**
```bash
cd serverless-service
mvn spring-boot:run
```


