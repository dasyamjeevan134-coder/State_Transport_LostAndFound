import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { LogIn, UserPlus, ShieldCheck, User, Sparkles, AlertCircle } from "lucide-react";
import { useAuth } from "../context/AuthContext";

export function Login() {
  const navigate = useNavigate();
  const { login, quickDemoLogin, demoAccounts } = useAuth();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const user = await login(email.trim(), password);
      if (user.role === "STAFF" || user.role === "ADMIN") {
        navigate("/dashboard");
      } else {
        navigate("/browse");
      }
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.message ||
        "Invalid email or password. Please verify your credentials or use a demo account below."
      );
    } finally {
      setLoading(false);
    }
  };

  const handleQuickFill = async (roleName) => {
    setError("");
    setLoading(true);
    try {
      const user = await quickDemoLogin(roleName);
      if (user.role === "STAFF" || user.role === "ADMIN") {
        navigate("/dashboard");
      } else {
        navigate("/browse");
      }
    } catch (err) {
      setError("Cannot connect to Spring Boot backend. Ensure backend is running on port 8080.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="spt-page auth-page">
      <div className="spt-container">
        <div className="auth-card">
          <div className="auth-card-header">
            <div className="auth-logo-circle">
              <LogIn size={24} />
            </div>
            <h2>Sign In to State Transit Portal</h2>
            <p>Access lost reports, depot recovery records, and item claims.</p>
          </div>

          {/* Quick Demo Fill Buttons for Easy Testing */}
          <div className="demo-accounts-box">
            <div className="demo-box-head">
              <Sparkles size={14} />
              <span>Instant One-Click Demo Access</span>
            </div>
            <div className="demo-btns-grid">
              <button
                type="button"
                className="btn-demo-pill demo-pass"
                onClick={() => handleQuickFill("PASSENGER")}
              >
                👤 Elena Rostova (Passenger)
              </button>
              <button
                type="button"
                className="btn-demo-pill demo-staff"
                onClick={() => handleQuickFill("STAFF")}
              >
                🛡️ Officer Vance (Depot Staff)
              </button>
              <button
                type="button"
                className="btn-demo-pill demo-admin"
                onClick={() => handleQuickFill("ADMIN")}
              >
                ⚡ System Administrator
              </button>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="auth-form">
            <div className="form-group">
              <label htmlFor="login-email">Official Email Address *</label>
              <input
                id="login-email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="passenger@transport.gov or staff@transport.gov"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="login-password">Password *</label>
              <input
                id="login-password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter your password"
                required
              />
            </div>

            {error && (
              <div className="error-alert">
                <AlertCircle size={16} />
                <span>{error}</span>
              </div>
            )}

            <button type="submit" className="btn-primary btn-submit-auth" disabled={loading}>
              {loading ? "Authenticating..." : "Sign In"}
            </button>
          </form>

          <div className="auth-footer-links">
            <p>
              Don't have an account? <Link to="/register">Create Passenger Account</Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

export function Register() {
  const navigate = useNavigate();
  const { register } = useAuth();

  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState("PASSENGER");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await register({
        fullName: fullName.trim(),
        email: email.trim(),
        phone: phone.trim(),
        password,
        role,
      });
      navigate(role === "PASSENGER" ? "/browse" : "/dashboard");
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.message ||
        "Registration failed. Email might already be registered."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="spt-page auth-page">
      <div className="spt-container">
        <div className="auth-card">
          <div className="auth-card-header">
            <div className="auth-logo-circle">
              <UserPlus size={24} />
            </div>
            <h2>Create New Account</h2>
            <p>Register as a passenger or state transport depot staff member.</p>
          </div>

          <form onSubmit={handleSubmit} className="auth-form">
            <div className="form-group">
              <label htmlFor="reg-name">Full Legal Name *</label>
              <input
                id="reg-name"
                type="text"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                placeholder="e.g. Johnathan Miller"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="reg-email">Email Address *</label>
              <input
                id="reg-email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="e.g. j.miller@example.com"
                required
              />
            </div>

            <div className="form-grid-2">
              <div className="form-group">
                <label htmlFor="reg-phone">Contact Phone *</label>
                <input
                  id="reg-phone"
                  type="tel"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  placeholder="e.g. +1 800 555 0199"
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="reg-role">Account Role *</label>
                <select id="reg-role" value={role} onChange={(e) => setRole(e.target.value)}>
                  <option value="PASSENGER">Transit Passenger</option>
                  <option value="STAFF">Depot Staff / Officer</option>
                </select>
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="reg-pass">Password (Min 6 characters) *</label>
              <input
                id="reg-pass"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Create secure password"
                minLength={6}
                required
              />
            </div>

            {error && (
              <div className="error-alert">
                <AlertCircle size={16} />
                <span>{error}</span>
              </div>
            )}

            <button type="submit" className="btn-primary btn-submit-auth" disabled={loading}>
              {loading ? "Creating Account..." : "Register Account"}
            </button>
          </form>

          <div className="auth-footer-links">
            <p>
              Already registered? <Link to="/login">Sign In</Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
