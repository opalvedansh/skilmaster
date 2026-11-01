import axios from "axios";

// Shared Axios client. Auth uses an HttpOnly cookie set by the backend, so no token is ever stored in JS.
// X-Requested-With is the CSRF header the backend requires on every state-changing request.
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
  withCredentials: true,
  timeout: 15000,
  headers: { "X-Requested-With": "XMLHttpRequest" },
});

let unauthorizedHandler = null;
export const setUnauthorizedHandler = (fn) => { unauthorizedHandler = fn; };

api.interceptors.response.use(
  (res) => res,
  (err) => {
    const url = err.config?.url || "";
    // Expired/invalid session on a protected call: let the auth context clear state and redirect to login.
    if (err.response?.status === 401 && !url.startsWith("/api/auth/") && unauthorizedHandler) unauthorizedHandler();
    return Promise.reject(err);
  }
);

export function errorMessage(err) {
  if (err?.response) {
    const d = err.response.data;
    if (d?.fieldErrors) return Object.values(d.fieldErrors).join(". ");
    if (d?.message) return d.message;
    return "Request failed (" + err.response.status + ")";
  }
  if (err?.code === "ECONNABORTED") return "The server took too long to respond. Please try again.";
  if (err?.request) return "Cannot reach the server. Check your connection and that the backend is running.";
  return err?.message || "Something went wrong";
}

export function fieldErrors(err) { return err?.response?.data?.fieldErrors || {}; }

export default api;
