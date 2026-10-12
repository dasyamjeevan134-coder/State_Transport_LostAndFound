import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { 
  LayoutDashboard, 
  PackageSearch, 
  RotateCcw, 
  FileCheck2, 
  Sparkles, 
  TrendingUp, 
  Building2, 
  Users, 
  Bus, 
  TrainFrontTunnel, 
  Train, 
  AlertCircle,
  ShieldCheck,
  CheckCircle2
} from "lucide-react";
import { dashboardService } from "../services/api";
import { useAuth } from "../context/AuthContext";

export default function StaffDashboard() {
  const { isStaff, user } = useAuth();
  const [stats, setStats] = useState({
    totalFoundItems: 0,
    totalLostItems: 0,
    returnedItems: 0,
    activeClaims: 0,
    categoryCounts: {},
    transportCounts: {},
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (isStaff) {
      loadStats();
    }
  }, [isStaff]);

  const loadStats = async () => {
    setLoading(true);
    try {
      const res = await dashboardService.getStats();
      if (res.data) {
        setStats(res.data);
      }
    } catch (err) {
      console.error("Dashboard stats error:", err);
    } finally {
      setLoading(false);
    }
  };

  if (!isStaff) {
    return (
      <div className="spt-page dashboard-page">
        <div className="spt-container">
          <div className="auth-alert-notice staff-alert">
            <AlertCircle size={24} />
            <div>
              <h3>Staff Clearance Required</h3>
              <p>Operational Dashboard access is restricted to Transit Staff and System Administrators.</p>
              <Link to="/login" className="btn-primary mt-3">Sign in as Staff</Link>
            </div>
          </div>
        </div>
      </div>
    );
  }

  const recoveryRate = stats.totalFoundItems > 0
    ? Math.round((stats.returnedItems / stats.totalFoundItems) * 100)
    : 0;

  return (
    <div className="spt-page dashboard-page">
      <div className="spt-container">
        {/* Header */}
        <div className="dash-header">
          <div>
            <div className="header-badge">
              <ShieldCheck size={16} />
              <span>TRANSIT AUTHORITY COMMAND CENTER</span>
            </div>
            <h1>Operational Metrics & Analytics</h1>
            <p>
              Real-time operational monitoring of depot custody, lost reports, matching algorithms, and return rates.
            </p>
          </div>

          <div className="officer-badge">
            <span className="officer-role">{user?.role}</span>
            <span className="officer-name">{user?.fullName}</span>
          </div>
        </div>

        {/* Big KPI Numbers */}
        <div className="kpi-grid">
          <div className="kpi-card kpi-blue">
            <div className="kpi-icon-box">
              <PackageSearch size={24} />
            </div>
            <div className="kpi-content">
              <span className="kpi-value">{stats.totalFoundItems}</span>
              <span className="kpi-title">Items Recovered in Depots</span>
              <span className="kpi-sub">Across all state transit vehicles & depots</span>
            </div>
          </div>

          <div className="kpi-card kpi-orange">
            <div className="kpi-icon-box">
              <Users size={24} />
            </div>
            <div className="kpi-content">
              <span className="kpi-value">{stats.totalLostItems}</span>
              <span className="kpi-title">Passenger Missing Reports</span>
              <span className="kpi-sub">Active public queries registered</span>
            </div>
          </div>

          <div className="kpi-card kpi-green">
            <div className="kpi-icon-box">
              <RotateCcw size={24} />
            </div>
            <div className="kpi-content">
              <span className="kpi-value">{stats.returnedItems}</span>
              <span className="kpi-title">Successfully Returned</span>
              <span className="kpi-sub">Full chain of custody executed</span>
            </div>
          </div>

          <div className="kpi-card kpi-purple">
            <div className="kpi-icon-box">
              <FileCheck2 size={24} />
            </div>
            <div className="kpi-content">
              <span className="kpi-value">{stats.activeClaims}</span>
              <span className="kpi-title">Active Claims in Queue</span>
              <span className="kpi-sub">Awaiting staff ownership review</span>
            </div>
          </div>
        </div>

        {/* Secondary Operational Stats */}
        <div className="dash-two-cols">
          {/* Recovery Rate Card */}
          <div className="dash-card">
            <h3>Custody Resolution & Return Rate</h3>
            <p className="card-sub">Percentage of depot items safely restored to legitimate passengers.</p>
            
            <div className="recovery-progress-container">
              <div className="progress-radial-text">
                <span className="pct-num">{recoveryRate}%</span>
                <span className="pct-label">Successful Restorations</span>
              </div>
              <div className="progress-bar-bg">
                <div 
                  className="progress-bar-fill" 
                  style={{ width: `${Math.min(100, Math.max(10, recoveryRate))}%` }}
                ></div>
              </div>
            </div>

            <div className="dash-quick-links">
              <Link to="/claims" className="dash-link-btn">
                <span>Inspect Pending Claims</span>
                <FileCheck2 size={16} />
              </Link>
              <Link to="/handovers" className="dash-link-btn">
                <span>View Handover Registry</span>
                <RotateCcw size={16} />
              </Link>
            </div>
          </div>

          {/* Quick Staff Actions */}
          <div className="dash-card">
            <h3>Depot Officer Quick Operations</h3>
            <p className="card-sub">Rapid shortcuts for daily transit property duties.</p>

            <div className="dash-shortcuts-grid">
              <Link to="/register-found" className="shortcut-tile">
                <div className="tile-icon"><PackageSearch size={22} /></div>
                <div className="tile-info">
                  <strong>Register New Found Item</strong>
                  <span>Catalog property found on bus, train or station</span>
                </div>
              </Link>

              <Link to="/matches" className="shortcut-tile">
                <div className="tile-icon"><Sparkles size={22} /></div>
                <div className="tile-info">
                  <strong>AI Match Engine</strong>
                  <span>Cross-match candidate reports</span>
                </div>
              </Link>

              <Link to="/claims" className="shortcut-tile">
                <div className="tile-icon"><CheckCircle2 size={22} /></div>
                <div className="tile-info">
                  <strong>Claims Verification Workbench</strong>
                  <span>Approve passenger ownership proofs</span>
                </div>
              </Link>

              <Link to="/handovers" className="shortcut-tile">
                <div className="tile-icon"><RotateCcw size={22} /></div>
                <div className="tile-info">
                  <strong>Issue Handover Receipt</strong>
                  <span>Release item & log return certificate</span>
                </div>
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
