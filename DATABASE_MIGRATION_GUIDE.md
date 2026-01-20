# 🗄️ Database Migration Guide - Bază de Date Comună

## 📋 Overview

Acest backend devine **baza de date comună** pentru:
- ✅ **TextOnly Android** - https://github.com/parjanul123/TextOnly_android.git
- ✅ **TextOnly Web** - https://github.com/parjanul123/TextOnly_web.git

## 🆕 Tabele Noi Adăugate

### 1. **servers** - Servere (ca pe Discord)
```sql
CREATE TABLE servers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    icon_url VARCHAR(500),
    owner_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 2. **channels** - Canale (TEXT/VOICE)
```sql
CREATE TABLE channels (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL, -- 'TEXT' sau 'VOICE'
    server_id BIGINT NOT NULL REFERENCES servers(id) ON DELETE CASCADE,
    position INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 3. **server_members** - Membri servere
```sql
CREATE TABLE server_members (
    server_id BIGINT NOT NULL REFERENCES servers(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (server_id, user_id)
);
```

### 4. **channel_messages** - Mesaje în canale
```sql
CREATE TABLE channel_messages (
    id BIGSERIAL PRIMARY KEY,
    channel_id BIGINT NOT NULL REFERENCES channels(id) ON DELETE CASCADE,
    sender_id BIGINT NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    message_type VARCHAR(20) DEFAULT 'TEXT',
    attachment_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 5. **store_items** - Items din Store
```sql
CREATE TABLE store_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price INTEGER NOT NULL,
    type VARCHAR(20) NOT NULL, -- 'EMOTICON', 'GIFT', 'FRAME', 'STICKER'
    icon_url VARCHAR(500),
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 6. **user_inventory** - Inventory utilizatori
```sql
CREATE TABLE user_inventory (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL REFERENCES store_items(id),
    quantity INTEGER DEFAULT 1,
    purchased_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 7. **user_wallet** - Wallet cu coins
```sql
CREATE TABLE user_wallet (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    coins INTEGER DEFAULT 0,
    total_spent INTEGER DEFAULT 0,
    total_earned INTEGER DEFAULT 0
);
```

### 8. **transactions** - Istoric tranzacții
```sql
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount INTEGER NOT NULL,
    type VARCHAR(50) NOT NULL, -- 'PURCHASE', 'GIFT_SENT', 'TOP_UP', etc.
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🔧 Setup Database

### 1. PostgreSQL via Docker (Recomandat)

```bash
cd d:\TextOnlyTurnServer
docker-compose up -d postgres
```

### 2. Creare Manuală Tabele

```sql
-- Conectează-te la PostgreSQL
psql -h localhost -p 5433 -U postgres -d textonly

-- Rulează script-ul SQL
\i create_tables.sql
```

### 3. Verificare Tabele

```sql
-- Vezi toate tabelele
\dt

-- Verifică structura
\d servers
\d channels
\d store_items
```

## 📡 Noi API Endpoints

### **Servere**

```http
# Creare server
POST /api/servers
{
  "name": "My Server",
  "description": "Server description"
}

# Lista servere utilizator
GET /api/servers

# Detalii server
GET /api/servers/{id}

# Ștergere server
DELETE /api/servers/{id}

# Adaugă membru
POST /api/servers/{id}/members/{userId}

# Scoate membru
DELETE /api/servers/{id}/members/{userId}
```

### **Canale**

```http
# Creare canal
POST /api/channels
{
  "serverId": 1,
  "name": "general",
  "type": "TEXT"
}

# Lista canale server
GET /api/channels/server/{serverId}

# Ștergere canal
DELETE /api/channels/{id}

# Trimite mesaj în canal
POST /api/channels/{id}/messages
{
  "content": "Hello everyone!"
}

# Mesaje canal
GET /api/channels/{id}/messages?limit=50
```

### **Store**

```http
# Lista items
GET /api/store/items?type=EMOTICON

# Cumpără item
POST /api/store/buy
{
  "itemId": 1
}

# Inventory utilizator
GET /api/store/inventory
```

### **Wallet**

```http
# Wallet info
GET /api/wallet

# Adaugă coins
POST /api/wallet/add
{
  "amount": 100
}

# Istoric tranzacții
GET /api/wallet/transactions
```

## 🔄 Migrare Date din Local Storage

### Android (SQLite → PostgreSQL)

```kotlin
// În aplicația Android, schimbă API client-ul
object Config {
    const val BASE_URL = "http://your-backend-url:8080"
    const val USE_REMOTE_DB = true // Toggle local vs remote
}

// Sync local data la pornire
suspend fun syncLocalToRemote() {
    val localServers = localDb.serverDao().getAllServers()
    localServers.forEach { server ->
        api.createServer(server.name, server.description)
    }
}
```

### Web (localStorage → PostgreSQL)

```javascript
// În aplicația Web
async function migrateLocalData() {
  // Servere
  const localServers = JSON.parse(localStorage.getItem('servers') || '[]');
  for (const server of localServers) {
    await api.post('/servers', server);
  }
  
  // Wallet
  const localCoins = localStorage.getItem('userCoins');
  if (localCoins) {
    await api.post('/wallet/add', { amount: parseInt(localCoins) });
  }
  
  // Clear local storage
  localStorage.clear();
}
```

## 🚀 Deployment

### 1. Local Development

```bash
# Backend + DB
docker-compose up -d

# Check logs
docker logs textonly-backend -f
```

### 2. Production (Render/Heroku)

```yaml
# render.yaml (deja există)
services:
  - type: web
    name: textonly-backend
    env: java
    buildCommand: ./mvnw clean package
    startCommand: java -jar target/textonly-backend-2.0.0.jar
    
databases:
  - name: textonly-postgres
    plan: starter
```

### 3. Environment Variables

```properties
# application-prod.properties
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

## 📊 Data Flow

```
┌─────────────────┐         ┌──────────────────┐         ┌─────────────────┐
│   Android App   │────────>│   Backend API    │<────────│    Web App      │
│   (Kotlin)      │ REST+WS │   (Port 8080)    │ REST+WS │   (React)       │
└─────────────────┘         └────────┬─────────┘         └─────────────────┘
                                     │
                                     ↓
                            ┌─────────────────┐
                            │   PostgreSQL    │
                            │   (Port 5433)   │
                            │                 │
                            │  - users        │
                            │  - messages     │
                            │  - servers      │
                            │  - channels     │
                            │  - store_items  │
                            │  - wallets      │
                            └─────────────────┘
```

## ✅ Checklist Integrare

### Android
- [ ] Update `Config.kt` cu backend URL
- [ ] Schimbă `@Dao` queries să folosească API calls
- [ ] Sync local DB la pornire
- [ ] Testează register/login
- [ ] Testează creare server
- [ ] Testează store & wallet

### Web
- [ ] Update `api.js` cu endpoints noi
- [ ] Șterge localStorage dependencies
- [ ] Folosește API pentru servers
- [ ] Folosește API pentru wallet
- [ ] Testează sync Web ↔ Android

### Backend
- [x] Models create
- [x] Repositories create
- [x] Controllers create
- [ ] Services implement
- [ ] WebSocket notifications
- [ ] Deploy to production

## 🔐 Security

```java
// Activează JWT pentru toate endpoints
@PreAuthorize("hasRole('USER')")
@GetMapping("/servers")
public ResponseEntity<List<ServerDTO>> getServers() {
    Long userId = getCurrentUserId();
    return ResponseEntity.ok(serverService.getUserServers(userId));
}

// Extract userId din JWT
private Long getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return ((UserDetails) auth.getPrincipal()).getUserId();
}
```

## 📝 Next Steps

1. **Implementează Services** pentru noile controllers
2. **DTOs** pentru request/response
3. **WebSocket** pentru real-time sync pe canale
4. **Testează** toate endpoints
5. **Deploy** pe Render/production
6. **Update** Android și Web să folosească noile API-uri

---

**🎉 Baza de date comună este pregătită! Android și Web vor sincroniza perfect!**
