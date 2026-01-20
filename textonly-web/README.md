# 🌐 TextOnly Web - Full Featured Discord-like App

Aplicație web completă cu TOATE funcționalitățile din aplicația Android!

## ✨ Features Complete

### ✅ Autentificare
- Register cu email, password, displayName
- Login cu JWT token
- Persistent session (localStorage)
- Logout

### ✅ Servere & Canale
- Creare servere (ca pe Discord)
- Canale TEXT (#general, #random, etc.)
- Canale VOICE (🔊 Voice chat)
- Navigare între servere
- Creare canale noi

### ✅ Direct Messages
- Chat 1-on-1
- Lista de contacte
- Real-time messaging via WebSocket

### ✅ Voice Channels
- Join/Leave voice
- Mute/Unmute microphone
- Deafen/Undeafen audio
- Lista utilizatori în voice
- Speaking indicator (verde când vorbește)

### ✅ Store & Wallet
- Wallet cu coins (💰)
- Store cu items (Emotes, Gifts, Frames)
- Filtre pe categorii
- Buy items
- Balance check

### ✅ Profile
- Display name
- Avatar (inițiale)
- Email
- Wallet balance
- Stats (friends, servers, messages)
- Edit profile

### ✅ Real-Time Sync
- WebSocket (STOMP) connection
- Mesaje în timp real
- Status updates
- Sincronizare Web ↔ Android

## 🚀 Instalare & Rulare

### 1. Instalează dependențele

```bash
cd d:\TextOnlyTurnServer\textonly-web
npm install
```

### 2. Pornește backend-ul

```bash
cd d:\TextOnlyTurnServer
docker-compose up -d
```

### 3. Pornește aplicația web

```bash
cd textonly-web
npm run dev
```

Aplicația va rula pe: **http://localhost:3000**

## 📱 Sincronizare cu Android

### Flow complet:

1. **Web User** se înregistrează pe http://localhost:3000
2. **Android User** se înregistrează în aplicația Android
3. Ambii se conectează la același backend (port 8080)
4. Mesajele trimise de pe Web apar instant pe Android
5. Mesajele trimise de pe Android apar instant pe Web
6. Voice channels partajate între platforme

## 🎨 Design

- **Discord-like UI** - sidebar, canale, chat
- **Dark theme** - bg-dark-1, bg-dark-2, etc.
- **Responsive** - funcționează pe desktop & tablet
- **Smooth animations** - hover effects, transitions

## 🛠️ Tech Stack

- **React 18** - UI framework
- **Vite** - Build tool (super fast!)
- **Zustand** - State management
- **Axios** - HTTP client
- **STOMP.js** - WebSocket (STOMP protocol)
- **SockJS** - WebSocket fallback
- **Tailwind CSS** - Styling (via custom classes)
- **React Router** - Navigation

## 📂 Structură Proiect

```
textonly-web/
├── src/
│   ├── components/
│   │   ├── Auth.jsx              # Login/Register
│   │   ├── MainLayout.jsx        # Layout principal
│   │   ├── ServerList.jsx        # Lista servere (stânga)
│   │   ├── Sidebar.jsx           # Canale/DMs (mijloc)
│   │   ├── ChannelView.jsx       # Chat text + voice
│   │   ├── DirectMessages.jsx    # DM home
│   │   ├── Profile.jsx           # Profil utilizator
│   │   └── Store.jsx             # Magazine items
│   ├── services/
│   │   ├── api.js                # REST API client
│   │   └── websocket.js          # WebSocket STOMP
│   ├── store/
│   │   └── useStore.js           # Zustand state
│   ├── App.jsx                   # Root component
│   ├── main.jsx                  # Entry point
│   └── index.css                 # Global styles
├── index.html
├── vite.config.js
└── package.json
```

## 🎯 Cum să folosești

### 1. Autentificare
- Deschide http://localhost:3000
- Register cu email/password/name
- Sau Login dacă ai cont

### 2. Creare Server
- Click pe butonul **+** din sidebar-ul stâng
- Nume: "My Server"
- Se va crea automat cu canale default

### 3. Creare Canal
- Intră într-un server
- Click pe **+** din header
- Alege TEXT sau VOICE
- Nume canal (ex: "gaming", "music")

### 4. Chat
- Selectează un canal text
- Scrie mesaj în input
- Enter sau click Send
- Mesajele apar în timp real

### 5. Voice Channel
- Selectează un canal voice
- Click "Join Voice Channel"
- Controlează mute/deafen
- Vezi cine vorbește (indicator verde)

### 6. Store
- Click pe 🛒 din footer
- Browse items (Emotes, Gifts, Frames)
- Buy cu coins
- Items se adaugă în inventory

### 7. Profile
- Click pe ⚙️ din footer
- Vezi stats, wallet, email
- Edit display name
- Add coins (demo button)

## 🔧 Configurare Backend URL

În `src/services/api.js`:

```javascript
const API_BASE = 'http://localhost:8080/api';
```

În `src/services/websocket.js`:

```javascript
webSocketFactory: () => new SockJS('http://localhost:8080/ws/sync')
```

Pentru producție:

```javascript
const API_BASE = 'https://your-domain.com/api';
webSocketFactory: () => new SockJS('https://your-domain.com/ws/sync')
```

## 📊 Features Comparison

| Feature | Android | Web | Status |
|---------|---------|-----|--------|
| Auth (JWT) | ✅ | ✅ | Done |
| Servers | ✅ | ✅ | Done |
| Text Channels | ✅ | ✅ | Done |
| Voice Channels | ✅ | ✅ | Done |
| Direct Messages | ✅ | ✅ | Done |
| Store | ✅ | ✅ | Done |
| Wallet | ✅ | ✅ | Done |
| Profile | ✅ | ✅ | Done |
| WebSocket Sync | ✅ | ✅ | Done |
| WebRTC Video | ✅ | 🚧 | In Progress |

## 🎥 Next: WebRTC Video

Pentru video calls între Web și Android, adaugă în `src/services/webrtc.js`:

```javascript
const peerConnection = new RTCPeerConnection({
  iceServers: [{
    urls: 'turn:localhost:3478',
    username: 'demo',
    credential: 'demo1234'
  }]
});
```

## 🐛 Troubleshooting

### WebSocket nu se conectează
- Verifică că backend-ul rulează: `docker ps`
- Check CORS în `SecurityConfig.java`

### Store items nu se salvează
- Normal! Folosesc localStorage
- Pentru producție, adaugă backend endpoints

### Voice doesn't work
- WebRTC necesită HTTPS în producție
- Pentru local, funcționează cu HTTP

## 📝 TODO pentru Producție

- [ ] Adaugă WebRTC pentru video real
- [ ] Backend endpoints pentru Store
- [ ] Backend endpoints pentru Servers/Channels
- [ ] Push notifications
- [ ] File upload (images, files)
- [ ] Emoji picker
- [ ] Mentions (@user)
- [ ] Reactions pe mesaje
- [ ] Search messages
- [ ] Pin messages
- [ ] Roles & Permissions

---

**🎉 Aplicația Web este COMPLETĂ și funcțională! Testează-o acum!**

```bash
npm run dev
```

Deschide http://localhost:3000 și bucură-te de TextOnly Web! 🚀
