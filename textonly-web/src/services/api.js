import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

class TextOnlyAPI {
  constructor() {
    this.client = axios.create({
      baseURL: API_BASE,
      headers: {
        'Content-Type': 'application/json'
      }
    });

    // Interceptor pentru token
    this.client.interceptors.request.use((config) => {
      const token = localStorage.getItem('authToken');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    });
  }

  // ==================== AUTH ====================
  async register(email, password, displayName) {
    const { data } = await this.client.post('/auth/register', { 
      email, password, displayName 
    });
    if (data.token) {
      localStorage.setItem('authToken', data.token);
      localStorage.setItem('userId', data.userId);
      localStorage.setItem('userEmail', email);
      localStorage.setItem('displayName', displayName);
    }
    return data;
  }

  async login(email, password) {
    const { data } = await this.client.post('/auth/login', { email, password });
    if (data.token) {
      localStorage.setItem('authToken', data.token);
      localStorage.setItem('userId', data.userId);
      localStorage.setItem('userEmail', data.email);
      localStorage.setItem('displayName', data.displayName);
    }
    return data;
  }

  async validateToken() {
    return await this.client.get('/auth/validate-token');
  }

  logout() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('displayName');
  }

  // ==================== USERS ====================
  async getProfile(userId) {
    const { data } = await this.client.get(`/users/${userId}`);
    return data;
  }

  async updateProfile(userId, profile) {
    const { data } = await this.client.put(`/users/${userId}/profile`, profile);
    return data;
  }

  async updateStatus(userId, status) {
    const { data } = await this.client.patch(`/users/${userId}/status?status=${status}`);
    return data;
  }

  async searchUsers(query) {
    const { data } = await this.client.get(`/users/search?query=${query}`);
    return data;
  }

  // ==================== MESSAGES ====================
  async sendMessage(receiverId, content) {
    const { data } = await this.client.post('/messages', { receiverId, content });
    return data;
  }

  async getConversation(otherUserId) {
    const { data } = await this.client.get(`/messages/conversation/${otherUserId}`);
    return data;
  }

  async getUnreadMessages() {
    const { data } = await this.client.get('/messages/unread');
    return data;
  }

  async markAsRead(messageId) {
    const { data } = await this.client.patch(`/messages/${messageId}/read`);
    return data;
  }

  // ==================== CONTACTS ====================
  async getContacts() {
    const { data } = await this.client.get('/contacts');
    return data;
  }

  async addContact(contactId) {
    const { data } = await this.client.post(`/contacts/${contactId}`);
    return data;
  }

  async removeContact(contactId) {
    const { data } = await this.client.delete(`/contacts/${contactId}`);
    return data;
  }

  // ==================== SERVERS (LOCAL STORAGE) ====================
  getServers() {
    const servers = localStorage.getItem('servers');
    return servers ? JSON.parse(servers) : [];
  }

  createServer(name) {
    const servers = this.getServers();
    const newServer = {
      id: Date.now(),
      name,
      channels: [
        { id: 1, name: 'general', type: 'TEXT' },
        { id: 2, name: 'Voice', type: 'VOICE' }
      ]
    };
    servers.push(newServer);
    localStorage.setItem('servers', JSON.stringify(servers));
    return newServer;
  }

  deleteServer(serverId) {
    const servers = this.getServers().filter(s => s.id !== serverId);
    localStorage.setItem('servers', JSON.stringify(servers));
  }

  // ==================== CHANNELS ====================
  createChannel(serverId, channelName, type = 'TEXT') {
    const servers = this.getServers();
    const server = servers.find(s => s.id === serverId);
    if (server) {
      const newChannel = {
        id: Date.now(),
        name: channelName,
        type
      };
      server.channels.push(newChannel);
      localStorage.setItem('servers', JSON.stringify(servers));
      return newChannel;
    }
  }

  // ==================== WALLET & STORE ====================
  getWallet() {
    const coins = localStorage.getItem('userCoins');
    return parseInt(coins) || 0;
  }

  updateWallet(amount) {
    const current = this.getWallet();
    const newAmount = current + amount;
    localStorage.setItem('userCoins', newAmount.toString());
    return newAmount;
  }

  getStoreItems() {
    // Mock data - în producție ar veni de pe server
    return [
      { id: 1, name: '🔥 Fire Emoji', price: 100, type: 'EMOTICON' },
      { id: 2, name: '💎 Diamond', price: 500, type: 'GIFT' },
      { id: 3, name: '👑 Crown Frame', price: 1000, type: 'FRAME' },
      { id: 4, name: '⭐ Star', price: 200, type: 'EMOTICON' },
      { id: 5, name: '🎁 Gift Box', price: 300, type: 'GIFT' }
    ];
  }

  getInventory() {
    const inventory = localStorage.getItem('inventory');
    return inventory ? JSON.parse(inventory) : [];
  }

  buyItem(item) {
    const inventory = this.getInventory();
    inventory.push(item);
    localStorage.setItem('inventory', JSON.stringify(inventory));
    this.updateWallet(-item.price);
  }
}

export default new TextOnlyAPI();
