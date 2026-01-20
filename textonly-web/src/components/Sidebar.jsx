import React, { useState } from 'react';
import { useStore } from '../store/useStore';
import { useNavigate, useLocation } from 'react-router-dom';

export default function Sidebar() {
  const { 
    currentServer, 
    selectChannel, 
    createChannel, 
    contacts, 
    user,
    toggleProfile,
    toggleStore,
    logout,
    coins
  } = useStore();
  
  const navigate = useNavigate();
  const location = useLocation();
  const [showChannelCreate, setShowChannelCreate] = useState(false);
  const [newChannelName, setNewChannelName] = useState('');
  const [channelType, setChannelType] = useState('TEXT');

  const handleCreateChannel = () => {
    if (newChannelName.trim() && currentServer) {
      createChannel(currentServer.id, newChannelName, channelType);
      setNewChannelName('');
      setShowChannelCreate(false);
    }
  };

  const handleSelectChannel = (channelId) => {
    if (currentServer) {
      navigate(`/server/${currentServer.id}/channel/${channelId}`);
    }
  };

  const isDM = location.pathname === '/';

  return (
    <div className="w-60 bg-dark-2 flex flex-col">
      {/* Header */}
      <div className="h-12 px-4 flex items-center justify-between border-b border-dark-4 shadow-md">
        <h2 className="font-semibold truncate">
          {currentServer ? currentServer.name : 'Direct Messages'}
        </h2>
        {currentServer && (
          <button
            onClick={() => setShowChannelCreate(true)}
            className="text-2xl text-gray-400 hover:text-white"
            title="Create Channel"
          >
            +
          </button>
        )}
      </div>

      {/* Channels or DMs */}
      <div className="flex-1 overflow-y-auto p-2">
        {currentServer ? (
          // Server Channels
          <div className="space-y-1">
            <div className="text-xs font-semibold text-gray-400 uppercase px-2 mb-1">
              Text Channels
            </div>
            {currentServer.channels.filter(c => c.type === 'TEXT').map(channel => (
              <button
                key={channel.id}
                onClick={() => handleSelectChannel(channel.id)}
                className="w-full px-2 py-1.5 rounded hover:bg-dark-3 text-left flex items-center gap-2 text-gray-300 hover:text-white"
              >
                <span className="text-gray-400">#</span>
                {channel.name}
              </button>
            ))}

            <div className="text-xs font-semibold text-gray-400 uppercase px-2 mt-4 mb-1">
              Voice Channels
            </div>
            {currentServer.channels.filter(c => c.type === 'VOICE').map(channel => (
              <button
                key={channel.id}
                onClick={() => handleSelectChannel(channel.id)}
                className="w-full px-2 py-1.5 rounded hover:bg-dark-3 text-left flex items-center gap-2 text-gray-300 hover:text-white"
              >
                <span className="text-gray-400">🔊</span>
                {channel.name}
              </button>
            ))}
          </div>
        ) : (
          // Direct Messages
          <div className="space-y-1">
            <div className="text-xs font-semibold text-gray-400 uppercase px-2 mb-1">
              Direct Messages
            </div>
            {contacts.map(contact => (
              <button
                key={contact.id}
                onClick={() => navigate(`/dm/${contact.id}`)}
                className="w-full px-2 py-1.5 rounded hover:bg-dark-3 text-left flex items-center gap-2"
              >
                <div className="w-8 h-8 rounded-full bg-purple-600 flex items-center justify-center text-sm font-bold">
                  {contact.displayName.charAt(0)}
                </div>
                <span className="text-gray-300 hover:text-white">{contact.displayName}</span>
              </button>
            ))}
          </div>
        )}
      </div>

      {/* Bottom User Panel */}
      <div className="h-14 px-2 bg-dark-3 flex items-center gap-2">
        <div className="w-8 h-8 rounded-full bg-purple-600 flex items-center justify-center text-sm font-bold">
          {user?.displayName?.charAt(0) || 'U'}
        </div>
        <div className="flex-1 min-w-0">
          <div className="text-sm font-semibold truncate">{user?.displayName}</div>
          <div className="text-xs text-gray-400">💰 {coins} Coins</div>
        </div>
        <button
          onClick={toggleStore}
          className="p-1.5 hover:bg-dark-4 rounded"
          title="Store"
        >
          🛒
        </button>
        <button
          onClick={toggleProfile}
          className="p-1.5 hover:bg-dark-4 rounded"
          title="Profile"
        >
          ⚙️
        </button>
        <button
          onClick={logout}
          className="p-1.5 hover:bg-dark-4 rounded text-red-400"
          title="Logout"
        >
          🚪
        </button>
      </div>

      {/* Create Channel Modal */}
      {showChannelCreate && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-dark-2 rounded-lg p-6 w-96">
            <h2 className="text-2xl font-bold mb-4">Create Channel</h2>
            <input
              type="text"
              value={newChannelName}
              onChange={(e) => setNewChannelName(e.target.value)}
              placeholder="Channel Name"
              className="w-full px-4 py-2 bg-dark-3 border border-gray-700 rounded-lg outline-none focus:border-purple-500 mb-4"
              onKeyPress={(e) => e.key === 'Enter' && handleCreateChannel()}
              autoFocus
            />
            <div className="mb-4 flex gap-4">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="radio"
                  value="TEXT"
                  checked={channelType === 'TEXT'}
                  onChange={(e) => setChannelType(e.target.value)}
                />
                <span># Text</span>
              </label>
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="radio"
                  value="VOICE"
                  checked={channelType === 'VOICE'}
                  onChange={(e) => setChannelType(e.target.value)}
                />
                <span>🔊 Voice</span>
              </label>
            </div>
            <div className="flex gap-2">
              <button
                onClick={handleCreateChannel}
                className="flex-1 py-2 bg-purple-600 hover:bg-purple-700 rounded-lg font-semibold"
              >
                Create
              </button>
              <button
                onClick={() => setShowChannelCreate(false)}
                className="flex-1 py-2 bg-gray-700 hover:bg-gray-600 rounded-lg font-semibold"
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
