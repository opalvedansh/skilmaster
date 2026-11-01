import api from "./api";

const get = (url, params) => api.get(url, { params }).then((r) => r.data);
const post = (url, body) => api.post(url, body).then((r) => r.data);
const put = (url, body) => api.put(url, body).then((r) => r.data);
const del = (url) => api.delete(url).then((r) => r.data);

export const authApi = {
  register: (body) => post("/api/auth/register", body),
  login: (body) => post("/api/auth/login", body),
  logout: () => post("/api/auth/logout"),
  me: () => get("/api/auth/me"),
};
export const usersApi = {
  list: (params) => get("/api/users", params),
  create: (body) => post("/api/users", body),
  update: (id, body) => put("/api/users/" + id, body),
  remove: (id) => del("/api/users/" + id),
};
export const subjectsApi = {
  list: () => get("/api/subjects"),
  get: (id) => get("/api/subjects/" + id),
  skills: (id) => get("/api/subjects/" + id + "/skills"),
  create: (body) => post("/api/subjects", body),
  update: (id, body) => put("/api/subjects/" + id, body),
  remove: (id) => del("/api/subjects/" + id),
};
export const skillsApi = {
  list: (params) => get("/api/skills", params),
  create: (body) => post("/api/skills", body),
  update: (id, body) => put("/api/skills/" + id, body),
  remove: (id) => del("/api/skills/" + id),
};
export const questionsApi = {
  list: (params) => get("/api/questions", params),
  get: (id) => get("/api/questions/" + id),
  create: (body) => post("/api/questions", body),
  update: (id, body) => put("/api/questions/" + id, body),
  remove: (id) => del("/api/questions/" + id),
};
export const progressApi = {
  mine: () => get("/api/progress/me"),
  setStatus: (skillId, status) => post("/api/progress", { skillId, status }),
};
export const attemptsApi = {
  mine: () => get("/api/attempts/me"),
  forQuestion: (id) => get("/api/attempts/me/question/" + id),
  record: (questionId, status) => post("/api/attempts", { questionId, status }),
};
export const projectsApi = {
  mine: () => get("/api/projects/me"),
  create: (body) => post("/api/projects", body),
  update: (id, body) => put("/api/projects/" + id, body),
  remove: (id) => del("/api/projects/" + id),
};
export const dashboardApi = {
  me: () => get("/api/dashboard/me"),
  progress: () => get("/api/dashboard/me/progress"),
  readiness: () => get("/api/dashboard/me/readiness"),
  admin: () => get("/api/dashboard/admin"),
};
