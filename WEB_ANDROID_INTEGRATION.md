# 🔗 TextOnly - Integrare Web ↔ Android prin TURN Server

## 📋 Arhitectura Completă

```
┌─────────────────────────────────────────────────────────────────────┐
│                    BACKEND COMUN (Port 8080)                        │
│  ┌──────────────────┐  ┌──────────────────┐  ┌─────────────────┐  │
│  │   REST API       │  │   WebSocket      │  │  TURN Server    │  │
│  │   /api/*         │  │   /ws/sync       │  │  Port 3478      │  │
│  │                  │  │   /ws/chat       │  │                 │  │
│  └──────────────────┘  └──────────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
           ↑                     ↑                      ↑
           │                     │                      │
    ┌──────┴──────┐       ┌─────┴─────┐       ┌───────┴────────┐
    │             │       │           │       │                │
    ↓             ↓       ↓           ↓       ↓                ↓
┌─────────┐  ┌─────────┐ │           │   ┌────────┐      ┌────────┐
│   WEB   │  │ ANDROID │ │  STOMP    │   │  WebRTC│      │ WebRTC │
│ Client  │  │  App    │ │  Protocol │   │  Web   │←────→│ Android│
│         │  │         │ │           │   │        │ NAT  │        │
│ React/  │  │ Kotlin  │ │           │   │ P2P    │ Relay│  P2P   │
│ Vue.js  │  │ Retrofit│ │           │   │ Video  │      │  Video │
└─────────┘  └─────────┘ └───────────┘   └────────┘      └────────┘
```

---

## 🚀 Partea 1: Configurare Backend (Deja Făcut ✅)

### Backend Status:
- ✅ Spring Boot 3.2 cu REST API
- ✅ WebSocket cu STOMP protocol
- ✅ PostgreSQL Database (Supabase)
- ✅ JWT Authentication
- ✅ TURN Server (coturn) pe port 3478

### Endpoints Disponibile:

#### **Autentificare**
```http
POST /api/auth/register
POST /api/auth/login
GET  /api/auth/validate-token
```

#### **Utilizatori**
```http
GET    /api/users/{userId}
PUT    /api/users/{userId}/profile
PATCH  /api/users/{userId}/status
GET    /api/users/search
```

#### **Mesaje**
```http
POST   /api/messages
GET    /api/messages/conversation/{otherUserId}
GET    /api/messages/unread
PATCH  /api/messages/{messageId}/read
```

#### **Contacte**
```http
GET    /api/contacts
POST   /api/contacts/{contactId}
DELETE /api/contacts/{contactId}
```

#### **WebSocket**
```
ws://your-domain:8080/ws/sync    (STOMP endpoint)
ws://your-domain:8080/ws/chat    (Alternative endpoint)

Topics:
- /topic/chat/{userId}           (Mesaje personale)
- /topic/user/{userId}           (Update-uri profil)
- /topic/users/status            (Status online/offline)
```

---

## 🌐 Partea 2: Integrare Web Frontend

### 2.1 Configurare API Client (JavaScript/TypeScript)

Creează fișierul **`src/config/api.js`**:

```javascript
// Configurație Backend
export const API_CONFIG = {
  BASE_URL: process.env.REACT_APP_API_URL || 'http://localhost:8080/api',
  WS_URL: process.env.REACT_APP_WS_URL || 'ws://localhost:8080/ws/sync',
  TURN_SERVER: {
    urls: 'turn:localhost:3478',  // Schimbă cu domeniul tău
    username: 'demo',
    credential: 'demo1234'
  }
};
```

### 2.2 REST API Client

**`src/services/TextOnlyAPI.js`**:

