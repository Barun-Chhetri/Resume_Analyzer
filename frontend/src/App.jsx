import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import LandingPage from './components/LandingPage';
import Dashboard from './components/Dashboard';
import ResumeManager from './components/ResumeManager';
import JobManager from './components/JobManager';
import AnalysisView from './components/AnalysisView';
import SkillExplorer from './components/SkillExplorer';
import ArchitectureView from './components/ArchitectureView';
import AuthModal from './components/AuthModal';
import api from './services/api';
import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';

function App() {
  const [activeTab, setActiveTab] = useState('landing');
  const [selectedAnalysisId, setSelectedAnalysisId] = useState(null);
  const [darkMode, setDarkMode] = useState(true);
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);
  
  // Current authenticated user session
  const [user, setUser] = useState(() => {
    try {
      const saved = localStorage.getItem('auth_user');
      return saved ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  });

  useEffect(() => {
    document.body.setAttribute('data-theme', darkMode ? 'dark' : 'light');
  }, [darkMode]);

  // Synchronize and validate session with backend
  useEffect(() => {
    const syncUser = () => {
      const token = localStorage.getItem('auth_token');
      if (token) {
        api.getCurrentUser()
          .then((userData) => {
            setUser(userData);
            localStorage.setItem('auth_user', JSON.stringify(userData));
          })
          .catch(() => {
            setUser(null);
            localStorage.removeItem('auth_token');
            localStorage.removeItem('auth_user');
          });
      } else {
        setUser(null);
      }
    };

    syncUser();
    window.addEventListener('auth_changed', syncUser);
    return () => window.removeEventListener('auth_changed', syncUser);
  }, []);

  const toggleDarkMode = () => {
    setDarkMode(!darkMode);
  };

  const handleSelectAnalysis = (analysisId) => {
    setSelectedAnalysisId(analysisId);
    setActiveTab('analysis');
  };

  const handleLogout = async () => {
    await api.logout();
    setUser(null);
  };

  return (
    <div className="d-flex flex-column min-vh-100">
      <Navbar 
        activeTab={activeTab} 
        setActiveTab={setActiveTab} 
        darkMode={darkMode} 
        toggleDarkMode={toggleDarkMode}
        user={user}
        onOpenAuthModal={() => setIsAuthModalOpen(true)}
        onLogout={handleLogout}
      />

      <main className="flex-grow-1">
        {activeTab === 'landing' && (
          <LandingPage 
            onGetStarted={() => {
              if (!user) {
                setIsAuthModalOpen(true);
              } else {
                setActiveTab('analysis');
              }
            }}
            onExploreArchitecture={() => setActiveTab('architecture')}
            onManageResumes={() => setActiveTab('resumes')}
          />
        )}

        {activeTab === 'dashboard' && (
          <Dashboard 
            user={user}
            onOpenAuthModal={() => setIsAuthModalOpen(true)}
            onNavigateToAnalysis={() => setActiveTab('analysis')}
            onSelectAnalysis={handleSelectAnalysis}
            onUploadResume={() => setActiveTab('resumes')}
            onCreateJob={() => setActiveTab('jobs')}
          />
        )}

        {activeTab === 'resumes' && (
          <ResumeManager 
            user={user} 
            onOpenAuthModal={() => setIsAuthModalOpen(true)} 
          />
        )}

        {activeTab === 'jobs' && (
          <JobManager 
            user={user} 
            onOpenAuthModal={() => setIsAuthModalOpen(true)} 
          />
        )}

        {activeTab === 'analysis' && (
          <AnalysisView 
            initialAnalysisId={selectedAnalysisId}
            user={user}
            onOpenAuthModal={() => setIsAuthModalOpen(true)}
          />
        )}

        {activeTab === 'skills' && <SkillExplorer />}

        {activeTab === 'architecture' && <ArchitectureView />}
      </main>

      {/* Production Footer */}
      <footer className="py-4 px-3 border-top border-subtle mt-auto" style={{ background: 'var(--bg-secondary)', fontSize: '0.85rem' }}>
        <div className="container d-flex flex-wrap align-items-center justify-content-between gap-3">
          <div className="text-secondary">
            <span className="fw-bold text-primary">AI Resume Intelligence Platform</span> • Built with Java 21, Spring Boot 3.4, Spring AI, PostgreSQL, PGVector & React.
          </div>
          <div className="d-flex align-items-center gap-3 text-secondary">
            <span className="badge-tag exact" style={{ fontSize: '0.65rem' }}>RAG Vector Pipeline Active</span>
            <span>Enterprise Multi-Tenant Sessions</span>
          </div>
        </div>
      </footer>

      {/* Auth Modal */}
      <AuthModal 
        isOpen={isAuthModalOpen} 
        onClose={() => setIsAuthModalOpen(false)}
        onSuccess={(loggedUser) => setUser(loggedUser)}
      />
    </div>
  );
}

export default App;
