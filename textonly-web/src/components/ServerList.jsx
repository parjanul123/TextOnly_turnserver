import React, { useState } from 'react';
import { useStore } from '../store/useStore';
import { useNavigate } from 'react-router-dom';

export default function ServerList() {
  const { servers, currentServer, selectServer, createServer, user } = useStore();
  const [showCreate, setShowCreate] = useState(false);
  const [newServerName, setNewServerName] = useState('');
  const navigate = useNavigate();

  const handleCreateServer = () => {
    if (newServerName.trim()) {
      const server = createServer(newServerName);
      setNewServerName('');
      setShowCreate(false);
      selectServer(server.id);
      navigate(`/server/${server.id}/channel/${server.channels[0].id}`);
    }
  };

  const handleSelectServer = (serverId) => {
    selectServer(serverId);
    const server = servers.find(s => s.id === serverId);
    if (server && server.channels.length > 0) {
      navigate(`/server/${serverId}/channel/${server.channels[0].id}`);
    }
  };

  return (
    <div className="w-18 bg-dark-4 flex flex-col items-center py-3 space-y-2">
      {/* Home Button */}
      <button
        onClick={() => navigate('/')}
        className="w-12 h-12 rounded-full bg-dark-2 hover:bg-purple-600 hover:rounded-2xl transition-all duration-200 flex items-center justify-center text-2xl"
        title="Direct Messages"
      >
        💬
      </button>

      <div className="w-8 h-0.5 bg-dark-2 rounded-full" />

      {/* Servers */}
      {servers.map(server => (
        <button
          key={server.id}
          onClick={() => handleSelectServer(server.id)}
          className={`w-12 h-12 rounded-full hover:rounded-2xl transition-all duration-200 flex items-center justify-center text-sm font-bold ${
            currentServer?.id === server.id
              ? 'bg-purple-600 rounded-2xl'
              : 'bg-dark-2 hover:bg-purple-500'
          }`}
          title={server.name}
        >
          {server.name.charAt(0).toUpperCase()}
        </button>
      ))}

      {/* Add Server Button */}
      <button
        onClick={() => setShowCreate(true)}
        className="w-12 h-12 rounded-full bg-dark-2 hover:bg-green-600 hover:rounded-2xl transition-all duration-200 flex items-center justify-center text-2xl text-green-500 hover:text-white"
        title="Create Server"
      >
        +
      </button>

      {/* Create Server Modal */}
      {showCreate && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-dark-2 rounded-lg p-6 w-96">
            <h2 className="text-2xl font-bold mb-4">Create Server</h2>
            <input
              type="text"
              value={newServerName}
              onChange={(e) => setNewServerName(e.target.value)}
              placeholder="Server Name"
              className="w-full px-4 py-2 bg-dark-3 border border-gray-700 rounded-lg outline-none focus:border-purple-500 mb-4"
              onKeyPress={(e) => e.key === 'Enter' && handleCreateServer()}
              autoFocus
            />
            <div className="flex gap-2">
              <button
                onClick={handleCreateServer}
                className="flex-1 py-2 bg-purple-600 hover:bg-purple-700 rounded-lg font-semibold"
              >
                Create
              </button>
              <button
                onClick={() => setShowCreate(false)}
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
