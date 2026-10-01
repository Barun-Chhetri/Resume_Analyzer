import React from 'react';
import { 
  Sparkles, 
  ArrowRight, 
  Upload, 
  Briefcase, 
  Cpu, 
  Target, 
  CheckCircle, 
  Database, 
  Layers, 
  ShieldCheck, 
  FileSearch,
  Code2
} from 'lucide-react';

const LandingPage = ({ onGetStarted, onExploreArchitecture, onManageResumes }) => {
  const steps = [
    { number: '01', title: 'Upload Resume', desc: 'Ingest PDF, DOCX, or TXT. Extracts contact info, work experience, projects, and technical skills.', icon: Upload },
    { number: '02', title: 'Add Job Posting', desc: 'Paste or upload target job description. Automatically separates required vs preferred qualifications.', icon: Briefcase },
    { number: '03', title: 'Multi-Factor Matching', desc: 'Matches skills via exact equality, normalized canonical aliases, and dense vector embeddings.', icon: Cpu },
    { number: '04', title: 'Identify Skill Gaps', desc: 'Surfaces missing requirements, categorizes severity (Critical/Moderate), and flags related experience.', icon: Target },
    { number: '05', title: 'Grounded Recommendations', desc: 'Synthesizes verified resume bullet improvements and portfolio project suggestions via RAG.', icon: Sparkles }
  ];

  const technologies = [
    { name: 'Java 21', desc: 'Modern LTS enterprise backend runtime', tag: 'Core Backend' },
    { name: 'Spring Boot 3.4', desc: 'High-performance microservice architecture', tag: 'Framework' },
    { name: 'Spring AI', desc: 'Portable AI abstractions and RAG workflows', tag: 'AI Engine' },
    { name: 'PostgreSQL', desc: 'ACID-compliant relational storage', tag: 'Database' },
    { name: 'PGVector', desc: '1536-dim vector similarity search with HNSW', tag: 'Vector DB' },
    { name: 'RAG Pipeline', desc: 'Vector retrieval before grounded synthesis', tag: 'Architecture' },
    { name: 'Dense Embeddings', desc: 'Cosine similarity semantic entity matching', tag: 'NLP' },
    { name: 'Docker', desc: 'Production containerization with multi-stage builds', tag: 'DevOps' }
  ];

  return (
    <div className="container py-5">
      {/* Hero Section */}
      <div className="row align-items-center justify-content-center text-center my-4 my-md-5">
        <div className="col-lg-10">
          <div className="d-inline-flex align-items-center gap-2 px-3 py-1 mb-3 glass-panel" style={{ borderRadius: '999px', fontSize: '0.85rem' }}>
            <Sparkles size={16} className="text-primary" />
            <span className="fw-semibold">Production-Grade AI Resume & Job Intelligence Platform</span>
          </div>

          <h1 className="display-4 fw-bold mb-4 tracking-tight" style={{ letterSpacing: '-0.03em' }}>
            Understand how your resume matches the{' '}
            <span className="hero-gradient-text">skills companies are looking for.</span>
          </h1>

          <p className="lead text-secondary mb-5 mx-auto" style={{ maxWidth: '780px', fontSize: '1.2rem' }}>
            Transform unstructured resumes and job descriptions into structured entities, generate vector embeddings,
            execute multi-factor semantic matching using PostgreSQL + PGVector, and receive explainable, grounded recommendations.
          </p>

          <div className="d-flex flex-wrap align-items-center justify-content-center gap-3">
            <button 
              className="btn-primary-custom px-4 py-3"
              style={{ fontSize: '1.05rem', borderRadius: '10px' }}
              onClick={onGetStarted}
            >
              <span>Analyze Your Resume</span>
              <ArrowRight size={18} />
            </button>
            <button 
              className="btn-secondary-custom px-4 py-3"
              style={{ fontSize: '1.05rem', borderRadius: '10px' }}
              onClick={onManageResumes}
            >
              <Upload size={18} />
              <span>Upload Resume</span>
            </button>
            <button 
              className="btn-secondary-custom px-4 py-3"
              style={{ fontSize: '1.05rem', borderRadius: '10px' }}
              onClick={onExploreArchitecture}
            >
              <Layers size={18} />
              <span>Explore Architecture</span>
            </button>
          </div>
        </div>
      </div>

      {/* How It Works Section */}
      <div className="my-5 py-4">
        <div className="text-center mb-5">
          <span className="badge-tag normalized mb-2">Core Workflow</span>
          <h2 className="fw-bold h2">How the Platform Works</h2>
          <p className="text-secondary">End-to-end data pipeline from document ingestion to RAG-grounded recommendations.</p>
        </div>

        <div className="row g-3">
          {steps.map((step) => {
            const Icon = step.icon;
            return (
              <div key={step.number} className="col-md-6 col-lg">
                <div className="glass-panel p-4 h-100 d-flex flex-column">
                  <div className="d-flex align-items-center justify-content-between mb-3">
                    <span className="font-monospace text-primary fw-bold" style={{ fontSize: '1.1rem' }}>{step.number}</span>
                    <div className="p-2 rounded bg-tertiary text-primary">
                      <Icon size={20} />
                    </div>
                  </div>
                  <h3 className="h6 fw-bold mb-2 text-primary-heading">{step.title}</h3>
                  <p className="text-secondary small mb-0 flex-grow-1">{step.desc}</p>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Architecture Showcase */}
      <div className="my-5 py-4">
        <div className="glass-panel p-4 p-md-5">
          <div className="row align-items-center g-4">
            <div className="col-lg-5">
              <span className="badge-tag exact mb-2">Technical Architecture</span>
              <h2 className="fw-bold mb-3">Beyond Simple Wrappers: Real Software Engineering</h2>
              <p className="text-secondary mb-4">
                The platform combines normalized relational entity catalogs with high-dimensional vector similarity.
                Skills like <code>AWS</code> and <code>Amazon Web Services</code> are unified through a canonical taxonomy layer,
                while domain adjacencies (e.g. <code>Docker</code> ↔ <code>Kubernetes</code>) are evaluated via cosine distance over PGVector embeddings.
              </p>
              <ul className="list-unstyled d-flex flex-column gap-2 text-secondary mb-4">
                <li className="d-flex align-items-center gap-2">
                  <CheckCircle size={18} className="text-success" />
                  <span>1536-dimensional normalized vector embeddings</span>
                </li>
                <li className="d-flex align-items-center gap-2">
                  <CheckCircle size={18} className="text-success" />
                  <span>Flyway version-controlled schema migrations</span>
                </li>
                <li className="d-flex align-items-center gap-2">
                  <CheckCircle size={18} className="text-success" />
                  <span>Zero-hallucination grounded RAG context retrieval</span>
                </li>
              </ul>
              <button className="btn-primary-custom" onClick={onExploreArchitecture}>
                <span>View Full System Architecture</span>
                <ArrowRight size={16} />
              </button>
            </div>

            <div className="col-lg-7">
              {/* Visual Pipeline Nodes */}
              <div className="d-flex flex-column gap-3">
                <div className="arch-node">
                  <div className="d-flex align-items-center justify-content-between">
                    <div>
                      <span className="arch-node-badge">STAGE 1: INGESTION & NORMALIZATION</span>
                      <div className="fw-semibold">Resume / JD Parsing ➔ Canonical Skill Taxonomy Mapping</div>
                    </div>
                    <FileSearch size={22} className="text-primary" />
                  </div>
                </div>

                <div className="arch-node" style={{ borderLeft: '4px solid #8b5cf6' }}>
                  <div className="d-flex align-items-center justify-content-between">
                    <div>
                      <span className="arch-node-badge" style={{ color: '#8b5cf6', background: 'rgba(139, 92, 246, 0.15)' }}>STAGE 2: VECTOR EMBEDDING</span>
                      <div className="fw-semibold">Spring AI ➔ 1536-dim Unit Dense Vectors ➔ PGVector Storage</div>
                    </div>
                    <Database size={22} style={{ color: '#8b5cf6' }} />
                  </div>
                </div>

                <div className="arch-node" style={{ borderLeft: '4px solid #10b981' }}>
                  <div className="d-flex align-items-center justify-content-between">
                    <div>
                      <span className="arch-node-badge" style={{ color: '#10b981', background: 'rgba(16, 185, 129, 0.15)' }}>STAGE 3: MULTI-STAGE MATCHING</span>
                      <div className="fw-semibold">Exact ➔ Normalized ➔ Alias ➔ Cosine Similarity Matching</div>
                    </div>
                    <Cpu size={22} className="text-success" />
                  </div>
                </div>

                <div className="arch-node" style={{ borderLeft: '4px solid #f59e0b' }}>
                  <div className="d-flex align-items-center justify-content-between">
                    <div>
                      <span className="arch-node-badge" style={{ color: '#f59e0b', background: 'rgba(245, 158, 11, 0.15)' }}>STAGE 4: RAG SYNTHESIS</span>
                      <div className="fw-semibold">Context Chunk Retrieval ➔ Grounded Recommendations & Gaps</div>
                    </div>
                    <Sparkles size={22} className="text-warning" />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Technology Stack Grid */}
      <div className="my-5 py-4">
        <div className="text-center mb-5">
          <span className="badge-tag category mb-2">Technology Stack</span>
          <h2 className="fw-bold h2">Production-Grade Engineering Stack</h2>
          <p className="text-secondary">Designed and built with modern enterprise backend and AI technologies.</p>
        </div>

        <div className="row g-3">
          {technologies.map((tech) => (
            <div key={tech.name} className="col-sm-6 col-lg-3">
              <div className="stat-card h-100">
                <span className="badge-tag category mb-2" style={{ fontSize: '0.7rem' }}>{tech.tag}</span>
                <div className="fw-bold mb-1" style={{ fontSize: '1.15rem' }}>{tech.name}</div>
                <div className="text-secondary small">{tech.desc}</div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default LandingPage;
