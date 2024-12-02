package org.example.thuan_security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailService userDetailService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomPermissionEvaluator customPermissionEvaluator;

    @Value("${keycloak.enabled}")
    private boolean isKeycloakEnabled;

    @Value("${idp.jwt-endpoint}")
    private String jwtEndpoint;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(jwtEndpoint);
        jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(jwtEndpoint));
        return jwtDecoder;
    }

    private final String[] SWAGGER_ENDPOINT = {"/swagger-ui.html", "/v3/api-docs/**", "/swagger-ui/**","/swagger-ui/index.html"};

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        if (isKeycloakEnabled) {
            httpSecurity
                    .csrf(csrf -> csrf.disable())
                    .authorizeRequests(authorizeRequests -> {
                        authorizeRequests.requestMatchers("api/auth/**").permitAll();
                        authorizeRequests.requestMatchers(SWAGGER_ENDPOINT).permitAll();
                        authorizeRequests.anyRequest().authenticated();
                    })
//                    .httpBasic(Customizer.withDefaults())
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())
                            .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                    .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        } else {
            httpSecurity
                    .csrf(csrf -> csrf.disable())
                    .authorizeRequests(authorizeRequests -> {
                        authorizeRequests.requestMatchers("api/auth/**").permitAll();
                        authorizeRequests.requestMatchers(SWAGGER_ENDPOINT).permitAll();
                        authorizeRequests.anyRequest().authenticated();
                    })
                    .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        }


        return httpSecurity.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
