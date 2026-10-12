import { useState, useEffect } from "react";
import { useSearchParams, Link } from "react-router-dom";
import { 
  Sparkles, 
  MapPin, 
  Clock, 
  Tag, 
  CheckCircle2, 
  ArrowRight, 
  FileCheck2, 
  Layers, 
  Building2, 
  ShieldCheck,
  Search,
  Percent
} from "lucide-react";
import { lostItemService, foundItemService, matchingService } from "../services/api";
import SubmitClaimModal from "../components/SubmitClaimModal";

export default function MatchesExplorer() {
  const [searchParams] = useSearchParams();
  const lostIdParam = searchParams.get("lostId");
  const foundIdParam = searchParams.get("foundId");

  const [lostItems, setLostItems] = useState([]);
  const [foundItems, setFoundItems] = useState([]);
  const [selectedLostId, setSelectedLostId] = useState(lostIdParam || "");
  const [selectedFoundId, setSelectedFoundId] = useState(foundIdParam || "");
  const [mode, setMode] = useState(foundIdParam ? "BY_FOUND" : "BY_LOST");

  const [matches, setMatches] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedMatch, setSelectedMatch] = useState(null);

  const [claimTargetItem, setClaimTargetItem] = useState(null);
  const [claimModalOpen, setClaimModalOpen] = useState(false);

  useEffect(() => {
    loadSelectorLists();
  }, []);

  useEffect(() => {
    if (mode === "BY_LOST" && selectedLostId) {
      fetchMatchesForLost(selectedLostId);
    } else if (mode === "BY_FOUND" && selectedFoundId) {
      fetchMatchesForFound(selectedFoundId);
    }
  }, [mode, selectedLostId, selectedFoundId]);

  const loadSelectorLists = async () => {
    try {
      const [lostRes, foundRes] = await Promise.all([
        lostItemService.getLostItems({ size: 25 }),
        foundItemService.getFoundItems({ size: 25 }),
      ]);
      const lList = lostRes.data?.content || [];
      const fList = foundRes.data?.content || [];
      setLostItems(lList);
      setFoundItems(fList);

      if (!selectedLostId && lList.length > 0 && mode === "BY_LOST") {
        setSelectedLostId(String(lList[0].id));
      }
      if (!selectedFoundId && fList.length > 0 && mode === "BY_FOUND") {
        setSelectedFoundId(String(fList[0].id));
      }
    } catch (err) {
      console.error("Failed to load items for match explorer:", err);
    }
  };

  const fetchMatchesForLost = async (id) => {
    setLoading(true);
    try {
      const res = await matchingService.getMatchesForLostItem(id);
      setMatches(res.data || []);
      setSelectedMatch(res.data?.[0] || null);
    } catch (err) {
      console.error(err);
      setMatches([]);
      setSelectedMatch(null);
    } finally {
      setLoading(false);
    }
  };

  const fetchMatchesForFound = async (id) => {
    setLoading(true);
    try {
      const res = await matchingService.getMatchesForFoundItem(id);
      setMatches(res.data || []);
      setSelectedMatch(res.data?.[0] || null);
    } catch (err) {
      console.error(err);
      setMatches([]);
      setSelectedMatch(null);
    } finally {
      setLoading(false);
    }
  };

  const activeLost = lostItems.find((l) => String(l.id) === String(selectedLostId));
  const activeFound = foundItems.find((f) => String(f.id) === String(selectedFoundId));

  return (
    <div className="spt-page matches-explorer-page">
      <div className="spt-container">
        {/* Header */}
        <div className="matches-page-header">
          <div className="header-badge">
            <Sparkles size={16} />
            <span>HEURISTIC AI CORRELATION ENGINE</span>
          </div>
          <h1>Automated Match Explorer</h1>
          <p>
            The system applies a rule-based matching score across Category (35%), Transit Route (15%), 
            Transit Mode (15%), Location (15%), Date Proximity (10%), and Keyword Overlap (10%) to rank candidates.
          </p>
        </div>

        {/* Algorithm Rules Explainer Ribbon */}
        <div className="algorithm-rules-ribbon">
          <div className="rule-badge">
            <span className="pts">35 pts</span>
            <span>Category Match</span>
          </div>
          <div className="rule-badge">
            <span className="pts">15 pts</span>
            <span>Transit Route #</span>
          </div>
          <div className="rule-badge">
            <span className="pts">15 pts</span>
            <span>Mode (Metro/Bus/Rail)</span>
          </div>
          <div className="rule-badge">
            <span className="pts">15 pts</span>
            <span>Station/Stop Overlap</span>
          </div>
          <div className="rule-badge">
            <span className="pts">10 pts</span>
            <span>Time Proximity (≤48h)</span>
          </div>
          <div className="rule-badge">
            <span className="pts">10 pts</span>
            <span>Name Keywords</span>
          </div>
        </div>

        {/* Mode Selector and Item Pickers */}
        <div className="match-controls-panel">
          <div className="mode-toggle">
            <button
              className={`mode-btn ${mode === "BY_LOST" ? "active" : ""}`}
              onClick={() => {
                setMode("BY_LOST");
                if (lostItems.length > 0 && !selectedLostId) setSelectedLostId(String(lostItems[0].id));
              }}
            >
              Scan Matches for a Lost Item Report
            </button>
            <button
              className={`mode-btn ${mode === "BY_FOUND" ? "active" : ""}`}
              onClick={() => {
                setMode("BY_FOUND");
                if (foundItems.length > 0 && !selectedFoundId) setSelectedFoundId(String(foundItems[0].id));
              }}
            >
              Scan Matches for a Depot Recovered Item
            </button>
          </div>

          <div className="item-select-bar">
            {mode === "BY_LOST" ? (
              <div className="select-box-wrapper">
                <label>Select Lost Item Report to Cross-Reference:</label>
                <select
                  value={selectedLostId}
                  onChange={(e) => setSelectedLostId(e.target.value)}
                >
                  {lostItems.map((item) => (
                    <option key={item.id} value={item.id}>
                      #{item.id} - {item.itemName} ({item.routeNumber} • {item.location})
                    </option>
                  ))}
                </select>
              </div>
            ) : (
              <div className="select-box-wrapper">
                <label>Select Depot Recovered Property to Cross-Reference:</label>
                <select
                  value={selectedFoundId}
                  onChange={(e) => setSelectedFoundId(e.target.value)}
                >
                  {foundItems.map((item) => (
                    <option key={item.id} value={item.id}>
                      #{item.id} - {item.itemName} (Locker: {item.storageLocation})
                    </option>
                  ))}
                </select>
              </div>
            )}
          </div>
        </div>

        {/* Main Side-by-side View */}
        {loading ? (
          <div className="loading-state">
            <div className="spinner"></div>
            <p>Executing heuristic algorithm comparisons...</p>
          </div>
        ) : (
          <div className="match-results-container">
            {/* Candidates Sidebar */}
            <div className="matches-sidebar">
              <div className="sidebar-head">
                <h3>Identified Matches</h3>
                <span className="count-pill">{matches.length} Candidates</span>
              </div>

              {matches.length === 0 ? (
                <div className="no-matches-found">
                  <p>No candidates reached the 40% threshold for this item.</p>
                </div>
              ) : (
                <div className="matches-list">
                  {matches.map((m) => {
                    const candidate = mode === "BY_LOST" ? m.foundItem : m.lostItem;
                    const isSelected = selectedMatch?.id === m.id;
                    const score = parseFloat(m.matchScore || 0);

                    return (
                      <div
                        key={m.id}
                        className={`match-entry-card ${isSelected ? "selected" : ""}`}
                        onClick={() => setSelectedMatch(m)}
                      >
                        <div className="entry-score-col">
                          <div className={`score-badge ${score >= 70 ? "high" : "med"}`}>
                            <span className="num">{score}%</span>
                            <span className="lbl">Score</span>
                          </div>
                        </div>
                        <div className="entry-details">
                          <h4 className="entry-title">{candidate?.itemName}</h4>
                          <div className="entry-route">
                            <MapPin size={12} />
                            <span>{candidate?.routeNumber} • {candidate?.location}</span>
                          </div>
                          <span className="entry-category">{candidate?.category?.replace(/_/g, " ")}</span>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>

            {/* Comparison Details Pane */}
            <div className="match-comparison-pane">
              {selectedMatch ? (
                <div className="comparison-content">
                  <div className="score-spotlight-bar">
                    <div className="spotlight-left">
                      <div className="large-score-circle">
                        <span>{parseFloat(selectedMatch.matchScore)}%</span>
                      </div>
                      <div>
                        <h3>Correlation Match Ranking</h3>
                        <p className="score-notes-text">
                          Algorithm Assessment: <strong>{selectedMatch.matchNotes}</strong>
                        </p>
                      </div>
                    </div>

                    <button
                      className="btn-primary btn-claim-spotlight"
                      onClick={() => {
                        setClaimTargetItem(selectedMatch.foundItem);
                        setClaimModalOpen(true);
                      }}
                    >
                      <FileCheck2 size={16} />
                      <span>Submit Claim for Found Item</span>
                    </button>
                  </div>

                  {/* Side by side comparison */}
                  <div className="comparison-columns">
                    {/* Lost Item Side */}
                    <div className="comparison-card card-lost-side">
                      <div className="card-badge-side badge-lost">
                        REPORTED MISSING (LOST ITEM #{selectedMatch.lostItem?.id})
                      </div>

                      {selectedMatch.lostItem?.photoUrl && (
                        <img 
                          src={selectedMatch.lostItem.photoUrl} 
                          alt="Lost photo" 
                          className="side-img" 
                        />
                      )}

                      <h4>{selectedMatch.lostItem?.itemName}</h4>
                      <p className="side-desc">{selectedMatch.lostItem?.description}</p>

                      <div className="spec-table">
                        <div className="spec-row">
                          <span className="spec-label">Category:</span>
                          <span className="spec-val">{selectedMatch.lostItem?.category?.replace(/_/g, " ")}</span>
                        </div>
                        <div className="spec-row">
                          <span className="spec-label">Transit Mode:</span>
                          <span className="spec-val">{selectedMatch.lostItem?.transportType}</span>
                        </div>
                        <div className="spec-row">
                          <span className="spec-label">Route:</span>
                          <span className="spec-val">{selectedMatch.lostItem?.routeNumber}</span>
                        </div>
                        <div className="spec-row">
                          <span className="spec-label">Station:</span>
                          <span className="spec-val">{selectedMatch.lostItem?.location}</span>
                        </div>
                        <div className="spec-row">
                          <span className="spec-label">Date Lost:</span>
                          <span className="spec-val">
                            {new Date(selectedMatch.lostItem?.lossDateTime).toLocaleString()}
                          </span>
                        </div>
                      </div>
                    </div>

                    {/* Found Item Side */}
                    <div className="comparison-card card-found-side">
                      <div className="card-badge-side badge-found">
                        RECOVERED PROPERTY (FOUND ITEM #{selectedMatch.foundItem?.id})
                      </div>

                      {selectedMatch.foundItem?.photoUrl && (
                        <img 
                          src={selectedMatch.foundItem.photoUrl} 
                          alt="Found photo" 
                          className="side-img" 
                        />
                      )}

                      <h4>{selectedMatch.foundItem?.itemName}</h4>
                      <p className="side-desc">{selectedMatch.foundItem?.description}</p>

                      <div className="spec-table">
                        <div className="spec-row">
                          <span className="spec-label">Category:</span>
                          <span className="spec-val">{selectedMatch.foundItem?.category?.replace(/_/g, " ")}</span>
                        </div>
                        <div className="spec-row">
                          <span className="spec-label">Transit Mode:</span>
                          <span className="spec-val">{selectedMatch.foundItem?.transportType}</span>
                        </div>
                        <div className="spec-row">
                          <span className="spec-label">Route:</span>
                          <span className="spec-val">{selectedMatch.foundItem?.routeNumber}</span>
                        </div>
                        <div className="spec-row">
                          <span className="spec-label">Location Found:</span>
                          <span className="spec-val">{selectedMatch.foundItem?.location}</span>
                        </div>
                        <div className="spec-row">
                          <span className="spec-label">Depot Locker:</span>
                          <span className="spec-val highlight">{selectedMatch.foundItem?.storageLocation}</span>
                        </div>
                        <div className="spec-row">
                          <span className="spec-label">Date Found:</span>
                          <span className="spec-val">
                            {new Date(selectedMatch.foundItem?.foundDateTime).toLocaleString()}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="select-prompt-box">
                  <Sparkles size={36} color="#60a5fa" />
                  <h3>Select a match candidate to compare</h3>
                  <p>Inspect attribute alignment, keyword overlap, and proximity scoring.</p>
                </div>
              )}
            </div>
          </div>
        )}
      </div>

      <SubmitClaimModal
        item={claimTargetItem}
        isOpen={claimModalOpen}
        onClose={() => setClaimModalOpen(false)}
        onSuccess={() => {
          if (mode === "BY_LOST") fetchMatchesForLost(selectedLostId);
          else fetchMatchesForFound(selectedFoundId);
        }}
      />
    </div>
  );
}
