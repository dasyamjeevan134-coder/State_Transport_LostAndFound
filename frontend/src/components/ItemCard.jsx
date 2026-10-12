import { 
  Bus, 
  TrainFrontTunnel, 
  Train, 
  CableCar, 
  Ship, 
  MapPin, 
  Calendar, 
  Sparkles, 
  Clock, 
  ArrowRight,
  ShieldAlert,
  Building2,
  FileCheck2
} from "lucide-react";
import { FOUND_STATUS_BADGES, LOST_STATUS_BADGES } from "../constants/transit";

export default function ItemCard({ item, type = "FOUND", onViewDetails, onClaim, onFindMatches }) {
  const isFound = type === "FOUND";
  const statusMeta = isFound 
    ? (FOUND_STATUS_BADGES[item.status] || { label: item.status, color: "badge-gray" })
    : (LOST_STATUS_BADGES[item.status] || { label: item.status, color: "badge-gray" });

  const getTransportIcon = (transType) => {
    switch (transType) {
      case "BUS": return <Bus size={15} />;
      case "METRO": return <TrainFrontTunnel size={15} />;
      case "TRAIN": return <Train size={15} />;
      case "TRAM": return <CableCar size={15} />;
      case "FERRY": return <Ship size={15} />;
      default: return <MapPin size={15} />;
    }
  };

  const formattedDate = item.lossDateTime || item.foundDateTime
    ? new Date(item.lossDateTime || item.foundDateTime).toLocaleDateString(undefined, {
        month: "short",
        day: "numeric",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      })
    : "Recent";

  return (
    <article className="spt-card">
      <div className="card-media">
        {item.photoUrl ? (
          <img src={item.photoUrl} alt={item.itemName} className="card-img" />
        ) : (
          <div className="card-placeholder-img">
            {getTransportIcon(item.transportType)}
            <span>{item.category?.replace(/_/g, " ")}</span>
          </div>
        )}
        <div className="card-badges-overlay">
          <span className={`status-pill ${statusMeta.color}`}>
            {statusMeta.label}
          </span>
          <span className="type-pill">
            {isFound ? "Recovered Property" : "Reported Missing"}
          </span>
        </div>
      </div>

      <div className="card-body">
        <div className="card-category-row">
          <span className="category-tag">{item.category?.replace(/_/g, " ")}</span>
          <div className="transit-mode-badge">
            {getTransportIcon(item.transportType)}
            <span>{item.transportType}</span>
          </div>
        </div>

        <h3 className="card-title" title={item.itemName}>
          {item.itemName}
        </h3>

        <p className="card-desc">
          {item.description ? (
            item.description.length > 100 
              ? item.description.substring(0, 100) + "..." 
              : item.description
          ) : "No additional description provided."}
        </p>

        <div className="card-meta-list">
          <div className="meta-item">
            <MapPin size={14} className="meta-icon" />
            <span className="meta-text">
              <strong>Route/Line:</strong> {item.routeNumber || "Transit Line"} ({item.location})
            </span>
          </div>
          {isFound && item.storageLocation && (
            <div className="meta-item">
              <Building2 size={14} className="meta-icon" />
              <span className="meta-text">
                <strong>Storage:</strong> {item.storageLocation}
              </span>
            </div>
          )}
          <div className="meta-item">
            <Clock size={14} className="meta-icon" />
            <span className="meta-text">{formattedDate}</span>
          </div>
        </div>
      </div>

      <div className="card-footer">
        <button 
          className="btn-card-action btn-outline"
          onClick={() => onViewDetails(item, type)}
        >
          <span>View Details</span>
          <ArrowRight size={14} />
        </button>

        {isFound && item.status === "AVAILABLE" && onClaim && (
          <button 
            className="btn-card-action btn-claim"
            onClick={() => onClaim(item)}
          >
            <FileCheck2 size={14} />
            <span>Claim</span>
          </button>
        )}

        {onFindMatches && (
          <button 
            className="btn-card-action btn-match-icon"
            onClick={() => onFindMatches(item, type)}
            title="Scan Heuristic Matches"
          >
            <Sparkles size={14} />
          </button>
        )}
      </div>
    </article>
  );
}
