# TextOnly - Unified Backend + TURN Server

**A complete, scalable real-time messaging platform with centralized backend, REST API, and WebSocket synchronization.**

## 📌 Overview

TextOnly is now a **single unified backend** serving both Web and Mobile clients with:

- ✅ **REST API** for CRUD operations
- ✅ **WebSocket/STOMP** for real-time sync
- ✅ **JWT Authentication** for security
- ✅ **PostgreSQL** for persistent storage
- ✅ **TURN Server** for WebRTC connectivity
- ✅ **Multi-platform** support (Web, Mobile, Desktop)

---

## 🚀 Quick Start (5 minutes)

### Prerequisites
- Docker & Docker Compose
- OR: Java 21, Maven 3.9, PostgreSQL 16

### Start with Docker (Recommended)

```bash
cd TextOnlyTurnServer
docker-compose up -d

# Verify
docker-compose ps

# Backend available at: http://localhost:8080
```

### Test API

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"test123","displayName":"Test"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"test123"}'
```

---

## 📚 Documentation

| Document | Purpose |
|----------|---------|
| **QUICK_START.md** | ⭐ Start here - 5-minute setup guide |
| **BACKEND_SETUP.md** | Detailed API docs, database schema, deployment |
| **WEB_FRONTEND_INTEGRATION.md** | React/Vue integration with examples |
| **ANDROID_INTEGRATION.md** | Android/Kotlin integration with Retrofit |
| **SYNC_ARCHITECTURE.md** | Complete architecture, sync flows, checklist |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│         TextOnly Backend (Spring Boot 3.2)          │
│  ┌──────────────────────────────────────────────┐  │
│  │  REST API (HTTP)    WebSocket (Real-Time)    │  │
│  │  /api/auth/*        /ws/sync                 │  │
│  │  /api/users/*       /topic/chat/{userId}     │  │
│  │  /api/messages/*    /topic/user/{userId}     │  │
│  │  /api/contacts/*    /topic/users/status      │  │
│  └──────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────┐  │
│  │         PostgreSQL (Data) | TURN (WebRTC)    │  │
│  └──────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
      ↑                   ↑                    ↑
      |                   |                    |
  [Web Client]      [Mobile Client]      [WebRTC Peers]
  (React/Vue)       (Android/Kotlin)      (Audio/Video)
```

---

## 🔌 API Endpoints

### Authentication
```http
POST   /api/auth/register         # Create account
POST   /api/auth/login            # Login
GET    /api/auth/validate-token   # Check token
```

### Users
```http
GET    /api/users/{id}              # Get profile
PUT    /api/users/{id}/profile      # Update profile
PATCH  /api/users/{id}/status       # Update status
GET    /api/users/search?query=xxx  # Search users
```

### Messages
```http
POST   /api/messages                  # Send message
GET    /api/messages/conversation/{id} # Chat history
GET    /api/messages/unread           # Unread messages
PATCH  /api/messages/{id}/read        # Mark as read
```

### Contacts
```http
GET    /api/contacts              # List contacts
POST   /api/contacts/{id}         # Add contact
DELETE /api/contacts/{id}         # Remove contact
```

### WebSocket
```
WS /ws/sync                         # Main endpoint
→ /app/chat/{receiverId}           # Send message
→ /app/user/{userId}/profile       # Update profile
← /topic/chat/{userId}             # Receive messages
← /topic/user/{userId}             # Profile updates
← /topic/users/status              # Status changes
```

---

## 🔄 Real-Time Sync

Changes are instantly synchronized across all connected clients:

- 💬 **Messages**: Sent and received in real-time
- 👤 **Profiles**: Updates visible immediately
- 🟢 **Status**: Online/offline changes broadcast instantly
- 📱 **Contacts**: Add/remove reflected everywhere

---

## 🔐 Security

- **JWT Authentication** (HS512, 24-hour expiry)
- **Password Hashing** (BCrypt)
- **CORS** enabled for development (configurable for production)
- **Input Validation** on all endpoints
- **Encrypted Storage** (SharedPreferences on Android)

---

## 📁 Project Structure

