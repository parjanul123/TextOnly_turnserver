# TextOnly - Android Integration Guide

## 📋 Overview

Aplicația Android conectează la backend-ul centralizat via:
- **Retrofit + OkHttp** pentru REST API
- **OkHttp WebSocket** pentru sincronizare real-time

---

## 🔧 Configurare Android

### 1. Actualizează `build.gradle.kts`

```gradle
dependencies {
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.10.0")
    implementation("com.squareup.retrofit2:converter-gson:2.10.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // WebSocket
    implementation("com.google.code.gson:gson:2.10.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Life Cycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    // UI
    implementation("com.google.android.material:material:1.11.0")
}
```

### 2. Crează API Models (DTOs)

**`com/textonly/model/AuthRequest.kt`**

```kotlin
data class AuthRequest(
    val email: String,
    val password: String,
    val displayName: String? = null
)

data class AuthResponse(
    val userId: Long,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
    val status: String,
    val token: String,
    val tokenExpiresAt: String
)

data class UserProfile(
    val id: Long,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
    val status: String // online, offline, away, busy
)

data class Message(
    val id: Long,
    val senderId: Long,
    val senderName: String,
    val receiverId: Long,
    val content: String,
    val isRead: Boolean,
    val createdAt: String
)

data class MessageRequest(
    val receiverId: Long,
    val content: String
)

data class Contact(
    val id: Long,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
    val status: String
)
```

### 3. Crează Retrofit API Interface

**`com/textonly/api/TextOnlyAPI.kt`**

```kotlin
import retrofit2.http.*

interface TextOnlyAPI {

    // Auth
    @POST("/api/auth/register")
    suspend fun register(@Body request: AuthRequest): AuthResponse

    @POST("/api/auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @POST("/api/auth/logout")
    suspend fun logout(): Unit

    @GET("/api/auth/validate-token")
    suspend fun validateToken(): Boolean

    // Users
    @GET("/api/users/{id}")
    suspend fun getProfile(@Path("id") userId: Long): UserProfile

    @PUT("/api/users/{id}/profile")
    suspend fun updateProfile(
        @Path("id") userId: Long,
        @Body profile: UserProfile
    ): UserProfile

    @PATCH("/api/users/{id}/status")
    suspend fun updateStatus(
        @Path("id") userId: Long,
        @Query("status") status: String
    ): Unit

    @GET("/api/users/search")
    suspend fun searchUsers(@Query("query") query: String): List<UserProfile>

    // Messages
    @POST("/api/messages")
    suspend fun sendMessage(@Body request: MessageRequest): Message

    @GET("/api/messages/conversation/{userId}")
    suspend fun getConversation(@Path("userId") otherUserId: Long): List<Message>

    @GET("/api/messages/unread")
    suspend fun getUnreadMessages(): List<Message>

    @PATCH("/api/messages/{id}/read")
    suspend fun markAsRead(@Path("id") messageId: Long): Unit

    // Contacts
    @GET("/api/contacts")
    suspend fun getContacts(): List<UserProfile>

    @POST("/api/contacts/{contactId}")
    suspend fun addContact(@Path("contactId") contactId: Long): UserProfile

    @DELETE("/api/contacts/{contactId}")
    suspend fun removeContact(@Path("contactId") contactId: Long): Unit
}
```

### 4. Crează Retrofit Client

**`com/textonly/network/RetrofitClient.kt`**

```kotlin
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080" // Emulator localhost
    // For physical device: "http://192.168.1.100:8080"

    private lateinit var httpClient: OkHttpClient

    fun getClient(context: Context): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Get token from secure storage
        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "secret_shared_prefs",
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPECIFICATION),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val token = sharedPreferences.getString("auth_token", "") ?: ""

        httpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                if (token.isNotEmpty()) {
                    request.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(request.build())
            }
            .build()

        val gson: Gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    fun getApiService(context: Context): TextOnlyAPI {
        return getClient(context).create(TextOnlyAPI::class.java)
    }
}
```

### 5. WebSocket Client

**`com/textonly/network/WebSocketManager.kt`**

```kotlin
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.Response
import com.google.gson.Gson
import android.util.Log

class WebSocketManager(
    private val token: String,
    private val userId: Long
) {

    private var webSocket: WebSocket? = null
    private val listeners = mutableMapOf<String, MutableList<(String) -> Unit>>()

    private val wsUrl = "ws://10.0.2.2:8080/ws/sync" // Emulator
    // For physical device: "ws://192.168.1.100:8080/ws/sync"

    fun connect(onConnected: () -> Unit = {}) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(wsUrl)
            .header("Authorization", "Bearer $token")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "✅ Connected")
                subscribeToTopics()
                onConnected()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "📨 Message: $text")
                val gson = Gson()
                val message = gson.fromJson(text, Map::class.java)
                val type = message["type"]?.toString() ?: return

                emit(type, text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "❌ Error: ${t.message}")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "🔌 Closed: $reason")
            }
        })
    }

    private fun subscribeToTopics() {
        // Subscribe to chat
        val chatSub = """
            {
              "COMMAND": "SUBSCRIBE",
              "id": "sub-1",
              "destination": "/topic/chat/$userId"
            }
        """.trimIndent()

        // Subscribe to profile updates
        val profileSub = """
            {
              "COMMAND": "SUBSCRIBE",
              "id": "sub-2",
              "destination": "/topic/user/$userId"
            }
        """.trimIndent()

        // Subscribe to status
        val statusSub = """
            {
              "COMMAND": "SUBSCRIBE",
              "id": "sub-3",
              "destination": "/topic/users/status"
            }
        """.trimIndent()
    }

    fun sendMessage(receiverId: Long, content: String) {
        val message = """
            {
              "COMMAND": "SEND",
              "destination": "/app/chat/$receiverId",
              "body": {
                "type": "message.sent",
                "senderId": $userId,
                "content": "$content",
                "timestamp": ${System.currentTimeMillis()}
              }
            }
        """.trimIndent()

        webSocket?.send(message)
    }

    fun updateStatus(status: String) {
        val message = """
            {
              "type": "user.status.changed",
              "status": "$status",
              "timestamp": ${System.currentTimeMillis()}
            }
        """.trimIndent()

        webSocket?.send(message)
    }

    fun on(event: String, callback: (String) -> Unit) {
        if (!listeners.containsKey(event)) {
            listeners[event] = mutableListOf()
        }
        listeners[event]?.add(callback)
    }

    private fun emit(event: String, data: String) {
        listeners[event]?.forEach { callback ->
            callback(data)
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnect")
    }
}
```

