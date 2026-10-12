export const ITEM_CATEGORIES = [
  { value: "ELECTRONICS", label: "Electronics & Gadgets", icon: "Laptop" },
  { value: "BAGS_AND_LUGGAGE", label: "Bags & Luggage", icon: "Luggage" },
  { value: "WALLETS_AND_PURSES", label: "Wallets & Purses", icon: "Wallet" },
  { value: "DOCUMENTS_AND_ID", label: "Documents & ID Cards", icon: "FileText" },
  { value: "CLOTHING_AND_ACCESSORIES", label: "Clothing & Accessories", icon: "Shirt" },
  { value: "KEYS", label: "Keys & Keychains", icon: "Key" },
  { value: "JEWELRY", label: "Jewelry & Watches", icon: "Watch" },
  { value: "BOOKS_AND_STATIONERY", label: "Books & Stationery", icon: "BookOpen" },
  { value: "MEDICAL", label: "Medical & Health Items", icon: "HeartPulse" },
  { value: "OTHER", label: "Other Belongings", icon: "Box" },
];

export const TRANSPORT_TYPES = [
  { value: "BUS", label: "State City/Depot Bus", icon: "Bus" },
  { value: "METRO", label: "Metro Rapid Transit", icon: "TrainFrontTunnel" },
  { value: "TRAIN", label: "Suburban & Regional Rail", icon: "Train" },
  { value: "TRAM", label: "City Tramway", icon: "CableCar" },
  { value: "FERRY", label: "Water Ferry / Terminal", icon: "Ship" },
  { value: "OTHER", label: "Transit Depot / Station Facility", icon: "MapPin" },
];

export const FOUND_STATUS_BADGES = {
  AVAILABLE: { label: "In Depot Safe", color: "badge-green" },
  CLAIM_PENDING: { label: "Claim Under Review", color: "badge-amber" },
  CLAIM_APPROVED: { label: "Ready for Handover", color: "badge-blue" },
  RETURNED: { label: "Returned to Owner", color: "badge-purple" },
  DISPOSED: { label: "Archived / Disposed", color: "badge-gray" },
};

export const LOST_STATUS_BADGES = {
  OPEN: { label: "Report Active", color: "badge-orange" },
  MATCH_FOUND: { label: "Potential Match Identified", color: "badge-blue" },
  CLAIMED: { label: "Claim Submitted", color: "badge-amber" },
  RETURNED: { label: "Recovered & Returned", color: "badge-green" },
  CLOSED: { label: "Report Closed", color: "badge-gray" },
};

export const CLAIM_STATUS_BADGES = {
  PENDING: { label: "Pending Verification", color: "badge-amber" },
  APPROVED: { label: "Claim Verified & Approved", color: "badge-green" },
  REJECTED: { label: "Claim Rejected", color: "badge-red" },
  CANCELLED: { label: "Cancelled by Passenger", color: "badge-gray" },
};
