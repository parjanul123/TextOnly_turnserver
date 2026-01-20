import { create } from 'zustand';
import api from '../services/api';
import ws from '../services/websocket';

export const useStore = create((set, get) => ({
  // Auth
  user: null,
  isAuthenticated: false,
  
  // Servers & Channels
  servers: [],
  currentServer: null,
  currentChannel: null,
  
  // Contacts & Conversations
  contacts: [],
  conversations: new Map(),
  
  // Messages
  messages: [],
  
  // Voice
  voiceConnected: false,
  voiceChannel: null,
  voiceUsers: [],
  
  // Wallet
  coins: 0,
  
  // UI State
  showProfile: false,
  showStore: false,
  showSettings: false,

  // ==================== ACTIONS ====================
  
  login: async (email, password) => {
    try {
      const data = await api.login(email, password);
      set({ 
        user: data, 
        isAuthenticated: true,
        coins: api.getWallet()
      });
      await ws.connect(data.userId);
      return data;
    } catch (error) {
      throw error;
    }
  },

  register: async (email, password, displayName) => {
    try {
      const data = await api.register(email, password, displayName);
      set({ 
        user: data, 
        isAuthenticated: true,
        coins: api.getWallet()
      });
      await ws.connect(data.userId);
      return data;
    } catch (error) {
      throw error;
    }
  },

  logout: () => {
    api.logout();
    ws.disconnect();
    set({ 
      user: null, 
      isAuthenticated: false,
      servers: [],
      contacts: [],
      messages: []
    });
  },

  loadServers: () => {
    const servers = api.getServers();
    set({ servers });
  },

  createServer: (name) => {
    const server = api.createServer(name);
    set({ servers: [...get().servers, server] });
    return server;
  },

  selectServer: (serverId) => {
    const server = get().servers.find(s => s.id === serverId);
    set({ currentServer: server, currentChannel: null });
  },

  selectChannel: (channelId) => {
    const channel = get().currentServer?.channels.find(c => c.id === channelId);
    set({ currentChannel: channel });
  },

  createChannel: (serverId, name, type) => {
    const channel = api.createChannel(serverId, name, type);
    const servers = get().servers.map(s => 
      s.id === serverId 
        ? { ...s, channels: [...s.channels, channel] }
        : s
    );
    set({ servers });
  },

  loadContacts: async () => {
    try {
      const contacts = await api.getContacts();
      set({ contacts });
    } catch (error) {
      console.error('Error loading contacts:', error);
    }
  },

  sendMessage: async (receiverId, content) => {
    try {
      const message = await api.sendMessage(receiverId, content);
      set({ messages: [...get().messages, message] });
      return message;
    } catch (error) {
      console.error('Error sending message:', error);
      throw error;
    }
  },

  loadConversation: async (userId) => {
    try {
      const messages = await api.getConversation(userId);
      set({ messages });
    } catch (error) {
      console.error('Error loading conversation:', error);
    }
  },

  updateWallet: (amount) => {
    const newAmount = api.updateWallet(amount);
    set({ coins: newAmount });
  },

  buyItem: (item) => {
    api.buyItem(item);
    set({ coins: api.getWallet() });
  },

  toggleProfile: () => set({ showProfile: !get().showProfile }),
  toggleStore: () => set({ showStore: !get().showStore }),
  toggleSettings: () => set({ showSettings: !get().showSettings }),
}));
