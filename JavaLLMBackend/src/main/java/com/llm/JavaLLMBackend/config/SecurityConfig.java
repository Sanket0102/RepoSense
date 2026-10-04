package com.llm.JavaLLMBackend.config;

import com.llm.JavaLLMBackend.security.GithubOauth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@EnableWebSecurity
@Component
public class SecurityConfig {

    @Autowired
    private GithubOauth2UserService githubOauth2UserService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Autowired
    private CorsConfig corsConfig;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http){
        return http.httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .sessionManagement(session -> {
                    session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
                })
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(
                                    "/api/auth/login-url",
                                    "/oauth2/**",
                                    "/login/oauth2",
                                    "/error"
                            ).permitAll()
                            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                            .requestMatchers("/api/**").authenticated()
                            .anyRequest().permitAll();
                })
                .oauth2Login(oauth -> {
                    oauth.userInfoEndpoint(userInfo -> userInfo.userService(githubOauth2UserService))
                            .successHandler(oauth2SuccessHandler())
                            .failureHandler(oauth2FailureHandler());
                })
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(((request, response, authentication) -> {
                            response.setStatus(HttpStatus.NO_CONTENT.value());
                        }))
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("LLM_SESSION")

                )
                .exceptionHandling(ex -> new RuntimeException("Error Occured"))
                .build();
    }

    @Bean
    public AuthenticationSuccessHandler oauth2SuccessHandler(){
        SimpleUrlAuthenticationSuccessHandler handler = new SimpleUrlAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl(frontendUrl + "/auth/callback");
        return handler;
    }

    @Bean
    public AuthenticationFailureHandler oauth2FailureHandler(){
        SimpleUrlAuthenticationFailureHandler handler = new SimpleUrlAuthenticationFailureHandler();
        handler.setDefaultFailureUrl(frontendUrl + "/login?error=oauth_failed");
        return handler;
    }
}
