# 🎉 TextOnly - Complete Synchronization Setup

## 📌 Summary

Ai acum o **arhitectură backend centralizată, scalabilă și în timp real** pentru TextOnly cu suport complet pentru Web și Android.

---

## 📦 Ce Am Creat Pentru Tine

### ✅ Backend (TextOnlyTurnServer)

**Structură completă Spring Boot 3.2 cu:**

```
src/main/java/com/textonly/backend/
├── auth/
│   ├── JwtTokenProvider.java          # JWT generation & validation
│   └── JwtAuthenticationFilter.java   # Security filter
├── controller/
│   ├── AuthController.java            # Login, Register, Validate
│   ├── UserController.java            # Profile management
│   ├── MessageController.java         # Messaging API
│   └── ContactController.java         # Contacts management
├── service/
│   ├── AuthService.java               # Authentication logic
│   ├── UserService.java               # User operations
│   ├── MessageService.java            # Message operations
│   └── ContactService.java            # Contact operations
├── model/
│   ├── User.java                      # User entity
│   ├── Message.java                   # Message entity
│   └── Contact.java                   # Contact entity
├── repository/
│   ├── UserRepository.java            # User data access
│   ├── MessageRepository.java         # Message data access
│   └── ContactRepository.java         # Contact data access
├── dto/
│   ├── AuthRequestDTO.java
│   ├── AuthResponseDTO.java
│   ├── UserProfileDTO.java
│   ├── MessageDTO.java
│   └── MessageCreateDTO.java
├── websocket/
│   ├── WebSocketConfig.java           # WebSocket setup
│   ├── WebSocketController.java       # Real-time message handler
│   └── SyncMessage.java               # Message model
├── config/
│   ├── SecurityConfig.java            # Spring Security + CORS
│   └── WebSocketConfig.java           # STOMP/WebSocket config
└── TextOnlyBackendApplication.java    # Main class
```

**Features:**
- ✅ JWT Authentication (HS512, 24h expiry)
- ✅ REST API (CRUD operations)
- ✅ WebSocket STOMP (Real-time sync)
- ✅ PostgreSQL (Persistent storage)
- ✅ CORS (Multiple clients)
- ✅ Input validation
- ✅ Error handling
- ✅ Docker support

---

## 🔌 API Endpoints Reference

### Authentication
```
POST   /api/auth/register         # Create account
POST   /api/auth/login            # Login & get JWT
POST   /api/auth/logout           # Logout
GET    /api/auth/validate-token   # Check token validity
```

### Users
```
GET    /api/users/{id}                    # Get profile
PUT    /api/users/{id}/profile            # Update profile
PATCH  /api/users/{id}/status?status=xxx  # Update status
GET    /api/users/search?query=xxx        # Search users
```

### Messages
```
POST   /api/messages                      # Send message
GET    /api/messages/conversation/{id}    # Get chat history
GET    /api/messages/unread               # Get unread messages
PATCH  /api/messages/{id}/read            # Mark as read
```

### Contacts
```
GET    /api/contacts              # List contacts
POST   /api/contacts/{id}         # Add contact
DELETE /api/contacts/{id}         # Remove contact
```

### WebSocket
```
/ws/sync                                  # Main WebSocket endpoint
/app/chat/{receiverId}                    # Send message
/app/user/{userId}/profile                # Update profile
/app/user/{userId}/status                 # Change status

Subscribe to:
/topic/chat/{userId}                      # Receive messages
/topic/user/{userId}                      # Profile updates
/topic/users/status                       # Status changes
```

---

## 🚀 Quick Start

### 1. Start Backend (Docker - Recommended)

```bash
cd d:\TextOnlyTurnServer
docker-compose up -d

# Verify services
docker-compose ps

# Check logs
docker logs textonly-backend
```

Services running:
- **Backend API**: http://localhost:8080
- **PostgreSQL**: localhost:5433 (user: postgres, pass: postgres)
- **TURN Server**: localhost:3478 (WebRTC)

### 2. Test with Postman/cURL

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email":"test@example.com",
    "password":"test123",
    "displayName":"Test User"
  }'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email":"test@example.com",
    "password":"test123"
  }'

