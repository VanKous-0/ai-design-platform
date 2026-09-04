package com.project.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.common.result.Result;
import com.project.common.result.ResultCode;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] WHITE_LIST = {
            "/api/auth/login",
            "/doc.html",
            "/knife4j/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/webjars/**",
            "/favicon.ico",
            "/assets/**",
            "/actuator/health",
            "/error"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            ObjectMapper objectMapper,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write(objectMapper.writeValueAsString(
                                    Result.failed(ResultCode.UNAUTHORIZED, "请先登录")
                            ));
                        })
                        .accessDeniedHandler((request, response, exception) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write(objectMapper.writeValueAsString(
                                    Result.failed(ResultCode.FORBIDDEN, "没有访问权限")
                            ));
                        }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/stages", "/api/stages/*", "/api/stages/*/steps").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/workflow-templates", "/api/workflow-templates/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tools", "/api/tools/*", "/api/tools/*/evaluations", "/api/tools/recommend").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/prompts", "/api/prompts/*", "/api/prompts/search", "/api/prompts/recommend", "/api/prompts/by-node", "/api/prompts/*/parameters", "/api/prompts/*/revisions", "/api/prompts/*/revisions/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/prompts/*/render").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/prompts/*/copy").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/cases", "/api/cases/*", "/api/cases/*/assets", "/api/cases/recommend").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews", "/api/reviews/*", "/api/reviews/*/assets", "/api/reviews/recommend").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/site/home", "/api/site/contents", "/api/site/contents/*", "/api/awards", "/api/awards/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/achievements", "/api/achievements/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ratings/tools/summary", "/api/ratings/workflows/summary").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/usage-events", "/api/survey-feedback").permitAll()
                        .requestMatchers(WHITE_LIST).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
