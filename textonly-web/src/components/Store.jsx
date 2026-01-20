import React, { useState } from 'react';
import { useStore } from '../store/useStore';
import api from '../services/api';

export default function Store() {
  const { showStore, toggleStore, coins, buyItem } = useStore();
  const [items] = useState(api.getStoreItems());
  const [filter, setFilter] = useState('ALL');

  if (!showStore) return null;

  const filteredItems = filter === 'ALL' 
    ? items 
    : items.filter(item => item.type === filter);

  const handleBuy = (item) => {
    if (coins >= item.price) {
      buyItem(item);
      alert(`✅ Purchased ${item.name}!`);
    } else {
      alert(`❌ Not enough coins! You need ${item.price - coins} more.`);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-70 flex items-center justify-center z-50" onClick={toggleStore}>
      <div className="bg-dark-2 rounded-lg w-full max-w-4xl max-h-[90vh] flex flex-col" onClick={(e) => e.stopPropagation()}>
        
        {/* Header */}
        <div className="p-6 border-b border-dark-4">
          <div className="flex justify-between items-center">
            <div>
              <h2 className="text-2xl font-bold">🛒 Store</h2>
              <p className="text-gray-400">Your balance: <span className="text-yellow-400 font-bold">💰 {coins} Coins</span></p>
            </div>
            <button onClick={toggleStore} className="text-2xl text-gray-400 hover:text-white">×</button>
          </div>
        </div>

        {/* Filters */}
        <div className="p-4 border-b border-dark-4 flex gap-2">
          {['ALL', 'EMOTICON', 'GIFT', 'FRAME'].map(f => (
            <button
              key={f}
              onClick={() => setFilter(f)}
              className={`px-4 py-2 rounded-lg font-semibold ${
                filter === f 
                  ? 'bg-purple-600' 
                  : 'bg-dark-3 hover:bg-dark-4'
              }`}
            >
              {f === 'ALL' ? '🌟 All' : 
               f === 'EMOTICON' ? '😀 Emotes' : 
               f === 'GIFT' ? '🎁 Gifts' : 
               '🖼️ Frames'}
            </button>
          ))}
        </div>

        {/* Items Grid */}
        <div className="flex-1 overflow-y-auto p-6">
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {filteredItems.map(item => (
              <div key={item.id} className="bg-dark-3 rounded-lg p-4 hover:bg-dark-4 transition">
                <div className="text-4xl mb-2 text-center">{item.name.split(' ')[0]}</div>
                <h3 className="font-semibold text-center mb-1">{item.name.split(' ').slice(1).join(' ')}</h3>
                <div className="text-yellow-400 font-bold text-center mb-3">💰 {item.price}</div>
                <button
                  onClick={() => handleBuy(item)}
                  disabled={coins < item.price}
                  className={`w-full py-2 rounded-lg font-semibold ${
                    coins >= item.price
                      ? 'bg-purple-600 hover:bg-purple-700'
                      : 'bg-gray-600 opacity-50 cursor-not-allowed'
                  }`}
                >
                  {coins >= item.price ? 'Buy' : 'Not Enough'}
                </button>
              </div>
            ))}
          </div>
        </div>

        {/* Featured Deals */}
        <div className="p-4 border-t border-dark-4 bg-dark-3">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="font-bold">💎 Special Offer!</h3>
              <p className="text-sm text-gray-400">Get 1000 coins for only $9.99</p>
            </div>
            <button className="px-6 py-2 bg-green-600 hover:bg-green-700 rounded-lg font-semibold">
              Buy Now
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
