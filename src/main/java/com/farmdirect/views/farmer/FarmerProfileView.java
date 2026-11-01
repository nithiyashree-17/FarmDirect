package com.farmdirect.views.farmer;

import com.farmdirect.entity.FarmerProfile;
import com.farmdirect.entity.User;
import com.farmdirect.repository.FarmerProfileRepository;
import com.farmdirect.security.SecurityUtils;
import com.farmdirect.service.UserService;
import com.farmdirect.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "farmer/profile", layout = MainLayout.class)
@PageTitle("Farm Profile")
@RolesAllowed("FARMER")
public class FarmerProfileView extends VerticalLayout {

    public FarmerProfileView(UserService userService, FarmerProfileRepository profileRepo) {
        addClassName("profile-view");
        setPadding(true);

        User farmer = userService.findByUsername(SecurityUtils.getLoggedInUsername()).orElseThrow();
        FarmerProfile profile = profileRepo.findByUser(farmer).orElseGet(() -> {
            FarmerProfile p = new FarmerProfile();
            p.setUser(farmer);
            return p;
        });

        H2 title = new H2("Farm Profile");

        TextField farmName = new TextField("Farm Name");
        farmName.setValue(profile.getFarmName() != null ? profile.getFarmName() : "");

        NumberField farmSize = new NumberField("Farm Size (acres)");
        farmSize.setValue(profile.getFarmSizeAcres());

        TextField crops = new TextField("Crops Grown (comma-separated)");
        crops.setValue(profile.getCropsGrown() != null ? profile.getCropsGrown() : "");

        ComboBox<String> farmingType = new ComboBox<>("Farming Type");
        farmingType.setItems("Organic", "Traditional", "Hydroponic", "Mixed");
        farmingType.setValue(profile.getFarmingType() != null ? profile.getFarmingType() : "Traditional");

        NumberField experience = new NumberField("Years of Experience");
        experience.setValue((double) profile.getYearsExperience());

        TextArea description = new TextArea("About Your Farm");
        description.setValue(profile.getDescription() != null ? profile.getDescription() : "");

        Checkbox delivery = new Checkbox("Offer Delivery", profile.isOffersDelivery());
        Checkbox pickup = new Checkbox("Offer Pickup", profile.isOffersPickup());

        // User details
        TextField phone = new TextField("Phone");
        phone.setValue(farmer.getPhone() != null ? farmer.getPhone() : "");

        TextField city = new TextField("City");
        city.setValue(farmer.getCity() != null ? farmer.getCity() : "");

        TextField district = new TextField("District");
        district.setValue(farmer.getDistrict() != null ? farmer.getDistrict() : "");

        FormLayout form = new FormLayout(farmName, farmSize, crops, farmingType,
                experience, description, delivery, pickup, phone, city, district);
        form.setMaxWidth("600px");

        Button save = new Button("Save Profile", e -> {
            profile.setFarmName(farmName.getValue());
            profile.setFarmSizeAcres(farmSize.getValue() != null ? farmSize.getValue() : 0);
            profile.setCropsGrown(crops.getValue());
            profile.setFarmingType(farmingType.getValue());
            profile.setYearsExperience(experience.getValue() != null ? experience.getValue().intValue() : 0);
            profile.setDescription(description.getValue());
            profile.setOffersDelivery(delivery.getValue());
            profile.setOffersPickup(pickup.getValue());
            profileRepo.save(profile);

            farmer.setPhone(phone.getValue());
            farmer.setCity(city.getValue());
            farmer.setDistrict(district.getValue());
            userService.save(farmer);

            Notification.show("Profile saved!", 2000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(title, form, save);
    }
}
