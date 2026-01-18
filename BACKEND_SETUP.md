# TextOnly - Unified Backend

## 📋 Descriere

Backend centralizat pentru aplicația TextOnly (Web + Mobile) cu sincronizare în timp real via WebSocket.

**Caracteristici:**
- ✅ Autentificare JWT
- ✅ REST API endpoints
- ✅ WebSocket pentru sincronizare real-time
- ✅ Chat messaging
- ✅ Contact management
- ✅ User profiles cu status
- ✅ PostgreSQL Database
- ✅ TURN Server pentru WebRTC

---

## 🚀 Quick Start

### 1. Docker Compose (Recomandat)

```bash
cd d:\TextOnlyTurnServer
docker-compose up -d
```

Aceasta va porni:
- **PostgreSQL** pe `localhost:5433`
- **Backend API** pe `http://localhost:8080`
- **TURN Server** pe `localhost:3478`

### 2. Local Development (Maven)

```bash
# Requirements: Java 21, Maven 3.9+, PostgreSQL 16

# 1. Start PostgreSQL
# 2. Update application.properties cu DB credentials
# 3. Run Maven
mvn spring-boot:run

# Backend available at: http://localhost:8080
```

---

## 📡 API Endpoints

### Authentication

```http
# Register
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "secure123",
  "displayName": "John Doe"
}

Response:
{
  "userId": 1,
  "email": "user@example.com",
  "displayName": "John Doe",
  "token": "eyJhbGc...",
  "tokenExpiresAt": "2025-01-19T10:00:00"
}

# Login
POST /api/auth/login
{
  "email": "user@example.com",
  "password": "secure123"
}

# Validate Token
GET /api/auth/validate-token
Authorization: Bearer {token}
```

### User Profile

```http
# Get Profile
GET /api/users/{userId}
Authorization: Bearer {token}

# Update Profile
PUT /api/users/{userId}/profile
Authorization: Bearer {token}
{
  "displayName": "Updated Name",
  "avatarUrl": "https://..."
}

# Update Status
PATCH /api/users/{userId}/status?status=online
Authorization: Bearer {token}

# Search Users
GET /api/users/search?query=john
Authorization: Bearer {token}
```

### Messages

```http
# Send Message
POST /api/messages
Authorization: Bearer {token}
{
  "receiverId": 2,
  "content": "Hello! How are you?"
}

# Get Conversation
GET /api/messages/conversation/{otherUserId}
Authorization: Bearer {token}

# Get Unread
GET /api/messages/unread
Authorization: Bearer {token}

# Mark as Read
PATCH /api/messages/{messageId}/read
Authorization: Bearer {token}
```

### Contacts

```http
# Get Contacts
GET /api/contacts
Authorization: Bearer {token}

# Add Contact
POST /api/contacts/{contactId}
Authorization: Bearer {token}

# Remove Contact
DELETE /api/contacts/{contactId}
Authorization: Bearer {token}
```

---

## 🔌 WebSocket Real-Time Sync

### Connection

```javascript
// JavaScript Client
const stompClient = new StompClient();
const socket = new SockJS('http://localhost:8080/ws/sync');
stompClient.connect(socket, {}, onConnected, onError);

function onConnected() {
  // Subscribe to user's chat
  stompClient.subscribe('/topic/chat/{userId}', onMessageReceived);
  
  // Subscribe to profile updates
  stompClient.subscribe('/topic/user/{userId}', onProfileUpdated);
  
  // Subscribe to status changes
  stompClient.subscribe('/topic/users/status', onStatusChanged);
}

function sendMessage(message) {
  stompClient.send('/app/chat/{receiverId}', {}, JSON.stringify({
    type: 'message.sent',
    senderId: currentUserId,
    content: message.text,
    timestamp: Date.now()
  }));
}

function updateProfile(profile) {
  stompClient.send('/app/user/{userId}/profile', {}, JSON.stringify({
    type: 'profile.updated',
    data: profile,
    timestamp: Date.now()
  }));
}
```

### Android/Kotlin Client

```kotlin
// Kotlin example
val webSocket = OkHttpClient().newWebSocket(
    Request.Builder().url("ws://localhost:8080/ws/sync").build(),
    object : WebSocketListener() {
        override fun onMessage(webSocket: WebSocket, text: String) {
            // Handle incoming message
            val message = JSONObject(text)
            when (message.getString("type")) {
                "message.sent" -> handleNewMessage(message)
                "profile.updated" -> handleProfileUpdate(message)
                "user.status.changed" -> handleStatusChange(message)
            }
        }
    }
)

fun sendMessage(text: String, receiverId: Long) {
    val payload = JSONObject().apply {
        put("type", "message.sent")
        put("senderId", currentUserId)
        put("content", text)
        put("timestamp", System.currentTimeMillis())
    }
    webSocket.send(payload.toString())
}
```

