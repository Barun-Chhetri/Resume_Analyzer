import React, { useState } from 'react';
import { 
  X, 
  LogIn, 
  UserPlus, 
  Lock, 
  Mail, 
  User, 
  Sparkles, 
  AlertCircle, 
  CheckCircle2, 
  Loader2 
} from 'lucide-react';
import api from '../services/api';

const AuthModal = ({ isOpen, onClose, onSuccess }) => {
  const [activeTab, setActiveTab] = useState('login'); // 'login' | 'register'
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successMsg, setSuccessMsg] = useState(null);

  if (!isOpen) return null;

  const resetForm = () => {
    setName('');
    setEmail('');
    setPassword('');
    setConfirmPassword('');
    setError(null);
    setSuccessMsg(null);
  };

  const handleTabSwitch = (tab) => {
    setActiveTab(tab);
    resetForm();
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccessMsg(null);
    setLoading(true);

    try {
      if (activeTab === 'login') {
        if (!email.trim() || !password) {
          throw new Error('Please enter both email and password.');
        }
        const data = await api.login(email.trim(), password);
        setSuccessMsg('Welcome back!');
        setTimeout(() => {
          if (onSuccess) onSuccess(data.user);
          onClose();
        }, 500);
      } else {
        if (!name.trim()) {
          throw new Error('Please enter your full name.');
        }
        if (!email.trim() || !email.includes('@')) {
          throw new Error('Please provide a valid email address.');
        }
        if (password.length < 6) {
          throw new Error('Password must be at least 6 characters long.');
        }
        if (password !== confirmPassword) {
          throw new Error('Passwords do not match.');
        }
        const data = await api.register(name.trim(), email.trim(), password);
        setSuccessMsg('Account created successfully!');
        setTimeout(() => {
          if (onSuccess) onSuccess(data.user);
          onClose();
        }, 500);
      }
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Authentication failed. Please try again.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleQuickDemo = async () => {
    setError(null);
    setLoading(true);
    try {
      const data = await api.login('demo@resumex.ai', 'Demo1234!');
      setSuccessMsg('Logged in as Demo User!');
      setTimeout(() => {
        if (onSuccess) onSuccess(data.user);
        onClose();
      }, 500);
    } catch (err) {
      setError('Demo login error: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div 
      className="modal fade show d-block" 
      tabIndex="-1" 
      style={{ backgroundColor: 'rgba(0, 0, 0, 0.75)', backdropFilter: 'blur(8px)', zIndex: 1050 }}
    >
      <div className="modal-dialog modal-dialog-centered" style={{ maxWidth: '440px' }}>
        <div className="modal-content border-0 shadow-lg" style={{ 
          background: 'var(--bg-card, #1e293b)', 
          borderRadius: '16px',
          overflow: 'hidden',
          border: '1px solid rgba(255, 255, 255, 0.1)'
        }}>
          {/* Header */}
          <div className="modal-header border-0 pb-0 pt-4 px-4 position-relative">
            <div className="d-flex align-items-center gap-2">
              <div 
                className="d-flex align-items-center justify-content-center"
                style={{
                  width: '36px',
                  height: '36px',
                  borderRadius: '10px',
                  background: 'linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%)',
                  color: 'white'
                }}
              >
                <Sparkles size={20} />
              </div>
              <div>
                <h5 className="modal-title fw-bold mb-0" style={{ fontSize: '1.15rem' }}>
                  {activeTab === 'login' ? 'Sign In' : 'Create Account'}
                </h5>
                <span className="text-secondary small">
                  {activeTab === 'login' ? 'Access your private resumes and jobs' : 'Start analyzing resumes with AI'}
                </span>
              </div>
            </div>
            <button 
              type="button" 
              className="btn-close btn-close-white position-absolute top-0 end-0 m-3" 
              onClick={onClose}
              aria-label="Close"
            />
          </div>

          {/* Mode Switch Tabs */}
          <div className="px-4 pt-3">
            <div 
              className="d-flex p-1 gap-1" 
              style={{ 
                background: 'rgba(0, 0, 0, 0.25)', 
                borderRadius: '10px',
                border: '1px solid rgba(255, 255, 255, 0.05)'
              }}
            >
              <button
                type="button"
                className={`btn flex-grow-1 btn-sm py-2 d-flex align-items-center justify-content-center gap-2 ${
                  activeTab === 'login' 
                    ? 'btn-primary shadow-sm fw-semibold' 
                    : 'text-secondary border-0'
                }`}
                style={{ borderRadius: '8px' }}
                onClick={() => handleTabSwitch('login')}
              >
                <LogIn size={15} />
                <span>Sign In</span>
              </button>
              <button
                type="button"
                className={`btn flex-grow-1 btn-sm py-2 d-flex align-items-center justify-content-center gap-2 ${
                  activeTab === 'register' 
                    ? 'btn-primary shadow-sm fw-semibold' 
                    : 'text-secondary border-0'
                }`}
                style={{ borderRadius: '8px' }}
                onClick={() => handleTabSwitch('register')}
              >
                <UserPlus size={15} />
                <span>Sign Up</span>
              </button>
            </div>
          </div>

          {/* Body Form */}
          <div className="modal-body p-4 pt-3">
            {error && (
              <div className="alert alert-danger d-flex align-items-center gap-2 py-2 px-3 small mb-3 border-0" style={{ borderRadius: '8px' }}>
                <AlertCircle size={16} className="flex-shrink-0" />
                <div>{error}</div>
              </div>
            )}

            {successMsg && (
              <div className="alert alert-success d-flex align-items-center gap-2 py-2 px-3 small mb-3 border-0" style={{ borderRadius: '8px' }}>
                <CheckCircle2 size={16} className="flex-shrink-0" />
                <div>{successMsg}</div>
              </div>
            )}

            <form onSubmit={handleSubmit}>
              {activeTab === 'register' && (
                <div className="mb-3">
                  <label className="form-label small fw-semibold text-secondary mb-1">
                    Full Name
                  </label>
                  <div className="input-group">
                    <span className="input-group-text bg-transparent border-secondary border-opacity-25 text-secondary">
                      <User size={16} />
                    </span>
                    <input
                      type="text"
                      className="form-control bg-transparent border-secondary border-opacity-25 text-light"
                      placeholder="e.g. Alex Morgan"
                      value={name}
                      onChange={(e) => setName(e.target.value)}
                      required
                    />
                  </div>
                </div>
              )}

              <div className="mb-3">
                <label className="form-label small fw-semibold text-secondary mb-1">
                  Email Address
                </label>
                <div className="input-group">
                  <span className="input-group-text bg-transparent border-secondary border-opacity-25 text-secondary">
                    <Mail size={16} />
                  </span>
                  <input
                    type="email"
                    className="form-control bg-transparent border-secondary border-opacity-25 text-light"
                    placeholder="name@example.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                </div>
              </div>

              <div className="mb-3">
                <label className="form-label small fw-semibold text-secondary mb-1">
                  Password
                </label>
                <div className="input-group">
                  <span className="input-group-text bg-transparent border-secondary border-opacity-25 text-secondary">
                    <Lock size={16} />
                  </span>
                  <input
                    type="password"
                    className="form-control bg-transparent border-secondary border-opacity-25 text-light"
                    placeholder="••••••••"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                </div>
              </div>

              {activeTab === 'register' && (
                <div className="mb-3">
                  <label className="form-label small fw-semibold text-secondary mb-1">
                    Confirm Password
                  </label>
                  <div className="input-group">
                    <span className="input-group-text bg-transparent border-secondary border-opacity-25 text-secondary">
                      <Lock size={16} />
                    </span>
                    <input
                      type="password"
                      className="form-control bg-transparent border-secondary border-opacity-25 text-light"
                      placeholder="••••••••"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      required
                    />
                  </div>
                </div>
              )}

              <button
                type="submit"
                disabled={loading}
                className="btn btn-primary w-100 py-2 d-flex align-items-center justify-content-center gap-2 fw-semibold mt-4 shadow"
                style={{ borderRadius: '10px' }}
              >
                {loading ? (
                  <>
                    <Loader2 size={16} className="spinner-border spinner-border-sm" />
                    <span>Processing...</span>
                  </>
                ) : activeTab === 'login' ? (
                  <>
                    <LogIn size={16} />
                    <span>Sign In</span>
                  </>
                ) : (
                  <>
                    <UserPlus size={16} />
                    <span>Create Free Account</span>
                  </>
                )}
              </button>
            </form>

            {/* Quick Demo Access */}
            <div className="mt-3 text-center">
              <div className="position-relative my-3">
                <hr className="border-secondary opacity-25 my-0" />
                <span 
                  className="position-absolute top-50 start-50 translate-middle px-2 text-secondary small"
                  style={{ background: 'var(--bg-card, #1e293b)' }}
                >
                  or
                </span>
              </div>
              <button
                type="button"
                className="btn btn-outline-secondary w-100 btn-sm py-2 d-flex align-items-center justify-content-center gap-2"
                style={{ borderRadius: '10px' }}
                onClick={handleQuickDemo}
                disabled={loading}
              >
                <Sparkles size={14} className="text-warning" />
                <span>One-Click Demo Session (`demo@resumex.ai`)</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AuthModal;