```javascript
class TextOnlyAPI {
  constructor() {
    this.baseURL = API_CONFIG.BASE_URL;
    this.token = localStorage.getItem('authToken');
  }

  setToken(token) {
    this.token = token;
    localStorage.setItem('authToken', token);
  }

  async fetch(endpoint, options = {}) {
    const headers = {
      'Content-Type': 'application/json',
      ...(this.token && { 'Authorization': `Bearer ${this.token}` }),
      ...options.headers
    };

    const response = await fetch(`${this.baseURL}${endpoint}`, {
      ...options,
      headers
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'API Error');
    }

    return response.json();
  }

  // Auth
  async register(email, password, displayName) {
    return this.fetch('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ email, password, displayName })
    });
  }

  async login(email, password) {
    const data = await this.fetch('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password })
    });
    this.setToken(data.token);
    return data;
  }

  // Messages
  async sendMessage(receiverId, content) {
    return this.fetch('/messages', {
      method: 'POST',
      body: JSON.stringify({ receiverId, content })
    });
  }

  async getConversation(otherUserId) {
    return this.fetch(`/messages/conversation/${otherUserId}`);
  }

  // Users
  async getProfile(userId) {
    return this.fetch(`/users/${userId}`);
  }

  async updateProfile(userId, profile) {
    return this.fetch(`/users/${userId}/profile`, {
      method: 'PUT',
      body: JSON.stringify(profile)
    });
  }

  async updateStatus(userId, status) {
    return this.fetch(`/users/${userId}/status?status=${status}`, {
      method: 'PATCH'
    });
  }

  // Contacts
  async getContacts() {
    return this.fetch('/contacts');
  }

  async addContact(contactId) {
    return this.fetch(`/contacts/${contactId}`, { method: 'POST' });
  }
}

export default new TextOnlyAPI();
```

### 2.3 WebSocket Client (STOMP)

**Instalează dependențe:**
```bash
npm install @stomp/stompjs sockjs-client
```

**`src/services/WebSocketService.js`**:

```javascript
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { API_CONFIG } from '../config/api';

class WebSocketService {
  constructor() {
    this.client = null;
    this.connected = false;
    this.subscriptions = {};
  }

  connect(userId, onConnected) {
    this.client = new Client({
      brokerURL: API_CONFIG.WS_URL,
      
      // Fallback pentru SockJS (compatibilitate cu Spring Boot)
      webSocketFactory: () => new SockJS('http://localhost:8080/ws/sync'),
      
      connectHeaders: {
        Authorization: `Bearer ${localStorage.getItem('authToken')}`
      },

      debug: (str) => {
        console.log('[STOMP Debug]', str);
      },

      onConnect: (frame) => {
        console.log('✅ WebSocket Connected!', frame);
        this.connected = true;

        // Subscribe la mesaje personale
        this.subscribeToChatMessages(userId);
        
        // Subscribe la status utilizatori
        this.subscribeToUserStatus();
        
        if (onConnected) onConnected();
      },

      onStompError: (frame) => {
        console.error('❌ STOMP Error:', frame);
      },

      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000
    });

    this.client.activate();
  }

  subscribeToChatMessages(userId, callback) {
    const destination = `/topic/chat/${userId}`;
    
    const subscription = this.client.subscribe(destination, (message) => {
      const data = JSON.parse(message.body);
      console.log('📩 New message received:', data);
      
      if (callback) callback(data);
      
      // Notificare sau actualizare UI
      this.handleIncomingMessage(data);
    });

    this.subscriptions[`chat-${userId}`] = subscription;
  }

  subscribeToUserStatus(callback) {
    const destination = '/topic/users/status';
    
    const subscription = this.client.subscribe(destination, (message) => {
      const data = JSON.parse(message.body);
      console.log('👤 User status update:', data);
      
      if (callback) callback(data);
    });

    this.subscriptions['user-status'] = subscription;
  }

  sendMessage(destination, payload) {
    if (this.connected && this.client) {
      this.client.publish({
        destination: `/app${destination}`,
        body: JSON.stringify(payload)
      });
    } else {
      console.error('WebSocket not connected!');
    }
  }

  handleIncomingMessage(data) {
    // Emit custom event pentru componente React/Vue
    window.dispatchEvent(new CustomEvent('new-message', { detail: data }));
  }

  disconnect() {
    if (this.client) {
      Object.values(this.subscriptions).forEach(sub => sub.unsubscribe());
      this.client.deactivate();
      this.connected = false;
    }
  }
}

export default new WebSocketService();
```

### 2.4 Utilizare în Componente React

**`src/components/ChatComponent.jsx`**:

