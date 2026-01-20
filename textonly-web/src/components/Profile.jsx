import React, { useState } from 'react';
import { useStore } from '../store/useStore';

export default function Profile() {
  const { showProfile, toggleProfile, user, updateWallet, coins } = useStore();
  const [displayName, setDisplayName] = useState(user?.displayName || '');

  if (!showProfile) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-70 flex items-center justify-center z-50" onClick={toggleProfile}>
      <div className="bg-dark-2 rounded-lg w-full max-w-2xl" onClick={(e) => e.stopPropagation()}>
        <div className="p-6 border-b border-dark-4">
          <div className="flex justify-between items-center">
            <h2 className="text-2xl font-bold">User Profile</h2>
            <button onClick={toggleProfile} className="text-2xl text-gray-400 hover:text-white">×</button>
          </div>
        </div>

        <div className="p-6 space-y-6">
          {/* Avatar & Name */}
          <div className="flex items-center gap-6">
            <div className="w-24 h-24 rounded-full bg-purple-600 flex items-center justify-center text-4xl font-bold">
              {displayName.charAt(0)}
            </div>
            <div className="flex-1">
              <label className="block text-sm font-medium text-gray-400 mb-2">Display Name</label>
              <input
                type="text"
                value={displayName}
                onChange={(e) => setDisplayName(e.target.value)}
                className="w-full px-4 py-2 bg-dark-3 border border-gray-700 rounded-lg outline-none focus:border-purple-500"
              />
            </div>
          </div>

          {/* Email */}
          <div>
            <label className="block text-sm font-medium text-gray-400 mb-2">Email</label>
            <input
              type="email"
              value={user?.email || ''}
              disabled
              className="w-full px-4 py-2 bg-dark-4 border border-gray-700 rounded-lg outline-none opacity-60"
            />
          </div>

          {/* Wallet */}
          <div className="bg-dark-3 rounded-lg p-4">
            <div className="flex items-center justify-between mb-4">
              <div>
                <h3 className="font-semibold">Wallet Balance</h3>
                <p className="text-2xl font-bold text-yellow-400">💰 {coins} Coins</p>
              </div>
              <button
                onClick={() => updateWallet(100)}
                className="px-4 py-2 bg-green-600 hover:bg-green-700 rounded-lg font-semibold"
              >
                + Add 100 (Demo)
              </button>
            </div>
            <div className="grid grid-cols-3 gap-2">
              <button className="bg-dark-4 hover:bg-dark-2 py-2 rounded">
                📊 Transactions
              </button>
              <button className="bg-dark-4 hover:bg-dark-2 py-2 rounded">
                🎒 Inventory
              </button>
              <button className="bg-dark-4 hover:bg-dark-2 py-2 rounded">
                💳 Top Up
              </button>
            </div>
          </div>

          {/* Stats */}
          <div className="grid grid-cols-3 gap-4">
            <div className="bg-dark-3 rounded-lg p-4 text-center">
              <div className="text-2xl mb-1">👥</div>
              <div className="text-xl font-bold">{Math.floor(Math.random() * 50)}</div>
              <div className="text-xs text-gray-400">Friends</div>
            </div>
            <div className="bg-dark-3 rounded-lg p-4 text-center">
              <div className="text-2xl mb-1">🎮</div>
              <div className="text-xl font-bold">{Math.floor(Math.random() * 10)}</div>
              <div className="text-xs text-gray-400">Servers</div>
            </div>
            <div className="bg-dark-3 rounded-lg p-4 text-center">
              <div className="text-2xl mb-1">💬</div>
              <div className="text-xl font-bold">{Math.floor(Math.random() * 1000)}</div>
              <div className="text-xs text-gray-400">Messages</div>
            </div>
          </div>

          {/* Save Button */}
          <button
            onClick={() => {
              // Save profile logic here
              toggleProfile();
            }}
            className="w-full py-3 bg-purple-600 hover:bg-purple-700 rounded-lg font-semibold"
          >
            Save Changes
          </button>
        </div>
      </div>
    </div>
  );
}
