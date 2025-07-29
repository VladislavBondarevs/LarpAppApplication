package de.larphelden.larp_app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/login","/error-page","/register_user", "/user_home/**" ,"/user_edit","/user_list",
                                "/register_organisation/**", "/organisation_request", "/pending_organisations","/organisations_list"
                                ,"/register_location","/location_view", "/location_list","/event_list","/event_view","/event_home","event_request",
                                "/approve-event", "/uploads/**","/choose_fraktion","/unit/**","/fraktion/**").permitAll()
                        .requestMatchers("/admin/**", "/admin_dashboard", "/grant-admin","/location_edit","/event_edit","/event_list"
                                ,"/organisation_edit/**","/fraktion_create","/fraktion/**","/unit/**","/sondercharakter/**",
                                "/admin/sondercharakter/approve/**", "/admin/sondercharakter/reject/**").hasRole("ADMIN")
                        .requestMatchers("/organisation_edit/**","/organisation_home","/event_edit","/fraktion_create",
                                "/fraktion/**","/unit/**","/sondercharakter/**").hasRole("ORGANISATOR")
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(customSuccessHandler())
                        .permitAll()
                )
                .logout((logout) -> logout
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/home")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return (request, response, authentication) -> {
            if (authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                response.sendRedirect("/admin_dashboard");
            } else {
                response.sendRedirect("/user_home");
            }
        };
    }
}
