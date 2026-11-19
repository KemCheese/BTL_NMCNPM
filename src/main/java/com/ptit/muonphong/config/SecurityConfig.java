package com.ptit.muonphong.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword == null ? "" : rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return rawPassword != null && encodedPassword != null
                        && encodedPassword.equals(rawPassword.toString());
            }
        };
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            if (exception instanceof AuthenticationServiceException
                    || exception instanceof InternalAuthenticationServiceException) {
                response.sendRedirect(request.getContextPath() + "/login?systemError=true");
            } else {
                response.sendRedirect(request.getContextPath() + "/login?error=true");
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(new EmptyCredentialFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/error").permitAll()
                        .requestMatchers("/tra-cuu-lich/**", "/nop-don/**", "/theo-doi-don/**").hasRole("NGUOI_MUON")
                        .requestMatchers("/duyet-cap-1/**").hasRole("CAN_BO_DOAN")
                        .requestMatchers("/duyet-cap-2/**", "/quan-ly-phong/**", "/bao-cao/**").hasRole("CAN_BO_CSVC")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/trang-chu", true)
                        .failureHandler(authenticationFailureHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/403")
                );

        return http.build();
    }

    private static class EmptyCredentialFilter extends OncePerRequestFilter {
        private final AntPathRequestMatcher loginPostMatcher = new AntPathRequestMatcher("/login", "POST");

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            if (loginPostMatcher.matches(request)) {
                String username = request.getParameter("username");
                String password = request.getParameter("password");
                if (username == null || username.isBlank() || password == null || password.isBlank()) {
                    response.sendRedirect(request.getContextPath() + "/login?empty=true");
                    return;
                }
            }
            filterChain.doFilter(request, response);
        }
    }
}
