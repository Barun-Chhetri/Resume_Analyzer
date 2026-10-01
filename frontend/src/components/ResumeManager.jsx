import React, { useState, useEffect } from 'react';
import { 
  Upload, 
  FileText, 
  Trash2, 
  Eye, 
  Plus, 
  CheckCircle, 
  AlertCircle, 
  Code, 
  Layers, 
  Sparkles,
  Edit2,
  X
} from 'lucide-react';
import api from '../services/api';

const ResumeManager = () => {
  const [resumes, setResumes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  // Selected Resume for Details Modal
  const [selectedResume, setSelectedResume] = useState(null);
  const [activeModalTab, setActiveModalTab] = useState('skills'); // skills, sections, raw, json

  // Upload Modal State
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [pasteMode, setPasteMode] = useState(false);
  const [candidateNameInput, setCandidateNameInput] = useState('');
  const [rawTextInput, setRawTextInput] = useState('');
  const [fileInput, setFileInput] = useState(null);

  useEffect(() => {
    loadResumes();
  }, []);

  const loadResumes = async () => {
    setLoading(true);
    try {
      const data = await api.getResumes();
      setResumes(data);
    } catch (err) {
      console.error('Failed to load resumes', err);
    } finally {
      setLoading(false);
    }
  };

  const handleFileUpload = async (e) => {
    e.preventDefault();
    setErrorMsg('');
    setSuccessMsg('');
    setUploading(true);

    try {
      if (pasteMode) {
        if (!rawTextInput.trim()) {
          setErrorMsg('Resume text cannot be blank.');
          setUploading(false);
          return;
        }
        await api.ingestResumeText(rawTextInput, `${candidateNameInput || 'candidate'}-resume.txt`, candidateNameInput);
      } else {
        if (!fileInput) {
          setErrorMsg('Please select a file to upload.');
          setUploading(false);
          return;
        }
        await api.uploadResumeFile(fileInput);
      }

      setSuccessMsg('Resume parsed, embedded, and stored successfully!');
      setShowUploadModal(false);
      setFileInput(null);
      setRawTextInput('');
      setCandidateNameInput('');
      await loadResumes();
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Failed to process resume.');
    } finally {
      setUploading(false);
    }
  };

  const handleDeleteResume = async (id, e) => {
    e.stopPropagation();
    if (!window.confirm('Are you sure you want to delete this resume?')) return;
    try {
      await api.deleteResume(id);
      if (selectedResume?.id === id) setSelectedResume(null);
      await loadResumes();
    } catch (err) {
      alert('Failed to delete resume: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleLoadSample = () => {
    setPasteMode(true);
    setCandidateNameInput('Barun Chhetri');
    setRawTextInput(`BARUN CHHETRI
Fullstack Developer & AI Engineer
barun.chhetri@example.com | +1 (555) 019-2834 | Dallas-Fort Worth, TX
GitHub: https://github.com/barunchhetri | LinkedIn: linkedin.com/in/barunchhetri

PROFESSIONAL SUMMARY
Results-driven Software Engineer with extensive experience building scalable Java and Spring Boot microservices, high-performance PostgreSQL and PGVector architectures, and RAG-grounded AI applications. Demonstrated record of delivering production-ready distributed systems.

TECHNICAL SKILLS
Languages: Java (21, 17), Python, SQL, JavaScript (ES6+), TypeScript, HTML5/CSS3
Frameworks & Libraries: Spring Boot, Spring AI, React, Next.js, Express.js, Bootstrap
Databases & Storage: PostgreSQL, PGVector, MySQL, Redis, MongoDB
Cloud & DevOps: Docker, Kubernetes, Amazon Web Services (AWS), CI/CD, Git, Linux
AI / Machine Learning: Retrieval-Augmented Generation (RAG), Vector Embeddings, Large Language Models (LLM), NLP
Architecture & Testing: REST API Design, Microservices, JUnit 5, Mockito, System Design

WORK EXPERIENCE
Software Engineering Intern | Tech Innovation Labs | May 2025 – August 2025
- Architected and implemented high-throughput REST APIs using Spring Boot, handling over 250,000 daily requests.
- Designed PostgreSQL schema with Flyway migrations and integrated PGVector similarity search for sub-second retrieval.
- Reduced API p95 query latency by 42% by structuring database indexes and optimizing connection pooling via HikariCP.
- Automated Docker containerization and continuous integration pipelines using GitHub Actions.

PROJECTS
AI Resume Intelligence Platform | Java, Spring Boot, Spring AI, PostgreSQL, PGVector
- Built end-to-end resume-to-job matching engine executing exact, normalized, and dense embedding similarity matching.
- Implemented RAG pipeline with 1536-dimensional vector embeddings, identifying critical skill gaps and synthesizing grounded recommendations.
- Achieved 98% test coverage across entity normalization and similarity calculations using JUnit 5 and Mockito.

EDUCATION
University of North Texas
Bachelor of Science in Computer Science | Expected Graduation: 2026
Coursework: Data Structures & Algorithms, Database Systems, Operating Systems, Software Engineering`);
  };

  return (
    <div className="container py-4">
      {/* Header */}
      <div className="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
        <div>
          <h1 className="h3 fw-bold mb-1">Resume Management</h1>
          <p className="text-secondary small mb-0">Upload, parse, normalize, and inspect candidate resume profiles.</p>
        </div>
        <button 
          className="btn-primary-custom"
          onClick={() => {
            setShowUploadModal(true);
            setErrorMsg('');
            setSuccessMsg('');
          }}
        >
          <Plus size={16} />
          <span>Upload New Resume</span>
        </button>
      </div>

      {successMsg && (
        <div className="alert alert-success py-2 px-3 small d-flex align-items-center gap-2 mb-3">
          <CheckCircle size={16} />
          <span>{successMsg}</span>
        </div>
      )}

      {/* Resumes List */}
      {loading ? (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status"></div>
          <p className="text-secondary small mt-2">Loading resumes...</p>
        </div>
      ) : resumes.length === 0 ? (
        <div className="glass-panel text-center py-5">
          <FileText size={48} className="text-secondary mb-3 opacity-50" />
          <h3 className="h5 fw-bold mb-1">No Resumes Ingested Yet</h3>
          <p className="text-secondary small mb-4">Upload a PDF, Word DOCX, or plain text resume to begin.</p>
          <div className="d-flex justify-content-center gap-2">
            <button className="btn-primary-custom" onClick={() => setShowUploadModal(true)}>
              <Upload size={16} />
              <span>Upload Resume</span>
            </button>
            <button className="btn-secondary-custom" onClick={() => { setShowUploadModal(true); handleLoadSample(); }}>
              <Sparkles size={16} />
              <span>Load Barun Chhetri Sample</span>
            </button>
          </div>
        </div>
      ) : (
        <div className="row g-3">
          {resumes.map((resume) => (
            <div key={resume.id} className="col-md-6 col-lg-4">
              <div 
                className="glass-panel p-4 h-100 d-flex flex-column cursor-pointer"
                style={{ cursor: 'pointer' }}
                onClick={() => setSelectedResume(resume)}
              >
                <div className="d-flex align-items-center justify-content-between mb-3">
                  <div className="p-2 rounded bg-tertiary text-primary">
                    <FileText size={20} />
                  </div>
                  <div className="d-flex align-items-center gap-2">
                    <span className="badge-tag exact">
                      {resume.skills?.length || 0} Skills
                    </span>
                    <button 
                      className="btn-outline-danger p-1"
                      title="Delete Resume"
                      onClick={(e) => handleDeleteResume(resume.id, e)}
                    >
                      <Trash2 size={14} />
                    </button>
                  </div>
                </div>

                <h3 className="h6 fw-bold mb-1 text-truncate">{resume.candidateName || resume.filename}</h3>
                <div className="text-secondary small text-truncate mb-2">{resume.email || 'No email specified'}</div>

                <p className="text-secondary small flex-grow-1" style={{ fontSize: '0.8rem', display: '-webkit-box', WebkitLineClamp: 3, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                  {resume.summary || resume.rawText?.substring(0, 150) + '...'}
                </p>

                <div className="d-flex align-items-center justify-content-between pt-3 mt-auto border-top border-subtle">
                  <span className="text-secondary" style={{ fontSize: '0.75rem' }}>
                    {resume.createdAt ? new Date(resume.createdAt).toLocaleDateString() : 'Active'}
                  </span>
                  <span className="text-primary small fw-semibold d-flex align-items-center gap-1">
                    Inspect Profile <Eye size={14} />
                  </span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Upload Modal */}
      {showUploadModal && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.7)', backdropFilter: 'blur(4px)' }}>
          <div className="modal-dialog modal-dialog-centered modal-lg">
            <div className="modal-content glass-panel border-strong" style={{ background: 'var(--bg-secondary)', color: 'inherit' }}>
              <div className="modal-header border-subtle">
                <h5 className="modal-title fw-bold">Upload & Ingest Candidate Resume</h5>
                <button type="button" className="btn-close btn-close-white" onClick={() => setShowUploadModal(false)}></button>
              </div>
              <form onSubmit={handleFileUpload}>
                <div className="modal-body">
                  {errorMsg && (
                    <div className="alert alert-danger py-2 px-3 small d-flex align-items-center gap-2 mb-3">
                      <AlertCircle size={16} />
                      <span>{errorMsg}</span>
                    </div>
                  )}

                  <div className="d-flex gap-2 mb-4">
                    <button 
                      type="button" 
                      className={`btn btn-sm ${!pasteMode ? 'btn-primary' : 'btn-outline-secondary'}`}
                      onClick={() => setPasteMode(false)}
                    >
                      <Upload size={14} className="me-1" /> Document File (PDF, DOCX, TXT)
                    </button>
                    <button 
                      type="button" 
                      className={`btn btn-sm ${pasteMode ? 'btn-primary' : 'btn-outline-secondary'}`}
                      onClick={() => setPasteMode(true)}
                    >
                      <FileText size={14} className="me-1" /> Paste Text Directly
                    </button>
                    <button 
                      type="button" 
                      className="btn btn-sm btn-outline-info ms-auto"
                      onClick={handleLoadSample}
                    >
                      <Sparkles size={14} className="me-1" /> Load Barun Chhetri Sample
                    </button>
                  </div>

                  {!pasteMode ? (
                    <div className="mb-3">
                      <label className="form-label small fw-semibold">Select Resume Document</label>
                      <input 
                        type="file" 
                        className="form-control bg-tertiary text-primary border-subtle"
                        accept=".pdf,.docx,.txt,.md"
                        onChange={(e) => setFileInput(e.target.files[0])}
                      />
                      <div className="form-text text-secondary" style={{ fontSize: '0.75rem' }}>
                        Supports PDF (Apache PDFBox), Word DOCX (Apache POI), and Plain Text. Text will be extracted, normalized, chunked, and embedded into 1536-dimensional vectors.
                      </div>
                    </div>
                  ) : (
                    <>
                      <div className="mb-3">
                        <label className="form-label small fw-semibold">Candidate Name (Optional)</label>
                        <input 
                          type="text" 
                          className="form-control bg-tertiary text-primary border-subtle"
                          placeholder="e.g. Barun Chhetri"
                          value={candidateNameInput}
                          onChange={(e) => setCandidateNameInput(e.target.value)}
                        />
                      </div>
                      <div className="mb-3">
                        <label className="form-label small fw-semibold">Resume Text Content</label>
                        <textarea 
                          className="form-control bg-tertiary text-primary border-subtle font-monospace"
                          rows={12}
                          placeholder="Paste resume content here..."
                          value={rawTextInput}
                          onChange={(e) => setRawTextInput(e.target.value)}
                          style={{ fontSize: '0.85rem' }}
                        />
                      </div>
                    </>
                  )}
                </div>
                <div className="modal-footer border-subtle">
                  <button type="button" className="btn btn-secondary btn-sm" onClick={() => setShowUploadModal(false)}>Cancel</button>
                  <button type="submit" className="btn-primary-custom" disabled={uploading}>
                    {uploading ? 'Processing & Embedding...' : 'Ingest & Embed Resume'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}

      {/* Resume Detail & Extracted Skills Drawer Modal */}
      {selectedResume && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.7)', backdropFilter: 'blur(4px)' }}>
          <div className="modal-dialog modal-dialog-centered modal-xl">
            <div className="modal-content glass-panel border-strong" style={{ background: 'var(--bg-secondary)', color: 'inherit', maxHeight: '90vh' }}>
              <div className="modal-header border-subtle">
                <div>
                  <h5 className="modal-title fw-bold mb-0">{selectedResume.candidateName || selectedResume.filename}</h5>
                  <div className="text-secondary small mt-1">
                    {selectedResume.email && <span className="me-3">{selectedResume.email}</span>}
                    {selectedResume.phone && <span className="me-3">{selectedResume.phone}</span>}
                    {selectedResume.location && <span>{selectedResume.location}</span>}
                  </div>
                </div>
                <button type="button" className="btn-close btn-close-white" onClick={() => setSelectedResume(null)}></button>
              </div>

              {/* Navigation Tabs */}
              <div className="px-4 pt-2 border-bottom border-subtle d-flex gap-2">
                <button 
                  className={`nav-link-btn ${activeModalTab === 'skills' ? 'active' : ''}`}
                  onClick={() => setActiveModalTab('skills')}
                >
                  <Sparkles size={16} />
                  <span>Extracted Skills ({selectedResume.skills?.length || 0})</span>
                </button>
                <button 
                  className={`nav-link-btn ${activeModalTab === 'sections' ? 'active' : ''}`}
                  onClick={() => setActiveModalTab('sections')}
                >
                  <Layers size={16} />
                  <span>Parsed Sections ({selectedResume.sections ? Object.keys(selectedResume.sections).length : 0})</span>
                </button>
                <button 
                  className={`nav-link-btn ${activeModalTab === 'raw' ? 'active' : ''}`}
                  onClick={() => setActiveModalTab('raw')}
                >
                  <FileText size={16} />
                  <span>Raw Text</span>
                </button>
                <button 
                  className={`nav-link-btn ${activeModalTab === 'json' ? 'active' : ''}`}
                  onClick={() => setActiveModalTab('json')}
                >
                  <Code size={16} />
                  <span>Structured JSON</span>
                </button>
              </div>

              <div className="modal-body overflow-auto" style={{ maxHeight: 'calc(90vh - 180px)' }}>
                {activeModalTab === 'skills' && (
                  <div>
                    <h6 className="fw-bold text-secondary mb-3">Normalized Skills by Category</h6>
                    {(!selectedResume.skills || selectedResume.skills.length === 0) ? (
                      <p className="text-secondary">No skills identified in this resume.</p>
                    ) : (
                      <div className="row g-3">
                        {selectedResume.skills.map((s) => (
                          <div key={s.id || s.rawName} className="col-md-6 col-lg-4">
                            <div className="p-3 rounded bg-tertiary border border-subtle h-100">
                              <div className="d-flex align-items-center justify-content-between mb-2">
                                <span className="fw-bold">{s.canonicalName || s.rawName}</span>
                                <span className="badge-tag category" style={{ fontSize: '0.65rem' }}>{s.category || 'Skill'}</span>
                              </div>
                              <div className="text-secondary small" style={{ fontSize: '0.75rem' }}>
                                Raw match: <code>{s.rawName}</code>
                              </div>
                              {s.contextSnippet && (
                                <div className="text-muted small mt-2 p-1 rounded bg-secondary" style={{ fontSize: '0.725rem' }}>
                                  "{s.contextSnippet}"
                                </div>
                              )}
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                )}

                {activeModalTab === 'sections' && (
                  <div className="d-flex flex-column gap-3">
                    {selectedResume.sections && Object.entries(selectedResume.sections).map(([secName, secContent]) => (
                      <div key={secName} className="p-3 rounded bg-tertiary border border-subtle">
                        <div className="d-flex align-items-center justify-content-between mb-2">
                          <span className="badge-tag normalized">{secName}</span>
                          <span className="badge-tag exact" style={{ fontSize: '0.65rem' }}>1536-dim Embedded</span>
                        </div>
                        <pre className="text-secondary mb-0" style={{ whiteSpace: 'pre-wrap', fontFamily: 'inherit', fontSize: '0.85rem' }}>
                          {secContent}
                        </pre>
                      </div>
                    ))}
                  </div>
                )}

                {activeModalTab === 'raw' && (
                  <pre className="p-3 rounded bg-tertiary border border-subtle text-secondary font-monospace" style={{ whiteSpace: 'pre-wrap', fontSize: '0.8rem' }}>
                    {selectedResume.rawText}
                  </pre>
                )}

                {activeModalTab === 'json' && (
                  <pre className="p-3 rounded bg-tertiary border border-subtle text-success font-monospace" style={{ whiteSpace: 'pre-wrap', fontSize: '0.8rem' }}>
                    {JSON.stringify(JSON.parse(selectedResume.parsedJson || '{}'), null, 2)}
                  </pre>
                )}
              </div>

              <div className="modal-footer border-subtle">
                <button type="button" className="btn btn-secondary btn-sm" onClick={() => setSelectedResume(null)}>Close</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ResumeManager;
