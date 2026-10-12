import { useState } from "react";
import { X, ShieldCheck, Upload, AlertCircle, CheckCircle2 } from "lucide-react";
import { claimService, uploadService } from "../services/api";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";

export default function SubmitClaimModal({ item, isOpen, onClose, onSuccess }) {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [claimDescription, setClaimDescription] = useState("");
  const [proofDetails, setProofDetails] = useState("");
  const [proofPhotoUrl, setProofPhotoUrl] = useState("");
  const [uploading, setUploading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);

  if (!isOpen || !item) return null;

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    setUploading(true);
    setError("");
    try {
      const res = await uploadService.uploadFile(file);
      if (res.data?.fileUrl) {
        setProofPhotoUrl(res.data.fileUrl);
      }
    } catch (err) {
      setError("Failed to upload verification photo. You can paste an image URL instead.");
    } finally {
      setUploading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!isAuthenticated) {
      setError("Please sign in as a passenger to submit an official ownership claim.");
      return;
    }

    if (!claimDescription.trim()) {
      setError("Please provide a description of why this item belongs to you.");
      return;
    }

    setSubmitting(true);
    setError("");
    try {
      await claimService.submitClaim({
        foundItemId: item.id,
        claimDescription: claimDescription.trim(),
        proofDetails: proofDetails.trim(),
        proofPhotoUrl: proofPhotoUrl.trim(),
      });

      setSuccess(true);
      setTimeout(() => {
        setSuccess(false);
        onClose();
        if (onSuccess) onSuccess();
        navigate("/claims");
      }, 1800);
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.message ||
        "Failed to submit claim. You might already have a pending claim for this item."
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-card claim-modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div className="modal-title-with-icon">
            <div className="modal-icon">
              <ShieldCheck size={22} color="#059669" />
            </div>
            <div>
              <h3>Submit Ownership Claim</h3>
              <p>Claiming: <strong>{item.itemName}</strong></p>
            </div>
          </div>
          <button className="modal-close-btn" onClick={onClose}>
            <X size={20} />
          </button>
        </div>

        {success ? (
          <div className="claim-success-card">
            <CheckCircle2 size={54} color="#059669" />
            <h3>Claim Submitted Successfully!</h3>
            <p>
              Your claim has been submitted to the transit depot staff for verification.
              You will be redirected to your claims tracker.
            </p>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="modal-form">
            <div className="modal-body">
              {!isAuthenticated && (
                <div className="auth-warning-banner">
                  <AlertCircle size={18} />
                  <span>
                    You must be logged in to submit an official claim.{" "}
                    <button 
                      type="button" 
                      className="inline-link" 
                      onClick={() => { onClose(); navigate("/login"); }}
                    >
                      Login now
                    </button>
                  </span>
                </div>
              )}

              <div className="claim-item-summary">
                <span className="summary-label">Target Recovered Item:</span>
                <span className="summary-name">{item.itemName}</span>
                <span className="summary-route">{item.routeNumber} • {item.location}</span>
                <span className="summary-depot">Locker: {item.storageLocation}</span>
              </div>

              <div className="form-group">
                <label htmlFor="claim-desc">
                  Claim Justification / Identifying Features *
                </label>
                <textarea
                  id="claim-desc"
                  rows={3}
                  value={claimDescription}
                  onChange={(e) => setClaimDescription(e.target.value)}
                  placeholder="Describe secret marks, contents, lock passcode pattern, exact stickers, or identifying details..."
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="proof-details">
                  Supporting Evidence / Serial Number / Purchase Date
                </label>
                <input
                  id="proof-details"
                  type="text"
                  value={proofDetails}
                  onChange={(e) => setProofDetails(e.target.value)}
                  placeholder="e.g. Serial #SN982741, Invoice from BestBuy, Student ID inside"
                />
              </div>

              <div className="form-group">
                <label>Supporting Ownership Photo or Proof</label>
                <div className="file-upload-row">
                  <input
                    type="file"
                    accept="image/*"
                    onChange={handleFileUpload}
                    id="claim-file-upload"
                    className="file-input-hidden"
                  />
                  <label htmlFor="claim-file-upload" className="btn-file-select">
                    <Upload size={14} />
                    <span>{uploading ? "Uploading..." : "Upload Photo / Receipt"}</span>
                  </label>
                  <input
                    type="url"
                    value={proofPhotoUrl}
                    onChange={(e) => setProofPhotoUrl(e.target.value)}
                    placeholder="Or paste proof image URL directly"
                    className="flex-1"
                  />
                </div>
                {proofPhotoUrl && (
                  <div className="photo-preview-tiny">
                    <img src={proofPhotoUrl} alt="Proof preview" />
                    <span className="preview-label">Proof Attached</span>
                  </div>
                )}
              </div>

              {error && (
                <div className="error-alert">
                  <AlertCircle size={16} />
                  <span>{error}</span>
                </div>
              )}
            </div>

            <div className="modal-footer">
              <button type="button" className="btn-secondary" onClick={onClose}>
                Cancel
              </button>
              <button 
                type="submit" 
                className="btn-primary" 
                disabled={submitting || !isAuthenticated}
              >
                {submitting ? "Submitting Claim..." : "Confirm & Submit Claim"}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
