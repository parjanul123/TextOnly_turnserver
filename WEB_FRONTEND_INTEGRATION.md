# TextOnly - Web Frontend Integration Guide

## 📋 Overview

Clientul Web conectează la backend centralizat (TextOnlyTurnServer) via:
- **REST API** pentru operații CRUD
- **WebSocket** pentru sincronizare real-time

---

## 🔧 Configurare Frontend

### 1. Crează API Service

**`src/services/apiClient.js`**

```javascript
const API_BASE = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

class TextOnlyAPI {
  constructor() {
    this.token = localStorage.getItem('token');
  }

  setToken(token) {
    this.token = token;
    localStorage.setItem('token', token);
  }

  getHeaders() {
    return {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${this.token}`
    };
  }

  // Auth endpoints
  async register(email, password, displayName) {
    return this.post('/auth/register', { email, password, displayName });
  }

  async login(email, password) {
    return this.post('/auth/login', { email, password });
  }

  async logout() {
    return this.post('/auth/logout', {});
  }

  async validateToken() {
    return this.get('/auth/validate-token');
  }

  // User endpoints
  async getProfile(userId) {
    return this.get(`/users/${userId}`);
  }

  async updateProfile(userId, profile) {
    return this.put(`/users/${userId}/profile`, profile);
  }

  async updateStatus(userId, status) {
    return this.patch(`/users/${userId}/status?status=${status}`);
  }

  async searchUsers(query) {
    return this.get(`/users/search?query=${query}`);
  }

  // Message endpoints
  async sendMessage(receiverId, content) {
    return this.post('/messages', { receiverId, content });
  }

  async getConversation(otherUserId) {
    return this.get(`/messages/conversation/${otherUserId}`);
  }

  async getUnreadMessages() {
    return this.get('/messages/unread');
  }

  async markAsRead(messageId) {
    return this.patch(`/messages/${messageId}/read`);
  }

  // Contact endpoints
  async getContacts() {
    return this.get('/contacts');
  }

  async addContact(contactId) {
    return this.post(`/contacts/${contactId}`);
  }

  async removeContact(contactId) {
    return this.delete(`/contacts/${contactId}`);
  }

  // Helper methods
  async get(endpoint) {
    const response = await fetch(`${API_BASE}${endpoint}`, {
      method: 'GET',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async post(endpoint, data) {
    const response = await fetch(`${API_BASE}${endpoint}`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(data)
    });
    return this.handleResponse(response);
  }

  async put(endpoint, data) {
    const response = await fetch(`${API_BASE}${endpoint}`, {
      method: 'PUT',
      headers: this.getHeaders(),
      body: JSON.stringify(data)
    });
    return this.handleResponse(response);
  }

  async patch(endpoint, data = {}) {
    const response = await fetch(`${API_BASE}${endpoint}`, {
      method: 'PATCH',
      headers: this.getHeaders(),
      body: Object.keys(data).length > 0 ? JSON.stringify(data) : null
    });
    return this.handleResponse(response);
  }

  async delete(endpoint) {
    const response = await fetch(`${API_BASE}${endpoint}`, {
      method: 'DELETE',
      headers: this.getHeaders()
    });
    return this.handleResponse(response);
  }

  async handleResponse(response) {
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'API Error');
    }
    return response.json();
  }
}

export default new TextOnlyAPI();
```

### 2. Configurare WebSocket

**`src/services/wsClient.js`**

```javascript
import SockJS from 'sockjs-client';
import StompJs from '@stomp/stompjs';

class WebSocketClient {
  constructor(token) {
    this.token = token;
    this.client = null;
    this.listeners = {};
  }

  connect() {
    return new Promise((resolve, reject) => {
      const socket = new SockJS('http://localhost:8080/ws/sync');
      
      this.client = new StompJs.Client({
        webSocketFactory: () => socket,
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000
      });

      this.client.onConnect = () => {
        console.log('✅ WebSocket connected');
        this.subscribeToTopics();
        resolve();
      };

      this.client.onStompError = (error) => {
        console.error('❌ WebSocket error:', error);
        reject(error);
      };

      this.client.activate();
    });
  }

  subscribeToTopics() {
    const userId = localStorage.getItem('userId');

    // Subscribe to chat messages
    this.client.subscribe(`/topic/chat/${userId}`, (message) => {
      this.emit('message', JSON.parse(message.body));
    });

    // Subscribe to profile updates
    this.client.subscribe(`/topic/user/${userId}`, (message) => {
      this.emit('profile', JSON.parse(message.body));
    });

    // Subscribe to status changes
    this.client.subscribe('/topic/users/status', (message) => {
      this.emit('status', JSON.parse(message.body));
    });
  }

