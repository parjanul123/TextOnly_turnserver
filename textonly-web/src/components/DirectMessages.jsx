import React from 'react';

export default function DirectMessages() {
  return (
    <div className="flex-1 flex items-center justify-center flex-col gap-4 p-8">
      <div className="text-6xl">💬</div>
      <h2 className="text-2xl font-bold">Direct Messages</h2>
      <p className="text-gray-400 text-center max-w-md">
        Select a friend from the sidebar to start chatting, or create/join a server to get started!
      </p>
      
      <div className="mt-8 grid grid-cols-2 gap-4">
        <div className="bg-dark-2 p-6 rounded-lg text-center">
          <div className="text-3xl mb-2">🎮</div>
          <h3 className="font-semibold mb-1">Create Server</h3>
          <p className="text-sm text-gray-400">Start your own community</p>
        </div>
        <div className="bg-dark-2 p-6 rounded-lg text-center">
          <div className="text-3xl mb-2">👥</div>
          <h3 className="font-semibold mb-1">Add Friends</h3>
          <p className="text-sm text-gray-400">Connect with others</p>
        </div>
      </div>
    </div>
  );
}
