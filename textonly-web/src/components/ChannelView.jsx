import React, { useState, useEffect, useRef } from 'react';
import { useParams } from 'react-router-dom';
import { useStore } from '../store/useStore';
import ws from '../services/websocket';

export default function ChannelView() {
  const { serverId, channelId } = useParams();
  const { currentServer, messages, sendMessage, user } = useStore();
  const [input, setInput] = useState('');
  const messagesEndRef = useRef(null);

  const channel = currentServer?.channels.find(c => c.id === parseInt(channelId));
  const isVoiceChannel = channel?.type === 'VOICE';

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  useEffect(() => {
    // Listen for WebSocket messages
    const unsubscribe = ws.onMessage((message) => {
      console.log('New message:', message);
    });

    return () => unsubscribe();
  }, []);

  const handleSend = async () => {
    if (input.trim()) {
      try {
        // For demo, we'll just add to local state
        // In production, this would go through the backend
        const message = {
          id: Date.now(),
          content: input,
          senderId: user.userId,
          senderName: user.displayName,
          timestamp: new Date().toISOString()
        };
        
        useStore.setState({ messages: [...messages, message] });
        setInput('');
      } catch (error) {
        console.error('Error sending message:', error);
      }
    }
  };

  if (!channel) {
    return (
      <div className="flex-1 flex items-center justify-center">
        <p className="text-gray-400">Channel not found</p>
      </div>
    );
  }

  if (isVoiceChannel) {
    return <VoiceChannel channel={channel} />;
  }

  return (
    <div className="flex-1 flex flex-col">
      {/* Channel Header */}
      <div className="h-12 px-4 flex items-center border-b border-dark-4 bg-dark-2 shadow-md">
        <span className="text-gray-400 mr-2">#</span>
        <span className="font-semibold">{channel.name}</span>
      </div>

      {/* Messages Area */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        {messages.length === 0 && (
          <div className="text-center text-gray-400 mt-8">
            <p className="text-2xl mb-2">👋</p>
            <p>Welcome to #{channel.name}!</p>
            <p className="text-sm mt-1">This is the start of your conversation.</p>
          </div>
        )}

        {messages.map(message => (
          <div key={message.id} className="flex gap-3 hover:bg-dark-3 p-2 rounded">
            <div className="w-10 h-10 rounded-full bg-purple-600 flex items-center justify-center text-sm font-bold flex-shrink-0">
              {message.senderName?.charAt(0) || 'U'}
            </div>
            <div className="flex-1">
              <div className="flex items-baseline gap-2">
                <span className="font-semibold">{message.senderName}</span>
                <span className="text-xs text-gray-400">
                  {new Date(message.timestamp).toLocaleTimeString()}
                </span>
              </div>
              <p className="text-gray-300">{message.content}</p>
            </div>
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>

      {/* Input Area */}
      <div className="p-4 bg-dark-2">
        <div className="flex gap-2">
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && handleSend()}
            placeholder={`Message #${channel.name}`}
            className="flex-1 px-4 py-3 bg-dark-3 rounded-lg outline-none focus:ring-2 focus:ring-purple-500"
          />
          <button
            onClick={handleSend}
            className="px-6 py-3 bg-purple-600 hover:bg-purple-700 rounded-lg font-semibold"
          >
            Send
          </button>
        </div>
      </div>
    </div>
  );
}

function VoiceChannel({ channel }) {
  const [isConnected, setIsConnected] = useState(false);
  const [isMuted, setIsMuted] = useState(false);
  const [isDeafened, setIsDeafened] = useState(false);
  const [users] = useState([
    { id: 1, name: 'User 1', speaking: false },
    { id: 2, name: 'User 2', speaking: true },
  ]);

  return (
    <div className="flex-1 flex flex-col">
      {/* Voice Header */}
      <div className="h-12 px-4 flex items-center border-b border-dark-4 bg-dark-2 shadow-md">
        <span className="text-gray-400 mr-2">🔊</span>
        <span className="font-semibold">{channel.name}</span>
      </div>

      {/* Voice Users */}
      <div className="flex-1 overflow-y-auto p-4">
        <div className="text-xs font-semibold text-gray-400 uppercase mb-3">
          In Voice — {users.length}
        </div>
        <div className="space-y-2">
          {users.map(user => (
            <div key={user.id} className="flex items-center gap-3 p-2 rounded hover:bg-dark-3">
              <div className={`w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold ${
                user.speaking ? 'ring-2 ring-green-500 bg-green-600' : 'bg-gray-600'
              }`}>
                {user.name.charAt(0)}
              </div>
              <span className={user.speaking ? 'text-green-400' : ''}>{user.name}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Voice Controls */}
      <div className="p-4 bg-dark-3 border-t border-dark-4">
        {!isConnected ? (
          <button
            onClick={() => setIsConnected(true)}
            className="w-full py-3 bg-green-600 hover:bg-green-700 rounded-lg font-semibold"
          >
            🎤 Join Voice Channel
          </button>
        ) : (
          <div className="space-y-2">
            <div className="flex gap-2">
              <button
                onClick={() => setIsMuted(!isMuted)}
                className={`flex-1 py-2 rounded-lg font-semibold ${
                  isMuted ? 'bg-red-600 hover:bg-red-700' : 'bg-gray-600 hover:bg-gray-700'
                }`}
              >
                {isMuted ? '🔇 Unmute' : '🎤 Mute'}
              </button>
              <button
                onClick={() => setIsDeafened(!isDeafened)}
                className={`flex-1 py-2 rounded-lg font-semibold ${
                  isDeafened ? 'bg-red-600 hover:bg-red-700' : 'bg-gray-600 hover:bg-gray-700'
                }`}
              >
                {isDeafened ? '🔇 Undeafen' : '🔊 Deafen'}
              </button>
            </div>
            <button
              onClick={() => setIsConnected(false)}
              className="w-full py-2 bg-red-600 hover:bg-red-700 rounded-lg font-semibold"
            >
              ❌ Leave Voice
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
