package com.farmdirect.security;

import com.farmdirect.views.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.util.Locale;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends VaadinWebSecurity {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Allow H2 console
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll()
        );
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        super.configure(http);
        setLoginView(http, LoginView.class);
        http.formLogin(form -> form.successHandler((request, response, authentication) -> {
            String requestedRole = request.getParameter("role");
            String selectedRole = requestedRole == null
                    ? null
                    : requestedRole.toLowerCase(Locale.ROOT);
            Set<String> supportedRoles = Set.of("farmer", "customer", "admin");
            String actualRole = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(authority -> authority.startsWith("ROLE_"))
                    .map(authority -> authority.substring("ROLE_".length()))
                    .findFirst()
                    .orElse("");

            if (selectedRole != null && !selectedRole.isBlank()
                    && (!supportedRoles.contains(selectedRole)
                    || !selectedRole.equalsIgnoreCase(actualRole))) {
                new SecurityContextLogoutHandler().logout(request, response, authentication);
                String destination = supportedRoles.contains(selectedRole)
                        ? "/login?role=" + selectedRole + "&roleMismatch"
                        : "/login?error";
                response.sendRedirect(request.getContextPath() + destination);
                return;
            }

            String destination = switch (actualRole) {
                case "FARMER" -> "/farmer/dashboard";
                case "CUSTOMER" -> "/customer/browse";
                case "ADMIN" -> "/admin/dashboard";
                default -> null;
            };
            if (destination == null) {
                new SecurityContextLogoutHandler().logout(request, response, authentication);
                response.sendRedirect(request.getContextPath() + "/login?error");
                return;
            }
            response.sendRedirect(request.getContextPath() + destination);
        }));
    }
}
