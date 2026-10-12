import { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import { 
  Search, 
  Filter, 
  RotateCcw, 
  PackageSearch, 
  Sparkles, 
  Bus, 
  TrainFrontTunnel, 
  Train, 
  FileCheck2,
  ChevronLeft,
  ChevronRight,
  Layers
} from "lucide-react";
import { foundItemService, lostItemService } from "../services/api";
import { ITEM_CATEGORIES, TRANSPORT_TYPES } from "../constants/transit";
import ItemCard from "../components/ItemCard";
import ItemDetailsModal from "../components/ItemDetailsModal";
import SubmitClaimModal from "../components/SubmitClaimModal";

export default function BrowseItems() {
  const [searchParams, setSearchParams] = useSearchParams();
  const initialQuery = searchParams.get("q") || "";
  const initialTab = searchParams.get("tab") || "FOUND";

  const [activeTab, setActiveTab] = useState(initialTab); // "FOUND" or "LOST"
  const [keyword, setKeyword] = useState(initialQuery);
  const [category, setCategory] = useState("");
  const [transportType, setTransportType] = useState("");
  const [status, setStatus] = useState("");
  const [location, setLocation] = useState("");

  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  const [selectedItem, setSelectedItem] = useState(null);
  const [selectedType, setSelectedType] = useState("FOUND");
  const [detailsOpen, setDetailsOpen] = useState(false);
  const [claimItem, setClaimItem] = useState(null);
  const [claimOpen, setClaimOpen] = useState(false);

  useEffect(() => {
    fetchItems();
  }, [activeTab, page, category, transportType, status]);

  const fetchItems = async () => {
    setLoading(true);
    try {
      const params = {
        page,
        size: 9,
        keyword: keyword || undefined,
        category: category || undefined,
        transportType: transportType || undefined,
        status: status || undefined,
        location: location || undefined,
      };

      const res = activeTab === "FOUND"
        ? await foundItemService.getFoundItems(params)
        : await lostItemService.getLostItems(params);

      const pagedData = res.data || {};
      setItems(pagedData.content || []);
      setTotalPages(pagedData.totalPages || 1);
      setTotalElements(pagedData.totalElements || 0);
    } catch (err) {
      console.error("Failed to fetch items:", err);
      setItems([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    fetchItems();
  };

  const handleResetFilters = () => {
    setKeyword("");
    setCategory("");
    setTransportType("");
    setStatus("");
    setLocation("");
    setPage(0);
  };

  const handleTabChange = (newTab) => {
    setActiveTab(newTab);
    setPage(0);
    setStatus(""); // reset status when switching types
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

  return (
    <div className="spt-page browse-page">
      <div className="spt-container">
        {/* Page Title & Tab Switcher */}
        <div className="browse-header">
          <div>
            <span className="section-eyebrow">STATE TRANSIT INVENTORY DIRECTORY</span>
            <h1>Browse Transit Property</h1>
            <p>Search and filter items reported lost or recovered across all public transport lines.</p>
          </div>

          <div className="type-toggle-pill">
            <button
              className={`toggle-btn ${activeTab === "FOUND" ? "active" : ""}`}
              onClick={() => handleTabChange("FOUND")}
            >
              <PackageSearch size={16} />
              <span>Recovered Found Items</span>
            </button>
            <button
              className={`toggle-btn ${activeTab === "LOST" ? "active" : ""}`}
              onClick={() => handleTabChange("LOST")}
            >
              <Layers size={16} />
              <span>Reported Lost Items</span>
            </button>
          </div>
        </div>

        {/* Search & Filter Toolbar */}
        <div className="filter-toolbar">
          <form onSubmit={handleSearchSubmit} className="search-row">
            <div className="search-input-wrapper">
              <Search size={18} className="search-icon-input" />
              <input
                type="text"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                placeholder="Search item name, brand, route number, or station..."
              />
            </div>
            <button type="submit" className="btn-filter-search">
              Filter Records
            </button>
          </form>

          <div className="dropdowns-row">
            <div className="filter-select-group">
              <label>Category</label>
              <select value={category} onChange={(e) => { setCategory(e.target.value); setPage(0); }}>
                <option value="">All Categories</option>
                {ITEM_CATEGORIES.map((c) => (
                  <option key={c.value} value={c.value}>{c.label}</option>
                ))}
              </select>
            </div>

            <div className="filter-select-group">
              <label>Transit Mode</label>
              <select value={transportType} onChange={(e) => { setTransportType(e.target.value); setPage(0); }}>
                <option value="">All Transit Modes</option>
                {TRANSPORT_TYPES.map((t) => (
                  <option key={t.value} value={t.value}>{t.label}</option>
                ))}
              </select>
            </div>

            <div className="filter-select-group">
              <label>Status</label>
              <select value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }}>
                <option value="">All Statuses</option>
                {activeTab === "FOUND" ? (
                  <>
                    <option value="AVAILABLE">Available in Depot</option>
                    <option value="CLAIM_PENDING">Claim Pending</option>
                    <option value="CLAIM_APPROVED">Claim Approved</option>
                    <option value="RETURNED">Returned to Owner</option>
                  </>
                ) : (
                  <>
                    <option value="OPEN">Open Missing Report</option>
                    <option value="MATCH_FOUND">Match Identified</option>
                    <option value="CLAIMED">Claim Submitted</option>
                    <option value="RETURNED">Returned & Closed</option>
                  </>
                )}
              </select>
            </div>

            <div className="filter-select-group">
              <label>Station / Route</label>
              <input
                type="text"
                value={location}
                onChange={(e) => setLocation(e.target.value)}
                onBlur={() => { setPage(0); fetchItems(); }}
                placeholder="Filter by stop/route"
                className="input-small"
              />
            </div>

            <button type="button" className="btn-reset-filters" onClick={handleResetFilters} title="Reset all filters">
              <RotateCcw size={15} />
              <span>Reset</span>
            </button>
          </div>
        </div>

        {/* Results Info & Count */}
        <div className="results-count-bar">
          <span>
            Showing <strong>{items.length}</strong> of <strong>{totalElements}</strong> {activeTab === "FOUND" ? "recovered found items" : "reported missing items"}
          </span>
        </div>

        {/* Items Grid */}
        {loading ? (
          <div className="loading-state">
            <div className="spinner"></div>
            <p>Querying State Transit database...</p>
          </div>
        ) : items.length > 0 ? (
          <div className="cards-grid">
            {items.map((item) => (
              <ItemCard
                key={item.id}
                item={item}
                type={activeTab}
                onViewDetails={handleViewDetails}
                onClaim={handleClaim}
              />
            ))}
          </div>
        ) : (
          <div className="empty-results-box">
            <PackageSearch size={48} color="#94a3b8" />
            <h3>No Property Records Found</h3>
            <p>Try adjusting your search terms, removing filters, or switching to the other tab.</p>
            <button className="btn-secondary" onClick={handleResetFilters}>
              Clear All Filters
            </button>
          </div>
        )}

        {/* Pagination Bar */}
        {totalPages > 1 && (
          <div className="pagination-bar">
            <button
              className="btn-page"
              disabled={page === 0}
              onClick={() => setPage((p) => Math.max(0, p - 1))}
            >
              <ChevronLeft size={16} />
              <span>Previous</span>
            </button>
            <span className="page-indicator">
              Page {page + 1} of {totalPages}
            </span>
            <button
              className="btn-page"
              disabled={page >= totalPages - 1}
              onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
            >
              <span>Next</span>
              <ChevronRight size={16} />
            </button>
          </div>
        )}
      </div>

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
        onSuccess={fetchItems}
      />
    </div>
  );
}