import { useState } from "react";
import { BrowserRouter, Routes, Route, Link } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import Navbar from "./components/Navbar";
import AivenConfigModal from "./components/AivenConfigModal";

import Home from "./pages/Home";
import BrowseItems from "./pages/BrowseItems";
import ReportLostItem from "./pages/ReportLostItem";
import RegisterFoundItem from "./pages/RegisterFoundItem";
import MatchesExplorer from "./pages/MatchesExplorer";
import ClaimsManager from "./pages/ClaimsManager";
import HandoverRegistry from "./pages/HandoverRegistry";
import StaffDashboard from "./pages/StaffDashboard";
import { Login, Register } from "./pages/AuthPages";

import { Compass, Database, ShieldCheck, Heart, ExternalLink } from "lucide-react";
import "./App.css";

function App() {
  const [aivenModalOpen, setAivenModalOpen] = useState(false);

  return (
    <AuthProvider>
      <BrowserRouter>
        <div className="spt-app-wrapper">
          <Navbar onOpenAivenModal={() => setAivenModalOpen(true)} />

          <main className="spt-main-content">
            <Routes>
              <Route path="/" element={<Home onOpenAivenModal={() => setAivenModalOpen(true)} />} />
              <Route path="/browse" element={<BrowseItems />} />
              <Route path="/report-lost" element={<ReportLostItem />} />
              <Route path="/register-found" element={<RegisterFoundItem />} />
              <Route path="/matches" element={<MatchesExplorer />} />
              <Route path="/claims" element={<ClaimsManager />} />
              <Route path="/handovers" element={<HandoverRegistry />} />
              <Route path="/dashboard" element={<StaffDashboard />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              {/* Fallback to Home */}
              <Route path="*" element={<Home onOpenAivenModal={() => setAivenModalOpen(true)} />} />
            </Routes>
          </main>

          {/* Official Footer */}
          <footer className="spt-footer">
            <div className="spt-container footer-content">
              <div className="footer-col brand-col">
                <div className="footer-brand">
                  <Compass size={22} />
                  <span>State Public Transport Lost & Found System</span>
                </div>
                <p>
                  Centralized multi-modal transit recovery network connecting passengers with state
                  metro rails, bus depots, and regional train terminals.
                </p>
                <div className="footer-db-status">
                  <span className="dot-pulse"></span>
                  <span>Backend: Spring Boot 3 + MySQL / Aiven Cloud Database</span>
                </div>
              </div>

              <div className="footer-col">
                <h4>Public Services</h4>
                <ul>
                  <li><Link to="/browse?tab=FOUND">Browse Recovered Items</Link></li>
                  <li><Link to="/report-lost">Report a Missing Item</Link></li>
                  <li><Link to="/matches">Automated Match Finder</Link></li>
                  <li><Link to="/claims">Track My Submitted Claims</Link></li>
                </ul>
              </div>

              <div className="footer-col">
                <h4>Depot & Staff Operations</h4>
                <ul>
                  <li><Link to="/register-found">Register Found Property</Link></li>
                  <li><Link to="/claims">Claims Verification Desk</Link></li>
                  <li><Link to="/handovers">Return & Handover Registry</Link></li>
                  <li><Link to="/dashboard">Operations Analytics</Link></li>
                </ul>
              </div>

              <div className="footer-col">
                <h4>System Architecture</h4>
                <div className="footer-tech-stack">
                  <span className="tech-badge">Java 17 Spring Boot</span>
                  <span className="tech-badge">React 19</span>
                  <span className="tech-badge">MySQL 8 / Aiven Console</span>
                  <span className="tech-badge">JWT Authentication</span>
                  <span className="tech-badge">Heuristic Matching Engine</span>
                </div>
                <button
                  type="button"
                  className="btn-aiven-footer"
                  onClick={() => setAivenModalOpen(true)}
                >
                  <Database size={14} />
                  <span>Configure Aiven MySQL</span>
                </button>
              </div>
            </div>

            <div className="footer-bottom-bar">
              <div className="spt-container bottom-bar-flex">
                <span>© 2026 State Public Transport Authority. All Rights Reserved.</span>
                <span>Centralized Lost & Found Management Platform</span>
              </div>
            </div>
          </footer>

          <AivenConfigModal
            isOpen={aivenModalOpen}
            onClose={() => setAivenModalOpen(false)}
          />
        </div>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;