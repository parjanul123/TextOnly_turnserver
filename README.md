
# TURN Server Comun pentru TextOnly

Acest folder conține infrastructura comună (coturn) folosită atât de backend, cât și de aplicația Android.

## Structură
- `Dockerfile` — imaginea pentru serverul coturn
- `render.yaml` — configurare deploy Render pentru coturn
- `README.md` — acest fișier, cu instrucțiuni

## Date de conectare (exemplu)
- **Host:** textonly.onrender.com (sau adresa Render după deploy)
- **Port:** 3478
- **User:** demo
- **Parolă:** demo1234
- **Realm:** textonly.onrender.com

## Exemplu configurare WebRTC (JavaScript/Android/Java)

### JavaScript (browser)
```js
const iceServers = [{
	urls: 'turn:textonly.onrender.com:3478',
	username: 'demo',
	credential: 'demo1234'
}];
```

### Android (Java/Kotlin)
```java
PeerConnection.IceServer turnServer = PeerConnection.IceServer.builder(
		"turn:textonly.onrender.com:3478")
		.setUsername("demo")
		.setPassword("demo1234")
		.createIceServer();
```

### Java (Spring backend, dacă e nevoie)
```java
// Exemplu de listă ICE servers pentru clienți WebRTC
List<IceServer> iceServers = List.of(
		new IceServer("turn:textonly.onrender.com:3478", "demo", "demo1234")
);
```

## Notă
- Poți schimba user/parolă/realm în Dockerfile și să redeployezi dacă vrei alte date.
- Folosește aceste date în orice client care are nevoie de relay NAT (WebRTC, etc).
"# TextOnly_turnserver" 
"# TextOnly_turnserver" 
"# TextOnly_turnserver" 
