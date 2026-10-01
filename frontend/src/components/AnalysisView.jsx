import React, { useState, useEffect } from 'react';
import { 
  Cpu, 
  CheckCircle2, 
  AlertTriangle, 
  Lightbulb, 
  Layers, 
  ArrowRight, 
  FileText, 
  Briefcase, 
  Play, 
  AlertCircle,
  HelpCircle,
  TrendingUp,
  Award,
  BookOpen,
  Compass
} from 'lucide-react';
import api from '../services/api';

const AnalysisView = ({ initialAnalysisId }) => {
  const [resumes, setResumes] = useState([]);
  const [jobs, setJobs] = useState([]);
  const [selectedResumeId, setSelectedResumeId] = useState('');
  const [selectedJobId, setSelectedJobId] = useState('');

  const [currentAnalysis, setCurrentAnalysis] = useState(null);
  const [loading, setLoading] = useState(false);
  const [analyzing, setAnalyzing] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  const [activeTab, setActiveTab] = useState('matches'); // matches, gaps, recommendations, evidence

  useEffect(() => {
    loadSelectors();
    if (initialAnalysisId) {
      loadAnalysis(initialAnalysisId);
    }
  }, [initialAnalysisId]);

  const loadSelectors = async () => {
    try {
      const [resumesData, jobsData] = await Promise.all([
        api.getResumes().catch(() => []),
        api.getJobs().catch(() => [])
      ]);
      setResumes(resumesData);
      setJobs(jobsData);
      if (resumesData.length > 0 && !selectedResumeId) setSelectedResumeId(resumesData[0].id);
      if (jobsData.length > 0 && !selectedJobId) setSelectedJobId(jobsData[0].id);
    } catch (err) {
      console.error('Error loading selectors', err);
    }
  };

  const loadAnalysis = async (id) => {
    setLoading(true);
    try {
      const data = await api.getAnalysisById(id);
      setCurrentAnalysis(data);
      setSelectedResumeId(data.resumeId);
      setSelectedJobId(data.jobId);
    } catch (err) {
      setErrorMsg('Failed to load analysis details.');
    } finally {
      setLoading(false);
    }
  };

  const handleRunAnalysis = async () => {
    if (!selectedResumeId || !selectedJobId) {
      setErrorMsg('Please select both a resume and a job description.');
      return;
    }
    setErrorMsg('');
    setAnalyzing(true);
    try {
      const result = await api.runAnalysis(selectedResumeId, selectedJobId);
      setCurrentAnalysis(result);
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Analysis execution failed.');
    } finally {
      setAnalyzing(false);
    }
  };

  return (
    <div className="container py-4">
      {/* Selector & Action Bar */}
      <div className="glass-panel p-3 p-md-4 mb-4">
        <div className="row g-3 align-items-end">
          <div className="col-md-5">
            <label className="form-label small fw-semibold text-secondary">Candidate Resume</label>
            <select 
              className="form-select bg-tertiary text-primary border-subtle"
              value={selectedResumeId}
              onChange={(e) => setSelectedResumeId(e.target.value)}
              disabled={analyzing}
            >
              {resumes.map(r => (
                <option key={r.id} value={r.id}>
                  {r.candidateName || r.filename} ({r.skills?.length || 0} skills)
                </option>
              ))}
            </select>
          </div>

          <div className="col-md-5">
            <label className="form-label small fw-semibold text-secondary">Target Job Posting</label>
            <select 
              className="form-select bg-tertiary text-primary border-subtle"
              value={selectedJobId}
              onChange={(e) => setSelectedJobId(e.target.value)}
              disabled={analyzing}
            >
              {jobs.map(j => (
                <option key={j.id} value={j.id}>
                  {j.title} @ {j.company || 'Enterprise'}
                </option>
              ))}
            </select>
          </div>

          <div className="col-md-2">
            <button 
              className="btn-primary-custom w-100 py-2"
              onClick={handleRunAnalysis}
              disabled={analyzing || !selectedResumeId || !selectedJobId}
            >
              {analyzing ? 'Evaluating...' : 'Run Analysis'}
            </button>
          </div>
        </div>

        {errorMsg && (
          <div className="alert alert-danger py-2 px-3 small d-flex align-items-center gap-2 mt-3 mb-0">
            <AlertCircle size={16} />
            <span>{errorMsg}</span>
          </div>
        )}
      </div>

      {loading ? (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status"></div>
          <p className="text-secondary small mt-2">Loading match evaluation...</p>
        </div>
      ) : !currentAnalysis ? (
        <div className="glass-panel text-center py-5">
          <Cpu size={48} className="text-secondary mb-3 opacity-50" />
          <h3 className="h5 fw-bold mb-1">Select Resume & Job to Run Analysis</h3>
          <p className="text-secondary small mb-4">
            Click "Run Analysis" above to compare candidate qualifications against required and preferred job competencies.
          </p>
        </div>
      ) : (
        <div>
          {/* Executive Overview Banner */}
          <div className="glass-panel p-4 mb-4" style={{ borderLeft: '4px solid var(--accent-primary)' }}>
            <div className="row align-items-center g-3">
              <div className="col-lg-8">
                <div className="d-flex align-items-center gap-2 mb-2">
                  <span className="badge-tag exact">Analysis Completed</span>
                  <span className="text-secondary small">ID: {currentAnalysis.id.substring(0, 8)}...</span>
                </div>
                <h2 className="h4 fw-bold mb-1">
                  {currentAnalysis.candidateName || 'Candidate'} ↔ {currentAnalysis.jobTitle}
                </h2>
                <div className="text-secondary small mb-3">
                  Company: <span className="fw-semibold text-primary">{currentAnalysis.jobCompany}</span> • Resume: <span className="fw-semibold">{currentAnalysis.resumeFilename}</span>
                </div>
                <p className="text-secondary small mb-0" style={{ maxWidth: '700px' }}>
                  {currentAnalysis.summary}
                </p>
              </div>

              {/* Composite Score Gauge */}
              <div className="col-lg-4 text-lg-end">
                <div className="d-inline-flex flex-column align-items-center p-3 rounded bg-tertiary">
                  <div className="stat-label">Composite Compatibility</div>
                  <div className="stat-value" style={{ color: currentAnalysis.overallMatchScore >= 70 ? '#10b981' : '#f59e0b' }}>
                    {Math.round(currentAnalysis.overallMatchScore)}%
                  </div>
                  <span className="text-secondary" style={{ fontSize: '0.725rem' }}>
                    Multi-factor grounded score
                  </span>
                </div>
              </div>
            </div>

            {/* Score Breakdown Row */}
            <div className="row g-2 mt-3 pt-3 border-top border-subtle">
              <div className="col-6 col-md-3">
                <div className="small text-secondary">Required Skills Coverage</div>
                <div className="h6 fw-bold mb-0 text-success">{Math.round(currentAnalysis.requiredSkillCoverage)}%</div>
              </div>
              <div className="col-6 col-md-3">
                <div className="small text-secondary">Preferred Skills Coverage</div>
                <div className="h6 fw-bold mb-0 text-primary">{Math.round(currentAnalysis.preferredSkillCoverage)}%</div>
              </div>
              <div className="col-6 col-md-3">
                <div className="small text-secondary">Semantic Domain Relevance</div>
                <div className="h6 fw-bold mb-0 text-info">{Math.round(currentAnalysis.semanticSimilarityScore)}%</div>
              </div>
              <div className="col-6 col-md-3">
                <div className="small text-secondary">Experience & Degree Alignment</div>
                <div className="h6 fw-bold mb-0 text-warning">{Math.round(currentAnalysis.experienceAlignmentScore)}%</div>
              </div>
            </div>
          </div>

          {/* Navigation Tabs */}
          <div className="glass-panel p-2 mb-4 d-flex flex-wrap gap-2">
            <button 
              className={`nav-link-btn ${activeTab === 'matches' ? 'active' : ''}`}
              onClick={() => setActiveTab('matches')}
            >
              <CheckCircle2 size={16} className="text-success" />
              <span>Matched Skills ({currentAnalysis.matches?.length || 0})</span>
            </button>
            <button 
              className={`nav-link-btn ${activeTab === 'gaps' ? 'active' : ''}`}
              onClick={() => setActiveTab('gaps')}
            >
              <AlertTriangle size={16} className="text-warning" />
              <span>Skill Gaps ({currentAnalysis.gaps?.length || 0})</span>
            </button>
            <button 
              className={`nav-link-btn ${activeTab === 'recommendations' ? 'active' : ''}`}
              onClick={() => setActiveTab('recommendations')}
            >
              <Lightbulb size={16} className="text-info" />
              <span>Grounded AI Recommendations ({currentAnalysis.recommendations?.length || 0})</span>
            </button>
            <button 
              className={`nav-link-btn ${activeTab === 'evidence' ? 'active' : ''}`}
              onClick={() => setActiveTab('evidence')}
            >
              <Compass size={16} />
              <span>Evidence Deep Dive</span>
            </button>
          </div>

          {/* TAB 1: Matched Skills */}
          {activeTab === 'matches' && (
            <div className="glass-panel p-4">
              <h3 className="h6 fw-bold text-secondary mb-3">Identified Competency Matches</h3>
              {(!currentAnalysis.matches || currentAnalysis.matches.length === 0) ? (
                <p className="text-secondary small">No matching skills identified.</p>
              ) : (
                <div className="table-responsive">
                  <table className="table table-hover align-middle mb-0" style={{ color: 'inherit' }}>
                    <thead>
                      <tr className="text-secondary small" style={{ borderBottomColor: 'var(--border-subtle)' }}>
                        <th>Job Requirement</th>
                        <th>Resume Evidence</th>
                        <th>Canonical Entity</th>
                        <th>Match Type</th>
                        <th>Cosine Similarity</th>
                        <th>Explanation</th>
                      </tr>
                    </thead>
                    <tbody>
                      {currentAnalysis.matches.map((m) => {
                        const badgeClass = m.matchType === 'EXACT' ? 'exact' :
                                           m.matchType === 'NORMALIZED' ? 'normalized' :
                                           m.matchType === 'ALIAS' ? 'alias' : 'semantic';
                        return (
                          <tr key={m.id || m.jobSkillName} style={{ borderBottomColor: 'var(--border-subtle)' }}>
                            <td className="fw-semibold">
                              {m.jobSkillName}
                              {m.isRequired && <span className="ms-1 text-danger" title="Required">*</span>}
                            </td>
                            <td>
                              <code>{m.resumeSkillName}</code>
                            </td>
                            <td className="text-secondary small">
                              {m.canonicalName || m.jobSkillName}
                            </td>
                            <td>
                              <span className={`badge-tag ${badgeClass}`}>
                                {m.matchType}
                              </span>
                            </td>
                            <td className="font-monospace text-primary fw-bold small">
                              {(m.similarityScore * 100).toFixed(0)}%
                            </td>
                            <td className="text-secondary small" style={{ maxWidth: '350px' }}>
                              {m.explanation}
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}

          {/* TAB 2: Skill Gaps */}
          {activeTab === 'gaps' && (
            <div className="glass-panel p-4">
              <h3 className="h6 fw-bold text-secondary mb-3">Identified Qualification Gaps</h3>
              {(!currentAnalysis.gaps || currentAnalysis.gaps.length === 0) ? (
                <div className="text-center py-4 text-success">
                  <CheckCircle2 size={36} className="mb-2" />
                  <p className="mb-0 fw-semibold">Zero Critical Gaps Found! All requirements matched.</p>
                </div>
              ) : (
                <div className="row g-3">
                  {currentAnalysis.gaps.map((g) => {
                    const isCrit = g.gapSeverity === 'CRITICAL' || g.isRequired;
                    return (
                      <div key={g.id || g.jobSkillName} className="col-md-6">
                        <div 
                          className="p-3 rounded bg-tertiary border h-100"
                          style={{ borderColor: isCrit ? 'rgba(244, 63, 94, 0.3)' : 'rgba(245, 158, 11, 0.3)' }}
                        >
                          <div className="d-flex align-items-center justify-content-between mb-2">
                            <span className="fw-bold h6 mb-0">{g.jobSkillName}</span>
                            <span className={`badge-tag ${isCrit ? 'critical' : 'moderate'}`}>
                              {isCrit ? 'Critical Required Gap' : 'Preferred Skill Gap'}
                            </span>
                          </div>
                          
                          <p className="text-secondary small mb-2">{g.explanation}</p>

                          {g.relatedEvidence && (
                            <div className="p-2 rounded bg-secondary small text-info" style={{ fontSize: '0.75rem' }}>
                              <strong>Related Resume Evidence:</strong> {g.relatedEvidence}
                            </div>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          )}

          {/* TAB 3: Grounded AI Recommendations */}
          {activeTab === 'recommendations' && (
            <div className="glass-panel p-4">
              <div className="d-flex align-items-center justify-content-between mb-3">
                <div>
                  <h3 className="h6 fw-bold mb-0">RAG Grounded Career & Resume Recommendations</h3>
                  <p className="text-secondary small mb-0">Synthesized strictly from verified resume chunks and target job specifications.</p>
                </div>
                <span className="badge-tag exact">Zero Hallucination</span>
              </div>

              {(!currentAnalysis.recommendations || currentAnalysis.recommendations.length === 0) ? (
                <p className="text-secondary small">No specific recommendations generated.</p>
              ) : (
                <div className="d-flex flex-column gap-3">
                  {currentAnalysis.recommendations.map((rec) => {
                    const priorityClass = rec.priority === 'HIGH' ? 'critical' : rec.priority === 'MEDIUM' ? 'moderate' : 'category';
                    return (
                      <div key={rec.id || rec.title} className="p-4 rounded bg-tertiary border border-subtle">
                        <div className="d-flex flex-wrap align-items-center justify-content-between gap-2 mb-2">
                          <div className="d-flex align-items-center gap-2">
                            <span className="badge-tag normalized" style={{ fontSize: '0.7rem' }}>
                              {rec.category?.replace(/_/g, ' ')}
                            </span>
                            <h4 className="h6 fw-bold mb-0">{rec.title}</h4>
                          </div>
                          <span className={`badge-tag ${priorityClass}`}>
                            {rec.priority} Priority
                          </span>
                        </div>

                        <p className="text-secondary small mb-3">{rec.description}</p>

                        {rec.actionableSteps && (
                          <div className="p-3 rounded bg-secondary">
                            <div className="text-primary fw-semibold small mb-1 d-flex align-items-center gap-1">
                              <BookOpen size={14} /> Actionable Next Steps:
                            </div>
                            <pre className="text-secondary mb-0" style={{ whiteSpace: 'pre-wrap', fontFamily: 'inherit', fontSize: '0.8rem' }}>
                              {rec.actionableSteps}
                            </pre>
                          </div>
                        )}
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          )}

          {/* TAB 4: Evidence Deep Dive */}
          {activeTab === 'evidence' && (
            <div className="glass-panel p-4">
              <h3 className="h6 fw-bold mb-3">Evaluation Logic & Context Evidence</h3>
              <p className="text-secondary small mb-4">
                How the matching engine identified candidate competencies and calculated semantic relevance scores.
              </p>

              <div className="row g-3">
                <div className="col-md-6">
                  <div className="p-3 rounded bg-tertiary border border-subtle h-100">
                    <h6 className="fw-bold text-primary mb-2">Multi-Factor Matching Engine Breakdown</h6>
                    <ul className="text-secondary small d-flex flex-column gap-2 mb-0">
                      <li><strong>Exact Match (1.00):</strong> Direct case-insensitive equality against raw text tokens.</li>
                      <li><strong>Normalized Match (0.98):</strong> Key normalization removing whitespace, punctuation, and casing (e.g. <code>spring boot</code> ➔ <code>springboot</code>).</li>
                      <li><strong>Alias Match (0.95):</strong> Canonical entity graph lookup (e.g. <code>AWS</code> ➔ <code>Amazon Web Services</code>, <code>k8s</code> ➔ <code>Kubernetes</code>).</li>
                      <li><strong>Semantic Embedding (0.65 - 0.94):</strong> Cosine similarity over 1536-dimensional unit dense vectors stored in PGVector.</li>
                    </ul>
                  </div>
                </div>

                <div className="col-md-6">
                  <div className="p-3 rounded bg-tertiary border border-subtle h-100">
                    <h6 className="fw-bold text-success mb-2">Composite Compatibility Calculation</h6>
                    <div className="text-secondary small">
                      <p className="mb-2">Rather than a scientifically invalid "hire probability," the platform calculates a verifiable multi-factor index:</p>
                      <ul className="d-flex flex-column gap-1 mb-0">
                        <li><strong>50% Weight:</strong> Required Skills Coverage ({currentAnalysis.requiredSkillCoverage}%)</li>
                        <li><strong>15% Weight:</strong> Preferred Skills Coverage ({currentAnalysis.preferredSkillCoverage}%)</li>
                        <li><strong>20% Weight:</strong> Semantic Domain Similarity ({currentAnalysis.semanticSimilarityScore}%)</li>
                        <li><strong>15% Weight:</strong> Experience & Degree Alignment ({currentAnalysis.experienceAlignmentScore}%)</li>
                      </ul>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default AnalysisView;
