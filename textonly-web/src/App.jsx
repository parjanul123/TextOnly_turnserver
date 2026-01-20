import React, { useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useStore } from './store/useStore';
import Auth from './components/Auth';
import MainLayout from './components/MainLayout';

function App() {
  const { isAuthenticated, user } = useStore();

  useEffect(() => {
    // Check if already logged in
    const token = localStorage.getItem('authToken');
    const userId = localStorage.getItem('userId');
    
    if (token && userId) {
      useStore.setState({ 
        isAuthenticated: true,
        user: {
          userId,
          email: localStorage.getItem('userEmail'),
          displayName: localStorage.getItem('displayName')
        }
      });
    }
  }, []);

  return (
    <BrowserRouter>
      <Routes>
        <Route 
          path="/auth" 
          element={!isAuthenticated ? <Auth /> : <Navigate to="/" />} 
        />
        <Route 
          path="/*" 
          element={isAuthenticated ? <MainLayout /> : <Navigate to="/auth" />} 
        />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
