import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { 
  PlusCircle, 
  Upload, 
  MapPin, 
  Calendar, 
  Clock, 
  Sparkles, 
  CheckCircle2, 
  AlertCircle,
  Bus,
  ArrowRight
} from "lucide-react";
import { lostItemService, uploadService } from "../services/api";
import { ITEM_CATEGORIES, TRANSPORT_TYPES } from "../constants/transit";
import { useAuth } from "../context/AuthContext";

export default function ReportLostItem() {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const [itemName, setItemName] = useState("");
  const [category, setCategory] = useState("ELECTRONICS");
  const [description, setDescription] = useState("");
  const [lossDateTime, setLossDateTime] = useState(
    new Date().toISOString().slice(0, 16)
  );
  const [transportType, setTransportType] = useState("METRO");
  const [routeNumber, setRouteNumber] = useState("");
  const [location, setLocation] = useState("");
  const [photoUrl, setPhotoUrl] = useState("");

  const [uploading, setUploading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [createdItem, setCreatedItem] = useState(null);
  const [matches, setMatches] = useState([]);

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    setUploading(true);
    setError("");
    try {
      const res = await uploadService.uploadFile(file);
      if (res.data?.fileUrl) {
        setPhotoUrl(res.data.fileUrl);
      }
    } catch (err) {
      setError("Failed to upload photo to server. You can provide a direct image URL instead.");
    } finally {
      setUploading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!isAuthenticated) {
      setError("Please sign in or register to submit an official missing property report.");
      return;
    }

    if (!itemName.trim() || !routeNumber.trim() || !location.trim()) {
      setError("Please fill out all required transit fields.");
      return;
    }

    setSubmitting(true);
    setError("");

    try {
      const res = await lostItemService.createLostItem({
        itemName: itemName.trim(),
        category,
        description: description.trim(),
        lossDateTime: new Date(lossDateTime).toISOString(),
        transportType,
        routeNumber: routeNumber.trim(),
        location: location.trim(),
        photoUrl: photoUrl.trim() || undefined,
      });

      const newItem = res.data;
      setCreatedItem(newItem);

      // Check for immediate heuristic matches
      if (newItem?.id) {
        try {
          const matchRes = await lostItemService.getMatchesForLostItem(newItem.id);
          setMatches(matchRes.data || []);
        } catch {
          // match check error handled
        }
      }
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.message ||
        "Failed to submit lost item report. Please check your network and credentials."
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="spt-page report-lost-page">
      <div className="spt-container">
        {createdItem ? (
          <div className="report-success-panel">
            <div className="success-banner-box">
              <CheckCircle2 size={48} color="#059669" />
              <div>
                <h2>Lost Property Report Filed Successfully</h2>
                <p>
                  Official Reference ID: <strong>#{createdItem.id}</strong> • Status: <strong>{createdItem.status}</strong>
                </p>
                <p className="sub-text">
                  Your report has been broadcast to all transit depot stations along <strong>{createdItem.routeNumber}</strong>.
                </p>
              </div>
            </div>

            {/* Instant Heuristic Match Feedback */}
            <div className="instant-matches-panel">
              <div className="panel-title-row">
                <Sparkles size={20} color="#2563eb" />
                <h3>Automated Heuristic Matches Identified ({matches.length})</h3>
              </div>

              {matches.length > 0 ? (
                <div className="match-cards-grid">
                  {matches.map((m) => (
                    <div key={m.id} className="match-card">
                      <div className="match-header">
                        <span className="match-pct">{parseFloat(m.matchScore)}% Match</span>
                        <span className="match-date">{m.foundItem?.storageLocation}</span>
                      </div>
                      <h4>{m.foundItem?.itemName}</h4>
                      <p className="match-desc">{m.matchNotes}</p>
                      <div className="match-location">
                        <MapPin size={13} />
                        <span>{m.foundItem?.routeNumber} • {m.foundItem?.location}</span>
                      </div>
                      <Link 
                        to={`/browse?tab=FOUND&q=${encodeURIComponent(m.foundItem?.itemName || "")}`}
                        className="btn-match-action"
                      >
                        Inspect & Submit Claim &rarr;
                      </Link>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="empty-matches-info">
                  <p>
                    No exact match was found in depot storage at this moment. 
                    Our automated matching engine continuously monitors newly registered found items 
                    and will notify you when a match is found.
                  </p>
                </div>
              )}
            </div>

            <div className="success-footer-actions">
              <Link to="/claims" className="btn-primary">
                View My Activity & Reports
              </Link>
              <button 
                className="btn-secondary"
                onClick={() => {
                  setCreatedItem(null);
                  setItemName("");
                  setDescription("");
                  setRouteNumber("");
                  setLocation("");
                  setPhotoUrl("");
                  setMatches([]);
                }}
              >
                File Another Report
              </button>
            </div>
          </div>
        ) : (
          <div className="report-form-container">
            <div className="form-page-header">
              <span className="section-eyebrow">PASSENGER SERVICE DESK</span>
              <h1>Report a Lost Item</h1>
              <p>
                Provide detailed transit and item specifics to allow depot staff and our automated 
                engine to cross-reference recovered property.
              </p>
            </div>

            {!isAuthenticated && (
              <div className="auth-alert-notice">
                <AlertCircle size={20} />
                <div>
                  <strong>Authentication Required: </strong>
                  You need to be logged in to file an official report.{" "}
                  <Link to="/login" className="inline-link">Sign in now</Link> or{" "}
                  <Link to="/register" className="inline-link">Create passenger account</Link>.
                </div>
              </div>
            )}

            <form onSubmit={handleSubmit} className="spt-form">
              {/* Section 1: Item Specifics */}
              <div className="form-section-card">
                <h3>1. Item Information</h3>
                <div className="form-grid-2">
                  <div className="form-group">
                    <label htmlFor="itemName">Item Name & Brand *</label>
                    <input
                      id="itemName"
                      type="text"
                      value={itemName}
                      onChange={(e) => setItemName(e.target.value)}
                      placeholder="e.g. Apple iPhone 15 Pro, Black Dell XPS Laptop, Blue Samsonite Bag"
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="category">Category *</label>
                    <select
                      id="category"
                      value={category}
                      onChange={(e) => setCategory(e.target.value)}
                    >
                      {ITEM_CATEGORIES.map((cat) => (
                        <option key={cat.value} value={cat.value}>
                          {cat.label}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                <div className="form-group">
                  <label htmlFor="description">Detailed Description & Distinctive Features *</label>
                  <textarea
                    id="description"
                    rows={4}
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    placeholder="Describe color, size, stickers, scratches, case material, contents inside, lock pattern..."
                    required
                  />
                </div>
              </div>

              {/* Section 2: Transit Details */}
              <div className="form-section-card">
                <h3>2. Public Transit Information</h3>
                <div className="form-grid-3">
                  <div className="form-group">
                    <label htmlFor="transportType">Transit Mode *</label>
                    <select
                      id="transportType"
                      value={transportType}
                      onChange={(e) => setTransportType(e.target.value)}
                    >
                      {TRANSPORT_TYPES.map((t) => (
                        <option key={t.value} value={t.value}>
                          {t.label}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="form-group">
                    <label htmlFor="routeNumber">Route / Line / Train Number *</label>
                    <input
                      id="routeNumber"
                      type="text"
                      value={routeNumber}
                      onChange={(e) => setRouteNumber(e.target.value)}
                      placeholder="e.g. Metro Blue Line, Bus Route 42B, Coach S-4"
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="lossDateTime">Date & Estimated Time of Loss *</label>
                    <input
                      id="lossDateTime"
                      type="datetime-local"
                      value={lossDateTime}
                      onChange={(e) => setLossDateTime(e.target.value)}
                      required
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label htmlFor="location">Station / Stop / Location Specifics *</label>
                  <input
                    id="location"
                    type="text"
                    value={location}
                    onChange={(e) => setLocation(e.target.value)}
                    placeholder="e.g. Platform 2 Central Station, Near Seat 18 in front, Terminal 3 Waiting Hall"
                    required
                  />
                </div>
              </div>

              {/* Section 3: Photo & Proof */}
              <div className="form-section-card">
                <h3>3. Photograph or Reference Image</h3>
                <div className="photo-upload-container">
                  <div className="file-upload-row">
                    <input
                      type="file"
                      id="photo-file"
                      accept="image/*"
                      onChange={handleFileUpload}
                      className="file-input-hidden"
                    />
                    <label htmlFor="photo-file" className="btn-file-select">
                      <Upload size={16} />
                      <span>{uploading ? "Uploading..." : "Upload Photo"}</span>
                    </label>
                    <input
                      type="url"
                      value={photoUrl}
                      onChange={(e) => setPhotoUrl(e.target.value)}
                      placeholder="Or enter direct photo URL (Unsplash, Cloudinary, etc.)"
                      className="flex-1"
                    />
                  </div>
                  {photoUrl && (
                    <div className="photo-preview-large">
                      <img src={photoUrl} alt="Item Preview" />
                      <span>Photo Preview</span>
                    </div>
                  )}
                </div>
              </div>

              {error && (
                <div className="error-alert">
                  <AlertCircle size={18} />
                  <span>{error}</span>
                </div>
              )}

              <div className="form-actions">
                <button
                  type="submit"
                  className="btn-primary btn-submit-large"
                  disabled={submitting || !isAuthenticated}
                >
                  <PlusCircle size={18} />
                  <span>{submitting ? "Submitting Report..." : "Submit Lost Property Report"}</span>
                </button>
              </div>
            </form>
          </div>
        )}
      </div>
    </div>
  );
}