```javascript
import React, { useEffect, useState } from 'react';
import TextOnlyAPI from '../services/TextOnlyAPI';
import WebSocketService from '../services/WebSocketService';

function ChatComponent({ currentUserId, otherUserId }) {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');

  useEffect(() => {
    // Conectare WebSocket
    WebSocketService.connect(currentUserId, () => {
      console.log('Connected to WebSocket');
    });

    // Listener pentru mesaje noi
    const handleNewMessage = (event) => {
      const message = event.detail;
      if (message.senderId === otherUserId || message.receiverId === otherUserId) {
        setMessages(prev => [...prev, message]);
      }
    };

    window.addEventListener('new-message', handleNewMessage);

    // Încarcă conversația existentă
    loadConversation();

    return () => {
      window.removeEventListener('new-message', handleNewMessage);
      WebSocketService.disconnect();
    };
  }, [currentUserId, otherUserId]);

  const loadConversation = async () => {
    try {
      const data = await TextOnlyAPI.getConversation(otherUserId);
      setMessages(data);
    } catch (error) {
      console.error('Error loading conversation:', error);
    }
  };

  const sendMessage = async () => {
    if (!newMessage.trim()) return;

    try {
      await TextOnlyAPI.sendMessage(otherUserId, newMessage);
      setNewMessage('');
    } catch (error) {
      console.error('Error sending message:', error);
    }
  };

  return (
    <div className="chat-container">
      <div className="messages">
        {messages.map((msg, index) => (
          <div key={index} className={msg.senderId === currentUserId ? 'sent' : 'received'}>
            {msg.content}
          </div>
        ))}
      </div>
      
      <div className="input-area">
        <input
          type="text"
          value={newMessage}
          onChange={(e) => setNewMessage(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
          placeholder="Scrie un mesaj..."
        />
        <button onClick={sendMessage}>Send</button>
      </div>
    </div>
  );
}

export default ChatComponent;
```

---

## 📱 Partea 3: Sincronizare Android (Deja Configurat ✅)

### 3.1 Android folosește deja:

**Config.kt** (deja există):
```kotlin
const val BASE_URL = "https://$BASE_DOMAIN"
const val WS_URL = "wss://$BASE_DOMAIN/ws"
```

**StompMessagingService.java** (deja există):
- Conectare la WebSocket
- Subscribe la `/topic/messages`
- Notificări push

### 3.2 Asigură-te că Android folosește același format:

```kotlin
// În aplicația Android, verifică că se conectează corect
val wsUrl = "wss://your-domain.com/ws/sync"
stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl)
```

---

## 🎥 Partea 4: WebRTC Video/Audio între Web și Android

### 4.1 Configurare TURN Server

**Date conexiune (din README.md)**:
```javascript
const iceServers = [{
  urls: 'turn:textonly.onrender.com:3478',
  username: 'demo',
  credential: 'demo1234'
}];
```

### 4.2 WebRTC Client pentru Web

**`src/services/WebRTCService.js`**:

