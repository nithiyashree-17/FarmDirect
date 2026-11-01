package com.farmdirect.views.customer;

import com.farmdirect.entity.User;
import com.farmdirect.security.SecurityUtils;
import com.farmdirect.service.ProductService;
import com.farmdirect.service.UserService;
import com.farmdirect.views.MainLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@Route(value = "customer/nearby", layout = MainLayout.class)
@PageTitle("Nearby Farmers")
@RolesAllowed("CUSTOMER")
public class NearbyFarmersView extends VerticalLayout {

    public NearbyFarmersView(UserService userService, ProductService productService) {
        addClassName("nearby-view");
        setPadding(true);

        User customer = userService.findByUsername(SecurityUtils.getLoggedInUsername()).orElseThrow();

        H2 title = new H2("Nearby Farmers");
        Paragraph subtitle = new Paragraph("Showing farmers within 50 km of your location ("
                + customer.getCity() + ")");
        add(title, subtitle);

        // Get all active farmers and calculate distances
        List<User> allFarmers = userService.findActiveFarmers();

        // Sort by distance
        List<FarmerDistance> nearbyFarmers = allFarmers.stream()
                .map(f -> new FarmerDistance(f,
                        ProductService.haversineDistance(
                                customer.getLatitude(), customer.getLongitude(),
                                f.getLatitude(), f.getLongitude())))
                .filter(fd -> fd.distance <= 50.0)
                .sorted((a, b) -> Double.compare(a.distance, b.distance))
                .toList();

        if (nearbyFarmers.isEmpty()) {
            add(new Paragraph("No farmers found within 50 km. Try browsing all products instead."));
            return;
        }

        // OpenStreetMap embed showing the area
        String mapUrl = String.format(
                "https://www.openstreetmap.org/export/embed.html?bbox=%.4f,%.4f,%.4f,%.4f&layer=mapnik",
                customer.getLongitude() - 0.5, customer.getLatitude() - 0.3,
                customer.getLongitude() + 0.5, customer.getLatitude() + 0.3
        );
        IFrame map = new IFrame(mapUrl);
        map.setWidth("100%");
        map.setHeight("300px");
        map.getElement().setAttribute("loading", "lazy");
        map.addClassName("nearby-map");
        add(map);

        // Farmer cards with distance
        for (FarmerDistance fd : nearbyFarmers) {
            User farmer = fd.farmer;
            Div card = new Div();
            card.addClassName("farmer-card");

            H3 name = new H3(farmer.getFullName());
            Paragraph location = new Paragraph(farmer.getCity()
                    + ", " + farmer.getDistrict()
                    + " — " + String.format("%.1f", fd.distance) + " km away");
            Paragraph phone = new Paragraph("Phone: " + (farmer.getPhone() != null ? farmer.getPhone() : "N/A"));

            long productCount = productService.countByFarmer(farmer);
            Paragraph products = new Paragraph(productCount + " products available");

            card.add(name, location, phone, products);
            add(card);
        }
    }

    private record FarmerDistance(User farmer, double distance) {}
}