```
TextOnlyTurnServer/
├── src/main/java/com/textonly/backend/
│   ├── auth/                # JWT & Security
│   ├── controller/          # REST endpoints
│   ├── service/             # Business logic
│   ├── model/               # Database entities
│   ├── repository/          # Data access
│   ├── dto/                 # Data transfer objects
│   ├── websocket/           # Real-time messaging
│   └── config/              # Spring configuration
├── src/main/resources/
│   └── application.properties  # Configuration
├── pom.xml                  # Maven dependencies
├── Dockerfile               # Container image
├── docker-compose.yml       # Multi-service setup
└── Documentation/
    ├── QUICK_START.md       # Get started in 5 minutes
    ├── BACKEND_SETUP.md     # Backend details
    ├── WEB_FRONTEND_INTEGRATION.md
    ├── ANDROID_INTEGRATION.md
    └── SYNC_ARCHITECTURE.md
```

---

## 🛠️ Technology Stack

**Backend**
- Java 21 LTS
- Spring Boot 3.2
- Spring Security + JWT
- Spring WebSocket/STOMP
- Spring Data JPA
- PostgreSQL 16

**Database**
- PostgreSQL with Docker
- Hibernate ORM
- Connection pooling (HikariCP)

**Additional**
- Maven for build
- Docker for containerization
- JWT for authentication
- SockJS for WebSocket fallback

---

## 🧪 Testing

```bash
# Run all tests
mvn test

# With coverage report
mvn test jacoco:report
open target/site/jacoco/index.html
```

---

## 📦 Docker Services

When you run `docker-compose up`, these services start:

| Service | Port | Purpose |
|---------|------|---------|
| **Backend** | 8080 | REST API + WebSocket |
| **PostgreSQL** | 5433 | Database |
| **TURN Server** | 3478 | WebRTC connectivity |

---

## 🚀 Integration Examples

### Web Client (JavaScript)

```javascript
// API Client
const api = new TextOnlyAPI(token);
await api.sendMessage(recipientId, "Hello!");

// WebSocket
const ws = new WebSocketClient(token);
ws.on('message', (msg) => updateUI(msg));
```

### Mobile Client (Kotlin)

```kotlin
// API Client
val api = RetrofitClient.getApiService(context)
api.sendMessage(MessageRequest(recipientId, "Hello!"))

// WebSocket
val wsManager = WebSocketManager(token, userId)
wsManager.connect()
wsManager.on("message.sent") { ... }
```

---

## 🌐 WebRTC TURN Configuration

The embedded TURN server enables peer-to-peer communication:

```javascript
const iceServers = [{
  urls: 'turn:localhost:3478',
  username: 'demo',
  credential: 'demo1234'
}];
```

---

## 📊 Database Schema

**users** - User accounts and profiles
**messages** - Chat messages between users
**contacts** - Contact relationships

See `BACKEND_SETUP.md` for full schema details.

---

## 🔄 Deployment

### Development
```bash
docker-compose up -d
# Runs on http://localhost:8080
```

### Production
See `BACKEND_SETUP.md` for:
- HTTPS/WSS setup
- Environment variables
- Database backups
- Monitoring setup
- Load balancing

---

## 📖 Getting Started

1. **Start Backend**: `docker-compose up -d`
2. **Test API**: Use Postman or curl
3. **Integrate Web**: Follow `WEB_FRONTEND_INTEGRATION.md`
4. **Integrate Mobile**: Follow `ANDROID_INTEGRATION.md`

---

## 🤝 Contributing

1. Create a feature branch
2. Make your changes
3. Run tests: `mvn test`
4. Submit a pull request

---

## 📞 Troubleshooting

**Backend won't start?**
```bash
docker logs textonly-backend
```

**Database connection error?**
```bash
docker logs textonly-postgres
```

**WebSocket not connecting?**
- Check CORS configuration
- Verify `/ws/sync` endpoint is accessible
- Check browser console for errors

See `BACKEND_SETUP.md` for detailed troubleshooting.

---

## 📚 Documentation

Start with **QUICK_START.md** for immediate setup, then explore:
- `BACKEND_SETUP.md` - Detailed API reference and deployment
- `WEB_FRONTEND_INTEGRATION.md` - React integration example
- `ANDROID_INTEGRATION.md` - Android integration example  
- `SYNC_ARCHITECTURE.md` - Complete architecture overview

---

## ✨ Features

✅ Multi-platform messaging (Web + Mobile)
✅ Real-time synchronization via WebSocket
✅ Secure JWT authentication
✅ User profiles with status
✅ Contact management
✅ Message history & persistence
✅ Offline support
✅ Docker containerization
✅ Production-ready code

---

## 📝 License

Proprietary - TextOnly 2025

---

## 🎯 Next Steps

1. Run `docker-compose up -d`
2. Read `QUICK_START.md`
3. Test API with Postman
4. Integrate your Web & Mobile clients
5. Deploy to production

**Your unified backend is ready!** 🚀
