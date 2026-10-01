import React, { useState, useEffect } from 'react';
import { 
  Layers, 
  Cpu, 
  Database, 
  Server, 
  ShieldCheck, 
  Sparkles, 
  Activity, 
  CheckCircle2, 
  Code2,
  Terminal,
  FileCheck
} from 'lucide-react';
import api from '../services/api';

const ArchitectureView = () => {
  const [archInfo, setArchInfo] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadArchitecture();
  }, []);

  const loadArchitecture = async () => {
    setLoading(true);
    try {
      const data = await api.getArchitectureInfo();
      setArchInfo(data);
    } catch (err) {
      console.error('Failed to load architecture info', err);
    } finally {
      setLoading(false);
    }
  };

  const pipelineStages = [
    { num: '01', title: 'Document Ingestion Layer', desc: 'Parses binary PDFs via Apache PDFBox, DOCX via Apache POI, and plain text with UTF-8 character normalization.', icon: FileCheck },
    { num: '02', title: 'Section Chunking & NLP', desc: 'Segments resume into semantic sections (Summary, Experience, Education, Skills, Projects) using header boundary heuristics.', icon: Layers },
    { num: '03', title: 'Canonical Entity Normalization', desc: 'Maps text variants (e.g. "AWS", "Amazon Web Services", "Amazon AWS") to singular canonical entities with category tagging.', icon: Code2 },
    { num: '04', title: 'Vector Embedding Generation', desc: 'Generates 1536-dimensional L2-normalized dense embeddings via Spring AI, caching hashes to prevent redundant recalculation.', icon: Cpu },
    { num: '05', title: 'PGVector Database Storage', desc: 'Persists document embeddings and section vectors into PostgreSQL with cosine distance (<=>) indexing.', icon: Database },
    { num: '06', title: 'Multi-Stage Matching Engine', desc: 'Executes exact string equality, normalized canonical comparison, alias resolution, and dense cosine similarity.', icon: Activity },
    { num: '07', title: 'Skill-Gap & Domain Analysis', desc: 'Partitions requirements into Matched vs Gaps, evaluates severity (Critical vs Preferred), and identifies adjacent evidence.', icon: ShieldCheck },
    { num: '08', title: 'RAG Context Assembly', desc: 'Retrieves relevant resume chunks and evidence matching the target role without blindly stuffing raw documents into the prompt.', icon: Terminal },
    { num: '09', title: 'Grounded Recommendation Synthesis', desc: 'Generates structured, verifiable resume improvements, project architectures, and learning roadmaps with zero hallucination.', icon: Sparkles }
  ];

  return (
    <div className="container py-4">
      {/* Header */}
      <div className="mb-4">
        <h1 className="h3 fw-bold mb-1">System Architecture & Engineering Design</h1>
        <p className="text-secondary small mb-0">
          In-depth technical specifications of the AI Resume Intelligence Platform backend, RAG pipeline, and vector storage.
        </p>
      </div>

      {/* Live System Specs Card */}
      <div className="glass-panel p-4 mb-5" style={{ borderLeft: '4px solid #3b82f6' }}>
        <div className="d-flex align-items-center justify-content-between mb-3">
          <div className="d-flex align-items-center gap-2">
            <Server size={20} className="text-primary" />
            <h2 className="h6 fw-bold mb-0">Live Backend Runtime & Vector Specifications</h2>
          </div>
          <span className="badge-tag exact">Production Profile</span>
        </div>

        <div className="row g-3">
          <div className="col-sm-6 col-md-3">
            <div className="p-3 rounded bg-tertiary">
              <div className="stat-label">Java / Spring Boot</div>
              <div className="h6 fw-bold mb-0 text-primary">
                Java {archInfo?.javaVersion ? archInfo.javaVersion.substring(0, 4) : '21'} • Boot {archInfo?.springBootVersion || '3.4.1'}
              </div>
            </div>
          </div>

          <div className="col-sm-6 col-md-3">
            <div className="p-3 rounded bg-tertiary">
              <div className="stat-label">Active Embedding Engine</div>
              <div className="h6 fw-bold mb-0 text-success text-truncate">
                {archInfo?.activeEmbeddingModel || '1536-dim Semantic Model'}
              </div>
            </div>
          </div>

          <div className="col-sm-6 col-md-3">
            <div className="p-3 rounded bg-tertiary">
              <div className="stat-label">Vector Dimension & Store</div>
              <div className="h6 fw-bold mb-0 text-info">
                {archInfo?.vectorDimensions || 1536} Dims • PGVector
              </div>
            </div>
          </div>

          <div className="col-sm-6 col-md-3">
            <div className="p-3 rounded bg-tertiary">
              <div className="stat-label">JVM Memory Usage</div>
              <div className="h6 fw-bold mb-0 text-warning">
                {archInfo?.systemMetrics?.heapUsedMb || 128} MB / {archInfo?.systemMetrics?.heapMaxMb || 2048} MB
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* End-to-End RAG Pipeline Diagram */}
      <div className="mb-5">
        <div className="mb-4">
          <span className="badge-tag normalized mb-2">Architectural Blueprint</span>
          <h2 className="h4 fw-bold">9-Stage End-to-End RAG & Ingestion Pipeline</h2>
          <p className="text-secondary small mb-0">Sequential data transformations from raw document upload to explainable AI feedback.</p>
        </div>

        <div className="row g-3">
          {pipelineStages.map((stage) => {
            const Icon = stage.icon;
            return (
              <div key={stage.num} className="col-md-6 col-lg-4">
                <div className="arch-node h-100 d-flex flex-column">
                  <div className="d-flex align-items-center justify-content-between mb-2">
                    <span className="font-monospace text-primary fw-bold" style={{ fontSize: '0.9rem' }}>
                      STAGE {stage.num}
                    </span>
                    <Icon size={18} className="text-secondary" />
                  </div>
                  <h3 className="h6 fw-bold mb-2">{stage.title}</h3>
                  <p className="text-secondary small mb-0 flex-grow-1" style={{ fontSize: '0.8rem' }}>
                    {stage.desc}
                  </p>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Technical Interview Deep Dive Topics */}
      <div className="glass-panel p-4">
        <h3 className="h5 fw-bold mb-3 d-flex align-items-center gap-2">
          <Sparkles size={18} className="text-primary" />
          Technical Highlights & Engineering Interview Trade-offs
        </h3>
        
        <div className="row g-4">
          <div className="col-md-6">
            <div className="p-3 rounded bg-tertiary h-100">
              <h4 className="h6 fw-bold text-success mb-2">1. Why PGVector over Separate Vector DBs?</h4>
              <p className="text-secondary small mb-0">
                Instead of running a separate vector database cluster (e.g. Pinecone or Milvus) requiring dual-write distributed transactions,
                PGVector allows relational entity foreign keys (Resumes, Sections, Jobs, Canonical Skills) and high-dimensional vector embeddings 
                to reside in the same ACID-compliant PostgreSQL database with unified backups and transactional integrity.
              </p>
            </div>
          </div>

          <div className="col-md-6">
            <div className="p-3 rounded bg-tertiary h-100">
              <h4 className="h6 fw-bold text-primary mb-2">2. Multi-Stage Matching vs Naive Equality</h4>
              <p className="text-secondary small mb-0">
                A simple <code>resumeSkill.equals(jobSkill)</code> fails in real-world scenarios where resumes write "Amazon Web Services" and job descriptions specify "AWS".
                The platform executes a 4-tier pipeline: Exact Match (1.00) ➔ Normalized Match (0.98) ➔ Canonical Alias Mapping (0.95) ➔ PGVector Cosine Distance (0.65-0.94).
              </p>
            </div>
          </div>

          <div className="col-md-6">
            <div className="p-3 rounded bg-tertiary h-100">
              <h4 className="h6 fw-bold text-warning mb-2">3. Zero-Hallucination Grounded Recommendations</h4>
              <p className="text-secondary small mb-0">
                Rather than blind prompt stuffing, RAG retrieval queries the vector index for specific resume experience chunks matching each identified requirement.
                The recommendation engine strictly verifies existing candidate evidence before offering actionable resume bullet optimizations.
              </p>
            </div>
          </div>

          <div className="col-md-6">
            <div className="p-3 rounded bg-tertiary h-100">
              <h4 className="h6 fw-bold text-info mb-2">4. Version-Controlled Schema with Flyway</h4>
              <p className="text-secondary small mb-0">
                Never relying on Hibernate <code>ddl-auto: create</code> in production. Schema evolution is managed via Flyway versioned SQL scripts
                (<code>V1__init_existing_schema.sql</code>, <code>V2__create_resume_intelligence_schema.sql</code>, <code>V3__seed_canonical_skills.sql</code>)
                ensuring predictable, repeatable database state across environments.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ArchitectureView;
