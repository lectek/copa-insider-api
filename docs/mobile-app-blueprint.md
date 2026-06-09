# Mobile App Blueprint (Android)

## Stack and architecture
- Kotlin + Jetpack Compose
- MVVM + UseCases + Repositories
- Hilt for DI
- Retrofit/OkHttp for API
- Coil for images
- DataStore (Encrypted) for tokens

## Package
- br.com.redemaisfarma.mobile

## Modules
- app (UI)
- core (network, storage, common)
- domain (models, use cases)
- data (repositories, DTOs, mappers)

## Core dependencies
- androidx.compose.*
- androidx.navigation:navigation-compose
- androidx.lifecycle:lifecycle-viewmodel-compose
- com.google.dagger:hilt-android
- com.squareup.retrofit2:retrofit
- com.squareup.okhttp3:okhttp
- io.coil-kt:coil-compose
- androidx.datastore:datastore-preferences

## MVP screens
- Splash
- Login
- OTP (opcional)
- Cadastro
- Home/Vitrine
- Catalogo + busca
- Detalhe do produto
- Carrinho
- Checkout
- Conta
- Pedidos (lista/detalhe)

## API base
- Base URL: http://localhost:18090
- Auth: /api/auth/*
- Catalogo: /api/v2/produtos
- Vitrine: /api/public/vitrine/destaques

## Release
- Keystore + signingConfig
- versionCode/versionName
- App bundle (.aab)
- Play Console listing + privacy policy

## API contracts (detalhado)
### Auth
- POST /api/auth/login
  req: { "usuario": "email", "senha": "senha" }
  res: { "accessToken": "", "refreshToken": "", "userId": 1, "roles": ["ROLE_CLIENTE"], "expiresAt": "" }

- POST /api/auth/register
  req: { "name": "", "email": "", "cpf": "", "password": "" }
  res: { "accessToken": "", "refreshToken": "" }

- POST /api/auth/otp/start
  req: { "canal": "email", "destino": "email" }
  res: { "deliveryId": "", "maskedDestino": "", "cooldownSec": 60, "ttlSeconds": 300 }

- POST /api/auth/otp/verify
  req: { "deliveryId": "", "code": "" }
  res: { "token": "" }

- POST /api/auth/register/complete-otp
  req: { "token": "", "email": "", "nome": "", "senha": "" }
  res: { "message": "Conta criada" }

- POST /api/auth/password/reset-otp
  req: { "token": "", "email": "", "novaSenha": "" }
  res: { "message": "Senha redefinida" }

### Catalogo
- GET /api/v2/produtos?page=0&size=20&sort=dataCadastro,desc
  res: { "content": [Produto], "page": 0, "size": 20, "totalElements": 100, "totalPages": 5 }

- GET /api/v2/produtos/{id}
  res: Produto

### Perfil
- GET /api/cliente/me
  res: ClienteResponse

- PUT /api/cliente/me
  req: { "nome": "", "telefone": "", "cpf": "" }
  res: ClienteResponse

- POST /api/cliente/me/avatar (multipart)
  res: { "avatarUrl": "" }

### Carrinho
- GET /api/cliente/me/carrinho
  res: { "items": [CartItem], "subtotal": 0, "total": 0 }

- POST /api/cliente/me/carrinho
  req: { "produtoId": 1, "quantidade": 1 }
  res: { "items": [CartItem], "subtotal": 0, "total": 0 }

- PUT /api/cliente/me/carrinho/{itemId}
  req: { "quantidade": 2 }
  res: { "items": [CartItem], "subtotal": 0, "total": 0 }

- DELETE /api/cliente/me/carrinho/{itemId}
  res: { "items": [CartItem], "subtotal": 0, "total": 0 }

### Checkout
- GET /api/cliente/me/checkout/resumo
  res: CheckoutResumoResponse

- POST /api/cliente/me/checkout/finalizar
  req: { "enderecoId": 1, "metodoPagamento": "PIX" }
  res: { "pedidoId": 123 }

### Pedidos
- GET /api/cliente/me/pedidos
  res: { "content": [PedidoResumo], "page": 0, "size": 20, "totalElements": 10, "totalPages": 1 }

- GET /api/cliente/me/pedidos/{id}
  res: PedidoDetalhe

## Navigation map
- Splash -> Login
- Login -> Home
- Home -> Catalogo -> Detalhe
- Detalhe -> Carrinho -> Checkout -> Confirmacao
- Home -> Pedidos (lista) -> Pedido (detalhe)
- Home -> Conta -> Dados/Foto

## Auth strategy
- Access token + refresh token
- Refresh antes de expirar (ex.: 2 min)
- Tokens em Encrypted DataStore
- Interceptor para 401 e refresh

## Build and release
- versionCode/versionName por build
- Keystore em CI e local
- Build AAB para Play Store
- Flavors: dev, staging, prod

## Play Store checklist
- App icon, feature graphic, screenshots
- Descricao curta e longa
- Politica de privacidade publica
- Content rating
- Target SDK atualizado

## CI/CD and QA
- Pipeline: lint + tests + assemble
- Release interno para QA
- Crash reporting (Firebase)
- Analytics basico (eventos)

