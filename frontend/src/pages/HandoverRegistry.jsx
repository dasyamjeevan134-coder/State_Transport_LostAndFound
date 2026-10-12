import { useState, useEffect } from "react";
import { useSearchParams, Link } from "react-router-dom";
import { 
  RotateCcw, 
  CheckCircle2, 
  ShieldCheck, 
  FileCheck2, 
  AlertCircle, 
  Building2, 
  Calendar, 
  Printer, 
  ArrowRight,
  UserCheck
} from "lucide-react";
import { returnRecordService, claimService, foundItemService } from "../services/api";
import { useAuth } from "../context/AuthContext";

export default function HandoverRegistry() {
  const [searchParams] = useSearchParams();
  const initialClaimId = searchParams.get("claimId") || "";
  const initialFoundItemId = searchParams.get("foundItemId") || "";

  const { isStaff, user, isAuthenticated } = useAuth();

  const [returnRecords, setReturnRecords] = useState([]);
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState(initialClaimId ? "NEW_HANDOVER" : "REGISTRY_LOG");

  // Form states
  const [foundItemId, setFoundItemId] = useState(initialFoundItemId);
  const [claimId, setClaimId] = useState(initialClaimId);
  const [recipientName, setRecipientName] = useState("");
  const [recipientContact, setRecipientContact] = useState("");
  const [verificationDetails, setVerificationDetails] = useState("National ID / Passport verified in person");
  const [remarks, setRemarks] = useState("Item received in good condition, all contents accounted for.");

  const [approvedClaims, setApprovedClaims] = useState([]);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [successRecord, setSuccessRecord] = useState(null);

  useEffect(() => {
    if (isAuthenticated && isStaff) {
      loadReturnRecords();
      loadApprovedClaims();
    }
  }, [isAuthenticated, isStaff]);

  const loadReturnRecords = async () => {
    setLoading(true);
    try {
      const res = await returnRecordService.getAllReturnRecords({ size: 50 });
      setReturnRecords(res.data?.content || []);
    } catch (err) {
      console.error("Failed to load return records:", err);
    } finally {
      setLoading(false);
    }
  };

  const loadApprovedClaims = async () => {
    try {
      const res = await claimService.getAllClaims({ status: "APPROVED", size: 50 });
      const list = res.data?.content || [];
      setApprovedClaims(list);

      // If initial claimId was passed, auto-populate recipient fields
      if (initialClaimId) {
        const target = list.find((c) => String(c.id) === String(initialClaimId));
        if (target) {
          setRecipientName(target.claimant?.fullName || "");
          setRecipientContact(target.claimant?.phone || target.claimant?.email || "");
          setFoundItemId(String(target.foundItem?.id || ""));
        }
      }
    } catch {
      // non-critical
    }
  };

  const handleClaimSelect = (e) => {
    const cid = e.target.value;
    setClaimId(cid);
    if (cid) {
      const target = approvedClaims.find((c) => String(c.id) === String(cid));
      if (target) {
        setFoundItemId(String(target.foundItem?.id || ""));
        setRecipientName(target.claimant?.fullName || "");
        setRecipientContact(target.claimant?.phone || target.claimant?.email || "");
      }
    }
  };

  const handleSubmitHandover = async (e) => {
    e.preventDefault();
    if (!foundItemId || !recipientName.trim() || !recipientContact.trim()) {
      setError("Please complete all recipient and found item identifier fields.");
      return;
    }

    setSubmitting(true);
    setError("");

    try {
      const res = await returnRecordService.createReturnRecord({
        foundItemId: Number(foundItemId),
        claimId: claimId ? Number(claimId) : undefined,
        recipientName: recipientName.trim(),
        recipientContact: recipientContact.trim(),
        verificationDetails: verificationDetails.trim(),
        remarks: remarks.trim(),
      });

      setSuccessRecord(res.data);
      loadReturnRecords();
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.message ||
        "Failed to record handover. Please verify that the item is approved for return."
      );
    } finally {
      setSubmitting(false);
    }
  };

  if (!isStaff) {
    return (
      <div className="spt-page handover-page">
        <div className="spt-container">
          <div className="auth-alert-notice staff-alert">
            <AlertCircle size={24} />
            <div>
              <h3>Staff Clearance Required</h3>
              <p>The Handover & Return Registry is restricted to authorized transit staff and depot officers.</p>
              <Link to="/login" className="btn-primary mt-3">Sign in as Staff</Link>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="spt-page handover-page">
      <div className="spt-container">
        {/* Header */}
        <div className="handover-header">
          <div>
            <div className="header-badge">
              <ShieldCheck size={16} />
              <span>OFFICIAL TRANSIT CUSTODY HANDOVER</span>
            </div>
            <h1>Return Records & Handover Registry</h1>
            <p>
              Execute formal property releases, record passenger ID verification, and generate official 
              transit return certificates.
            </p>
          </div>

          <div className="type-toggle-pill">
            <button
              className={`toggle-btn ${activeTab === "NEW_HANDOVER" ? "active" : ""}`}
              onClick={() => { setActiveTab("NEW_HANDOVER"); setSuccessRecord(null); }}
            >
              <UserCheck size={16} />
              <span>Issue New Handover</span>
            </button>
            <button
              className={`toggle-btn ${activeTab === "REGISTRY_LOG" ? "active" : ""}`}
              onClick={() => setActiveTab("REGISTRY_LOG")}
            >
              <RotateCcw size={16} />
              <span>Audit History ({returnRecords.length})</span>
            </button>
          </div>
        </div>

        {activeTab === "NEW_HANDOVER" ? (
          successRecord ? (
            <div className="return-certificate-card">
              <div className="cert-top">
                <div className="cert-badge">
                  <CheckCircle2 size={32} color="#059669" />
                  <span>OFFICIAL STATE PUBLIC TRANSPORT RETURN RECEIPT</span>
                </div>
                <button className="btn-print" onClick={() => window.print()}>
                  <Printer size={16} />
                  <span>Print Receipt</span>
                </button>
              </div>

              <div className="cert-body">
                <h2>Return Handover Certificate #{successRecord.id}</h2>
                <p className="cert-timestamp">
                  Executed on: {new Date(successRecord.returnDate).toLocaleString()}
                </p>

                <div className="cert-grid">
                  <div className="cert-col">
                    <span className="cert-lbl">Recovered Item</span>
                    <span className="cert-val strong">{successRecord.foundItem?.itemName}</span>
                    <span className="cert-sub">Route: {successRecord.foundItem?.routeNumber}</span>
                  </div>

                  <div className="cert-col">
                    <span className="cert-lbl">Recipient (Owner)</span>
                    <span className="cert-val strong">{successRecord.recipientName}</span>
                    <span className="cert-sub">{successRecord.recipientContact}</span>
                  </div>

                  <div className="cert-col">
                    <span className="cert-lbl">Verification Method</span>
                    <span className="cert-val">{successRecord.verificationDetails}</span>
                  </div>

                  <div className="cert-col">
                    <span className="cert-lbl">Handover Staff Officer</span>
                    <span className="cert-val strong">{successRecord.staff?.fullName || user?.fullName}</span>
                    <span className="cert-sub">Depot Custody Relinquished</span>
                  </div>
                </div>

                <div className="cert-notes">
                  <strong>Official Remarks: </strong>
                  <span>{successRecord.remarks}</span>
                </div>
              </div>

              <div className="cert-footer">
                <button 
                  className="btn-primary"
                  onClick={() => {
                    setSuccessRecord(null);
                    setFoundItemId("");
                    setClaimId("");
                    setRecipientName("");
                    setRecipientContact("");
                  }}
                >
                  Record Another Handover
                </button>
                <button 
                  className="btn-secondary"
                  onClick={() => setActiveTab("REGISTRY_LOG")}
                >
                  View All Historical Returns
                </button>
              </div>
            </div>
          ) : (
            <div className="handover-form-panel">
              <div className="form-page-header">
                <h3>Record In-Person Depot Property Handover</h3>
                <p>Verify passenger credentials against approved claims before releasing custody.</p>
              </div>

              <form onSubmit={handleSubmitHandover} className="spt-form">
                <div className="form-section-card">
                  <h4>1. Select Target Approved Claim</h4>
                  <div className="form-group">
                    <label>Approved Claim from Queue</label>
                    <select value={claimId} onChange={handleClaimSelect}>
                      <option value="">-- Manual Handover / Direct Return --</option>
                      {approvedClaims.map((ac) => (
                        <option key={ac.id} value={ac.id}>
                          Claim #{ac.id}: {ac.foundItem?.itemName} (Claimant: {ac.claimant?.fullName})
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="form-group">
                    <label>Found Item Reference ID *</label>
                    <input
                      type="number"
                      value={foundItemId}
                      onChange={(e) => setFoundItemId(e.target.value)}
                      placeholder="e.g. 1"
                      required
                    />
                  </div>
                </div>

                <div className="form-section-card">
                  <h4>2. Recipient Information & ID Verification</h4>
                  <div className="form-grid-2">
                    <div className="form-group">
                      <label>Recipient Legal Full Name *</label>
                      <input
                        type="text"
                        value={recipientName}
                        onChange={(e) => setRecipientName(e.target.value)}
                        placeholder="e.g. Elena Rostova"
                        required
                      />
                    </div>

                    <div className="form-group">
                      <label>Recipient Contact (Phone / Email) *</label>
                      <input
                        type="text"
                        value={recipientContact}
                        onChange={(e) => setRecipientContact(e.target.value)}
                        placeholder="e.g. +1 800 555 0122"
                        required
                      />
                    </div>
                  </div>

                  <div className="form-group">
                    <label>ID Document & Verification Type *</label>
                    <input
                      type="text"
                      value={verificationDetails}
                      onChange={(e) => setVerificationDetails(e.target.value)}
                      placeholder="e.g. Government Driving License #DL-88219 verified in person at Depot Safe 3"
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label>Handover Remarks & Depot Receipt Notes</label>
                    <textarea
                      rows={3}
                      value={remarks}
                      onChange={(e) => setRemarks(e.target.value)}
                      placeholder="Document physical condition upon return, baggage contents verified..."
                    />
                  </div>
                </div>

                {error && (
                  <div className="error-alert">
                    <AlertCircle size={16} />
                    <span>{error}</span>
                  </div>
                )}

                <div className="form-actions">
                  <button type="submit" className="btn-primary btn-submit-large" disabled={submitting}>
                    <CheckCircle2 size={18} />
                    <span>{submitting ? "Executing Handover..." : "Execute Handover & Relinquish Custody"}</span>
                  </button>
                </div>
              </form>
            </div>
          )
        ) : (
          /* Audit History Table */
          <div className="handover-history-panel">
            {loading ? (
              <div className="loading-state">
                <div className="spinner"></div>
                <p>Loading historical handover logs...</p>
              </div>
            ) : returnRecords.length === 0 ? (
              <div className="empty-results-box">
                <RotateCcw size={48} color="#94a3b8" />
                <h3>No Return Records Yet</h3>
                <p>Completed item releases will be permanently recorded in this state audit registry.</p>
              </div>
            ) : (
              <div className="table-responsive">
                <table className="spt-table">
                  <thead>
                    <tr>
                      <th>Record ID</th>
                      <th>Return Date</th>
                      <th>Item Recovered</th>
                      <th>Recipient Owner</th>
                      <th>Verification Type</th>
                      <th>Staff Officer</th>
                      <th>Remarks</th>
                    </tr>
                  </thead>
                  <tbody>
                    {returnRecords.map((r) => (
                      <tr key={r.id}>
                        <td><strong>#{r.id}</strong></td>
                        <td>{new Date(r.returnDate).toLocaleString()}</td>
                        <td>
                          <strong>{r.foundItem?.itemName}</strong>
                          <div className="tbl-sub">Route: {r.foundItem?.routeNumber}</div>
                        </td>
                        <td>
                          <strong>{r.recipientName}</strong>
                          <div className="tbl-sub">{r.recipientContact}</div>
                        </td>
                        <td>{r.verificationDetails}</td>
                        <td>
                          <span className="staff-pill">{r.staff?.fullName || "Transit Staff"}</span>
                        </td>
                        <td className="remarks-cell">{r.remarks}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