```javascript
class WebRTCService {
  constructor() {
    this.peerConnection = null;
    this.localStream = null;
    this.remoteStream = null;
    this.iceServers = [{
      urls: 'turn:your-domain.com:3478',  // TURN Server
      username: 'demo',
      credential: 'demo1234'
    }];
  }

  async startCall(isVideo = true) {
    try {
      // Obține stream local (camera/microfon)
      this.localStream = await navigator.mediaDevices.getUserMedia({
        video: isVideo,
        audio: true
      });

      // Creează peer connection
      this.peerConnection = new RTCPeerConnection({
        iceServers: this.iceServers
      });

      // Adaugă stream-ul local
      this.localStream.getTracks().forEach(track => {
        this.peerConnection.addTrack(track, this.localStream);
      });

      // Listener pentru stream remote
      this.peerConnection.ontrack = (event) => {
        this.remoteStream = event.streams[0];
        this.onRemoteStream(this.remoteStream);
      };

      // Listener pentru ICE candidates
      this.peerConnection.onicecandidate = (event) => {
        if (event.candidate) {
          this.sendSignalingMessage({
            type: 'ice-candidate',
            candidate: event.candidate
          });
        }
      };

      // Creează oferta SDP
      const offer = await this.peerConnection.createOffer();
      await this.peerConnection.setLocalDescription(offer);

      // Trimite oferta către peer (Android)
      this.sendSignalingMessage({
        type: 'offer',
        sdp: offer
      });

      return this.localStream;

    } catch (error) {
      console.error('Error starting call:', error);
      throw error;
    }
  }

  async handleOffer(offer) {
    await this.peerConnection.setRemoteDescription(new RTCSessionDescription(offer));
    
    const answer = await this.peerConnection.createAnswer();
    await this.peerConnection.setLocalDescription(answer);

    this.sendSignalingMessage({
      type: 'answer',
      sdp: answer
    });
  }

  async handleAnswer(answer) {
    await this.peerConnection.setRemoteDescription(new RTCSessionDescription(answer));
  }

  async handleIceCandidate(candidate) {
    await this.peerConnection.addIceCandidate(new RTCIceCandidate(candidate));
  }

  sendSignalingMessage(message) {
    // Trimite prin WebSocket către peer
    WebSocketService.sendMessage('/signal', message);
  }

  onRemoteStream(stream) {
    // Override this în componenta care folosește serviciul
    console.log('Remote stream received:', stream);
  }

  endCall() {
    if (this.peerConnection) {
      this.peerConnection.close();
      this.peerConnection = null;
    }

    if (this.localStream) {
      this.localStream.getTracks().forEach(track => track.stop());
      this.localStream = null;
    }

    this.remoteStream = null;
  }
}

export default new WebRTCService();
```

### 4.3 Componenta Video Call (Web)

```javascript
import React, { useRef, useEffect } from 'react';
import WebRTCService from '../services/WebRTCService';

function VideoCallComponent({ peerId }) {
  const localVideoRef = useRef();
  const remoteVideoRef = useRef();

  useEffect(() => {
    initializeCall();
    
    return () => {
      WebRTCService.endCall();
    };
  }, []);

  const initializeCall = async () => {
    try {
      // Obține stream local
      const localStream = await WebRTCService.startCall(true);
      localVideoRef.current.srcObject = localStream;

      // Handler pentru remote stream
      WebRTCService.onRemoteStream = (stream) => {
        remoteVideoRef.current.srcObject = stream;
      };

    } catch (error) {
      console.error('Error initializing call:', error);
    }
  };

  return (
    <div className="video-call">
      <video ref={localVideoRef} autoPlay muted className="local-video" />
      <video ref={remoteVideoRef} autoPlay className="remote-video" />
      
      <button onClick={() => WebRTCService.endCall()}>
        End Call
      </button>
    </div>
  );
}

export default VideoCallComponent;
```

---

## 🔧 Partea 5: Backend - Signaling pentru WebRTC

Adaugă un controller pentru signaling:

**`WebRTCController.java`**:

```java
package com.textonly.backend.controller;

import com.textonly.backend.dto.SignalMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebRTCController {
    
    private final SimpMessagingTemplate messagingTemplate;

    public WebRTCController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/signal")
    public void handleSignaling(SignalMessage message) {
        // Forward signaling message către destinatar
        messagingTemplate.convertAndSend(
            "/topic/signal/" + message.getReceiverId(),
            message
        );
    }
}
```

**`SignalMessage.java` (DTO)**:

```java
package com.textonly.backend.dto;

public class SignalMessage {
    private String senderId;
    private String receiverId;
    private String type; // "offer", "answer", "ice-candidate"
    private Object data; // SDP sau ICE candidate

    // Getters & Setters
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    
    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}
```

---

## 🚀 Deployment & Testing

### 1. Pornește Backend + TURN Server

```bash
cd d:\TextOnlyTurnServer
docker-compose up -d
```

Verifică:
- ✅ Backend: http://localhost:8080
- ✅ PostgreSQL: localhost:5433
- ✅ TURN: localhost:3478

### 2. Testează conexiunea REST API

```bash
# Test register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "displayName": "Test User"
  }'

# Test login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

### 3. Testează WebSocket

**Browser Console:**
```javascript
const socket = new SockJS('http://localhost:8080/ws/sync');
const stompClient = Stomp.over(socket);

