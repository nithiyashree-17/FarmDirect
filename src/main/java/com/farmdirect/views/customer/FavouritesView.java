package com.farmdirect.views.customer;

import com.farmdirect.entity.Favorite;
import com.farmdirect.entity.Product;
import com.farmdirect.entity.User;
import com.farmdirect.security.SecurityUtils;
import com.farmdirect.service.CartService;
import com.farmdirect.service.FavoriteService;
import com.farmdirect.service.UserService;
import com.farmdirect.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@Route(value = "customer/favourites", layout = MainLayout.class)
@PageTitle("My Favourites")
@RolesAllowed("CUSTOMER")
public class FavouritesView extends VerticalLayout {

    public FavouritesView(FavoriteService favoriteService, CartService cartService,
                          UserService userService) {
        addClassName("favourites-view");
        setPadding(true);

        H2 title = new H2("My Favourites");
        add(title);

        User customer = userService.findByUsername(SecurityUtils.getLoggedInUsername()).orElseThrow();
        List<Favorite> favs = favoriteService.findByCustomer(customer);

        if (favs.isEmpty()) {
            add(new Paragraph("No favourites yet. Browse products and add some to your favourites!"));
            return;
        }

        FlexLayout grid = new FlexLayout();
        grid.addClassName("product-grid");
        grid.setFlexWrap(FlexLayout.FlexWrap.WRAP);

        for (Favorite fav : favs) {
            Product p = fav.getProduct();

            Div card = new Div();
            card.addClassName("product-card");

            H3 name = new H3(p.getName());
            Paragraph price = new Paragraph("₹" + String.format("%.0f", p.getPricePerUnit()) + "/" + p.getUnit());
            price.addClassName("product-price");
            Paragraph farmer = new Paragraph(p.getFarmer().getFullName());

            Button addToCart = new Button("Add to Cart", e -> {
                cartService.addToCart(p, 1);
                Notification.show("Added to cart!", 2000, Notification.Position.BOTTOM_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            });
            addToCart.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

            Button removeFav = new Button("Remove", e -> {
                favoriteService.toggleFavorite(customer, p);
                getUI().ifPresent(ui -> ui.getPage().reload());
            });
            removeFav.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

            card.add(name, price, farmer, new HorizontalLayout(addToCart, removeFav));
            grid.add(card);
        }

        add(grid);
    }
}
