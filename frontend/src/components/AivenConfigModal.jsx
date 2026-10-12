import { useState, useEffect } from "react";
import { Database, CheckCircle2, Copy, ExternalLink, X, ShieldCheck, Terminal, Server } from "lucide-react";
import api from "../services/api";

export default function AivenConfigModal({ isOpen, onClose }) {
  const [copied, setCopied] = useState(false);
  const [backendStatus, setBackendStatus] = useState("checking");
  const [dbInfo, setDbInfo] = useState(null);

  useEffect(() => {
    if (isOpen) {
      checkBackend();
    }
  }, [isOpen]);

  const checkBackend = async () => {
    setBackendStatus("checking");
    try {
      const res = await api.get("/lost-items?size=1");
      setBackendStatus("connected");
      setDbInfo("Connected & operational");
    } catch (err) {
      if (err.response) {
        setBackendStatus("connected"); // Server responded, so springboot is up
      } else {
        setBackendStatus("offline");
      }
    }
  };

  const aivenPropertiesSnippet = `# ==========================================================
# AIVEN CONSOLE MYSQL DATABASE CONFIGURATION
# Provide your Aiven MySQL Service details in:
# backend/src/main/resources/application.properties
# ==========================================================

# Option A: Direct Service URI from Aiven Console (Recommended)
DB_URL=jdbc:mysql://<aiven-host>.aivencloud.com:<port>/<dbname>?sslMode=REQUIRED

# Option B: Individual Service Credentials from Aiven Overview tab:
DB_HOST=<your-aiven-hostname>.aivencloud.com
DB_PORT=<your-aiven-port>
DB_NAME=defaultdb
DB_USERNAME=avnadmin
DB_PASSWORD=<your-aiven-password>
DB_SSL=true`;

  const handleCopy = () => {
    navigator.clipboard.writeText(aivenPropertiesSnippet);
    setCopied(true);
    setTimeout(() => setCopied(false), 2500);
  };

  if (!isOpen) return null;

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-card aiven-modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div className="modal-title-with-icon">
            <div className="modal-icon aiven-icon-box">
              <Database size={22} />
            </div>
            <div>
              <h3>Aiven Console MySQL Integration</h3>
              <p>Cloud Relational Database for State Public Transport Lost & Found System</p>
            </div>
          </div>
          <button className="modal-close-btn" onClick={onClose}>
            <X size={20} />
          </button>
        </div>

        <div className="modal-body">
          {/* Status Banner */}
          <div className={`status-banner ${backendStatus}`}>
            <div className="status-indicator-dot"></div>
            <div>
              <strong>Backend Service Status: </strong>
              {backendStatus === "checking" && "Connecting to Spring Boot backend..."}
              {backendStatus === "connected" && "Spring Boot API is Online (Port 8080) & MySQL Driver Ready"}
              {backendStatus === "offline" && "Backend Offline - Start Spring Boot using `mvn spring-boot:run`"}
            </div>
          </div>

          <div className="aiven-steps">
            <h4>How to Connect to your Aiven MySQL Service:</h4>
            <ol>
              <li>Log into your <strong>Aiven Console</strong> (<a href="https://console.aiven.io" target="_blank" rel="noreferrer">console.aiven.io <ExternalLink size={12} /></a>).</li>
              <li>Create or select your <strong>MySQL</strong> service instance.</li>
              <li>Navigate to the <strong>Overview</strong> tab and copy your <strong>Service URI</strong> or connection parameters (Host, Port, User <code>avnadmin</code>, Password).</li>
              <li>Set the environment variables or update <code>backend/src/main/resources/application.properties</code> as shown below:</li>
            </ol>
          </div>

          <div className="code-block-container">
            <div className="code-header">
              <span>application.properties / Environment Variables</span>
              <button className="btn-copy" onClick={handleCopy}>
                {copied ? <CheckCircle2 size={14} color="#10b981" /> : <Copy size={14} />}
                <span>{copied ? "Copied!" : "Copy Configuration"}</span>
              </button>
            </div>
            <pre className="code-display">{aivenPropertiesSnippet}</pre>
          </div>

          <div className="aiven-features-grid">
            <div className="aiven-feat">
              <ShieldCheck size={18} color="#059669" />
              <div>
                <strong>SSL Encrypted Connection</strong>
                <span>Configured with <code>sslMode=REQUIRED</code> for secure transit database access.</span>
              </div>
            </div>
            <div className="aiven-feat">
              <Server size={18} color="#0284c7" />
              <div>
                <strong>Auto-DDL Schema Update</strong>
                <span>Hibernate auto-creates tables (<code>users</code>, <code>lost_items</code>, <code>found_items</code>, <code>claims</code>, <code>matches</code>, <code>return_records</code>).</span>
              </div>
            </div>
          </div>
        </div>

        <div className="modal-footer">
          <button className="btn-secondary" onClick={checkBackend}>
            Re-test Backend Connection
          </button>
          <button className="btn-primary" onClick={onClose}>
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
