package com.farmdirect;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Theme("farmdirect")
public class FarmDirectApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(FarmDirectApplication.class, args);
    }
}