---

## 🛢️ Database Schema

### users
```sql
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  display_name VARCHAR(255),
  avatar_url TEXT,
  status VARCHAR(20) DEFAULT 'offline',
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### messages
```sql
CREATE TABLE messages (
  id BIGSERIAL PRIMARY KEY,
  sender_id BIGINT NOT NULL REFERENCES users(id),
  receiver_id BIGINT NOT NULL REFERENCES users(id),
  content TEXT NOT NULL,
  is_read BOOLEAN DEFAULT false,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### contacts
```sql
CREATE TABLE contacts (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id),
  contact_id BIGINT NOT NULL REFERENCES users(id),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(user_id, contact_id)
);
```

---

## 🔐 JWT Token Structure

```
Header:
{
  "alg": "HS512",
  "typ": "JWT"
}

Payload:
{
  "userId": 1,
  "email": "user@example.com",
  "sub": "user@example.com",
  "iat": 1705584000,
  "exp": 1705670400
}

Secret: (Configurabil în application.properties)
```

---

## 📱 Client Integration

### Web Frontend

```javascript
// Initialize API client
const API_BASE = 'http://localhost:8080/api';

class TextOnlyAPI {
  constructor(token) {
    this.token = token;
  }

  async register(email, password, displayName) {
    const response = await fetch(`${API_BASE}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password, displayName })
    });
    return response.json();
  }

  async getContacts() {
    const response = await fetch(`${API_BASE}/contacts`, {
      headers: { 'Authorization': `Bearer ${this.token}` }
    });
    return response.json();
  }

  async sendMessage(receiverId, content) {
    const response = await fetch(`${API_BASE}/messages`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.token}`
      },
      body: JSON.stringify({ receiverId, content })
    });
    return response.json();
  }
}
```

### Android Integration

```kotlin
// Android client would use Retrofit + OkHttp
val api = TextOnlyAPI.create("http://localhost:8080")
val response = api.login(email, password)
val token = response.token

// WebSocket for real-time updates
val wsClient = WebSocketClient(token)
wsClient.connect()
wsClient.onMessageReceived { message ->
    // Update UI with new message
}
```

---

## 🧪 Test Coverage

```bash
# Run tests
mvn test

# With coverage
mvn test jacoco:report

# Reports available at: target/site/jacoco/index.html
```

---

## 📊 Performance Tips

1. **Database Indexing**: Create indexes pe `email`, `user_id`
2. **Connection Pooling**: HikariCP configured in pom.xml
3. **WebSocket Scaling**: Use Redis for distributed messaging (future)
4. **Caching**: Add Spring Cache annotations pe frequently accessed data

---

## 🐛 Troubleshooting

### Connection refused
```bash
# Verify services are running
docker-compose ps

# Check logs
docker logs textonly-backend
docker logs textonly-postgres
```

### JWT validation fails
- Verify token hasn't expired
- Check JWT secret matches between server and client
- Ensure Authorization header format: `Bearer {token}`

### WebSocket connection fails
- Check CORS configuration in `SecurityConfig`
- Verify `/ws/sync` endpoint is accessible
- Check browser console for connection errors

---

## 📚 Architecture Overview

```
TextOnlyTurnServer (Backend)
├── REST API (HTTP)
│   ├── /api/auth/*
│   ├── /api/users/*
│   ├── /api/messages/*
│   └── /api/contacts/*
├── WebSocket (/ws/sync)
│   ├── /topic/chat/{userId}
│   ├── /topic/user/{userId}
│   └── /topic/users/status
├── PostgreSQL Database
└── TURN Server (WebRTC)

TextOnlyWeb (Client)
├── REST API Consumer
├── WebSocket Client
└── UI Components

TextOnlyMobile (Client)
├── REST API Consumer (Retrofit/OkHttp)
├── WebSocket Client
└── Android UI
```

---

## 📝 License

Proprietary - TextOnly 2025

---

## 🤝 Contributing

1. Fork & branch
2. Make changes
3. Run tests: `mvn test`
4. Submit PR

---

## 📞 Support

Pentru probleme sau sugestii:
1. Check logs: `docker logs textonly-backend`
2. Verify DB connection
3. Test endpoints cu Postman
4. Check WebSocket connection

---

**Last Updated**: January 18, 2025
**Backend Version**: 2.0.0
**Java**: 21 LTS
**Framework**: Spring Boot 3.2.0
