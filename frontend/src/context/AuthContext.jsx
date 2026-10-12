import { createContext, useContext, useState, useEffect } from "react";
import { authService } from "../services/api";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem("user");
    try {
      return saved ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  });

  const [token, setToken] = useState(() => localStorage.getItem("token") || null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (token && !user) {
      // Attempt to load current user profile
      authService
        .getCurrentUser()
        .then((res) => {
          if (res.data) {
            setUser(res.data);
            localStorage.setItem("user", JSON.stringify(res.data));
          }
        })
        .catch(() => {
          logout();
        });
    }
  }, [token]);

  const login = async (email, password) => {
    setLoading(true);
    try {
      const res = await authService.login({ email, password });
      const data = res.data || res;
      const authToken = data.token || data.accessToken;
      const userData = data.user || {
        id: data.id,
        fullName: data.fullName,
        email: data.email,
        role: data.role,
        phone: data.phone,
      };

      if (!authToken) {
        throw new Error("No token returned by server");
      }

      setToken(authToken);
      setUser(userData);
      localStorage.setItem("token", authToken);
      localStorage.setItem("user", JSON.stringify(userData));
      return userData;
    } finally {
      setLoading(false);
    }
  };

  const register = async (userData) => {
    setLoading(true);
    try {
      const res = await authService.register(userData);
      const data = res.data || res;
      const authToken = data.token || data.accessToken;
      const userObj = data.user || {
        id: data.id,
        fullName: data.fullName,
        email: data.email,
        role: data.role,
        phone: data.phone,
      };

      if (authToken) {
        setToken(authToken);
        setUser(userObj);
        localStorage.setItem("token", authToken);
        localStorage.setItem("user", JSON.stringify(userObj));
      }
      return data;
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem("token");
    localStorage.removeItem("user");
  };

  // Quick switch credentials helper for evaluation
  const demoAccounts = {
    ADMIN: { email: "admin@transport.gov", password: "Admin@12345", name: "System Administrator" },
    STAFF: { email: "staff@transport.gov", password: "Staff@12345", name: "Officer Vance" },
    PASSENGER: { email: "passenger@transport.gov", password: "Pass@12345", name: "Elena Rostova" },
  };

  const quickDemoLogin = async (roleName) => {
    const creds = demoAccounts[roleName];
    if (creds) {
      return await login(creds.email, creds.password);
    }
  };

  const role = user?.role || "GUEST";
  const isAuthenticated = !!token && !!user;
  const isStaff = role === "STAFF" || role === "ADMIN";
  const isAdmin = role === "ADMIN";
  const isPassenger = role === "PASSENGER";

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        role,
        isAuthenticated,
        isStaff,
        isAdmin,
        isPassenger,
        login,
        register,
        logout,
        quickDemoLogin,
        demoAccounts,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
