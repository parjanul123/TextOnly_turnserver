import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class WebSocketService {
  constructor() {
    this.client = null;
    this.connected = false;
    this.subscriptions = new Map();
    this.messageCallbacks = [];
    this.statusCallbacks = [];
  }

  connect(userId) {
    return new Promise((resolve, reject) => {
      const token = localStorage.getItem('authToken');
      
      this.client = new Client({
        webSocketFactory: () => new SockJS('http://localhost:8080/ws/sync'),
        
        connectHeaders: {
          Authorization: `Bearer ${token}`
        },

        debug: (str) => {
          console.log('[STOMP]', str);
        },

        onConnect: () => {
          console.log('✅ WebSocket Connected');
          this.connected = true;

          // Subscribe la mesaje personale
          this.client.subscribe(`/topic/chat/${userId}`, (message) => {
            const data = JSON.parse(message.body);
            this.messageCallbacks.forEach(cb => cb(data));
          });

          // Subscribe la status
          this.client.subscribe('/topic/users/status', (message) => {
            const data = JSON.parse(message.body);
            this.statusCallbacks.forEach(cb => cb(data));
          });

          resolve();
        },

        onStompError: (frame) => {
          console.error('❌ STOMP Error:', frame);
          this.connected = false;
          reject(frame);
        },

        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000
      });

      this.client.activate();
    });
  }

  onMessage(callback) {
    this.messageCallbacks.push(callback);
    return () => {
      this.messageCallbacks = this.messageCallbacks.filter(cb => cb !== callback);
    };
  }

  onStatus(callback) {
    this.statusCallbacks.push(callback);
    return () => {
      this.statusCallbacks = this.statusCallbacks.filter(cb => cb !== callback);
    };
  }

  sendMessage(destination, payload) {
    if (this.connected && this.client) {
      this.client.publish({
        destination: `/app${destination}`,
        body: JSON.stringify(payload)
      });
    }
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.connected = false;
      this.messageCallbacks = [];
      this.statusCallbacks = [];
    }
  }
}

export default new WebSocketService();
