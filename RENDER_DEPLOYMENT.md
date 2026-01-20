# 🚀 Ghid Deployment pe Render.com

## 📋 Pregătirea aplicației

Backend-ul tău TextOnly este acum **gata pentru deployment** pe Render! Am pregătit toate fișierele necesare:

### ✅ Ce am pregătit:

1. **4 Service-uri noi** - ServerService, ChannelService, StoreService, WalletService
2. **JWT complet funcțional** - toate controller-ele extrag userId din token
3. **Dockerfile optimizat** - multi-stage build cu Maven + Java 21
4. **render.yaml configurat** - PostgreSQL database + Spring Boot backend
5. **application-prod.properties** - `ddl-auto=update` pentru creare automată de tabele

---

## 🎯 Pași pentru Deployment pe Render

### **Pas 1: Pregătește repository-ul GitHub**

```bash
# Din directorul d:\TextOnlyTurnServer\

# Adaugă toate fișierele noi
git add .

# Commit cu toate schimbările
git commit -m "Prepare for Render deployment: Add Services, JWT, and new models"

# Push pe GitHub
git push origin main
```

### **Pas 2: Creează cont pe Render (dacă nu ai)**

1. Mergi pe [render.com](https://render.com)
2. Sign up cu GitHub account
3. Autorizează Render să acceseze repository-urile tale

### **Pas 3: Deploy folosind render.yaml (Recomandat)**

#### **Opțiunea A: Blueprint (Automat - RECOMANDAT)**

1. În Render Dashboard, click pe **"New +"** → **"Blueprint"**
2. Selectează repository-ul: `TextOnlyTurnServer`
3. Render va detecta automat `render.yaml`
4. Click pe **"Apply"**
5. Render va crea automat:
   - PostgreSQL database (`textonly-db`)
   - Web service (`textonly-backend`)
   - Toate environment variables configurate automat

#### **Opțiunea B: Manual**

**Pas 3.1: Creează PostgreSQL Database**

1. Click **"New +"** → **"PostgreSQL"**
2. Configurare:
   - **Name**: `textonly-db`
   - **Database**: `textonly`
   - **User**: `postgres` (auto-generat)
   - **Region**: Frankfurt (sau cel mai apropiat)
   - **Plan**: Free
3. Click **"Create Database"**
4. Așteaptă 2-3 minute până devine disponibil
5. **Notează Internal Database URL** (o să-l folosești mai jos)

**Pas 3.2: Creează Web Service**

1. Click **"New +"** → **"Web Service"**
2. Conectează repository-ul GitHub: `TextOnlyTurnServer`
3. Configurare:
   - **Name**: `textonly-backend`
   - **Region**: Frankfurt (același cu database-ul)
   - **Branch**: `main`
   - **Root Directory**: `.` (rădăcina)
   - **Environment**: **Docker**
   - **Dockerfile Path**: `./Dockerfile`
   - **Plan**: Free

**Pas 3.3: Environment Variables**

Adaugă următoarele variabile (click **"Advanced"** → **"Add Environment Variable"**):

```bash
# Port Render
PORT=10000

# Spring Profile
SPRING_PROFILES_ACTIVE=prod

# Database Connection (copiază Internal Database URL de la Pas 3.1)
DATABASE_URL=jdbc:postgresql://dpg-xxxxx.frankfurt-postgres.render.com/textonly

# Database Credentials (din Render PostgreSQL Dashboard)
DB_USERNAME=postgres
DB_PASSWORD=<password-generat-de-render>

# JWT Secret (generează un string aleator de 64+ caractere)
JWT_SECRET=ThisIsAVerySecureRandomSecretKeyForProductionUseAtLeast64Characters123456789
```

**Pas 3.4: Deploy**

1. Click **"Create Web Service"**
2. Render va:
   - Clona repository-ul
   - Rula `docker build` (5-10 minute prima dată)
   - Deploy aplicația
3. Monitorizează progresul în **Logs**

---

## 📊 Verificare Deployment

### **Pas 4: Testează API-ul**

După ce deployment-ul este **Live** (verde în Dashboard):

```bash
# URL-ul tău va fi ceva de genul:
https://textonly-backend.onrender.com

# Testează health check
curl https://textonly-backend.onrender.com/api/users/me

# Testează register
curl -X POST https://textonly-backend.onrender.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "displayName": "Test User",
    "password": "password123"
  }'

# Testează login
curl -X POST https://textonly-backend.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'

# Răspuns așteptat:
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "email": "test@example.com",
  "displayName": "Test User"
}
```

### **Pas 5: Verifică Database-ul**

1. În Render Dashboard → **textonly-db** → **Connect**
2. Copiază **PSQL Command**:
   ```bash
   PGPASSWORD=xxx psql -h dpg-xxx.frankfurt-postgres.render.com -U postgres textonly
   ```
3. Rulează în terminal local (trebuie să ai `psql` instalat)
4. Verifică tabelele create automat:
   ```sql
   \dt  -- listează toate tabelele
   
   SELECT * FROM users LIMIT 5;
   SELECT * FROM servers LIMIT 5;
   SELECT * FROM channels LIMIT 5;
   ```

---

## 🎨 Endpoints Disponibile

După deployment, backend-ul tău expune:

### **Authentication**
- `POST /api/auth/register` - Înregistrare user nou
- `POST /api/auth/login` - Login și primire JWT token

### **Users**
- `GET /api/users/me` - Profilul meu (JWT required)
- `GET /api/users/search?query=john` - Căutare useri

### **Messages**
- `POST /api/messages` - Trimite mesaj 1-on-1
- `GET /api/messages/conversation/{userId}` - Conversație cu cineva

### **Servers (Discord-like)**
- `POST /api/servers` - Creează server nou
- `GET /api/servers` - Serverele tale
- `POST /api/servers/{id}/members/{userId}` - Adaugă membru

### **Channels**
- `POST /api/channels` - Creează channel în server
- `GET /api/channels/server/{serverId}` - Channel-urile unui server
- `POST /api/channels/{id}/messages` - Trimite mesaj în channel
- `GET /api/channels/{id}/messages` - Citește mesaje din channel

### **Store**
- `GET /api/store/items?type=EMOTICON` - Produse din store
- `POST /api/store/buy` - Cumpără item
- `GET /api/store/inventory` - Inventarul meu

### **Wallet**
- `GET /api/wallet` - Wallet-ul meu (coins, spent, earned)
- `POST /api/wallet/add` - Adaugă coins
- `GET /api/wallet/transactions` - Istoric tranzacții

---

## 🔧 Troubleshooting

### **Eroare: "Container failed to start"**
- Verifică **Logs** în Render Dashboard
- Caută erori de conexiune la database
- Asigură-te că `DATABASE_URL` este corect

### **Eroare: "Database connection failed"**
- Verifică că database-ul `textonly-db` este **Available** (verde)
- Verifică că `DB_USERNAME` și `DB_PASSWORD` sunt corecte
- Testează conexiunea manual cu `psql`

### **Eroare: "Port already in use"**
- Asigură-te că `PORT=10000` este setat în Environment Variables
- Render va asigna automat portul corect

### **Eroare: "JWT token invalid"**
- Verifică că `JWT_SECRET` este setat în Environment Variables
- Token-ul trebuie să fie același la login și la verificare

### **Build-ul durează prea mult**
- Prima dată durează 5-10 minute (descarcă dependencies)
- Build-urile următoare sunt mai rapide (cache)
- Dacă durează >15 minute, verifică Logs pentru erori

---

## 🚀 Deployment Automat (CI/CD)

În `render.yaml` am setat `autoDeploy: true`, deci:

- **Orice push pe `main`** → Render va rebuild & redeploy automat
- **Durată rebuild**: 3-5 minute (după primul build)
- **Zero downtime**: Render păstrează vechea versiune până când noua e ready

---

## 📱 Integrare cu Android & Web

După ce backend-ul e live, actualizează URL-ul în aplicațiile tale:

### **Android (Kotlin)**
```kotlin
// ApiClient.kt
object ApiConfig {
    const val BASE_URL = "https://textonly-backend.onrender.com/"
    const val WS_URL = "wss://textonly-backend.onrender.com/ws"
}
```

### **Web (React)**
```javascript
// src/services/api.js
const API_BASE_URL = 'https://textonly-backend.onrender.com/api';
const WS_URL = 'wss://textonly-backend.onrender.com/ws';
```

---

## ⚡ Performance Notes

### **Free Plan Limitations:**
- Backend-ul va **sleep după 15 min inactivitate**
- Primul request după sleep durează **30-60 secunde** (cold start)
- Database: **256MB RAM**, **1GB storage**

### **Soluții pentru Cold Start:**
1. **Cron job ping** (Render nu permite pe Free plan)
2. **Upgrade la Starter plan** ($7/lună) - no sleep
3. **UptimeRobot** - ping la 5 minute (externe)

---

## 🎉 Success!

Dacă vezi în Logs:
```
Started TextOnlyBackendApplication in X seconds
Tomcat started on port(s): 10000 (http)
```

✅ **Backend-ul tău este LIVE pe internet!**

URL-ul final: `https://textonly-backend.onrender.com`

---

## 📚 Next Steps

1. **Testează toate endpoint-urile** cu Postman/Insomnia
2. **Actualizează URL-ul** în aplicațiile Android și Web
3. **Creează câteva servere și channel-uri** de test
4. **Monitorizează performanța** în Render Dashboard
5. **Activează Backups** pentru database (Settings → Backups)

---

## 🆘 Need Help?

- **Render Docs**: https://render.com/docs
- **Render Community**: https://community.render.com
- **Spring Boot Logs**: Check Render Dashboard → Logs
- **Database Logs**: textonly-db → Logs

---

**Backend-ul tău este production-ready! 🎊**
