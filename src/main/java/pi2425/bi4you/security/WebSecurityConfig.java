package pi2425.bi4you.security;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import pi2425.bi4you.security.jwt.AuthEntryPointJwt;
import pi2425.bi4you.security.jwt.AuthTokenFilter;
import pi2425.bi4you.security.jwt.JwtUtils;
import pi2425.bi4you.security.services.UserDetailsServiceImpl;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE )
public class WebSecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthEntryPointJwt unauthorizedHandler;
    private final JwtUtils jwtUtils;

    public WebSecurityConfig(UserDetailsServiceImpl userDetailsService, AuthEntryPointJwt unauthorizedHandler, JwtUtils jwtUtils) {
        this.userDetailsService = userDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtUtils = jwtUtils;
    }

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter(jwtUtils, userDetailsService);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http ) throws Exception {
        http
                .cors(cors -> cors
                        .configurationSource(request -> {
                            CorsConfiguration corsConfig = new CorsConfiguration( );
                            corsConfig.setAllowCredentials(true);
                            corsConfig.addAllowedOrigin("http://localhost:4200" );
                            corsConfig.addAllowedHeader("*");
                            corsConfig.addAllowedMethod("*");
                            return corsConfig;
                        })
                )
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth
                                // La méthode moderne pour spécifier les matchers
                                .requestMatchers("/api/auth/**").permitAll()
                                // Vous pouvez ajouter d'autres routes publiques ici si nécessaire
                                // .requestMatchers("/public/**").permitAll()
                                .anyRequest().authenticated() // Toutes les autres requêtes nécessitent une authentification
                );

        http.authenticationProvider(authenticationProvider( ));
        http.addFilterBefore(authenticationJwtTokenFilter( ), UsernamePasswordAuthenticationFilter.class);

        return http.build( );
    }
}
