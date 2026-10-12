import axios from "axios";

const API_BASE_URL = "http://localhost:8080/api";

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

// Automatically inject JWT Bearer token into all outgoing requests
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Unified response error interceptor
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      // Optional auto-logout on token expiration
      // localStorage.removeItem("token");
      // localStorage.removeItem("user");
    }
    return Promise.reject(error);
  }
);

/* ============================================================================
   AUTH SERVICES
   ============================================================================ */
export const authService = {
  login: async (credentials) => {
    const response = await api.post("/auth/login", credentials);
    return response.data;
  },
  register: async (userData) => {
    const response = await api.post("/auth/register", userData);
    return response.data;
  },
  getCurrentUser: async () => {
    const response = await api.get("/auth/me");
    return response.data;
  },
  updateProfile: async (profileData) => {
    const response = await api.put("/users/profile", profileData);
    return response.data;
  },
};

/* ============================================================================
   LOST ITEMS SERVICES
   ============================================================================ */
export const lostItemService = {
  getLostItems: async (params = {}) => {
    const response = await api.get("/lost-items", { params });
    return response.data;
  },
  getLostItemById: async (id) => {
    const response = await api.get(`/lost-items/${id}`);
    return response.data;
  },
  createLostItem: async (data) => {
    const response = await api.post("/lost-items", data);
    return response.data;
  },
  updateLostItem: async (id, data) => {
    const response = await api.put(`/lost-items/${id}`, data);
    return response.data;
  },
  deleteLostItem: async (id) => {
    const response = await api.delete(`/lost-items/${id}`);
    return response.data;
  },
  getMyLostItems: async (params = {}) => {
    const response = await api.get("/users/my-lost-items", { params });
    return response.data;
  },
  getMatchesForLostItem: async (id) => {
    const response = await api.get(`/lost-items/${id}/matches`);
    return response.data;
  },
};

/* ============================================================================
   FOUND ITEMS SERVICES
   ============================================================================ */
export const foundItemService = {
  getFoundItems: async (params = {}) => {
    const response = await api.get("/found-items", { params });
    return response.data;
  },
  getFoundItemById: async (id) => {
    const response = await api.get(`/found-items/${id}`);
    return response.data;
  },
  createFoundItem: async (data) => {
    const response = await api.post("/found-items", data);
    return response.data;
  },
  updateFoundItem: async (id, data) => {
    const response = await api.put(`/found-items/${id}`, data);
    return response.data;
  },
  deleteFoundItem: async (id) => {
    const response = await api.delete(`/found-items/${id}`);
    return response.data;
  },
  getMyFoundItems: async (params = {}) => {
    const response = await api.get("/users/my-found-items", { params });
    return response.data;
  },
  getMatchesForFoundItem: async (id) => {
    const response = await api.get(`/found-items/${id}/matches`);
    return response.data;
  },
  getClaimsForFoundItem: async (id) => {
    const response = await api.get(`/found-items/${id}/claims`);
    return response.data;
  },
};

/* ============================================================================
   CLAIM SERVICES
   ============================================================================ */
export const claimService = {
  submitClaim: async (data) => {
    const response = await api.post("/claims", data);
    return response.data;
  },
  getClaimById: async (id) => {
    const response = await api.get(`/claims/${id}`);
    return response.data;
  },
  getAllClaims: async (params = {}) => {
    const response = await api.get("/claims", { params });
    return response.data;
  },
  getMyClaims: async (params = {}) => {
    const response = await api.get("/users/my-claims", { params });
    return response.data;
  },
  reviewClaim: async (id, statusData) => {
    const response = await api.put(`/claims/${id}/status`, statusData);
    return response.data;
  },
  cancelClaim: async (id) => {
    const response = await api.post(`/claims/${id}/cancel`);
    return response.data;
  },
};

/* ============================================================================
   HANDOVER & RETURN RECORD SERVICES
   ============================================================================ */
export const returnRecordService = {
  createReturnRecord: async (data) => {
    const response = await api.post("/returns", data);
    return response.data;
  },
  getAllReturnRecords: async (params = {}) => {
    const response = await api.get("/returns", { params });
    return response.data;
  },
  getReturnRecordById: async (id) => {
    const response = await api.get(`/returns/${id}`);
    return response.data;
  },
  getByFoundItemId: async (foundItemId) => {
    const response = await api.get(`/returns/by-found-item/${foundItemId}`);
    return response.data;
  },
  getByClaimId: async (claimId) => {
    const response = await api.get(`/returns/by-claim/${claimId}`);
    return response.data;
  },
};

/* ============================================================================
   MATCHING ENGINE SERVICES
   ============================================================================ */
export const matchingService = {
  getMatchesForLostItem: async (lostItemId) => {
    const response = await api.get(`/matches/lost-item/${lostItemId}`);
    return response.data;
  },
  getMatchesForFoundItem: async (foundItemId) => {
    const response = await api.get(`/matches/found-item/${foundItemId}`);
    return response.data;
  },
};

/* ============================================================================
   DASHBOARD SERVICES
   ============================================================================ */
export const dashboardService = {
  getStats: async () => {
    const response = await api.get("/dashboard/stats");
    return response.data;
  },
};

/* ============================================================================
   FILE UPLOAD SERVICE
   ============================================================================ */
export const uploadService = {
  uploadFile: async (file) => {
    const formData = new FormData();
    formData.append("file", file);
    const response = await api.post("/uploads", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
    return response.data;
  },
};

export default api;
