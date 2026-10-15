import React, { useState, useEffect } from 'react';
import { 
  Sparkles, 
  FileText, 
  Briefcase, 
  Cpu, 
  Search, 
  LayoutDashboard, 
  Sun, 
  Moon, 
  Layers,
  LogIn,
  LogOut,
  UserCheck
} from 'lucide-react';
import api from '../services/api';

const Navbar = ({ activeTab, setActiveTab, darkMode, toggleDarkMode, user, onOpenAuthModal, onLogout }) => {
  const [backendOnline, setBackendOnline] = useState(false);

  useEffect(() => {
    const checkHealth = () => {
      api.getArchitectureInfo()
        .then(() => setBackendOnline(true))
        .catch(() => setBackendOnline(false));
    };
    checkHealth();
    const interval = setInterval(checkHealth, 15000);
    return () => clearInterval(interval);
  }, []);

  const navItems = [
    { id: 'landing', label: 'Overview', icon: Sparkles },
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { id: 'resumes', label: 'Resumes', icon: FileText },
    { id: 'jobs', label: 'Jobs', icon: Briefcase },
    { id: 'analysis', label: 'Analyze', icon: Cpu },
    { id: 'skills', label: 'Skill Explorer', icon: Search },
    { id: 'architecture', label: 'Architecture', icon: Layers },
  ];

  const getInitials = (name) => {
    if (!name) return 'U';
    const parts = name.trim().split(/\s+/);
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return parts[0].substring(0, 2).toUpperCase();
  };

  return (
    <nav className="app-navbar py-2 px-3 px-md-4">
      <div className="container-fluid d-flex flex-wrap align-items-center justify-content-between gap-3">
        {/* Brand */}
        <div 
          className="d-flex align-items-center gap-2 cursor-pointer"
          style={{ cursor: 'pointer' }}
          onClick={() => setActiveTab('landing')}
        >
          <div 
            className="d-flex align-items-center justify-content-center"
            style={{
              width: '36px',
              height: '36px',
              borderRadius: '8px',
              background: 'linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%)',
              color: 'white',
              boxShadow: '0 2px 10px rgba(59, 130, 246, 0.35)'
            }}
          >
            <Cpu size={20} />
          </div>
          <div>
            <div className="fw-bold lh-1" style={{ fontSize: '1.05rem', letterSpacing: '-0.02em' }}>
              AI Resume Intelligence
            </div>
            <div className="d-flex align-items-center gap-1 mt-1">
              <span className="badge-tag exact" style={{ fontSize: '0.625rem', padding: '0.1rem 0.4rem' }}>
                Spring AI & PGVector
              </span>
            </div>
          </div>
        </div>

        {/* Nav Links */}
        <div className="d-flex align-items-center flex-wrap gap-1">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                className={`nav-link-btn ${isActive ? 'active' : ''}`}
                onClick={() => setActiveTab(item.id)}
              >
                <Icon size={16} />
                <span>{item.label}</span>
              </button>
            );
          })}
        </div>

        {/* Right Controls: User Profile / Auth, Health, Theme Toggle */}
        <div className="d-flex align-items-center gap-2">
          {/* User Auth Control */}
          {user ? (
            <div className="d-flex align-items-center gap-2 bg-dark bg-opacity-25 px-2 py-1 border border-secondary border-opacity-25" style={{ borderRadius: '999px' }}>
              <div 
                className="d-flex align-items-center justify-content-center text-white fw-bold shadow-sm"
                style={{
                  width: '28px',
                  height: '28px',
                  borderRadius: '50%',
                  background: 'linear-gradient(135deg, #10b981 0%, #3b82f6 100%)',
                  fontSize: '0.75rem'
                }}
                title={user.email}
              >
                {getInitials(user.name)}
              </div>
              <span className="d-none d-md-inline small fw-semibold text-light pe-1" style={{ maxWidth: '120px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                {user.name}
              </span>
              <button
                className="btn btn-sm btn-link text-danger p-0 d-flex align-items-center"
                onClick={onLogout}
                title="Sign Out"
                style={{ textDecoration: 'none' }}
              >
                <LogOut size={15} />
              </button>
            </div>
          ) : (
            <button
              className="btn btn-primary btn-sm d-flex align-items-center gap-1 px-3 py-1 shadow-sm fw-semibold"
              onClick={onOpenAuthModal}
              style={{ borderRadius: '8px' }}
            >
              <LogIn size={15} />
              <span>Sign In</span>
            </button>
          )}

          {/* Backend Status Pill */}
          <div 
            className="d-flex align-items-center gap-1 px-2 py-1"
            style={{ 
              fontSize: '0.75rem', 
              borderRadius: '999px',
              background: backendOnline ? 'rgba(16, 185, 129, 0.12)' : 'rgba(244, 63, 94, 0.12)',
              color: backendOnline ? '#10b981' : '#f43f5e',
              border: `1px solid ${backendOnline ? 'rgba(16, 185, 129, 0.25)' : 'rgba(244, 63, 94, 0.25)'}`
            }}
            title={backendOnline ? 'Backend API connected' : 'Backend API disconnected or connecting...'}
          >
            <span 
              style={{ 
                width: '6px', 
                height: '6px', 
                borderRadius: '50%', 
                backgroundColor: backendOnline ? '#10b981' : '#f43f5e',
                boxShadow: backendOnline ? '0 0 6px #10b981' : 'none'
              }} 
            />
            <span className="d-none d-lg-inline">{backendOnline ? 'API Connected' : 'Connecting'}</span>
          </div>

          {/* Theme Toggle */}
          <button 
            onClick={toggleDarkMode}
            className="btn-secondary-custom p-2"
            style={{ borderRadius: '8px' }}
            title={darkMode ? 'Switch to Light Mode' : 'Switch to Dark Mode'}
          >
            {darkMode ? <Sun size={17} /> : <Moon size={17} />}
          </button>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
