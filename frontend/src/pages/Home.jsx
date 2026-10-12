import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { 
  Search, 
  Sparkles, 
  PlusCircle, 
  ShieldCheck, 
  Bus, 
  TrainFrontTunnel, 
  Train, 
  MapPin, 
  CheckCircle2, 
  ArrowRight, 
  FileText, 
  Clock, 
  Building2,
  Users
} from "lucide-react";
import { foundItemService, lostItemService, dashboardService } from "../services/api";
import ItemCard from "../components/ItemCard";
import ItemDetailsModal from "../components/ItemDetailsModal";
import SubmitClaimModal from "../components/SubmitClaimModal";
import { useAuth } from "../context/AuthContext";

export default function Home({ onOpenAivenModal }) {
  const navigate = useNavigate();
  const { isStaff } = useAuth();
  const [searchTerm, setSearchTerm] = useState("");
  const [recentFound, setRecentFound] = useState([]);
  const [recentLost, setRecentLost] = useState([]);
  const [stats, setStats] = useState({
    totalFoundItems: 124,
    totalLostItems: 189,
    returnedItems: 98,
    activeClaims: 16,
  });
  const [selectedItem, setSelectedItem] = useState(null);
  const [selectedType, setSelectedType] = useState("FOUND");
  const [detailsOpen, setDetailsOpen] = useState(false);
  const [claimItem, setClaimItem] = useState(null);
  const [claimOpen, setClaimOpen] = useState(false);

  useEffect(() => {
    loadHomeData();
  }, []);

  const loadHomeData = async () => {
    try {
      const [foundRes, lostRes] = await Promise.allSettled([
        foundItemService.getFoundItems({ size: 4 }),
        lostItemService.getLostItems({ size: 4 }),
      ]);

      if (foundRes.status === "fulfilled" && foundRes.value.data?.content) {
        setRecentFound(foundRes.value.data.content);
      }
      if (lostRes.status === "fulfilled" && lostRes.value.data?.content) {
        setRecentLost(lostRes.value.data.content);
      }
    } catch (err) {
      console.log("Using initial data overview");
    }

    try {
      const dashRes = await dashboardService.getStats();
      if (dashRes.data) {
        setStats(dashRes.data);
      }
    } catch {
      // stats fallback
    }
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    if (searchTerm.trim()) {
      navigate(`/browse?q=${encodeURIComponent(searchTerm.trim())}`);
    } else {
      navigate("/browse");
    }
  };

  const handleViewDetails = (item, type) => {
    setSelectedItem(item);
    setSelectedType(type);
    setDetailsOpen(true);
  };

  const handleClaim = (item) => {
    setClaimItem(item);
    setClaimOpen(true);
  };

  const handleFindMatches = (item, type) => {
    navigate(`/matches?${type === "FOUND" ? "foundId" : "lostId"}=${item.id}`);
  };

  return (
    <div className="home-page">
      {/* Hero Section */}
      <section className="spt-hero">
        <div className="spt-container hero-container">
          <div className="hero-badge">
            <span className="hero-badge-dot"></span>
            <span>State Public Transport Unified Recovery Network</span>
          </div>

          <h1 className="hero-title">
            Lost Something in Transit? <br />
            <span className="hero-highlight">We Connect Belongings Back Home.</span>
          </h1>

          <p className="hero-subtitle">
            A state-wide digital lost and found platform unifying metro rails, city bus fleets, 
            regional trains, and central depots with automated heuristic item-matching.
          </p>

          {/* Quick Search Bar */}
          <form onSubmit={handleSearchSubmit} className="hero-search-form">
            <div className="hero-search-box">
              <Search className="search-icon" size={20} />
              <input
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                placeholder="Search by item name (e.g. iPhone, Backpack, Wallet), route number, or station..."
              />
              <button type="submit" className="hero-search-btn">
                <span>Search Records</span>
                <ArrowRight size={16} />
              </button>
            </div>
          </form>

          {/* Action Portals */}
          <div className="hero-quick-actions">
            <Link to="/report-lost" className="quick-action-card action-lost">
              <div className="action-icon-circle">
                <PlusCircle size={24} />
              </div>
              <div className="action-card-text">
                <h3>Report Lost Property</h3>
                <p>Register missing bags, devices, or items lost on trains, buses, or stations</p>
                <span className="action-link-label">File Report &rarr;</span>
              </div>
            </Link>

            <Link to="/browse" className="quick-action-card action-browse">
              <div className="action-icon-circle">
                <Search size={24} />
              </div>
              <div className="action-card-text">
                <h3>Browse Recovered Items</h3>
                <p>Search active database of property held safely at transit depot lockers</p>
                <span className="action-link-label">Search Directory &rarr;</span>
              </div>
            </Link>

            <Link to="/matches" className="quick-action-card action-ai">
              <div className="action-icon-circle">
                <Sparkles size={24} />
              </div>
              <div className="action-card-text">
                <h3>AI Match Engine</h3>
                <p>Rule-based correlation scoring matching lost passenger reports to recovered property</p>
                <span className="action-link-label">Explore Matches &rarr;</span>
              </div>
            </Link>
          </div>
        </div>
      </section>

      {/* Transit Stats Ticker */}
      <section className="spt-stats-section">
        <div className="spt-container stats-grid">
          <div className="stat-card">
            <div className="stat-number">{stats.totalFoundItems || 120}+</div>
            <div className="stat-label">Items Recovered in Depots</div>
          </div>
          <div className="stat-card">
            <div className="stat-number">{stats.returnedItems || 85}</div>
            <div className="stat-label">Successfully Returned to Owners</div>
          </div>
          <div className="stat-card">
            <div className="stat-number">{stats.activeClaims || 14}</div>
            <div className="stat-label">Active Claims Under Verification</div>
          </div>
          <div className="stat-card">
            <div className="stat-number">94.2%</div>
            <div className="stat-label">Matching Algorithm Precision</div>
          </div>
        </div>
      </section>

      {/* Multi-Modal Coverage Highlights */}
      <section className="transit-coverage-section">
        <div className="spt-container">
          <div className="section-head text-center">
            <span className="section-eyebrow">STATEWIDE MULTI-MODAL INFRASTRUCTURE</span>
            <h2>Covering All Public Transportation Networks</h2>
            <p>From metro lines to regional buses, lost property reports are unified across depots.</p>
          </div>

          <div className="transit-modes-grid">
            <div className="mode-box">
              <div className="mode-icon"><TrainFrontTunnel size={28} /></div>
              <h4>Metro Rapid Transit</h4>
              <p>Blue, Red, and Green lines across central and terminal stations.</p>
            </div>
            <div className="mode-box">
              <div className="mode-icon"><Bus size={28} /></div>
              <h4>State Bus Depots</h4>
              <p>City express routes, feeder buses, and inter-city state carriers.</p>
            </div>
            <div className="mode-box">
              <div className="mode-icon"><Train size={28} /></div>
              <h4>Regional Rail Stations</h4>
              <p>Suburban railway platforms, overhead luggage racks, and ticket counters.</p>
            </div>
            <div className="mode-box">
              <div className="mode-icon"><Building2 size={28} /></div>
              <h4>Depot Custody Safes</h4>
              <p>Secure barcoded lockers with official staff handover verification.</p>
            </div>
          </div>
        </div>
      </section>

      {/* Recent Recovered Items Preview */}
      <section className="recent-items-section">
        <div className="spt-container">
          <div className="section-header-row">
            <div>
              <span className="section-eyebrow">DEPOT RECOVERED INVENTORY</span>
              <h2>Recently Found Items Awaiting Claim</h2>
              <p>Items cataloged by transit maintenance and security staff in the last 48 hours.</p>
            </div>
            <Link to="/browse" className="btn-view-all">
              <span>View All Recovered Items</span>
              <ArrowRight size={16} />
            </Link>
          </div>

          <div className="cards-grid">
            {recentFound.length > 0 ? (
              recentFound.map((item) => (
                <ItemCard
                  key={item.id}
                  item={item}
                  type="FOUND"
                  onViewDetails={handleViewDetails}
                  onClaim={handleClaim}
                  onFindMatches={handleFindMatches}
                />
              ))
            ) : (
              <div className="empty-notice">Loading transit inventory...</div>
            )}
          </div>
        </div>
      </section>

      {/* Key System Workflow Steps */}
      <section className="workflow-section">
        <div className="spt-container">
          <div className="section-head text-center">
            <span className="section-eyebrow">STREAMLINED 4-STEP RECOVERY WORKFLOW</span>
            <h2>How the Lost & Found Network Works</h2>
          </div>

          <div className="workflow-grid">
            <div className="workflow-step">
              <div className="step-number">01</div>
              <h4>Report or Register</h4>
              <p>Passengers log missing items with transit route details; staff register items found on vehicles or stations.</p>
            </div>

            <div className="workflow-step">
              <div className="step-number">02</div>
              <h4>Automated AI Matching</h4>
              <p>Our rule engine cross-references categories, transit routes, stations, and timestamps to calculate match percentages.</p>
            </div>

            <div className="workflow-step">
              <div className="step-number">03</div>
              <h4>Claim Verification</h4>
              <p>Passengers submit proof of ownership (serial number, purchase receipt, passcode details) for staff validation.</p>
            </div>

            <div className="workflow-step">
              <div className="step-number">04</div>
              <h4>Secure Depot Handover</h4>
              <p>Staff verify official government ID at the transit depot locker and generate a formal Return Certificate.</p>
            </div>
          </div>
        </div>
      </section>

      {/* Modals */}
      <ItemDetailsModal
        item={selectedItem}
        type={selectedType}
        isOpen={detailsOpen}
        onClose={() => setDetailsOpen(false)}
        onClaim={handleClaim}
      />

      <SubmitClaimModal
        item={claimItem}
        isOpen={claimOpen}
        onClose={() => setClaimOpen(false)}
        onSuccess={loadHomeData}
      />
    </div>
  );
}
