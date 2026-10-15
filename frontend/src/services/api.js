import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL !== undefined && import.meta.env.VITE_API_BASE_URL !== ''
  ? import.meta.env.VITE_API_BASE_URL
  : (import.meta.env.PROD ? '' : 'http://localhost:8080');

const client = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor: attach bearer token from localStorage
client.interceptors.request.use((config) => {
  const token = localStorage.getItem('auth_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, (error) => Promise.reject(error));

// Response interceptor: auto-logout on unauthorized
client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('auth_token');
      localStorage.removeItem('auth_user');
      window.dispatchEvent(new Event('auth_changed'));
    }
    return Promise.reject(error);
  }
);

export const api = {
  // Authentication & Session
  login: async (email, password) => {
    const res = await client.post('/api/auth/login', { email, password });
    if (res.data && res.data.token) {
      localStorage.setItem('auth_token', res.data.token);
      localStorage.setItem('auth_user', JSON.stringify(res.data.user));
      window.dispatchEvent(new Event('auth_changed'));
    }
    return res.data;
  },

  register: async (name, email, password) => {
    const res = await client.post('/api/auth/register', { name, email, password });
    if (res.data && res.data.token) {
      localStorage.setItem('auth_token', res.data.token);
      localStorage.setItem('auth_user', JSON.stringify(res.data.user));
      window.dispatchEvent(new Event('auth_changed'));
    }
    return res.data;
  },

  getCurrentUser: async () => {
    const res = await client.get('/api/auth/me');
    return res.data;
  },

  logout: async () => {
    try {
      await client.post('/api/auth/logout');
    } catch {
      // ignore
    } finally {
      localStorage.removeItem('auth_token');
      localStorage.removeItem('auth_user');
      window.dispatchEvent(new Event('auth_changed'));
    }
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
