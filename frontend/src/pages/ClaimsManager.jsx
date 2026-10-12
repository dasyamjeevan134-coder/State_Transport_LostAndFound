import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { 
  FileCheck2, 
  CheckCircle2, 
  XCircle, 
  Clock, 
  ShieldCheck, 
  AlertCircle, 
  Eye, 
  RotateCcw,
  Building2,
  MapPin,
  User,
  ArrowRight
} from "lucide-react";
import { claimService } from "../services/api";
import { useAuth } from "../context/AuthContext";
import { CLAIM_STATUS_BADGES } from "../constants/transit";

export default function ClaimsManager() {
  const navigate = useNavigate();
  const { isStaff, user, isAuthenticated } = useAuth();

  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(false);
  const [statusFilter, setStatusFilter] = useState("");
  const [selectedClaim, setSelectedClaim] = useState(null);

  // Review modal state (for staff)
  const [reviewModalOpen, setReviewModalOpen] = useState(false);
  const [reviewDecision, setReviewDecision] = useState("APPROVED");
  const [verificationRemarks, setVerificationRemarks] = useState("");
  const [submittingReview, setSubmittingReview] = useState(false);
  const [reviewError, setReviewError] = useState("");

  useEffect(() => {
    if (isAuthenticated) {
      loadClaims();
    }
  }, [isAuthenticated, isStaff, statusFilter]);

  const loadClaims = async () => {
    setLoading(true);
    try {
      const res = isStaff 
        ? await claimService.getAllClaims({ status: statusFilter || undefined, size: 50 })
        : await claimService.getMyClaims({ size: 50 });

      const dataList = res.data?.content || [];
      setClaims(dataList);
      if (dataList.length > 0 && !selectedClaim) {
        setSelectedClaim(dataList[0]);
      }
    } catch (err) {
      console.error("Failed to load claims:", err);
      setClaims([]);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenReview = (claim, decision) => {
    setSelectedClaim(claim);
    setReviewDecision(decision);
    setVerificationRemarks(
      decision === "APPROVED" 
        ? "Ownership verified against serial number & matching identifying marks."
        : "Evidence does not sufficiently establish ownership of this property."
    );
    setReviewError("");
    setReviewModalOpen(true);
  };

  const handleConfirmReview = async (e) => {
    e.preventDefault();
    if (!selectedClaim?.id) return;

    setSubmittingReview(true);
    setReviewError("");

    try {
      await claimService.reviewClaim(selectedClaim.id, {
        status: reviewDecision,
        verificationRemarks: verificationRemarks.trim(),
      });
      setReviewModalOpen(false);
      loadClaims();
    } catch (err) {
      setReviewError(
        err.response?.data?.message ||
        err.message ||
        "Failed to update claim review status."
      );
    } finally {
      setSubmittingReview(false);
    }
  };

  const handleCancelClaim = async (claimId) => {
    if (!window.confirm("Are you sure you wish to cancel this ownership claim?")) return;
    try {
      await claimService.cancelClaim(claimId);
      loadClaims();
    } catch (err) {
      alert("Failed to cancel claim.");
    }
  };

  if (!isAuthenticated) {
    return (
      <div className="spt-page claims-page">
        <div className="spt-container">
          <div className="auth-alert-notice">
            <AlertCircle size={24} />
            <div>
              <h3>Sign In Required</h3>
              <p>Please log in to manage your submitted claims or access staff verification.</p>
              <Link to="/login" className="btn-primary mt-3">Log In</Link>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="spt-page claims-page">
      <div className="spt-container">
        {/* Header */}
        <div className="claims-header">
          <div>
            <div className="header-badge">
              <ShieldCheck size={16} />
              <span>{isStaff ? "STAFF VERIFICATION WORKBENCH" : "PASSENGER CLAIM TRACKER"}</span>
            </div>
            <h1>{isStaff ? "Ownership Claims Management" : "My Ownership Claims"}</h1>
            <p>
              {isStaff
                ? "Review passenger ownership evidence, verify identifying credentials, and authorize depot handovers."
                : "Track verification progress for property you have claimed from state transit depot safekeeping."}
            </p>
          </div>

          {isStaff && (
            <div className="status-filter-buttons">
              {["", "PENDING", "APPROVED", "REJECTED"].map((st) => (
                <button
                  key={st}
                  className={`st-btn ${statusFilter === st ? "active" : ""}`}
                  onClick={() => setStatusFilter(st)}
                >
                  {st === "" ? "All Claims" : st}
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Claims Main Layout */}
        {loading ? (
          <div className="loading-state">
            <div className="spinner"></div>
            <p>Loading claims registry...</p>
          </div>
        ) : claims.length === 0 ? (
          <div className="empty-results-box">
            <FileCheck2 size={48} color="#94a3b8" />
            <h3>No Claims Recorded</h3>
            <p>
              {isStaff 
                ? "There are currently no ownership claims matching this filter." 
                : "You haven't submitted any ownership claims yet. Browse recovered items to find yours."}
            </p>
            {!isStaff && (
              <Link to="/browse?tab=FOUND" className="btn-primary">
                Browse Recovered Items
              </Link>
            )}
          </div>
        ) : (
          <div className="claims-layout">
            {/* Claims Master List */}
            <div className="claims-list-pane">
              <div className="pane-header">
                <h3>Registered Claims ({claims.length})</h3>
              </div>

              <div className="claims-cards">
                {claims.map((c) => {
                  const badge = CLAIM_STATUS_BADGES[c.status] || { label: c.status, color: "badge-gray" };
                  const isSelected = selectedClaim?.id === c.id;

                  return (
                    <div
                      key={c.id}
                      className={`claim-item-card ${isSelected ? "selected" : ""}`}
                      onClick={() => setSelectedClaim(c)}
                    >
                      <div className="claim-card-top">
                        <span className="claim-id">Claim #{c.id}</span>
                        <span className={`status-pill ${badge.color}`}>{badge.label}</span>
                      </div>

                      <h4 className="claim-item-title">{c.foundItem?.itemName}</h4>

                      <div className="claim-meta-sub">
                        <span>Route: {c.foundItem?.routeNumber}</span>
                        <span>•</span>
                        <span>{new Date(c.createdAt).toLocaleDateString()}</span>
                      </div>

                      <p className="claim-snippet">
                        {c.claimDescription?.substring(0, 80)}...
                      </p>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Claim Details & Action Workspace */}
            <div className="claim-inspection-pane">
              {selectedClaim ? (
                <div className="inspection-card">
                  <div className="inspection-head">
                    <div>
                      <span className="inspection-tag">Claim Reference #{selectedClaim.id}</span>
                      <h2>{selectedClaim.foundItem?.itemName}</h2>
                      <span className="claim-date-full">
                        Submitted: {new Date(selectedClaim.createdAt).toLocaleString()}
                      </span>
                    </div>

                    <div className="inspection-status">
                      <span className={`status-pill large ${CLAIM_STATUS_BADGES[selectedClaim.status]?.color}`}>
                        {CLAIM_STATUS_BADGES[selectedClaim.status]?.label || selectedClaim.status}
                      </span>
                    </div>
                  </div>

                  {/* Claimant & Found Item Details */}
                  <div className="inspection-grid">
                    <div className="inspection-col">
                      <h4>Claimant Details</h4>
                      <div className="info-box">
                        <div className="info-row">
                          <span className="info-lbl">Full Name:</span>
                          <span className="info-val">{selectedClaim.claimant?.fullName || "Passenger"}</span>
                        </div>
                        <div className="info-row">
                          <span className="info-lbl">Email:</span>
                          <span className="info-val">{selectedClaim.claimant?.email}</span>
                        </div>
                        <div className="info-row">
                          <span className="info-lbl">Phone:</span>
                          <span className="info-val">{selectedClaim.claimant?.phone}</span>
                        </div>
                      </div>
                    </div>

                    <div className="inspection-col">
                      <h4>Recovered Property Custody</h4>
                      <div className="info-box">
                        <div className="info-row">
                          <span className="info-lbl">Depot Locker:</span>
                          <span className="info-val highlight">{selectedClaim.foundItem?.storageLocation}</span>
                        </div>
                        <div className="info-row">
                          <span className="info-lbl">Transit Line:</span>
                          <span className="info-val">{selectedClaim.foundItem?.routeNumber}</span>
                        </div>
                        <div className="info-row">
                          <span className="info-lbl">Found Date:</span>
                          <span className="info-val">
                            {new Date(selectedClaim.foundItem?.foundDateTime).toLocaleDateString()}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* Ownership Evidence Section */}
                  <div className="evidence-section">
                    <h4>Ownership Justification & Distinguishing Marks</h4>
                    <p className="claim-full-text">{selectedClaim.claimDescription}</p>

                    {selectedClaim.proofDetails && (
                      <div className="proof-details-box">
                        <strong>Serial Number / Purchase Record:</strong>
                        <p>{selectedClaim.proofDetails}</p>
                      </div>
                    )}

                    {selectedClaim.proofPhotoUrl && (
                      <div className="proof-photo-section">
                        <strong>Uploaded Proof of Ownership Photo:</strong>
                        <div className="proof-photo-wrapper">
                          <img 
                            src={selectedClaim.proofPhotoUrl} 
                            alt="Claimant Proof" 
                            className="proof-img" 
                          />
                        </div>
                      </div>
                    )}
                  </div>

                  {/* Verification Review Notes (if already reviewed) */}
                  {selectedClaim.verificationRemarks && (
                    <div className="review-history-box">
                      <h4>Depot Staff Verification Notes</h4>
                      <p className="remarks-text">{selectedClaim.verificationRemarks}</p>
                      <span className="verified-staff-name">
                        Reviewed by: {selectedClaim.verifiedBy?.fullName || "Transit Verification Staff"}
                      </span>
                    </div>
                  )}

                  {/* Action Bar */}
                  <div className="inspection-actions-bar">
                    {isStaff && selectedClaim.status === "PENDING" && (
                      <div className="staff-actions-group">
                        <button
                          className="btn-approve"
                          onClick={() => handleOpenReview(selectedClaim, "APPROVED")}
                        >
                          <CheckCircle2 size={16} />
                          <span>Approve Claim</span>
                        </button>
                        <button
                          className="btn-reject"
                          onClick={() => handleOpenReview(selectedClaim, "REJECTED")}
                        >
                          <XCircle size={16} />
                          <span>Reject Claim</span>
                        </button>
                      </div>
                    )}

                    {isStaff && selectedClaim.status === "APPROVED" && (
                      <div className="handover-prompt-box">
                        <div className="handover-text">
                          <CheckCircle2 size={18} color="#059669" />
                          <span>Claim has been officially approved. Proceed to depot handover.</span>
                        </div>
                        <Link
                          to={`/handovers?claimId=${selectedClaim.id}&foundItemId=${selectedClaim.foundItem?.id}`}
                          className="btn-primary"
                        >
                          <span>Complete Item Handover & Issue Certificate &rarr;</span>
                        </Link>
                      </div>
                    )}

                    {!isStaff && selectedClaim.status === "PENDING" && (
                      <button
                        className="btn-cancel-claim"
                        onClick={() => handleCancelClaim(selectedClaim.id)}
                      >
                        Cancel This Claim
                      </button>
                    )}
                  </div>
                </div>
              ) : (
                <div className="select-prompt-box">
                  <p>Select a claim from the list to review details.</p>
                </div>
              )}
            </div>
          </div>
        )}
      </div>

      {/* Staff Review Decision Modal */}
      {reviewModalOpen && (
        <div className="modal-backdrop" onClick={() => setReviewModalOpen(false)}>
          <div className="modal-card review-modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>
                {reviewDecision === "APPROVED" ? "Approve Ownership Claim" : "Reject Ownership Claim"}
              </h3>
            </div>

            <form onSubmit={handleConfirmReview}>
              <div className="modal-body">
                <p>
                  Claimant: <strong>{selectedClaim?.claimant?.fullName}</strong> for{" "}
                  <strong>{selectedClaim?.foundItem?.itemName}</strong>
                </p>

                <div className="form-group">
                  <label>Official Verification Remarks *</label>
                  <textarea
                    rows={4}
                    value={verificationRemarks}
                    onChange={(e) => setVerificationRemarks(e.target.value)}
                    placeholder="Document reasons for approval/rejection, verification of serial numbers, ID check..."
                    required
                  />
                </div>

                {reviewError && (
                  <div className="error-alert">
                    <AlertCircle size={16} />
                    <span>{reviewError}</span>
                  </div>
                )}
              </div>

              <div className="modal-footer">
                <button 
                  type="button" 
                  className="btn-secondary" 
                  onClick={() => setReviewModalOpen(false)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className={reviewDecision === "APPROVED" ? "btn-approve" : "btn-reject"}
                  disabled={submittingReview}
                >
                  {submittingReview ? "Saving Decision..." : `Confirm ${reviewDecision}`}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
