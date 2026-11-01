import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { authApi } from "../services";
import { setUnauthorizedHandler } from "../services/api";

export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [sessionExpired, setSessionExpired] = useState(false);

  // Restore the session from the HttpOnly cookie (the browser sends it; JS never sees the token).
  useEffect(() => {
    let active = true;
    authApi.me().then((u) => active && setUser(u)).catch(() => {}).finally(() => active && setLoading(false));
    return () => { active = false; };
  }, []);

  useEffect(() => {
    setUnauthorizedHandler(() => { setUser((u) => { if (u) setSessionExpired(true); return null; }); });
    return () => setUnauthorizedHandler(null);
  }, []);

  const login = useCallback(async (email, password) => {
    const u = await authApi.login({ email, password });
    setSessionExpired(false); setUser(u); return u;
  }, []);
  const register = useCallback(async (payload) => {
    const u = await authApi.register(payload);
    setSessionExpired(false); setUser(u); return u;
  }, []);
  const logout = useCallback(async () => {
    try { await authApi.logout(); } catch { /* cookie may already be gone */ }
    setUser(null);
  }, []);

  const value = useMemo(() => ({
    user, setUser, loading, sessionExpired, login, register, logout,
    isAdmin: user?.role === "ADMIN", isAuthenticated: !!user,
  }), [user, loading, sessionExpired, login, register, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