  sendMessage(receiverId, content) {
    if (!this.client?.connected) {
      console.error('WebSocket not connected');
      return;
    }

    this.client.publish({
      destination: `/app/chat/${receiverId}`,
      body: JSON.stringify({
        type: 'message.sent',
        senderId: localStorage.getItem('userId'),
        content,
        timestamp: Date.now()
      })
    });
  }

  updateProfile(profile) {
    if (!this.client?.connected) return;

    const userId = localStorage.getItem('userId');
    this.client.publish({
      destination: `/app/user/${userId}/profile`,
      body: JSON.stringify({
        type: 'profile.updated',
        data: profile,
        timestamp: Date.now()
      })
    });
  }

  updateStatus(status) {
    if (!this.client?.connected) return;

    const userId = localStorage.getItem('userId');
    this.client.publish({
      destination: `/app/user/${userId}/status`,
      body: JSON.stringify({
        type: 'user.status.changed',
        status,
        timestamp: Date.now()
      })
    });
  }

  on(event, callback) {
    if (!this.listeners[event]) {
      this.listeners[event] = [];
    }
    this.listeners[event].push(callback);
  }

  emit(event, data) {
    if (this.listeners[event]) {
      this.listeners[event].forEach(callback => callback(data));
    }
  }

  disconnect() {
    if (this.client?.connected) {
      this.client.deactivate();
    }
  }
}

export default WebSocketClient;
```

### 3. Exemplu React Component

**`src/components/ChatWindow.jsx`**

```javascript
import React, { useState, useEffect } from 'react';
import apiClient from '../services/apiClient';
import WebSocketClient from '../services/wsClient';

function ChatWindow({ contactId }) {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [wsClient, setWsClient] = useState(null);

  useEffect(() => {
    // Initialize WebSocket
    const ws = new WebSocketClient(localStorage.getItem('token'));
    ws.connect().then(() => {
      setWsClient(ws);

      // Listen for incoming messages
      ws.on('message', (msg) => {
        if (msg.senderId === contactId || msg.receiverId === contactId) {
          setMessages(prev => [...prev, msg]);
        }
      });
    });

    // Load conversation history
    loadMessages();

    return () => ws.disconnect();
  }, [contactId]);

  async function loadMessages() {
    try {
      const conversation = await apiClient.getConversation(contactId);
      setMessages(conversation.reverse());
    } catch (error) {
      console.error('Failed to load messages:', error);
    }
  }

  async function handleSendMessage() {
    if (!newMessage.trim()) return;

    try {
      // Send via API
      await apiClient.sendMessage(contactId, newMessage);

      // Also send via WebSocket for real-time
      wsClient?.sendMessage(contactId, newMessage);

      setNewMessage('');
    } catch (error) {
      console.error('Failed to send message:', error);
    }
  }

  return (
    <div className="chat-window">
      <div className="messages">
        {messages.map(msg => (
          <div key={msg.id} className={msg.senderId === contactId ? 'message-received' : 'message-sent'}>
            <strong>{msg.senderName}</strong>
            <p>{msg.content}</p>
            <small>{new Date(msg.createdAt).toLocaleTimeString()}</small>
          </div>
        ))}
      </div>

      <div className="input-area">
        <input
          type="text"
          value={newMessage}
          onChange={e => setNewMessage(e.target.value)}
          onKeyPress={e => e.key === 'Enter' && handleSendMessage()}
          placeholder="Type a message..."
        />
        <button onClick={handleSendMessage}>Send</button>
      </div>
    </div>
  );
}

export default ChatWindow;
```

### 4. Environment Variables

**`.env`**

```env
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_WS_URL=http://localhost:8080/ws
NODE_ENV=development
```

### 5. Package.json Dependencies

```json
{
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "sockjs-client": "^1.6.1",
    "@stomp/stompjs": "^7.0.0"
  }
}
```

---

## 🚀 Deployment

### Production API URL

```javascript
// src/services/apiClient.js
const API_BASE = process.env.NODE_ENV === 'production'
  ? 'https://api.textonly.com/api'  // Your production URL
  : 'http://localhost:8080/api';
```

---

## ✅ Testing

```bash
# Test API connectivity
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"test123"}'

# Test WebSocket
npm test
```

---

## 📱 Cross-Origin (CORS)

Backend-ul este configurat cu CORS permis pe `*` pentru development.

Pentru **production**, actualizează `SecurityConfig.java`:

```java
configuration.setAllowedOrigins(Arrays.asList("https://yourdomain.com"));
```

---

**Continuează cu Android Integration Guide!**
