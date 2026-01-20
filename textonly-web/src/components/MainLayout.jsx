import React, { useEffect } from 'react';
import { Routes, Route } from 'react-router-dom';
import { useStore } from '../store/useStore';
import Sidebar from './Sidebar';
import ServerList from './ServerList';
import ChannelView from './ChannelView';
import DirectMessages from './DirectMessages';
import Profile from './Profile';
import Store from './Store';

export default function MainLayout() {
  const { loadServers, loadContacts, user } = useStore();

  useEffect(() => {
    loadServers();
    loadContacts();
  }, []);

  return (
    <div className="flex h-screen overflow-hidden">
      {/* Left Sidebar - Servers */}
      <ServerList />

      {/* Middle Sidebar - Channels/DMs */}
      <Sidebar />

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col">
        <Routes>
          <Route path="/" element={<DirectMessages />} />
          <Route path="/server/:serverId/channel/:channelId" element={<ChannelView />} />
          <Route path="/dm/:userId" element={<DirectMessages />} />
        </Routes>
      </div>

      {/* Modals */}
      <Profile />
      <Store />
    </div>
  );
}
