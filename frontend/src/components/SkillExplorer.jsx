import React, { useState, useEffect } from 'react';
import { Search, Sparkles, Database, Layers, ArrowRight, Code, Tag } from 'lucide-react';
import api from '../services/api';

const SkillExplorer = () => {
  const [categories, setCategories] = useState([]);
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [skills, setSkills] = useState([]);
  const [loading, setLoading] = useState(true);

  // Live interactive search state
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResult, setSearchResult] = useState(null);
  const [searching, setSearching] = useState(false);

  useEffect(() => {
    loadCategories();
    loadSkills();
  }, []);

  const loadCategories = async () => {
    try {
      const cats = await api.getSkillCategories();
      setCategories(['ALL', ...cats]);
    } catch (err) {
      console.error('Error loading categories', err);
    }
  };

  const loadSkills = async (category = selectedCategory) => {
    setLoading(true);
    try {
      const data = await api.getCanonicalSkills(category);
      setSkills(data);
    } catch (err) {
      console.error('Error loading skills', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCategoryChange = (cat) => {
    setSelectedCategory(cat);
    loadSkills(cat);
  };

  const handleLiveSearch = async (e) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;

    setSearching(true);
    try {
      const res = await api.searchSkills(searchQuery.trim());
      setSearchResult(res);
    } catch (err) {
      console.error('Search error', err);
    } finally {
      setSearching(false);
    }
  };

  const quickPicks = ['AWS', 'JS', 'Postgres', 'K8s', 'Spring Boot', 'RAG', 'Docker', 'Machine Learning'];

  return (
    <div className="container py-4">
      {/* Header */}
      <div className="mb-4">
        <h1 className="h3 fw-bold mb-1">Normalized Skill Taxonomy Explorer</h1>
        <p className="text-secondary small mb-0">
          Explore canonical technical entities, aliases, and live vector similarity projections across PostgreSQL + PGVector.
        </p>
      </div>

      {/* Live Interactive Search Box */}
      <div className="glass-panel p-4 mb-4" style={{ borderLeft: '4px solid #8b5cf6' }}>
        <div className="d-flex align-items-center gap-2 mb-2">
          <Sparkles size={20} style={{ color: '#8b5cf6' }} />
          <h2 className="h6 fw-bold mb-0">Interactive Normalization & Semantic Vector Search</h2>
        </div>
        <p className="text-secondary small mb-3">
          Type any raw acronym or technology variation to inspect how the normalization layer maps it to a canonical entity and retrieves similar vector neighbors.
        </p>

        <form onSubmit={handleLiveSearch} className="row g-2 align-items-center mb-3">
          <div className="col-md-9">
            <div className="input-group">
              <span className="input-group-text bg-tertiary border-subtle text-secondary">
                <Search size={16} />
              </span>
              <input 
                type="text" 
                className="form-control bg-tertiary text-primary border-subtle"
                placeholder="Try 'AWS', 'K8s', 'JS', 'Postgres SQL', 'RAG', 'Spring', 'CI/CD'..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
          </div>
          <div className="col-md-3">
            <button type="submit" className="btn-primary-custom w-100 py-2" disabled={searching || !searchQuery.trim()}>
              {searching ? 'Querying Vectors...' : 'Search & Normalize'}
            </button>
          </div>
        </form>

        {/* Quick Picks */}
        <div className="d-flex flex-wrap align-items-center gap-2">
          <span className="text-secondary small">Quick Test:</span>
          {quickPicks.map(p => (
            <button 
              key={p} 
              type="button" 
              className="badge-tag category border-0 cursor-pointer"
              style={{ cursor: 'pointer' }}
              onClick={() => { setSearchQuery(p); }}
            >
              {p}
            </button>
          ))}
        </div>

        {/* Live Search Output Drawer */}
        {searchResult && (
          <div className="mt-4 p-3 rounded bg-tertiary border border-subtle">
            <div className="row g-3">
              <div className="col-md-6">
                <span className="badge-tag normalized mb-2">Entity Normalization Result</span>
                <div className="h5 fw-bold mb-1">{searchResult.normalized?.canonicalName}</div>
                <div className="text-secondary small mb-2">
                  Category: <span className="fw-semibold text-primary">{searchResult.normalized?.category}</span> • Normalized Key: <code>{searchResult.normalized?.normalizedName}</code>
                </div>
                <div className="small text-secondary">
                  Match Type: <span className="badge-tag exact">{searchResult.normalized?.matchType}</span>
                </div>
              </div>

              <div className="col-md-6 border-start border-subtle">
                <span className="badge-tag semantic mb-2">PGVector Cosine Similarity Neighbors</span>
                {(!searchResult.semanticMatches || searchResult.semanticMatches.length === 0) ? (
                  <p className="text-secondary small mb-0">No adjacent vector neighbors found above threshold.</p>
                ) : (
                  <div className="d-flex flex-wrap gap-2">
                    {searchResult.semanticMatches.map(m => (
                      <span key={m.content} className="badge-tag semantic" style={{ fontSize: '0.75rem' }}>
                        {m.content} ({(m.similarityScore * 100).toFixed(0)}%)
                      </span>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Category Filter Pills */}
      <div className="d-flex flex-wrap gap-2 mb-4">
        {categories.map((cat) => (
          <button
            key={cat}
            className={`btn btn-sm ${selectedCategory === cat ? 'btn-primary' : 'btn-outline-secondary'}`}
            style={{ borderRadius: '999px', fontSize: '0.8rem', padding: '0.3rem 0.8rem' }}
            onClick={() => handleCategoryChange(cat)}
          >
            {cat}
          </button>
        ))}
      </div>

      {/* Canonical Taxonomy Grid */}
      {loading ? (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status"></div>
          <p className="text-secondary small mt-2">Loading canonical taxonomy...</p>
        </div>
      ) : skills.length === 0 ? (
        <div className="glass-panel text-center py-5">
          <p className="text-secondary mb-0">No skills found in category {selectedCategory}.</p>
        </div>
      ) : (
        <div className="row g-3">
          {skills.map((skill) => (
            <div key={skill.id || skill.canonicalName} className="col-md-6 col-lg-4">
              <div className="glass-panel p-4 h-100 d-flex flex-column">
                <div className="d-flex align-items-center justify-content-between mb-2">
                  <h3 className="h6 fw-bold mb-0 text-truncate">{skill.canonicalName}</h3>
                  <span className="badge-tag category" style={{ fontSize: '0.65rem' }}>{skill.category}</span>
                </div>
                
                <p className="text-secondary small mb-3 flex-grow-1" style={{ fontSize: '0.8rem' }}>
                  {skill.description || 'Enterprise technical competence stored in canonical knowledge base.'}
                </p>

                <div className="border-top border-subtle pt-2 mt-auto">
                  <div className="text-secondary small fw-semibold mb-1" style={{ fontSize: '0.7rem' }}>
                    Known Canonical Aliases:
                  </div>
                  <div className="d-flex flex-wrap gap-1">
                    {skill.aliases && skill.aliases.length > 0 ? (
                      skill.aliases.slice(0, 5).map((alias) => (
                        <code key={alias} className="px-1 py-0 rounded bg-tertiary" style={{ fontSize: '0.7rem' }}>
                          {alias}
                        </code>
                      ))
                    ) : (
                      <span className="text-secondary" style={{ fontSize: '0.7rem' }}>canonical</span>
                    )}
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default SkillExplorer;
