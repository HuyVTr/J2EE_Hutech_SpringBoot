package fit.hutech.spring.utils;

import java.util.ArrayList;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.SecurityFilterChain;

import fit.hutech.spring.services.OAuthService;
import fit.hutech.spring.services.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

        private final OAuthService oAuthService;
        private final UserService userService;

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(@NotNull HttpSecurity http) throws Exception {
                return http.authorizeHttpRequests(auth -> auth
                                // Cho phép truy cập công khai
                                .requestMatchers("/css/**", "/js/**", "/", "/oauth/**", "/register", "/error")
                                .permitAll()
                                // Phân quyền ADMIN cho các thao tác quản trị
                                .requestMatchers("/books/edit/**", "/books/add", "/books/delete/**")
                                .hasAnyAuthority("ADMIN")
                                // Cấu hình phân quyền cho API theo hình 8.1
                                .requestMatchers("/api/**")
                                .hasAnyAuthority("ADMIN", "USER")
                                // Phân quyền cho người dùng xem sách và giỏ hàng
                                .requestMatchers("/books", "/cart", "/cart/**")
                                .hasAnyAuthority("ADMIN", "USER")
                                .anyRequest().authenticated())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login")
                                                .deleteCookies("JSESSIONID")
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true)
                                                .permitAll())
                                .formLogin(formLogin -> formLogin
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .defaultSuccessUrl("/")
                                                .failureUrl("/login?error")
                                                .permitAll())
                                .oauth2Login(oauth2Login -> oauth2Login
                                                .loginPage("/login")
                                                .failureUrl("/login?error")
                                                .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
                                                                .userService(oAuthService))
                                                // Tích hợp Success Handler lưu người dùng OAuth vào DB
                                                .successHandler((request, response, authentication) -> {
                                                        var oidcUser = (DefaultOidcUser) authentication.getPrincipal();
                                                        userService.saveOauthUser(oidcUser.getEmail(),
                                                                        oidcUser.getName());

                                                        // --- FIX: Lấy user từ DB và cập nhật SecurityContext để có
                                                        // quyền (Role) ---
                                                        var dbUser = userService.findByEmail(oidcUser.getEmail())
                                                                        .orElse(null);
                                                        if (dbUser != null) {
                                                                var authorities = new ArrayList<GrantedAuthority>(
                                                                                oidcUser.getAuthorities());
                                                                dbUser.getRoles().forEach(role -> authorities
                                                                                .add(new SimpleGrantedAuthority(
                                                                                                role.getName())));
                                                                var newToken = new OAuth2AuthenticationToken(oidcUser,
                                                                                authorities,
                                                                                ((OAuth2AuthenticationToken) authentication)
                                                                                                .getAuthorizedClientRegistrationId());
                                                                SecurityContextHolder.getContext()
                                                                                .setAuthentication(newToken);
                                                        }
                                                        // -------------------------------------------------------------------------

                                                        response.sendRedirect("/");
                                                })
                                                .permitAll())
                                .rememberMe(rememberMe -> rememberMe
                                                .key("hutech")
                                                .rememberMeCookieName("hutech")
                                                .tokenValiditySeconds(24 * 60 * 60)
                                                .userDetailsService(userService))
                                .exceptionHandling(exceptionHandling -> exceptionHandling
                                                // Cấu hình trang lỗi 403 khi bị từ chối truy cập
                                                .accessDeniedPage("/403"))
                                .sessionManagement(sessionManagement -> sessionManagement
                                                .maximumSessions(1)
                                                .expiredUrl("/login"))
                                .httpBasic(httpBasic -> httpBasic.realmName("hutech"))
                                .build();
        }
}