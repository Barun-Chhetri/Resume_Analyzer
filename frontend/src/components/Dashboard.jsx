import React, { useState, useEffect } from 'react';
import { 
  FileText, 
  Briefcase, 
  Cpu, 
  ArrowRight, 
  Play, 
  CheckCircle2, 
  Clock, 
  AlertCircle, 
  Sparkles,
  Search,
  ExternalLink
} from 'lucide-react';
import api from '../services/api';

const Dashboard = ({ onNavigateToAnalysis, onSelectAnalysis, onUploadResume, onCreateJob }) => {
  const [resumes, setResumes] = useState([]);
  const [jobs, setJobs] = useState([]);
  const [analyses, setAnalyses] = useState([]);
  const [loading, setLoading] = useState(true);

  const [selectedResumeId, setSelectedResumeId] = useState('');
  const [selectedJobId, setSelectedJobId] = useState('');
  const [analyzing, setAnalyzing] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    setLoading(true);
    try {
      const [resumesData, jobsData, analysesData] = await Promise.all([
        api.getResumes().catch(() => []),
        api.getJobs().catch(() => []),
        api.getAnalyses().catch(() => [])
      ]);
      setResumes(resumesData);
      setJobs(jobsData);
      setAnalyses(analysesData);

      if (resumesData.length > 0) setSelectedResumeId(resumesData[0].id);
      if (jobsData.length > 0) setSelectedJobId(jobsData[0].id);
    } catch (err) {
      console.error('Failed to load dashboard data', err);
    } finally {
      setLoading(false);
    }
  };

  const handleQuickAnalyze = async () => {
    if (!selectedResumeId || !selectedJobId) {
      setErrorMsg('Please select both a resume and a job description.');
      return;
    }
    setErrorMsg('');
    setAnalyzing(true);
    try {
      const res = await api.runAnalysis(selectedResumeId, selectedJobId);
      onSelectAnalysis(res.id);
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Failed to run analysis.');
    } finally {
      setAnalyzing(false);
    }
  };

  const avgMatchScore = analyses.length > 0 
    ? Math.round(analyses.reduce((acc, a) => acc + (a.overallMatchScore || 0), 0) / analyses.length) 
    : 0;

  return (
    <div className="container py-4">
      {/* Header */}
      <div className="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
        <div>
          <h1 className="h3 fw-bold mb-1">Intelligence Dashboard</h1>
          <p className="text-secondary small mb-0">Overview of resumes, active job requirements, and semantic match analyses.</p>
        </div>
        <div className="d-flex gap-2">
          <button className="btn-secondary-custom" onClick={onUploadResume}>
            <FileText size={16} />
            <span>Upload Resume</span>
          </button>
          <button className="btn-secondary-custom" onClick={onCreateJob}>
            <Briefcase size={16} />
            <span>Add Job</span>
          </button>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="row g-3 mb-4">
        <div className="col-sm-6 col-lg-3">
          <div className="stat-card emerald">
            <div className="stat-label">Resumes Ingested</div>
            <div className="stat-value">{resumes.length}</div>
            <div className="text-secondary small mt-2 d-flex align-items-center gap-1">
              <CheckCircle2 size={14} className="text-success" />
              <span>Parsed & vector embedded</span>
            </div>
          </div>
        </div>

        <div className="col-sm-6 col-lg-3">
          <div className="stat-card indigo">
            <div className="stat-label">Target Jobs</div>
            <div className="stat-value">{jobs.length}</div>
            <div className="text-secondary small mt-2 d-flex align-items-center gap-1">
              <Briefcase size={14} className="text-primary" />
              <span>Requirements cataloged</span>
            </div>
          </div>
        </div>

        <div className="col-sm-6 col-lg-3">
          <div className="stat-card amber">
            <div className="stat-label">Analyses Run</div>
            <div className="stat-value">{analyses.length}</div>
            <div className="text-secondary small mt-2 d-flex align-items-center gap-1">
              <Cpu size={14} className="text-warning" />
              <span>Gap & RAG reports</span>
            </div>
          </div>
        </div>

        <div className="col-sm-6 col-lg-3">
          <div className="stat-card">
            <div className="stat-label">Avg Compatibility</div>
            <div className="stat-value">{avgMatchScore}%</div>
            <div className="text-secondary small mt-2 d-flex align-items-center gap-1">
              <Sparkles size={14} className="text-primary" />
              <span>Multi-factor alignment</span>
            </div>
          </div>
        </div>
      </div>

      {/* Quick Analysis Launcher */}
      <div className="glass-panel p-4 mb-4">
        <div className="d-flex align-items-center gap-2 mb-3">
          <Cpu size={20} className="text-primary" />
          <h2 className="h5 fw-bold mb-0">Quick Multi-Factor Match Launcher</h2>
        </div>
        <p className="text-secondary small mb-4">
          Select a candidate resume and a target job posting to execute exact matching, alias resolution, 
          PGVector cosine similarity matching, and RAG recommendation synthesis.
        </p>

        {errorMsg && (
          <div className="alert alert-danger py-2 px-3 small d-flex align-items-center gap-2 mb-3">
            <AlertCircle size={16} />
            <span>{errorMsg}</span>
          </div>
        )}

        <div className="row g-3 align-items-end">
          <div className="col-md-5">
            <label className="form-label small fw-semibold text-secondary">Candidate Resume</label>
            <select 
              className="form-select bg-tertiary text-primary border-subtle"
              value={selectedResumeId}
              onChange={(e) => setSelectedResumeId(e.target.value)}
              disabled={resumes.length === 0}
            >
              {resumes.length === 0 ? (
                <option value="">No resumes uploaded yet</option>
              ) : (
                resumes.map(r => (
                  <option key={r.id} value={r.id}>
                    {r.candidateName || r.filename} ({r.skills?.length || 0} skills)
                  </option>
                ))
              )}
            </select>
          </div>

          <div className="col-md-5">
            <label className="form-label small fw-semibold text-secondary">Target Job Posting</label>
            <select 
              className="form-select bg-tertiary text-primary border-subtle"
              value={selectedJobId}
              onChange={(e) => setSelectedJobId(e.target.value)}
              disabled={jobs.length === 0}
            >
              {jobs.length === 0 ? (
                <option value="">No jobs created yet</option>
              ) : (
                jobs.map(j => (
                  <option key={j.id} value={j.id}>
                    {j.title} @ {j.company || 'Unknown Company'}
                  </option>
                ))
              )}
            </select>
          </div>

          <div className="col-md-2">
            <button 
              className="btn-primary-custom w-100 py-2"
              onClick={handleQuickAnalyze}
              disabled={analyzing || resumes.length === 0 || jobs.length === 0}
            >
              {analyzing ? (
                <span>Matching...</span>
              ) : (
                <>
                  <Play size={16} />
                  <span>Analyze</span>
                </>
              )}
            </button>
          </div>
        </div>
      </div>

      {/* Recent Analyses & Saved Lists */}
      <div className="row g-4">
        {/* Recent Analyses */}
        <div className="col-lg-8">
          <div className="glass-panel p-4 h-100">
            <div className="d-flex align-items-center justify-content-between mb-3">
              <h3 className="h6 fw-bold mb-0">Recent Match Analyses</h3>
              <span className="badge-tag category">{analyses.length} Total</span>
            </div>

            {analyses.length === 0 ? (
              <div className="text-center py-5 text-secondary">
                <Clock size={36} className="mb-2 opacity-50" />
                <p className="mb-2">No analyses performed yet.</p>
                <p className="small">Upload a resume and job description above to generate your first match report.</p>
              </div>
            ) : (
              <div className="table-responsive">
                <table className="table table-hover align-middle mb-0" style={{ color: 'inherit' }}>
                  <thead>
                    <tr className="text-secondary small" style={{ borderBottomColor: 'var(--border-subtle)' }}>
                      <th>Candidate</th>
                      <th>Target Role</th>
                      <th>Score</th>
                      <th>Coverage</th>
                      <th>Date</th>
                      <th></th>
                    </tr>
                  </thead>
                  <tbody>
                    {analyses.slice(0, 6).map((a) => {
                      const score = Math.round(a.overallMatchScore || 0);
                      const scoreClass = score >= 75 ? 'exact' : score >= 50 ? 'moderate' : 'critical';
                      return (
                        <tr key={a.id} style={{ borderBottomColor: 'var(--border-subtle)' }}>
                          <td>
                            <div className="fw-semibold">{a.candidateName || 'Candidate'}</div>
                            <div className="text-secondary small">{a.resumeFilename}</div>
                          </td>
                          <td>
                            <div>{a.jobTitle}</div>
                            <div className="text-secondary small">{a.jobCompany}</div>
                          </td>
                          <td>
                            <span className={`badge-tag ${scoreClass}`}>
                              {score}%
                            </span>
                          </td>
                          <td className="small text-secondary">
                            Req: {Math.round(a.requiredSkillCoverage || 0)}%
                          </td>
                          <td className="small text-secondary">
                            {new Date(a.createdAt).toLocaleDateString()}
                          </td>
                          <td className="text-end">
                            <button 
                              className="btn btn-sm btn-link text-primary p-0 text-decoration-none"
                              onClick={() => onSelectAnalysis(a.id)}
                            >
                              View <ArrowRight size={14} />
                            </button>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>

        {/* Saved Resumes & Jobs summary */}
        <div className="col-lg-4">
          <div className="d-flex flex-column gap-4 h-100">
            {/* Quick Resume Inventory */}
            <div className="glass-panel p-4 flex-grow-1">
              <div className="d-flex align-items-center justify-content-between mb-3">
                <h3 className="h6 fw-bold mb-0">Saved Resumes</h3>
                <span className="badge-tag category">{resumes.length}</span>
              </div>
              {resumes.length === 0 ? (
                <p className="text-secondary small mb-0">No resumes stored yet.</p>
              ) : (
                <div className="d-flex flex-column gap-2">
                  {resumes.slice(0, 3).map(r => (
                    <div key={r.id} className="p-2 rounded bg-tertiary d-flex align-items-center justify-content-between">
                      <div className="text-truncate me-2" style={{ maxWidth: '180px' }}>
                        <div className="fw-semibold small text-truncate">{r.candidateName || r.filename}</div>
                        <div className="text-secondary" style={{ fontSize: '0.75rem' }}>{r.skills?.length || 0} skills identified</div>
                      </div>
                      <span className="badge-tag exact" style={{ fontSize: '0.65rem' }}>Active</span>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Quick Job Inventory */}
            <div className="glass-panel p-4 flex-grow-1">
              <div className="d-flex align-items-center justify-content-between mb-3">
                <h3 className="h6 fw-bold mb-0">Saved Job Postings</h3>
                <span className="badge-tag category">{jobs.length}</span>
              </div>
              {jobs.length === 0 ? (
                <p className="text-secondary small mb-0">No job postings created yet.</p>
              ) : (
                <div className="d-flex flex-column gap-2">
                  {jobs.slice(0, 3).map(j => (
                    <div key={j.id} className="p-2 rounded bg-tertiary d-flex align-items-center justify-content-between">
                      <div className="text-truncate me-2" style={{ maxWidth: '180px' }}>
                        <div className="fw-semibold small text-truncate">{j.title}</div>
                        <div className="text-secondary" style={{ fontSize: '0.75rem' }}>{j.company || 'Company'}</div>
                      </div>
                      <span className="badge-tag normalized" style={{ fontSize: '0.65rem' }}>
                        {j.skills?.length || 0} skills
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
