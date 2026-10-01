import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080';

const client = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const api = {
  // Legacy Portfolio Resume
  getLegacyResume: async () => {
    const res = await client.get('/api/resume');
    return res.data;
  },

  // Resumes
  getResumes: async () => {
    const res = await client.get('/api/resumes');
    return res.data;
  },

  getResumeById: async (id) => {
    const res = await client.get(`/api/resumes/${id}`);
    return res.data;
  },

  uploadResumeFile: async (file) => {
    const formData = new FormData();
    formData.append('file', file);
    const res = await client.post('/api/resumes/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return res.data;
  },

  ingestResumeText: async (rawText, filename, candidateName) => {
    const res = await client.post('/api/resumes', { rawText, filename, candidateName });
    return res.data;
  },

  deleteResume: async (id) => {
    await client.delete(`/api/resumes/${id}`);
  },

  // Job Postings
  getJobs: async () => {
    const res = await client.get('/api/jobs');
    return res.data;
  },

  getJobById: async (id) => {
    const res = await client.get(`/api/jobs/${id}`);
    return res.data;
  },

  createJob: async (jobData) => {
    const res = await client.post('/api/jobs', jobData);
    return res.data;
  },

  uploadJobFile: async (file, title, company) => {
    const formData = new FormData();
    formData.append('file', file);
    if (title) formData.append('title', title);
    if (company) formData.append('company', company);
    const res = await client.post('/api/jobs/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return res.data;
  },

  deleteJob: async (id) => {
    await client.delete(`/api/jobs/${id}`);
  },

  // Analyses
  runAnalysis: async (resumeId, jobId) => {
    const res = await client.post('/api/analyses', { resumeId, jobId });
    return res.data;
  },

  getAnalyses: async () => {
    const res = await client.get('/api/analyses');
    return res.data;
  },

  getAnalysisById: async (id) => {
    const res = await client.get(`/api/analyses/${id}`);
    return res.data;
  },

  // Skills & Architecture
  getCanonicalSkills: async (category, query) => {
    const params = {};
    if (category) params.category = category;
    if (query) params.query = query;
    const res = await client.get('/api/skills/canonical', { params });
    return res.data;
  },

  getSkillCategories: async () => {
    const res = await client.get('/api/skills/categories');
    return res.data;
  },

  searchSkills: async (query) => {
    const res = await client.post('/api/search/skills', { query });
    return res.data;
  },

  getArchitectureInfo: async () => {
    const res = await client.get('/api/architecture');
    return res.data;
  },
};

export default api;
