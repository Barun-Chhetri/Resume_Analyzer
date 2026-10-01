import React, { useState, useEffect } from 'react';
import { 
  Briefcase, 
  Plus, 
  Trash2, 
  Eye, 
  Upload, 
  CheckCircle, 
  AlertCircle, 
  Sparkles,
  MapPin,
  Clock,
  GraduationCap
} from 'lucide-react';
import api from '../services/api';

const JobManager = () => {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  // Selected Job for Details Modal
  const [selectedJob, setSelectedJob] = useState(null);

  // Create Modal State
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [title, setTitle] = useState('');
  const [company, setCompany] = useState('');
  const [location, setLocation] = useState('Remote');
  const [employmentType, setEmploymentType] = useState('Full-time');
  const [rawText, setRawText] = useState('');

  useEffect(() => {
    loadJobs();
  }, []);

  const loadJobs = async () => {
    setLoading(true);
    try {
      const data = await api.getJobs();
      setJobs(data);
    } catch (err) {
      console.error('Failed to load jobs', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateJob = async (e) => {
    e.preventDefault();
    if (!rawText.trim()) {
      setErrorMsg('Job description text cannot be blank.');
      return;
    }
    setErrorMsg('');
    setSuccessMsg('');
    setSubmitting(true);

    try {
      await api.createJob({
        title: title || 'Software Engineer',
        company: company || 'Enterprise Labs',
        location,
        employmentType,
        rawText
      });
      setSuccessMsg('Job posting created and requirements parsed successfully!');
      setShowCreateModal(false);
      resetForm();
      await loadJobs();
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Failed to create job posting.');
    } finally {
      setSubmitting(false);
    }
  };

  const resetForm = () => {
    setTitle('');
    setCompany('');
    setLocation('Remote');
    setEmploymentType('Full-time');
    setRawText('');
  };

  const handleDeleteJob = async (id, e) => {
    e.stopPropagation();
    if (!window.confirm('Are you sure you want to delete this job posting?')) return;
    try {
      await api.deleteJob(id);
      if (selectedJob?.id === id) setSelectedJob(null);
      await loadJobs();
    } catch (err) {
      alert('Failed to delete job: ' + (err.response?.data?.message || err.message));
    }
  };

  const loadPresetJob = (type) => {
    if (type === 'senior-backend') {
      setTitle('Senior Java / Spring Boot Backend Engineer');
      setCompany('FinTech Scale Systems');
      setLocation('Dallas, TX (Hybrid)');
      setEmploymentType('Full-time');
      setRawText(`Senior Java Backend Engineer
FinTech Scale Systems - Dallas, TX

About The Role:
We are seeking an experienced Backend Engineer to architect, build, and maintain our high-throughput transaction processing microservices. You will work closely with database architects to ensure 99.99% availability and ultra-low latency.

Minimum Qualifications & Required Skills:
- 3+ years of professional software development experience in Java (Java 17 or Java 21)
- Deep expertise in Spring Boot, Spring Data JPA, and REST API microservices architecture
- Strong proficiency in PostgreSQL, relational database design, query optimization, and connection pooling
- Solid hands-on experience with Docker, containerization, and Linux server environments
- Proven track record with Amazon Web Services (AWS - EC2, S3, RDS, Lambda)
- Familiarity with CI/CD automation pipelines (GitHub Actions, Jenkins)
- Bachelor's degree in Computer Science, Software Engineering, or related technical field

Preferred & Bonus Qualifications:
- Experience with Kubernetes (k8s) container orchestration
- Hands-on experience with AI architectures, Spring AI, Vector Databases, or PGVector
- Familiarity with Redis caching and Apache Kafka distributed streaming
- Experience implementing comprehensive unit tests with JUnit 5 and Mockito`);
    } else if (type === 'fullstack') {
      setTitle('Full Stack Software Engineer (Java & React)');
      setCompany('CloudScale Innovations');
      setLocation('Remote');
      setEmploymentType('Full-time');
      setRawText(`Full Stack Software Engineer
CloudScale Innovations - Remote

Role Overview:
Join our engineering team to build scalable modern web applications. You will be responsible for building responsive React frontends and robust Spring Boot REST backend services.

Required Skills:
- Proficiency in Java and Spring Boot framework
- Strong experience with JavaScript, TypeScript, React, and modern CSS frameworks
- Relational database experience with PostgreSQL or MySQL
- Experience writing RESTful APIs and integrating backend services
- Proficiency with Git version control and collaborative workflows

Preferred Skills:
- Experience with Docker and cloud environments (AWS or Azure)
- Understanding of RAG pipelines or Large Language Model integration
- Experience with Next.js and server-side rendering`);
    }
  };

  return (
    <div className="container py-4">
      {/* Header */}
      <div className="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
        <div>
          <h1 className="h3 fw-bold mb-1">Target Job Postings</h1>
          <p className="text-secondary small mb-0">Create, store, and inspect role requirements for candidate matching.</p>
        </div>
        <button 
          className="btn-primary-custom"
          onClick={() => {
            setShowCreateModal(true);
            setErrorMsg('');
            setSuccessMsg('');
          }}
        >
          <Plus size={16} />
          <span>Add Job Description</span>
        </button>
      </div>

      {successMsg && (
        <div className="alert alert-success py-2 px-3 small d-flex align-items-center gap-2 mb-3">
          <CheckCircle size={16} />
          <span>{successMsg}</span>
        </div>
      )}

      {/* Jobs List */}
      {loading ? (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status"></div>
          <p className="text-secondary small mt-2">Loading job postings...</p>
        </div>
      ) : jobs.length === 0 ? (
        <div className="glass-panel text-center py-5">
          <Briefcase size={48} className="text-secondary mb-3 opacity-50" />
          <h3 className="h5 fw-bold mb-1">No Job Descriptions Stored</h3>
          <p className="text-secondary small mb-4">Add a job description to analyze candidate resumes against.</p>
          <div className="d-flex justify-content-center gap-2">
            <button className="btn-primary-custom" onClick={() => setShowCreateModal(true)}>
              <Plus size={16} />
              <span>Create Job</span>
            </button>
            <button 
              className="btn-secondary-custom" 
              onClick={() => { setShowCreateModal(true); loadPresetJob('senior-backend'); }}
            >
              <Sparkles size={16} />
              <span>Load Senior Java Backend Preset</span>
            </button>
          </div>
        </div>
      ) : (
        <div className="row g-3">
          {jobs.map((job) => {
            const reqSkillsCount = job.skills?.filter(s => s.isRequired).length || 0;
            const prefSkillsCount = job.skills?.filter(s => !s.isRequired).length || 0;

            return (
              <div key={job.id} className="col-md-6 col-lg-4">
                <div 
                  className="glass-panel p-4 h-100 d-flex flex-column cursor-pointer"
                  style={{ cursor: 'pointer' }}
                  onClick={() => setSelectedJob(job)}
                >
                  <div className="d-flex align-items-center justify-content-between mb-3">
                    <div className="p-2 rounded bg-tertiary text-primary">
                      <Briefcase size={20} />
                    </div>
                    <div className="d-flex align-items-center gap-2">
                      <span className="badge-tag exact" style={{ fontSize: '0.65rem' }}>
                        {reqSkillsCount} Required
                      </span>
                      <button 
                        className="btn-outline-danger p-1"
                        title="Delete Job"
                        onClick={(e) => handleDeleteJob(job.id, e)}
                      >
                        <Trash2 size={14} />
                      </button>
                    </div>
                  </div>

                  <h3 className="h6 fw-bold mb-1 text-truncate">{job.title}</h3>
                  <div className="text-secondary small text-truncate mb-2">
                    {job.company || 'Enterprise Company'} • {job.location || 'Remote'}
                  </div>

                  <p className="text-secondary small flex-grow-1" style={{ fontSize: '0.8rem', display: '-webkit-box', WebkitLineClamp: 3, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                    {job.summary || job.rawText?.substring(0, 150) + '...'}
                  </p>

                  <div className="d-flex align-items-center justify-content-between pt-3 mt-auto border-top border-subtle">
                    <span className="text-secondary" style={{ fontSize: '0.75rem' }}>
                      {job.requiredExperienceYears > 0 ? `${job.requiredExperienceYears}+ yrs exp` : 'Any exp'}
                    </span>
                    <span className="text-primary small fw-semibold d-flex align-items-center gap-1">
                      Inspect Requirements <Eye size={14} />
                    </span>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Create Job Modal */}
      {showCreateModal && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.7)', backdropFilter: 'blur(4px)' }}>
          <div className="modal-dialog modal-dialog-centered modal-lg">
            <div className="modal-content glass-panel border-strong" style={{ background: 'var(--bg-secondary)', color: 'inherit' }}>
              <div className="modal-header border-subtle">
                <h5 className="modal-title fw-bold">Create Target Job Posting</h5>
                <button type="button" className="btn-close btn-close-white" onClick={() => setShowCreateModal(false)}></button>
              </div>
              <form onSubmit={handleCreateJob}>
                <div className="modal-body">
                  {errorMsg && (
                    <div className="alert alert-danger py-2 px-3 small d-flex align-items-center gap-2 mb-3">
                      <AlertCircle size={16} />
                      <span>{errorMsg}</span>
                    </div>
                  )}

                  <div className="d-flex gap-2 mb-3">
                    <span className="small text-secondary fw-semibold">Quick Presets:</span>
                    <button 
                      type="button" 
                      className="btn btn-sm btn-outline-info py-0 px-2"
                      style={{ fontSize: '0.75rem' }}
                      onClick={() => loadPresetJob('senior-backend')}
                    >
                      Senior Java Backend
                    </button>
                    <button 
                      type="button" 
                      className="btn btn-sm btn-outline-info py-0 px-2"
                      style={{ fontSize: '0.75rem' }}
                      onClick={() => loadPresetJob('fullstack')}
                    >
                      Full Stack (Java + React)
                    </button>
                  </div>

                  <div className="row g-2 mb-3">
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Job Title</label>
                      <input 
                        type="text" 
                        className="form-control bg-tertiary text-primary border-subtle"
                        placeholder="e.g. Senior Java Backend Engineer"
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Company</label>
                      <input 
                        type="text" 
                        className="form-control bg-tertiary text-primary border-subtle"
                        placeholder="e.g. Enterprise Systems"
                        value={company}
                        onChange={(e) => setCompany(e.target.value)}
                      />
                    </div>
                  </div>

                  <div className="row g-2 mb-3">
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Location</label>
                      <input 
                        type="text" 
                        className="form-control bg-tertiary text-primary border-subtle"
                        placeholder="e.g. Remote, Dallas, TX"
                        value={location}
                        onChange={(e) => setLocation(e.target.value)}
                      />
                    </div>
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Employment Type</label>
                      <select 
                        className="form-select bg-tertiary text-primary border-subtle"
                        value={employmentType}
                        onChange={(e) => setEmploymentType(e.target.value)}
                      >
                        <option value="Full-time">Full-time</option>
                        <option value="Internship">Internship</option>
                        <option value="Contract">Contract</option>
                        <option value="Part-time">Part-time</option>
                      </select>
                    </div>
                  </div>

                  <div className="mb-3">
                    <label className="form-label small fw-semibold">Job Description Content</label>
                    <textarea 
                      className="form-control bg-tertiary text-primary border-subtle font-monospace"
                      rows={10}
                      placeholder="Paste the full job description here..."
                      value={rawText}
                      onChange={(e) => setRawText(e.target.value)}
                      style={{ fontSize: '0.85rem' }}
                    />
                    <div className="form-text text-secondary" style={{ fontSize: '0.75rem' }}>
                      The parser automatically extracts Required vs Preferred skills, required years of experience, and degrees.
                    </div>
                  </div>
                </div>
                <div className="modal-footer border-subtle">
                  <button type="button" className="btn btn-secondary btn-sm" onClick={() => setShowCreateModal(false)}>Cancel</button>
                  <button type="submit" className="btn-primary-custom" disabled={submitting}>
                    {submitting ? 'Parsing Requirements...' : 'Save & Parse Job'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}

      {/* Job Detail Modal */}
      {selectedJob && (
        <div className="modal show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.7)', backdropFilter: 'blur(4px)' }}>
          <div className="modal-dialog modal-dialog-centered modal-xl">
            <div className="modal-content glass-panel border-strong" style={{ background: 'var(--bg-secondary)', color: 'inherit', maxHeight: '90vh' }}>
              <div className="modal-header border-subtle">
                <div>
                  <h5 className="modal-title fw-bold mb-0">{selectedJob.title}</h5>
                  <div className="text-secondary small mt-1 d-flex flex-wrap gap-3">
                    <span><Briefcase size={14} className="me-1" />{selectedJob.company}</span>
                    <span><MapPin size={14} className="me-1" />{selectedJob.location}</span>
                    <span><Clock size={14} className="me-1" />{selectedJob.requiredExperienceYears}+ Years Experience</span>
                    <span><GraduationCap size={14} className="me-1" />{selectedJob.requiredDegree}</span>
                  </div>
                </div>
                <button type="button" className="btn-close btn-close-white" onClick={() => setSelectedJob(null)}></button>
              </div>

              <div className="modal-body overflow-auto" style={{ maxHeight: 'calc(90vh - 160px)' }}>
                {/* Required vs Preferred Skills */}
                <div className="row g-4 mb-4">
                  <div className="col-md-6">
                    <div className="p-3 rounded bg-tertiary border border-subtle h-100">
                      <div className="d-flex align-items-center justify-content-between mb-3">
                        <h6 className="fw-bold mb-0 text-success">Required Technical Competencies</h6>
                        <span className="badge-tag exact">
                          {selectedJob.skills?.filter(s => s.isRequired).length || 0} Skills
                        </span>
                      </div>
                      <div className="d-flex flex-wrap gap-2">
                        {selectedJob.skills?.filter(s => s.isRequired).map((s) => (
                          <span key={s.id || s.rawName} className="badge-tag exact" style={{ fontSize: '0.8rem' }}>
                            {s.canonicalName || s.rawName}
                          </span>
                        ))}
                      </div>
                    </div>
                  </div>

                  <div className="col-md-6">
                    <div className="p-3 rounded bg-tertiary border border-subtle h-100">
                      <div className="d-flex align-items-center justify-content-between mb-3">
                        <h6 className="fw-bold mb-0 text-primary">Preferred & Bonus Competencies</h6>
                        <span className="badge-tag normalized">
                          {selectedJob.skills?.filter(s => !s.isRequired).length || 0} Skills
                        </span>
                      </div>
                      <div className="d-flex flex-wrap gap-2">
                        {selectedJob.skills?.filter(s => !s.isRequired).map((s) => (
                          <span key={s.id || s.rawName} className="badge-tag normalized" style={{ fontSize: '0.8rem' }}>
                            {s.canonicalName || s.rawName}
                          </span>
                        ))}
                      </div>
                    </div>
                  </div>
                </div>

                {/* Raw Job Description */}
                <div>
                  <h6 className="fw-bold text-secondary mb-2">Original Job Description Text</h6>
                  <pre className="p-3 rounded bg-tertiary border border-subtle text-secondary font-monospace" style={{ whiteSpace: 'pre-wrap', fontSize: '0.8rem' }}>
                    {selectedJob.rawText}
                  </pre>
                </div>
              </div>

              <div className="modal-footer border-subtle">
                <button type="button" className="btn btn-secondary btn-sm" onClick={() => setSelectedJob(null)}>Close</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default JobManager;