stompClient.connect({}, (frame) => {
  console.log('Connected:', frame);
  
  stompClient.subscribe('/topic/chat/1', (message) => {
    console.log('Received:', JSON.parse(message.body));
  });
});
```

### 4. Pornește Aplicația Web

```bash
npm start
# sau
yarn start
```

### 5. Pornește Aplicația Android

1. Deschide proiectul în Android Studio
2. Verifică `Config.kt` - schimbă `BASE_DOMAIN` cu domeniul tău
3. Run pe emulator sau device

---

## 📊 Flow Complet de Comunicare

```
Web User                Backend              Android User
   |                       |                      |
   |--POST /api/auth/login->                     |
   |<---JWT Token----------|                     |
   |                       |                      |
   |--WS Connect---------->|                     |
   |<--Connected-----------|                     |
   |                       |<--WS Connect---------|
   |                       |---Connected--------->|
   |                       |                      |
   |--Send Message-------->|                     |
   |                       |--WS Broadcast------>|
   |<--Message Delivered---|                     |
   |                       |                      |
   |--Start Video Call---->|                     |
   |                       |--Signal (offer)---->|
   |                       |<--Signal (answer)---|
   |<--Signal (answer)-----|                     |
   |                       |                      |
   |<--------WebRTC P2P Connection (via TURN)-->|
   |                       |                      |
```

---

## 🔐 Securitate

### Web
```javascript
// Salvează token-ul
localStorage.setItem('authToken', token);

// Adaugă la toate request-urile
headers: {
  'Authorization': `Bearer ${token}`
}
```

### Android
```kotlin
// SharedPreferences
val token = prefs.getString("authToken", "")

// Retrofit interceptor
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}
```

---

## 📝 Variabile de Mediu

### Web (.env)
```env
REACT_APP_API_URL=https://your-domain.com/api
REACT_APP_WS_URL=wss://your-domain.com/ws/sync
REACT_APP_TURN_URL=turn:your-domain.com:3478
REACT_APP_TURN_USER=demo
REACT_APP_TURN_PASS=demo1234
```

### Android (Config.kt)
```kotlin
const val BASE_DOMAIN = "your-domain.com"
const val BASE_URL = "https://$BASE_DOMAIN"
const val WS_URL = "wss://$BASE_DOMAIN/ws"
const val TURN_URL = "turn:$BASE_DOMAIN:3478"
```

---

## 🎯 Checklist Final

### Backend
- [x] REST API funcțional
- [x] WebSocket configurat
- [x] TURN Server activ
- [x] PostgreSQL conectat
- [x] JWT implementat

### Web
- [ ] API Client creat
- [ ] WebSocket Client STOMP
- [ ] WebRTC Service
- [ ] Componente UI (Chat, Video)
- [ ] Autentificare

### Android
- [x] REST API integrat
- [x] WebSocket (STOMP)
- [x] Autentificare JWT
- [ ] WebRTC pentru video calls

### Testing
- [ ] Test mesaje Web → Android
- [ ] Test mesaje Android → Web
- [ ] Test video call Web ↔ Android
- [ ] Test TURN relay

---

## 🆘 Troubleshooting

### WebSocket nu se conectează
```javascript
// Verifică CORS în SecurityConfig.java
.cors().configurationSource(request -> {
    CorsConfiguration cors = new CorsConfiguration();
    cors.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://your-web-domain.com"));
    cors.setAllowedMethods(Arrays.asList("*"));
    cors.setAllowedHeaders(Arrays.asList("*"));
    cors.setAllowCredentials(true);
    return cors;
})
```

### TURN Server nu funcționează
```bash
# Test TURN Server
docker exec -it coturn-server turnutils_uclient -v -u demo -w demo1234 your-domain.com
```

### Android nu primește mesaje
- Verifică că `StompMessagingService` e pornit
- Verifică token-ul JWT
- Verifică topic-ul: `/topic/chat/{userId}`

---

## 📞 Contact & Support

Pentru întrebări sau probleme:
- Backend logs: `docker logs textonly-backend`
- TURN logs: `docker logs coturn-server`
- Database: `docker exec -it textonly-postgres psql -U postgres -d textonly`

**Succes cu integrarea! 🚀**
