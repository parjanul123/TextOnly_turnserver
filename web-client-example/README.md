# 🌐 TextOnly Web Client - Demo

Demo client web care se conectează la backend-ul TextOnly și demonstrează integrarea completă Web ↔ Android.

## 🚀 Cum să folosești

### 1. Pornește Backend-ul

```bash
cd d:\TextOnlyTurnServer
docker-compose up -d
```

Verifică că serviciile rulează:
- ✅ Backend: http://localhost:8080
- ✅ PostgreSQL: localhost:5433
- ✅ TURN Server: localhost:3478

### 2. Deschide Demo-ul Web

Simplu deschide fișierul în browser:

```bash
# Windows
start index.html

# Sau
# Dublu-click pe index.html
```

Sau folosește un server HTTP local:

```bash
# Python 3
python -m http.server 3000

# Node.js (http-server)
npx http-server -p 3000

# Apoi deschide: http://localhost:3000
```

### 3. Testează Conexiunea

#### Pas 1: Înregistrare
1. Verifică că Backend URL este corect: `http://localhost:8080`
2. Completează:
   - Email: `test@example.com`
   - Password: `password123`
   - Display Name: `Web User`
3. Click pe **Register**

#### Pas 2: WebSocket Auto-Connect
După înregistrare, clientul se va conecta automat la WebSocket.

Vei vedea în console:
```
✅ Înregistrare reușită!
🔌 Conectare la WebSocket...
✅ WebSocket conectat!
📝 Subscribe la /topic/chat/{userId}
👤 Subscribe la /topic/users/status
```

#### Pas 3: Trimite Mesaje
1. Scrie un mesaj în input
2. Apasă Enter sau click pe Send
3. Mesajul va fi trimis prin REST API
4. Confirmarea vine prin WebSocket

### 4. Testează cu Android

#### Pe Android:
1. Deschide aplicația TextOnly Android
2. Înregistrează-te cu alt cont (ex: `android@example.com`)
3. Conectează-te

#### Pe Web:
1. Deja ești conectat cu `test@example.com`
2. Trimite un mesaj către utilizatorul Android

#### Sincronizare Real-Time:
- Mesajul trimis de pe Web va apărea instant pe Android prin WebSocket
- Mesajul trimis de pe Android va apărea instant pe Web prin WebSocket

---

## 🎯 Features Demo

### ✅ Autentificare
- Register cu email, password, displayName
- Login cu email, password
- JWT Token storage
- Token validation

### ✅ WebSocket (STOMP)
- Conectare la `/ws/sync`
- Subscribe la `/topic/chat/{userId}` pentru mesaje
- Subscribe la `/topic/users/status` pentru status
- Reconnect automat în caz de deconectare

### ✅ REST API
- POST `/api/auth/register`
- POST `/api/auth/login`
- POST `/api/messages` (trimite mesaj)
- GET `/api/messages/conversation/{userId}` (încarcă conversație)

### ✅ UI Features
- Status indicator (conectat/deconectat)
- Real-time message display
- Sent vs Received message styling
- Console log cu timestamp
- Error handling

---

## 🔧 Customizare

### Schimbă Backend URL

```javascript
// În index.html, linia ~13
const backendUrl = 'https://your-production-domain.com';
```

Sau schimbă direct în input din interfață.

### Adaugă Receiver ID

Pentru a trimite mesaje către un utilizator specific:

```javascript
// În funcția sendMessage(), linia ~320
body: JSON.stringify({
    receiverId: 123,  // Schimbă cu ID-ul real al destinatarului
    content: content
})
```

### TURN Server pentru WebRTC

Pentru video calls Web ↔ Android, adaugă:

```javascript
const iceServers = [{
    urls: 'turn:localhost:3478',
    username: 'demo',
    credential: 'demo1234'
}];

const peerConnection = new RTCPeerConnection({ iceServers });
```

---

## 📊 Flow de Date

```
┌──────────────┐                  ┌──────────────┐                  ┌──────────────┐
│  Web Client  │                  │   Backend    │                  │   Android    │
│ (index.html) │                  │  Port 8080   │                  │     App      │
└──────┬───────┘                  └──────┬───────┘                  └──────┬───────┘
       │                                 │                                 │
       │ POST /api/auth/register         │                                 │
       ├────────────────────────────────>│                                 │
       │ JWT Token                        │                                 │
       │<────────────────────────────────┤                                 │
       │                                 │                                 │
       │ WS Connect /ws/sync             │                                 │
       ├────────────────────────────────>│                                 │
       │ Connected                        │                                 │
       │<────────────────────────────────┤                                 │
       │                                 │                                 │
       │                                 │ WS Connect /ws/sync             │
       │                                 │<────────────────────────────────┤
       │                                 │ Connected                        │
       │                                 ├────────────────────────────────>│
       │                                 │                                 │
       │ POST /api/messages              │                                 │
       ├────────────────────────────────>│                                 │
       │                                 │ WS: /topic/chat/{userId}        │
       │                                 ├────────────────────────────────>│
       │                                 │ Message Delivered               │
       │ Confirmation via WS             │                                 │
       │<────────────────────────────────┤                                 │
       │                                 │                                 │
```

---

## 🐛 Troubleshooting

### CORS Error
```
Access to fetch at 'http://localhost:8080/api/auth/login' 
from origin 'http://localhost:3000' has been blocked by CORS policy
```

**Fix:** Verifică `SecurityConfig.java` - trebuie să permită originea ta:

```java
.cors().configurationSource(request -> {
    CorsConfiguration cors = new CorsConfiguration();
    cors.setAllowedOrigins(Arrays.asList("http://localhost:3000", "*"));
    return cors;
})
```

### WebSocket Connection Failed
```
WebSocket connection to 'ws://localhost:8080/ws/sync' failed
```

**Fix:**
1. Verifică că backend-ul rulează: `docker ps`
2. Test endpoint: `curl http://localhost:8080/actuator/health`
3. Verifică firewall/antivirus

### Token Expired
```
JWT token is expired
```

**Fix:** Fă logout și login din nou.

---

## 📝 Next Steps

### Pentru Producție:

1. **HTTPS/WSS**
   ```javascript
   const backendUrl = 'https://your-domain.com';
   const wsUrl = 'wss://your-domain.com/ws/sync';
   ```

2. **Environment Variables**
   ```javascript
   const API_BASE = process.env.REACT_APP_API_URL;
   const WS_URL = process.env.REACT_APP_WS_URL;
   ```

3. **React/Vue Integration**
   - Convertește în componente
   - State management (Redux/Vuex)
   - Routing
   - Better UI/UX

4. **WebRTC Video Calls**
   - Adaugă RTCPeerConnection
   - Signaling prin WebSocket
   - TURN relay pentru NAT traversal

---

## 📚 Documentație Utilă

- [Spring WebSocket Docs](https://docs.spring.io/spring-framework/reference/web/websocket.html)
- [STOMP Protocol](https://stomp.github.io/)
- [WebRTC API](https://developer.mozilla.org/en-US/docs/Web/API/WebRTC_API)
- [SockJS Client](https://github.com/sockjs/sockjs-client)

---

**🎉 Demo gata de testat! Conectează Web la Android și vezi sincronizarea în timp real!**
