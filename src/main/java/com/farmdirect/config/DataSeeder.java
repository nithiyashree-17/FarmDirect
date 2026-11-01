package com.farmdirect.config;

import com.farmdirect.entity.*;
import com.farmdirect.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Seeds the database with realistic Tamil Nadu dummy data.
 * Runs ONLY when the database is empty (no users exist).
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepo;
    private final FarmerProfileRepository profileRepo;
    private final ProductRepository productRepo;
    private final DeliveryAgentRepository agentRepo;
    private final PasswordEncoder encoder;

    public DataSeeder(UserRepository userRepo,
                      FarmerProfileRepository profileRepo,
                      ProductRepository productRepo,
                      DeliveryAgentRepository agentRepo,
                      PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
        this.productRepo = productRepo;
        this.agentRepo = agentRepo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (userRepo.count() > 0) {
            System.out.println("⏭ Database already seeded. Skipping.");
            return;
        }

        System.out.println("🌾 Seeding FarmDirect database...");
        String pwd = encoder.encode("pass123");

        // ═══════════════ ADMIN ═══════════════
        User admin = createUser("admin", pwd, "Admin Kumar", Role.ADMIN,
                "admin@farmdirect.in", "9876500000",
                "Coimbatore", "Coimbatore", "641001",
                11.0168, 76.9558);

        // ═══════════════ FARMERS ═══════════════

        // Farmer 1: Vegetables — Coimbatore
        User farmer1 = createUser("murugan", pwd, "Murugan Velusamy", Role.FARMER,
                "murugan@farm.in", "9876500001",
                "Pollachi", "Coimbatore", "642001",
                10.6590, 77.0083);
        createProfile(farmer1, "Murugan Organic Farm", 5.0, "Tomato, Brinjal, Drumstick, Okra",
                "Organic", 15, 4.5, 120,
                "Traditional organic farm near Pollachi, growing fresh vegetables using natural manure.");

        // Farmer 2: Fruits — Dindigul
        User farmer2 = createUser("lakshmi", pwd, "Lakshmi Natarajan", Role.FARMER,
                "lakshmi@farm.in", "9876500002",
                "Kodaikanal", "Dindigul", "624101",
                10.2381, 77.4892);
        createProfile(farmer2, "Kodai Hills Fruits", 8.0, "Banana, Mango, Guava, Pomegranate",
                "Traditional", 20, 4.8, 250,
                "Hill-grown fruits from Kodaikanal with natural sweetness. No chemical pesticides.");

        // Farmer 3: Grains & Pulses — Thanjavur
        User farmer3 = createUser("senthil", pwd, "Senthil Pandiyan", Role.FARMER,
                "senthil@farm.in", "9876500003",
                "Thanjavur", "Thanjavur", "613001",
                10.7870, 79.1378);
        createProfile(farmer3, "Cauvery Delta Grains", 12.0, "Rice, Millets, Urad Dal, Toor Dal",
                "Traditional", 25, 4.6, 180,
                "From the fertile Cauvery delta — premium Ponni rice and traditional millets.");

        // Farmer 4: Spices — Theni
        User farmer4 = createUser("kavitha", pwd, "Kavitha Rajan", Role.FARMER,
                "kavitha@farm.in", "9876500004",
                "Theni", "Theni", "625531",
                10.0104, 77.4760);
        createProfile(farmer4, "Western Ghats Spices", 3.5, "Cardamom, Pepper, Cinnamon, Clove",
                "Organic", 18, 4.9, 320,
                "Premium spices from the Western Ghats. Hand-picked, sun-dried, pesticide-free.");

        // Farmer 5: Dairy — Erode
        User farmer5 = createUser("ravi", pwd, "Ravi Gounder", Role.FARMER,
                "ravi@farm.in", "9876500005",
                "Erode", "Erode", "638001",
                11.3410, 77.7172);
        createProfile(farmer5, "Ravi's Dairy Farm", 6.0, "Milk, Curd, Paneer, Ghee",
                "Traditional", 10, 4.3, 90,
                "Farm-fresh dairy products from native breed cows. Delivered within hours of milking.");

        // ═══════════════ CUSTOMERS ═══════════════

        User cust1 = createUser("priya", pwd, "Priya Shankar", Role.CUSTOMER,
                "priya@mail.com", "9876500010",
                "Coimbatore", "Coimbatore", "641018",
                11.0168, 76.9558);

        User cust2 = createUser("arjun", pwd, "Arjun Vijay", Role.CUSTOMER,
                "arjun@mail.com", "9876500011",
                "Madurai", "Madurai", "625001",
                9.9252, 78.1198);

        User cust3 = createUser("deepa", pwd, "Deepa Krishnan", Role.CUSTOMER,
                "deepa@mail.com", "9876500012",
                "Salem", "Salem", "636001",
                11.6643, 78.1460);

        // ═══════════════ PRODUCTS ═══════════════

        LocalDate today = LocalDate.now();

        // Murugan's Vegetables
        createProduct(farmer1, "Tomato", "தக்காளி", "Vegetables", 40, "kg", 200,
                "Fresh farm tomatoes, deep red, juicy", today.minusDays(1), today.plusDays(5), true);
        createProduct(farmer1, "Brinjal", "கத்திரிக்காய்", "Vegetables", 35, "kg", 150,
                "Purple brinjal, perfect for bhartha", today, today.plusDays(6), true);
        createProduct(farmer1, "Drumstick", "முருங்கைக்காய்", "Vegetables", 60, "bundle", 80,
                "Long, tender drumsticks for sambar", today, today.plusDays(4), true);
        createProduct(farmer1, "Lady's Finger", "வெண்டைக்காய்", "Vegetables", 45, "kg", 120,
                "Fresh okra, no spots", today, today.plusDays(3), true);
        createProduct(farmer1, "Curry Leaves", "கறிவேப்பிலை", "Vegetables", 10, "bundle", 500,
                "Aromatic curry leaves", today, today.plusDays(7), true);

        // Lakshmi's Fruits
        createProduct(farmer2, "Banana (Nendran)", "நேந்திரம் வாழை", "Fruits", 50, "dozen", 100,
                "Kerala Nendran variety, great for chips and bajji", today.minusDays(2), today.plusDays(4), false);
        createProduct(farmer2, "Mango (Alphonso)", "மாம்பழம்", "Fruits", 300, "kg", 60,
                "Premium Alphonso mangoes, naturally ripened", today.minusDays(1), today.plusDays(5), false);
        createProduct(farmer2, "Guava", "கொய்யா", "Fruits", 80, "kg", 90,
                "Pink guava from Kodaikanal hills", today, today.plusDays(6), false);
        createProduct(farmer2, "Pomegranate", "மாதுளை", "Fruits", 180, "kg", 70,
                "Large, juicy pomegranates", today, today.plusDays(8), false);

        // Senthil's Grains
        createProduct(farmer3, "Ponni Rice", "பொன்னி அரிசி", "Grains", 65, "kg", 500,
                "Premium Thanjavur Ponni rice, aged 1 year", today.minusDays(30), today.plusDays(180), false);
        createProduct(farmer3, "Ragi (Finger Millet)", "ராகி", "Grains", 55, "kg", 300,
                "Organic ragi, high calcium content", today.minusDays(20), today.plusDays(150), true);
        createProduct(farmer3, "Toor Dal", "துவரம் பருப்பு", "Pulses", 120, "kg", 200,
                "Fresh toor dal, cooks quickly", today.minusDays(15), today.plusDays(120), false);
        createProduct(farmer3, "Urad Dal (Black Gram)", "உளுந்து", "Pulses", 130, "kg", 180,
                "Whole urad dal for idli batter", today.minusDays(10), today.plusDays(120), false);

        // Kavitha's Spices
        createProduct(farmer4, "Cardamom", "ஏலக்காய்", "Spices", 2500, "kg", 30,
                "Theni cardamom — bold, aromatic pods", today.minusDays(5), today.plusDays(365), true);
        createProduct(farmer4, "Black Pepper", "மிளகு", "Spices", 800, "kg", 50,
                "Tellicherry grade black pepper", today.minusDays(10), today.plusDays(300), true);
        createProduct(farmer4, "Cinnamon", "பட்டை", "Spices", 600, "kg", 40,
                "Sri Lankan variety cinnamon sticks", today.minusDays(7), today.plusDays(365), true);
        createProduct(farmer4, "Turmeric Powder", "மஞ்சள் தூள்", "Spices", 200, "kg", 100,
                "Erode turmeric, high curcumin content", today.minusDays(3), today.plusDays(180), true);

        // Ravi's Dairy
        createProduct(farmer5, "Fresh Cow Milk", "பசும்பால்", "Dairy", 60, "litre", 50,
                "A2 milk from native breed cows", today, today.plusDays(2), false);
        createProduct(farmer5, "Curd", "தயிர்", "Dairy", 50, "litre", 40,
                "Thick set curd, naturally set", today, today.plusDays(3), false);
        createProduct(farmer5, "Paneer", "பன்னீர்", "Dairy", 350, "kg", 25,
                "Fresh homemade paneer", today, today.plusDays(4), false);
        createProduct(farmer5, "Pure Ghee", "நெய்", "Dairy", 700, "litre", 20,
                "Traditional cow ghee, slow-cooked", today.minusDays(5), today.plusDays(90), false);

        // ═══════════════ DELIVERY AGENTS ═══════════════

        createAgent("Karthik M", "9876500020", "Bike", "TN-38-AB-1234", "Coimbatore", 4.7, 85);
        createAgent("Suresh R", "9876500021", "Auto", "TN-38-CD-5678", "Coimbatore", 4.5, 120);
        createAgent("Manoj K", "9876500022", "Van", "TN-45-EF-9012", "Pollachi", 4.8, 200);
        createAgent("Ganesh P", "9876500023", "Bike", "TN-58-GH-3456", "Madurai", 4.3, 65);
        createAgent("Dinesh V", "9876500024", "Auto", "TN-33-IJ-7890", "Thanjavur", 4.6, 150);

        System.out.println("✅ Database seeded successfully!");
        System.out.println("   Farmers: 5 | Customers: 3 | Products: " + productRepo.count()
                + " | Agents: " + agentRepo.count());
        System.out.println("   Login with any username and password: pass123");
    }

    private User createUser(String username, String encodedPwd, String fullName, Role role,
                            String email, String phone,
                            String city, String district, String pincode,
                            double lat, double lon) {
        User user = new User(username, encodedPwd, fullName, role);
        user.setEmail(email);
        user.setPhone(phone);
        user.setCity(city);
        user.setDistrict(district);
        user.setPincode(pincode);
        user.setLatitude(lat);
        user.setLongitude(lon);
        return userRepo.save(user);
    }

    private void createProfile(User farmer, String farmName, double acres, String crops,
                               String type, int exp, double rating, int orders, String desc) {
        FarmerProfile profile = new FarmerProfile();
        profile.setUser(farmer);
        profile.setFarmName(farmName);
        profile.setFarmSizeAcres(acres);
        profile.setCropsGrown(crops);
        profile.setFarmingType(type);
        profile.setYearsExperience(exp);
        profile.setRating(rating);
        profile.setTotalOrders(orders);
        profile.setDescription(desc);
        profileRepo.save(profile);
    }

    private void createProduct(User farmer, String name, String nameTamil, String category,
                               double price, String unit, double stock,
                               String desc, LocalDate harvest, LocalDate expiry, boolean organic) {
        Product p = new Product();
        p.setFarmer(farmer);
        p.setName(name);
        p.setNameTamil(nameTamil);
        p.setCategory(category);
        p.setPricePerUnit(price);
        p.setUnit(unit);
        p.setStockQuantity(stock);
        p.setDescription(desc);
        p.setHarvestDate(harvest);
        p.setExpiryDate(expiry);
        p.setOrganic(organic);
        p.setAvailable(true);
        p.setRating(3.5 + Math.random() * 1.5); // 3.5–5.0
        p.setReviewCount((int)(Math.random() * 50));
        productRepo.save(p);
    }

    private void createAgent(String name, String phone, String vehicle,
                             String number, String area, double rating, int deliveries) {
        DeliveryAgent agent = new DeliveryAgent();
        agent.setName(name);
        agent.setPhone(phone);
        agent.setVehicleType(vehicle);
        agent.setVehicleNumber(number);
        agent.setArea(area);
        agent.setRating(rating);
        agent.setCompletedDeliveries(deliveries);
        agent.setAvailable(true);
        agentRepo.save(agent);
    }
}
