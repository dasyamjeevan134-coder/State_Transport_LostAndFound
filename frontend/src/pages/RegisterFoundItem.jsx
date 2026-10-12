import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { 
  PlusCircle, 
  Upload, 
  Building2, 
  MapPin, 
  Sparkles, 
  CheckCircle2, 
  AlertCircle,
  ShieldCheck,
  PackageSearch
} from "lucide-react";
import { foundItemService, uploadService } from "../services/api";
import { ITEM_CATEGORIES, TRANSPORT_TYPES } from "../constants/transit";
import { useAuth } from "../context/AuthContext";

export default function RegisterFoundItem() {
  const navigate = useNavigate();
  const { isStaff, isAuthenticated } = useAuth();

  const [itemName, setItemName] = useState("");
  const [category, setCategory] = useState("ELECTRONICS");
  const [description, setDescription] = useState("");
  const [foundDateTime, setFoundDateTime] = useState(
    new Date().toISOString().slice(0, 16)
  );
  const [transportType, setTransportType] = useState("METRO");
  const [routeNumber, setRouteNumber] = useState("");
  const [location, setLocation] = useState("");
  const [storageLocation, setStorageLocation] = useState("");
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
    if (!isStaff) {
      setError("Staff authorization is required to register custody of recovered property.");
      return;
    }

    if (!itemName.trim() || !routeNumber.trim() || !location.trim() || !storageLocation.trim()) {
      setError("Please fill in all mandatory fields including depot storage locker.");
      return;
    }

    setSubmitting(true);
    setError("");

    try {
      const res = await foundItemService.createFoundItem({
        itemName: itemName.trim(),
        category,
        description: description.trim(),
        foundDateTime: new Date(foundDateTime).toISOString(),
        transportType,
        routeNumber: routeNumber.trim(),
        location: location.trim(),
        storageLocation: storageLocation.trim(),
        photoUrl: photoUrl.trim() || undefined,
      });

      const newItem = res.data;
      setCreatedItem(newItem);

      // Check for immediate heuristic matches against open lost reports
      if (newItem?.id) {
        try {
          const matchRes = await foundItemService.getMatchesForFoundItem(newItem.id);
          setMatches(matchRes.data || []);
        } catch {
          // match check error handled
        }
      }
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.message ||
        "Failed to register found item. Please verify depot staff credentials."
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="spt-page register-found-page">
      <div className="spt-container">
        {createdItem ? (
          <div className="report-success-panel">
            <div className="success-banner-box">
              <CheckCircle2 size={48} color="#059669" />
              <div>
                <h2>Recovered Property Registered in Depot Custody</h2>
                <p>
                  Found Item Registry ID: <strong>#{createdItem.id}</strong> • Storage Location: <strong>{createdItem.storageLocation}</strong>
                </p>
                <p className="sub-text">
                  Item is now cataloged in the centralized state database. Automated matching algorithm was triggered.
                </p>
              </div>
            </div>

            {/* Instant Heuristic Match Feedback */}
            <div className="instant-matches-panel">
              <div className="panel-title-row">
                <Sparkles size={20} color="#2563eb" />
                <h3>Open Lost Reports Matching This Property ({matches.length})</h3>
              </div>

              {matches.length > 0 ? (
                <div className="match-cards-grid">
                  {matches.map((m) => (
                    <div key={m.id} className="match-card">
                      <div className="match-header">
                        <span className="match-pct">{parseFloat(m.matchScore)}% Match</span>
                        <span className="match-date">Report #{m.lostItem?.id}</span>
                      </div>
                      <h4>{m.lostItem?.itemName}</h4>
                      <p className="match-desc">{m.matchNotes}</p>
                      <div className="match-location">
                        <MapPin size={13} />
                        <span>{m.lostItem?.routeNumber} • Reported by: {m.lostItem?.passenger?.fullName || "Passenger"}</span>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="empty-matches-info">
                  <p>
                    No open passenger reports currently correlate above 40%. 
                    This item will remain securely logged in depot inventory available for public search and passenger claims.
                  </p>
                </div>
              )}
            </div>

            <div className="success-footer-actions">
              <Link to="/browse?tab=FOUND" className="btn-primary">
                View in Found Inventory
              </Link>
              <button 
                className="btn-secondary"
                onClick={() => {
                  setCreatedItem(null);
                  setItemName("");
                  setDescription("");
                  setRouteNumber("");
                  setLocation("");
                  setStorageLocation("");
                  setPhotoUrl("");
                  setMatches([]);
                }}
              >
                Register Another Found Item
              </button>
            </div>
          </div>
        ) : (
          <div className="report-form-container">
            <div className="form-page-header">
              <div className="staff-header-tag">
                <ShieldCheck size={16} />
                <span>AUTHORIZED TRANSIT STAFF DESK</span>
              </div>
              <h1>Register Found Property</h1>
              <p>
                Catalog items recovered from trains, buses, stations, or terminal platforms into depot safe storage.
              </p>
            </div>

            {!isStaff && (
              <div className="auth-alert-notice staff-alert">
                <AlertCircle size={20} />
                <div>
                  <strong>Staff Access Required: </strong>
                  Only verified transit depot staff or administrators can register found property.{" "}
                  <Link to="/login" className="inline-link">Sign in as Staff (e.g. Officer Vance)</Link>.
                </div>
              </div>
            )}

            <form onSubmit={handleSubmit} className="spt-form">
              {/* Section 1: Recovered Item Details */}
              <div className="form-section-card">
                <h3>1. Recovered Property Information</h3>
                <div className="form-grid-2">
                  <div className="form-group">
                    <label htmlFor="itemName">Item Name / Identification *</label>
                    <input
                      id="itemName"
                      type="text"
                      value={itemName}
                      onChange={(e) => setItemName(e.target.value)}
                      placeholder="e.g. Samsung Galaxy S23, Black Leather Wallet, Silver Keyring with FOB"
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
                  <label htmlFor="description">Circumstances of Recovery & Visual Description *</label>
                  <textarea
                    id="description"
                    rows={4}
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    placeholder="Found under seat 14B by cleaning team; contains student card; scratches on front..."
                    required
                  />
                </div>
              </div>

              {/* Section 2: Transit Location & Depot Storage */}
              <div className="form-section-card">
                <h3>2. Transit Route & Custody Depot</h3>
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
                    <label htmlFor="routeNumber">Route / Train / Bus Number *</label>
                    <input
                      id="routeNumber"
                      type="text"
                      value={routeNumber}
                      onChange={(e) => setRouteNumber(e.target.value)}
                      placeholder="e.g. Metro Blue Line - Train 104, Bus 42B"
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="foundDateTime">Date & Time Found *</label>
                    <input
                      id="foundDateTime"
                      type="datetime-local"
                      value={foundDateTime}
                      onChange={(e) => setFoundDateTime(e.target.value)}
                      required
                    />
                  </div>
                </div>

                <div className="form-grid-2">
                  <div className="form-group">
                    <label htmlFor="location">Discovery Location (Station / Vehicle Area) *</label>
                    <input
                      id="location"
                      type="text"
                      value={location}
                      onChange={(e) => setLocation(e.target.value)}
                      placeholder="e.g. Central Metro Platform 3, Bus Rear Bench"
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="storageLocation">Depot Storage Locker / Shelf Location *</label>
                    <input
                      id="storageLocation"
                      type="text"
                      value={storageLocation}
                      onChange={(e) => setStorageLocation(e.target.value)}
                      placeholder="e.g. Central Metro Depot - Safe Vault Locker B-14"
                      required
                    />
                  </div>
                </div>
              </div>

              {/* Section 3: Photo Upload */}
              <div className="form-section-card">
                <h3>3. Custody Photo</h3>
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
                      placeholder="Or enter direct photo URL"
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
                  disabled={submitting || !isStaff}
                >
                  <PlusCircle size={18} />
                  <span>{submitting ? "Logging into Depot..." : "Register Found Item & Trigger AI Match"}</span>
                </button>
              </div>
            </form>
          </div>
        )}
      </div>
    </div>
  );
}
