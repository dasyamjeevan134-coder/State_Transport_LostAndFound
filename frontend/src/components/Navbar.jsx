import { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { 
  Compass, 
  Search, 
  PlusCircle, 
  Sparkles, 
  FileCheck2, 
  RotateCcw, 
  LayoutDashboard, 
  LogIn, 
  LogOut, 
  UserCircle2, 
  Database,
  Menu,
  X
} from "lucide-react";

export default function Navbar({ onOpenAivenModal }) {
  const { user, isAuthenticated, isStaff, isAdmin, isPassenger, logout, quickDemoLogin, demoAccounts } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [demoMenuOpen, setDemoMenuOpen] = useState(false);

  const isActive = (path) => location.pathname === path;

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  const handleQuickLogin = async (roleName) => {
    try {
      await quickDemoLogin(roleName);
      setDemoMenuOpen(false);
      navigate(roleName === "STAFF" || roleName === "ADMIN" ? "/dashboard" : "/browse");
    } catch (err) {
      alert("Failed to login as " + roleName + ". Please ensure Spring Boot backend is running.");
    }
  };

  return (
    <header className="spt-header">
      {/* Top Government Portal Bar */}
      <div className="spt-top-bar">
        <div className="spt-container top-bar-content">
          <div className="spt-top-left">
            <span className="gov-seal">🏛️ OFFICIAL STATE TRANSIT PORTAL</span>
            <span className="gov-divider">•</span>
            <span className="gov-sub">Department of Transport & Rapid Transit Infrastructure</span>
          </div>
          <div className="spt-top-right">
            <button 
              className="aiven-db-btn"
              onClick={onOpenAivenModal}
              title="Aiven Cloud MySQL Connection Info"
            >
              <Database size={13} />
              <span>Aiven MySQL Console</span>
              <span className="status-dot"></span>
            </button>
            <div className="demo-switch-wrapper">
              <button 
                className="demo-switch-btn"
                onClick={() => setDemoMenuOpen(!demoMenuOpen)}
              >
                <Sparkles size={13} />
                <span>Quick Demo Role ▾</span>
              </button>
              {demoMenuOpen && (
                <div className="demo-dropdown">
                  <div className="demo-dropdown-title">Instant Role Switcher</div>
                  <button onClick={() => handleQuickLogin("PASSENGER")}>
                    👤 Elena Rostova (Passenger)
                  </button>
                  <button onClick={() => handleQuickLogin("STAFF")}>
                    🛡️ Officer Vance (Depot Staff)
                  </button>
                  <button onClick={() => handleQuickLogin("ADMIN")}>
                    ⚡ System Administrator
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Main Navigation Bar */}
      <nav className="spt-nav">
        <div className="spt-container nav-content">
          <Link to="/" className="spt-brand">
            <div className="spt-logo-icon">
              <Compass size={24} />
            </div>
            <div className="spt-brand-text">
              <span className="spt-brand-main">State Transit</span>
              <span className="spt-brand-sub">LOST & FOUND SYSTEM</span>
            </div>
          </Link>

          <button 
            className="mobile-toggle"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            aria-label="Toggle navigation"
          >
            {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
          </button>

          <div className={`nav-links-wrapper ${mobileMenuOpen ? "open" : ""}`}>
            <div className="spt-nav-links">
              <Link 
                to="/" 
                className={`nav-link ${isActive("/") ? "active" : ""}`}
                onClick={() => setMobileMenuOpen(false)}
              >
                Home
              </Link>

              <Link 
                to="/browse" 
                className={`nav-link ${isActive("/browse") ? "active" : ""}`}
                onClick={() => setMobileMenuOpen(false)}
              >
                <Search size={16} />
                <span>Browse Directory</span>
              </Link>

              <Link 
                to="/report-lost" 
                className={`nav-link ${isActive("/report-lost") ? "active" : ""}`}
                onClick={() => setMobileMenuOpen(false)}
              >
                <PlusCircle size={16} />
                <span>Report Lost Item</span>
              </Link>

              {isStaff && (
                <Link 
                  to="/register-found" 
                  className={`nav-link staff-link ${isActive("/register-found") ? "active" : ""}`}
                  onClick={() => setMobileMenuOpen(false)}
                >
                  <PlusCircle size={16} />
                  <span>Register Found Item</span>
                </Link>
              )}

              <Link 
                to="/matches" 
                className={`nav-link ${isActive("/matches") ? "active" : ""}`}
                onClick={() => setMobileMenuOpen(false)}
              >
                <Sparkles size={16} />
                <span>AI Match Engine</span>
              </Link>

              {isAuthenticated && (
                <Link 
                  to="/claims" 
                  className={`nav-link ${isActive("/claims") ? "active" : ""}`}
                  onClick={() => setMobileMenuOpen(false)}
                >
                  <FileCheck2 size={16} />
                  <span>{isStaff ? "Claim Verification" : "My Claims"}</span>
                </Link>
              )}

              {isStaff && (
                <Link 
                  to="/handovers" 
                  className={`nav-link staff-link ${isActive("/handovers") ? "active" : ""}`}
                  onClick={() => setMobileMenuOpen(false)}
                >
                  <RotateCcw size={16} />
                  <span>Returns & Handover</span>
                </Link>
              )}

              {isStaff && (
                <Link 
                  to="/dashboard" 
                  className={`nav-link staff-link ${isActive("/dashboard") ? "active" : ""}`}
                  onClick={() => setMobileMenuOpen(false)}
                >
                  <LayoutDashboard size={16} />
                  <span>Operations Dashboard</span>
                </Link>
              )}
            </div>

            <div className="spt-nav-auth">
              {isAuthenticated ? (
                <div className="user-profile-badge">
                  <div className="user-info">
                    <span className="user-name">{user?.fullName || "User"}</span>
                    <span className={`user-role role-${user?.role?.toLowerCase()}`}>
                      {user?.role}
                    </span>
                  </div>
                  <button 
                    onClick={handleLogout} 
                    className="spt-btn-logout"
                    title="Logout"
                  >
                    <LogOut size={16} />
                  </button>
                </div>
              ) : (
                <div className="auth-actions">
                  <Link 
                    to="/login" 
                    className="btn-login-nav"
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    <LogIn size={15} />
                    <span>Sign In</span>
                  </Link>
                  <Link 
                    to="/register" 
                    className="btn-register-nav"
                    onClick={() => setMobileMenuOpen(false)}
                  >
                    <span>Register</span>
                  </Link>
                </div>
              )}
            </div>
          </div>
        </div>
      </nav>
    </header>
  );
}
