package com.transport.lostfound.config;

import com.transport.lostfound.entity.Claim;
import com.transport.lostfound.entity.FoundItem;
import com.transport.lostfound.entity.LostItem;
import com.transport.lostfound.entity.ReturnRecord;
import com.transport.lostfound.entity.User;
import com.transport.lostfound.enums.ClaimStatus;
import com.transport.lostfound.enums.FoundItemStatus;
import com.transport.lostfound.enums.ItemCategory;
import com.transport.lostfound.enums.LostItemStatus;
import com.transport.lostfound.enums.Role;
import com.transport.lostfound.enums.TransportType;
import com.transport.lostfound.repository.ClaimRepository;
import com.transport.lostfound.repository.FoundItemRepository;
import com.transport.lostfound.repository.LostItemRepository;
import com.transport.lostfound.repository.ReturnRecordRepository;
import com.transport.lostfound.repository.UserRepository;
import com.transport.lostfound.service.ItemMatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final LostItemRepository lostItemRepository;
    private final FoundItemRepository foundItemRepository;
    private final ClaimRepository claimRepository;
    private final ReturnRecordRepository returnRecordRepository;
    private final ItemMatchService matchService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.initial-admin.enabled:true}")
    private boolean adminEnabled;

    @Value("${app.initial-admin.name:System Administrator}")
    private String adminName;

    @Value("${app.initial-admin.email:admin@transport.gov}")
    private String adminEmail;

    @Value("${app.initial-admin.password:Admin@12345}")
    private String adminPassword;

    @Value("${app.initial-admin.phone:+19876543210}")
    private String adminPhone;

    public DataInitializer(UserRepository userRepository,
                           LostItemRepository lostItemRepository,
                           FoundItemRepository foundItemRepository,
                           ClaimRepository claimRepository,
                           ReturnRecordRepository returnRecordRepository,
                           ItemMatchService matchService,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.lostItemRepository = lostItemRepository;
        this.foundItemRepository = foundItemRepository;
        this.claimRepository = claimRepository;
        this.returnRecordRepository = returnRecordRepository;
        this.matchService = matchService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!adminEnabled) {
            logger.info("Initial admin creation is disabled by configuration");
            return;
        }

        User admin;
        if (!userRepository.existsByEmail(adminEmail)) {
            admin = new User(
                    adminName,
                    adminEmail,
                    passwordEncoder.encode(adminPassword),
                    adminPhone,
                    Role.ADMIN
            );
            userRepository.save(admin);
            logger.info("Default administrator account initialized with email: {}", adminEmail);
        } else {
            admin = userRepository.findByEmail(adminEmail).orElse(null);
            logger.info("Administrator account already exists with email: {}", adminEmail);
        }

        // Seed demo Staff user
        String staffEmail = "staff@transport.gov";
        User staff;
        if (!userRepository.existsByEmail(staffEmail)) {
            staff = new User(
                    "Officer Robert Vance",
                    staffEmail,
                    passwordEncoder.encode("Staff@12345"),
                    "+18005550199",
                    Role.STAFF
            );
            userRepository.save(staff);
            logger.info("Default transit staff account initialized with email: {}", staffEmail);
        } else {
            staff = userRepository.findByEmail(staffEmail).orElse(null);
        }

        // Seed demo Passenger user
        String passengerEmail = "passenger@transport.gov";
        User passenger;
        if (!userRepository.existsByEmail(passengerEmail)) {
            passenger = new User(
                    "Elena Rostova",
                    passengerEmail,
                    passwordEncoder.encode("Pass@12345"),
                    "+18005550122",
                    Role.PASSENGER
            );
            userRepository.save(passenger);
            logger.info("Default passenger account initialized with email: {}", passengerEmail);
        } else {
            passenger = userRepository.findByEmail(passengerEmail).orElse(null);
        }

        // Seed second demo Passenger user
        String passenger2Email = "marcus.chen@transit.org";
        User passenger2;
        if (!userRepository.existsByEmail(passenger2Email)) {
            passenger2 = new User(
                    "Marcus Chen",
                    passenger2Email,
                    passwordEncoder.encode("Pass@12345"),
                    "+18005550177",
                    Role.PASSENGER
            );
            userRepository.save(passenger2);
        } else {
            passenger2 = userRepository.findByEmail(passenger2Email).orElse(null);
        }

        // Seed initial lost & found items if database has fewer items
        if (foundItemRepository.count() <= 2 && passenger != null && staff != null) {
            logger.info("Seeding rich sample public transport lost & found records and claim workflows...");

            // 1. iPhone 15 Pro (METRO)
            LostItem lostItem1 = new LostItem(
                    "Apple iPhone 15 Pro (Titanium Gray)",
                    ItemCategory.ELECTRONICS,
                    "Lost inside the 8:45 AM train coach. Has a dark case with an orange MagSafe ring.",
                    LocalDateTime.now().minusHours(26),
                    TransportType.METRO,
                    "Metro Blue Line - Train 104",
                    "Central Station Platform 2",
                    "https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=600&auto=format&fit=crop&q=60",
                    passenger,
                    LostItemStatus.OPEN
            );
            lostItemRepository.save(lostItem1);

            FoundItem foundItem1 = new FoundItem(
                    "iPhone 15 with Gray Finish & Ring Case",
                    ItemCategory.ELECTRONICS,
                    "Recovered from third seat row by maintenance crew after terminal stop at Metro Blue Line.",
                    LocalDateTime.now().minusHours(24),
                    TransportType.METRO,
                    "Metro Blue Line - Train 104",
                    "Central Station Terminal (Locker B-12)",
                    "https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=600&auto=format&fit=crop&q=60",
                    staff,
                    FoundItemStatus.AVAILABLE
            );
            foundItemRepository.save(foundItem1);

            // 2. Vintage Leather Backpack (BUS)
            LostItem lostItem2 = new LostItem(
                    "Leather Travel Backpack",
                    ItemCategory.BAGS_AND_LUGGAGE,
                    "Brown vintage leather backpack containing a university notebook and blue water bottle.",
                    LocalDateTime.now().minusDays(2),
                    TransportType.BUS,
                    "Route 42B Express",
                    "Downtown Depot Stop 5",
                    "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600&auto=format&fit=crop&q=60",
                    passenger,
                    LostItemStatus.OPEN
            );
            lostItemRepository.save(lostItem2);

            FoundItem foundItem2 = new FoundItem(
                    "Brown Leather Backpack",
                    ItemCategory.BAGS_AND_LUGGAGE,
                    "Found on overhead luggage rack on Route 42B bus entering Depot 3.",
                    LocalDateTime.now().minusDays(2).plusHours(3),
                    TransportType.BUS,
                    "Route 42B Express",
                    "North Bus Depot Bay 4 (Security Shelf 4)",
                    "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600&auto=format&fit=crop&q=60",
                    staff,
                    FoundItemStatus.AVAILABLE
            );
            foundItemRepository.save(foundItem2);

            // 3. Apple MacBook Pro 14" (TRAIN) - Under Claim
            FoundItem foundItem3 = new FoundItem(
                    "Apple MacBook Pro 14\" Space Black",
                    ItemCategory.ELECTRONICS,
                    "Left under seat 34B in carriage 4 of regional express commuter train. Serial number begins with G06...",
                    LocalDateTime.now().minusDays(3),
                    TransportType.TRAIN,
                    "Regional Express Intercity #402",
                    "Grand Central Baggage Office (Safe A-3)",
                    "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600&auto=format&fit=crop&q=60",
                    staff,
                    FoundItemStatus.CLAIM_APPROVED
            );
            foundItemRepository.save(foundItem3);

            // 4. Montblanc Meisterstück Leather Wallet (FERRY) - Handed over & Returned
            FoundItem foundItem4 = new FoundItem(
                    "Montblanc Black Leather Bifold Wallet",
                    ItemCategory.WALLETS_AND_PURSES,
                    "Found by deck steward on Harbor Ferry upper observation deck. Contains travel passes and national ID.",
                    LocalDateTime.now().minusDays(5),
                    TransportType.FERRY,
                    "Harbor Star Ferry - Line F1",
                    "Pier 9 Maritime Lost & Found Desk",
                    "https://images.unsplash.com/photo-1627123424574-724758594e93?w=600&auto=format&fit=crop&q=60",
                    staff,
                    FoundItemStatus.RETURNED
            );
            foundItemRepository.save(foundItem4);

            // 5. Sony WH-1000XM5 Wireless Headphones (TRAM)
            FoundItem foundItem5 = new FoundItem(
                    "Sony WH-1000XM5 Silver Noise-Canceling Headphones",
                    ItemCategory.ELECTRONICS,
                    "Found in overhead rack of Westside Tram Line 12. Stored in original gray zipped hardcase.",
                    LocalDateTime.now().minusHours(18),
                    TransportType.TRAM,
                    "Westside Tram Line 12",
                    "West Depot Station (Bin C-04)",
                    "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=60",
                    staff,
                    FoundItemStatus.AVAILABLE
            );
            foundItemRepository.save(foundItem5);

            // 6. French Republic Biometric Passport (BUS)
            FoundItem foundItem6 = new FoundItem(
                    "Burgundy European Biometric Passport",
                    ItemCategory.DOCUMENTS_AND_ID,
                    "Recovered from passenger pocket pouch in Airport Express Coach 900. Belongs to passenger E. Rostova.",
                    LocalDateTime.now().minusHours(36),
                    TransportType.BUS,
                    "Airport Express Shuttle 900",
                    "Terminal 3 Transit Hub Desk (Locker SEC-01)",
                    "https://images.unsplash.com/photo-1544717305-2782549b5136?w=600&auto=format&fit=crop&q=60",
                    staff,
                    FoundItemStatus.AVAILABLE
            );
            foundItemRepository.save(foundItem6);

            // 7. Toyota Smart Key Fob with Red Carabiner (METRO)
            FoundItem foundItem7 = new FoundItem(
                    "Toyota Smart Key Fob with Red Anodized Carabiner",
                    ItemCategory.KEYS,
                    "Found near ticket validation barrier at South Interchange Station.",
                    LocalDateTime.now().minusHours(12),
                    TransportType.METRO,
                    "Metro Red Line",
                    "South Interchange Station Office",
                    "https://images.unsplash.com/photo-1582139329536-e7284fece509?w=600&auto=format&fit=crop&q=60",
                    staff,
                    FoundItemStatus.AVAILABLE
            );
            foundItemRepository.save(foundItem7);

            // Run match calculations
            matchService.generateMatchesForFoundItem(foundItem1);
            matchService.generateMatchesForFoundItem(foundItem2);
            matchService.generateMatchesForFoundItem(foundItem3);
            matchService.generateMatchesForFoundItem(foundItem4);
            matchService.generateMatchesForFoundItem(foundItem5);
            matchService.generateMatchesForFoundItem(foundItem6);
            matchService.generateMatchesForFoundItem(foundItem7);
            matchService.generateMatchesForLostItem(lostItem1);
            matchService.generateMatchesForLostItem(lostItem2);

            // Seed Claims
            if (claimRepository.count() == 0) {
                // Claim 1: PENDING on the backpack by Elena Rostova
                Claim claim1 = new Claim(
                        foundItem2,
                        passenger,
                        "Contains my university biology notebook with initials E.R. on the cover and an engraved navy Hydroflask.",
                        ClaimStatus.PENDING
                );
                claimRepository.save(claim1);

                // Claim 2: APPROVED on the MacBook Pro by Marcus Chen
                Claim claim2 = new Claim(
                        foundItem3,
                        passenger2,
                        "MacBook Pro 14-inch Space Black. Serial number confirmed with purchase receipt from Apple Fifth Ave.",
                        ClaimStatus.APPROVED
                );
                claim2.setReviewer(staff);
                claim2.setReviewedAt(LocalDateTime.now().minusDays(1));
                claim2.setReviewNotes("Government photo ID verified against receipt serial number. Claim approved for handover.");
                claimRepository.save(claim2);

                // Claim 3: APPROVED & COMPLETED handover for the Wallet
                Claim claim3 = new Claim(
                        foundItem4,
                        passenger,
                        "Montblanc wallet with driver license issued in my name (Elena Rostova) and Metro transit card.",
                        ClaimStatus.APPROVED
                );
                claim3.setReviewer(staff);
                claim3.setReviewedAt(LocalDateTime.now().minusDays(4));
                claim3.setReviewNotes("Driver license verified in person at Pier 9 Maritime Lost & Found Desk.");
                claimRepository.save(claim3);

                // Return Record for Claim 3
                if (returnRecordRepository.count() == 0) {
                    ReturnRecord returnRecord = new ReturnRecord(
                            foundItem4,
                            claim3,
                            staff,
                            LocalDateTime.now().minusDays(4).plusHours(2),
                            "Successfully handed over to Elena Rostova. Photo ID logged and return acknowledgment certificate signed."
                    );
                    returnRecordRepository.save(returnRecord);
                }
            }

            logger.info("Rich transit items, pending/approved claims, and verified return records successfully seeded!");
        }
    }
}

