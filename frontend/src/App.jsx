import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import LandingPage from './components/LandingPage';
import Dashboard from './components/Dashboard';
import ResumeManager from './components/ResumeManager';
import JobManager from './components/JobManager';
import AnalysisView from './components/AnalysisView';
import SkillExplorer from './components/SkillExplorer';
import ArchitectureView from './components/ArchitectureView';
import MeroinfoComponent from './components/MeroinfoComponent';
import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';

function App() {
  const [activeTab, setActiveTab] = useState('landing');
  const [selectedAnalysisId, setSelectedAnalysisId] = useState(null);
  const [darkMode, setDarkMode] = useState(true);

  useEffect(() => {
    document.body.setAttribute('data-theme', darkMode ? 'dark' : 'light');
  }, [darkMode]);

  const toggleDarkMode = () => {
    setDarkMode(!darkMode);
  };

  const handleSelectAnalysis = (analysisId) => {
    setSelectedAnalysisId(analysisId);
    setActiveTab('analysis');
  };

  return (
    <div className="d-flex flex-column min-vh-100">
      <Navbar 
        activeTab={activeTab} 
        setActiveTab={setActiveTab} 
        darkMode={darkMode} 
        toggleDarkMode={toggleDarkMode} 
      />

      <main className="flex-grow-1">
        {activeTab === 'landing' && (
          <LandingPage 
            onGetStarted={() => setActiveTab('analysis')}
            onExploreArchitecture={() => setActiveTab('architecture')}
            onManageResumes={() => setActiveTab('resumes')}
          />
        )}

        {activeTab === 'dashboard' && (
          <Dashboard 
            onNavigateToAnalysis={() => setActiveTab('analysis')}
            onSelectAnalysis={handleSelectAnalysis}
            onUploadResume={() => setActiveTab('resumes')}
            onCreateJob={() => setActiveTab('jobs')}
          />
        )}

        {activeTab === 'resumes' && <ResumeManager />}

        {activeTab === 'jobs' && <JobManager />}

        {activeTab === 'analysis' && (
          <AnalysisView initialAnalysisId={selectedAnalysisId} />
        )}

        {activeTab === 'skills' && <SkillExplorer />}

        {activeTab === 'architecture' && <ArchitectureView />}

        {activeTab === 'my-resume' && (
          <div className="py-4">
            <div className="container mb-3 d-flex align-items-center justify-content-between">
              <div>
                <span className="badge-tag exact">Original Portfolio Mode</span>
                <h2 className="h5 fw-bold mb-0 mt-1">Barun Chhetri - Developer Profile</h2>
              </div>
              <button className="btn-secondary-custom btn-sm" onClick={() => setActiveTab('dashboard')}>
                Back to Dashboard
              </button>
            </div>
            <MeroinfoComponent />
          </div>
        )}
      </main>

      {/* Production Footer */}
      <footer className="py-4 px-3 border-top border-subtle mt-auto" style={{ background: 'var(--bg-secondary)', fontSize: '0.85rem' }}>
        <div className="container d-flex flex-wrap align-items-center justify-content-between gap-3">
          <div className="text-secondary">
            <span className="fw-bold text-primary">AI Resume Intelligence Platform</span> • Built with Java 21, Spring Boot 3.4, Spring AI, PostgreSQL, PGVector & React.
          </div>
          <div className="d-flex align-items-center gap-3 text-secondary">
            <span className="badge-tag exact" style={{ fontSize: '0.65rem' }}>RAG Vector Pipeline Active</span>
            <span>Created by Barun Chhetri</span>
          </div>
        </div>
      </footer>
    </div>
  );
}

export default App;
