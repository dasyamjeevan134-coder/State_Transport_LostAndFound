import { useState, useEffect } from "react";
import { 
  X, 
  MapPin, 
  Clock, 
  Building2, 
  Sparkles, 
  FileCheck2, 
  Tag, 
  AlertCircle,
  ExternalLink,
  ShieldCheck,
  User,
  ArrowRight
} from "lucide-react";
import { matchingService } from "../services/api";
import { FOUND_STATUS_BADGES, LOST_STATUS_BADGES } from "../constants/transit";

export default function ItemDetailsModal({ item, type, isOpen, onClose, onClaim, onSelectMatchItem }) {
  const [matches, setMatches] = useState([]);
  const [loadingMatches, setLoadingMatches] = useState(false);

  useEffect(() => {
    if (item && isOpen) {
      loadMatches();
    }
  }, [item, isOpen]);

  const loadMatches = async () => {
    if (!item?.id) return;
    setLoadingMatches(true);
    try {
      const res = type === "FOUND" 
        ? await matchingService.getMatchesForFoundItem(item.id)
        : await matchingService.getMatchesForLostItem(item.id);
      setMatches(res.data || []);
    } catch (err) {
      console.error("Failed to load match scores:", err);
      setMatches([]);
    } finally {
      setLoadingMatches(false);
    }
  };

  if (!isOpen || !item) return null;

  const isFound = type === "FOUND";
  const statusMeta = isFound 
    ? (FOUND_STATUS_BADGES[item.status] || { label: item.status, color: "badge-gray" })
    : (LOST_STATUS_BADGES[item.status] || { label: item.status, color: "badge-gray" });

  const formattedDate = item.lossDateTime || item.foundDateTime
    ? new Date(item.lossDateTime || item.foundDateTime).toLocaleString(undefined, {
        dateStyle: "medium",
        timeStyle: "short",
      })
    : "Not recorded";

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-card item-modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div className="modal-title-with-icon">
            <span className={`status-pill ${statusMeta.color}`}>
              {statusMeta.label}
            </span>
            <h3>{item.itemName}</h3>
          </div>
          <button className="modal-close-btn" onClick={onClose}>
            <X size={20} />
          </button>
        </div>

        <div className="modal-body item-modal-body">
          <div className="item-detail-grid">
            {/* Left: Media & Key Metadata */}
            <div className="item-left-col">
              {item.photoUrl ? (
                <img src={item.photoUrl} alt={item.itemName} className="item-detail-image" />
              ) : (
                <div className="item-detail-no-image">
                  <span>No Photo Uploaded</span>
                </div>
              )}

              <div className="meta-box">
                <h4>Transit & Custody Record</h4>
                <div className="meta-row">
                  <span className="lbl">Record Type:</span>
                  <span className="val">{isFound ? "Recovered Property (Depot Logged)" : "Passenger Missing Report"}</span>
                </div>
                <div className="meta-row">
                  <span className="lbl">Category:</span>
                  <span className="val">{item.category?.replace(/_/g, " ")}</span>
                </div>
                <div className="meta-row">
                  <span className="lbl">Transit Mode:</span>
                  <span className="val">{item.transportType}</span>
                </div>
                <div className="meta-row">
                  <span className="lbl">Route / Vehicle:</span>
                  <span className="val">{item.routeNumber}</span>
                </div>
                <div className="meta-row">
                  <span className="lbl">Station / Stop:</span>
                  <span className="val">{item.location}</span>
                </div>
                {isFound && item.storageLocation && (
                  <div className="meta-row">
                    <span className="lbl">Depot Locker:</span>
                    <span className="val highlight">{item.storageLocation}</span>
                  </div>
                )}
                <div className="meta-row">
                  <span className="lbl">Timestamp:</span>
                  <span className="val">{formattedDate}</span>
                </div>
                <div className="meta-row">
                  <span className="lbl">Logged By:</span>
                  <span className="val">{item.reportedBy?.fullName || "Transit Authority"}</span>
                </div>
              </div>
            </div>

            {/* Right: Description & Matching Engine Suggestions */}
            <div className="item-right-col">
              <div className="desc-section">
                <h4>Circumstances & Detailed Description</h4>
                <p className="full-description">
                  {item.description || "No detailed description was recorded for this item."}
                </p>
              </div>

              {/* Automated Heuristic Match Engine Section */}
              <div className="match-engine-section">
                <div className="match-engine-header">
                  <div className="match-engine-title">
                    <Sparkles size={18} color="#2563eb" />
                    <strong>Automated Matching Engine Suggestions</strong>
                  </div>
                  <span className="match-badge-count">{matches.length} Candidates</span>
                </div>

                <p className="match-engine-explainer">
                  Rule-based correlation calculated on Category (35%), Transit Line (15%), Mode (15%), Location (15%), Date Proximity (10%), and Keyword Overlap (10%).
                </p>

                {loadingMatches ? (
                  <div className="match-loading">Analyzing records for correlations...</div>
                ) : matches.length > 0 ? (
                  <div className="match-candidates-list">
                    {matches.map((m) => {
                      const counterpart = isFound ? m.lostItem : m.foundItem;
                      const scoreNum = parseFloat(m.matchScore || 0);
                      return (
                        <div key={m.id} className="match-item-row">
                          <div className="match-score-pill">
                            <span className="score-val">{scoreNum}%</span>
                            <span className="score-lbl">Match</span>
                          </div>
                          <div className="match-info">
                            <div className="match-name">{counterpart?.itemName}</div>
                            <div className="match-notes">{m.matchNotes}</div>
                            <div className="match-sub">
                              {counterpart?.routeNumber} • {counterpart?.location}
                            </div>
                          </div>
                          {isFound && onClaim && (
                            <button
                              className="btn-match-claim"
                              onClick={() => {
                                onClose();
                                onClaim(item);
                              }}
                            >
                              Claim This
                            </button>
                          )}
                        </div>
                      );
                    })}
                  </div>
                ) : (
                  <div className="no-matches-box">
                    <AlertCircle size={16} />
                    <span>No automated correlations above 40% threshold identified yet.</span>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>

        <div className="modal-footer">
          {isFound && item.status === "AVAILABLE" && onClaim && (
            <button 
              className="btn-primary" 
              onClick={() => {
                onClose();
                onClaim(item);
              }}
            >
              <FileCheck2 size={16} />
              <span>Submit Ownership Claim</span>
            </button>
          )}
          <button className="btn-secondary" onClick={onClose}>
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