# Use token in subsequent requests
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/api/users/1
```

---

## 📚 Documentation Files

I've created comprehensive guides for you:

| File | Purpose |
|------|---------|
| **BACKEND_SETUP.md** | Backend installation, API docs, DB schema |
| **WEB_FRONTEND_INTEGRATION.md** | React/Web integration with API client & WebSocket |
| **ANDROID_INTEGRATION.md** | Android/Kotlin integration with Retrofit & OkHttp |
| **SYNC_ARCHITECTURE.md** | Complete architecture, sync flows, checklist |

---

## 🎯 Integration Path

### For Web Frontend (React/Vue)

1. Copy code from `WEB_FRONTEND_INTEGRATION.md`
2. Install dependencies:
   ```bash
   npm install axios sockjs-client @stomp/stompjs
   ```
3. Create `apiClient.js` and `wsClient.js`
4. Integrate in your components
5. Test with backend running

### For Mobile (Android)

1. Copy code from `ANDROID_INTEGRATION.md`
2. Add dependencies in `build.gradle.kts`:
   ```gradle
   implementation("com.squareup.retrofit2:retrofit:2.10.0")
   implementation("com.squareup.okhttp3:okhttp:4.11.0")
   ```
3. Create API service & WebSocket manager
4. Build authentication & chat screens
5. Test with backend running

---

## 🔐 Security Configuration

### JWT Token

- **Algorithm**: HS512
- **Secret**: Configurable in `application.properties`
- **Expiry**: 24 hours (customizable)
- **Claims**: userId, email, issued at, expiration

```properties
jwt.secret=ThisIsAVeryLongSecretKeyForJWTTokenGenerationAndValidation12345678
jwt.expiration=86400000  # 24 hours in milliseconds
```

### CORS

Enabled for all origins in development:
```java
configuration.setAllowedOrigins(Arrays.asList("*"));
```

For production, restrict to:
```java
configuration.setAllowedOrigins(Arrays.asList("https://yourdomain.com"));
```

---

## 📊 Database Schema

### Users Table
```sql
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL (hashed with BCrypt),
  display_name VARCHAR(255),
  avatar_url TEXT,
  status VARCHAR(20) DEFAULT 'offline', -- online, away, busy, offline
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email ON users(email);
CREATE INDEX idx_display_name ON users(display_name);
```

### Messages Table
```sql
CREATE TABLE messages (
  id BIGSERIAL PRIMARY KEY,
  sender_id BIGINT NOT NULL REFERENCES users(id),
  receiver_id BIGINT NOT NULL REFERENCES users(id),
  content TEXT NOT NULL,
  is_read BOOLEAN DEFAULT false,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sender_receiver ON messages(sender_id, receiver_id);
CREATE INDEX idx_receiver_read ON messages(receiver_id, is_read);
```

### Contacts Table
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

## 🔄 Real-Time Sync Examples

### Example 1: User A sends message to User B

```javascript
// Client A (Web)
const message = {
  receiverId: 2,
  content: "Hello, how are you?"
};

// 1. Send via REST API
POST /api/messages
{
  "receiverId": 2,
  "content": "Hello, how are you?"
}

// 2. Also send via WebSocket for instant delivery
ws.send('/app/chat/2', {
  "type": "message.sent",
  "senderId": 1,
  "content": "Hello, how are you?",
  "timestamp": 1705584000000
})

// Server broadcasts:
// To User A: /topic/chat/1 (confirmation)
// To User B: /topic/chat/2 (new message)

// Result:
// - User A sees checkmark (delivered)
// - User B sees new message instantly (if online)
// - Message stored in DB (if offline, shows when reconnects)
```

### Example 2: User updates profile

```javascript
// Client A updates profile
PUT /api/users/1/profile
{
  "displayName": "John Updated",
  "avatarUrl": "https://..."
}

// Server:
// 1. Updates user in DB
// 2. Broadcasts to all subscribers
// ws.convertAndSend('/topic/user/1', {...})

// All contacts of User A see:
// - New display name
// - New avatar
// - In real-time (no page refresh needed)
```

### Example 3: Status changes broadcast

```javascript
// User changes status
PATCH /api/users/1/status?status=away

// Server broadcasts:
// To all connected users
// ws.convertAndSend('/topic/users/status', {
//   "userId": 1,
//   "status": "away",
//   "timestamp": 1705584000000
// })

// All clients subscribed to /topic/users/status see:
// - User 1 is now AWAY
// - Contact list updates instantly
// - No polling needed
```

---

## 🧪 Testing

### Manual Testing (Postman)

1. Import this collection into Postman:
   - Base URL: `http://localhost:8080`
   - Add Authorization header: `Bearer {token}`

### Automated Testing

```bash
# Backend tests
cd TextOnlyTurnServer
mvn test

# Run with coverage
mvn test jacoco:report
```

---

## ⚠️ Important Notes

### For Development

- **CORS**: Enabled for all origins (fine for dev)
- **JWT Secret**: Placeholder value (change before production!)
- **Database**: PostgreSQL running in Docker
- **WebSocket**: SockJS with fallbacks enabled

### Before Production

- [ ] Change JWT secret to a strong random value
- [ ] Restrict CORS to specific domains
- [ ] Enable HTTPS/WSS
- [ ] Setup database backups
- [ ] Configure monitoring & logging
- [ ] Load test the infrastructure
- [ ] Setup rate limiting
- [ ] Implement API versioning

---

## 🐛 Troubleshooting

### Backend won't start
```bash
# Check logs
docker logs textonly-backend

# Verify database is running
docker logs textonly-postgres

# Restart all services
docker-compose restart
```

### WebSocket connection fails
- Check CORS configuration
- Verify `/ws/sync` endpoint is accessible
- Check browser console for errors
- Ensure token is valid

### Messages not syncing
- Check WebSocket subscription: `/topic/chat/{userId}`
- Verify user IDs are correct
- Check network tab for WebSocket messages
- Review backend logs for errors

### Database connection error
- Verify PostgreSQL is running: `docker logs textonly-postgres`
- Check credentials in `application.properties`
- Verify port 5433 is not in use

---

## 📈 Next Steps

1. **Start Backend**: `docker-compose up -d`
2. **Test APIs**: Use Postman with examples above
3. **Integrate Web**: Follow `WEB_FRONTEND_INTEGRATION.md`
4. **Integrate Mobile**: Follow `ANDROID_INTEGRATION.md`
5. **Setup CI/CD**: GitHub Actions for automated testing
6. **Deploy**: To staging, then production

---

## 📞 Support

For issues or questions:

1. **Check logs**: `docker logs textonly-backend`
2. **Review docs**: Check the detailed guides in BACKEND_SETUP.md
3. **Test endpoint**: Use Postman to isolate issues
4. **Check network**: Verify services are running with `docker-compose ps`

---

## 🎓 Learning Resources

- **Spring Boot**: https://spring.io/guides
- **WebSocket/STOMP**: https://spring.io/guides/gs/messaging-stomp-websocket/
- **JWT**: https://jwt.io/
- **PostgreSQL**: https://www.postgresql.org/docs/
- **Retrofit**: https://square.github.io/retrofit/
- **React Hooks**: https://react.dev/reference/react

---

## 📝 File Structure Summary

```
TextOnlyTurnServer/
├── src/main/java/com/textonly/backend/  # Main source code
├── src/main/resources/                  # Config files
├── pom.xml                              # Maven configuration
├── Dockerfile                           # Docker image
├── docker-compose.yml                   # Multi-service setup
├── BACKEND_SETUP.md                     # Backend documentation
├── WEB_FRONTEND_INTEGRATION.md          # Web integration guide
├── ANDROID_INTEGRATION.md               # Android integration guide
├── SYNC_ARCHITECTURE.md                 # Architecture overview
└── THIS_FILE.md                         # Quick reference
```

---

## ✨ What You Can Do Now

✅ **User Authentication**
- Register new accounts
- Login with email/password
- Secure JWT tokens
- Token validation

✅ **Real-Time Messaging**
- Send/receive messages instantly
- View chat history
- Mark messages as read
- Unread message count

✅ **User Profiles**
- Update display name & avatar
- Set online/away/busy status
- Search other users
- View user profiles

✅ **Contacts Management**
- Add/remove contacts
- View contact list
- Real-time status updates

✅ **Real-Time Synchronization**
- All changes sync across devices
- WebSocket for instant delivery
- Database persistence
- Offline support (REST fallback)

✅ **Multi-Platform**
- Web (React/Vue/Angular)
- Mobile (Android/iOS)
- Desktop (Electron)
- Any HTTP client

---

## 🎉 You're All Set!

Your TextOnly application now has:
- ✅ Centralized backend
- ✅ RESTful API
- ✅ WebSocket real-time sync
- ✅ Secure authentication
- ✅ Multi-platform support
- ✅ Production-ready structure

**Time to integrate your Web and Mobile clients!**

---

**Created**: January 18, 2025
**Backend Version**: 2.0.0
**Java**: 21 LTS
**Spring Boot**: 3.2.0
**Database**: PostgreSQL 16

---

For detailed integration instructions, see:
- Web: `WEB_FRONTEND_INTEGRATION.md`
- Mobile: `ANDROID_INTEGRATION.md`
- Architecture: `SYNC_ARCHITECTURE.md`
- Deployment: `BACKEND_SETUP.md`