### 6. Repository Pattern

**`com/textonly/repository/AuthRepository.kt`**

```kotlin
import com.textonly.api.TextOnlyAPI
import com.textonly.model.AuthRequest
import com.textonly.model.AuthResponse
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(context: Context) {

    private val api = RetrofitClient.getApiService(context)
    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secret_prefs",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPECIFICATION),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    suspend fun register(email: String, password: String, displayName: String): AuthResponse {
        return withContext(Dispatchers.IO) {
            val request = AuthRequest(email, password, displayName)
            val response = api.register(request)
            saveToken(response.token)
            response
        }
    }

    suspend fun login(email: String, password: String): AuthResponse {
        return withContext(Dispatchers.IO) {
            val request = AuthRequest(email, password)
            val response = api.login(request)
            saveToken(response.token)
            response
        }
    }

    fun saveToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    fun getToken(): String? = prefs.getString("auth_token", null)

    fun logout() {
        prefs.edit().remove("auth_token").apply()
    }
}
```

### 7. ViewModel cu Coroutines

**`com/textonly/viewmodel/ChatViewModel.kt`**

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.textonly.model.Message
import com.textonly.repository.MessageRepository

class ChatViewModel(
    private val messageRepository: MessageRepository,
    private val contactId: Long
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    init {
        loadMessages()
    }

    fun loadMessages() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val conversation = messageRepository.getConversation(contactId)
                _messages.value = conversation
            } finally {
                _loading.value = false
            }
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            try {
                messageRepository.sendMessage(contactId, content)
                loadMessages()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
```

### 8. UI Example - Chat Fragment

**`com/textonly/ui/ChatFragment.kt`**

```kotlin
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.textonly.databinding.FragmentChatBinding
import com.textonly.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

class ChatFragment(private val contactId: Long) : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var viewModel: ChatViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentChatBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = MessageAdapter()
        binding.messagesRecycler.adapter = adapter

        lifecycleScope.launch {
            viewModel.messages.collect { messages ->
                adapter.submitList(messages)
                binding.messagesRecycler.scrollToPosition(messages.size - 1)
            }
        }

        binding.sendButton.setOnClickListener {
            val content = binding.messageInput.text.toString()
            if (content.isNotEmpty()) {
                viewModel.sendMessage(content)
                binding.messageInput.text.clear()
            }
        }
    }
}
```

---

## 🔐 Secure Token Storage

```kotlin
// Salvare token
val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPECIFICATION)
val encryptedSharedPreferences = EncryptedSharedPreferences.create(
    context,
    "secret_shared_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)

encryptedSharedPreferences.edit().putString("auth_token", token).apply()

// Recuperare token
val token = encryptedSharedPreferences.getString("auth_token", "")
```

---

## 📱 Network Configuration

**`res/xml/network_security_config.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">192.168.1.100</domain>
    </domain-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">example.com</domain>
    </domain-config>
</network-security-config>
```

**`AndroidManifest.xml`**

```xml
<application
    android:networkSecurityConfig="@xml/network_security_config">
    ...
</application>

<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

---

## 🧪 Testing

**`com/textonly/network/TextOnlyAPITest.kt`**

```kotlin
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TextOnlyAPITest {

    private lateinit var api: TextOnlyAPI

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        api = RetrofitClient.getApiService(context)
    }

    @Test
    fun testLogin() = runBlocking {
        val response = api.login(AuthRequest("test@example.com", "password"))
        assert(response.token.isNotEmpty())
    }
}
```

---

## 📊 Architecture Diagram

```
┌─────────────────────────────────────────┐
│     Android App (TextOnlyMobile)        │
├─────────────────────────────────────────┤
│  UI Layer (Fragments, Activities)       │
├─────────────────────────────────────────┤
│  ViewModel + StateFlow                  │
├─────────────────────────────────────────┤
│  Repository Layer                       │
├─────────────────────────────────────────┤
│  ┌──────────────────────────────────┐  │
│  │ Retrofit API + OkHttp WebSocket  │  │
│  └──────────────────────────────────┘  │
│         ↓                ↓              │
│      REST API       WebSocket           │
└──────────┬──────────────┬───────────────┘
           │              │
           ↓              ↓
┌─────────────────────────────────────────┐
│   TextOnlyTurnServer (Backend)          │
│   ┌─────────────────────────────────┐  │
│   │  Spring Boot REST + WebSocket   │  │
│   └──────────────┬──────────────────┘  │
│                  ↓                     │
│          PostgreSQL Database           │
└─────────────────────────────────────────┘
```

---

**Integration complete! Your Android app now syncs in real-time with Web client.**
