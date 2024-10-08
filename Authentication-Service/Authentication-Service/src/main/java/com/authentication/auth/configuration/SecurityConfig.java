package com.authentication.auth.configuration;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.oauth2.core.OAuth2Error;
//
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
	    private String issuer;
	
	    @Value("${auth0.audience}")
	    private String audience;

	    @Bean
	    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	        http
	            .csrf(csrf -> csrf.disable())
	            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
	            .authorizeHttpRequests(authz -> authz
	                .requestMatchers("/auth/**").permitAll()
	                .anyRequest().authenticated()
	            )
	            .oauth2ResourceServer(oauth2 -> oauth2
	                .jwt(jwt -> jwt.decoder(jwtDecoder()))
	                .authenticationEntryPoint((request, response, authException) -> {
	                    String token = request.getHeader("Authorization");
	                    
	                    if (token != null && token.startsWith("Bearer ")) {
	                        String[] parts = token.substring(7).split("\\.");
	                        
	                        
	                    }
	                    System.out.println("JWT validation failed: " + authException.getMessage());
	                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: " + authException.getMessage());
	                })
	            );
	        
	        return http.build();
	    	
	    }
	    @Bean
	  public JwtDecoder jwtDecoder() {
	      NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder)
	              JwtDecoders.fromOidcIssuerLocation(issuer);
	
	      OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);
	      OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
	      OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);
	
	      jwtDecoder.setJwtValidator(withAudience);
	
	      return token -> {
	          try {
	              return jwtDecoder.decode(token);
	          } catch (Exception e) {
	              System.out.println("JWT decoding failed: " + e.getMessage());
	              throw e;
	          }
	      };
	  }

	  @Bean
	  public CorsConfigurationSource corsConfigurationSource() {
	      CorsConfiguration configuration = new CorsConfiguration();
	      configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
	      configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
	      configuration.setAllowCredentials(true);
	      configuration.setAllowedHeaders(Arrays.asList("*"));
	      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	      source.registerCorsConfiguration("/**", configuration);
	      return source;
	  }
	  class AudienceValidator implements OAuth2TokenValidator<Jwt> {
	    private final String audience;
	
	    AudienceValidator(String audience) {
	        this.audience = audience;
	    }
	
	    public OAuth2TokenValidatorResult validate(Jwt jwt) {
	        
	        if (jwt.getAudience().contains(audience)) {
	            return OAuth2TokenValidatorResult.success();
	        }
	        System.out.println("Audience validation failed");
	        return OAuth2TokenValidatorResult.failure(
	            new OAuth2Error("The token was not issued for the correct audience")
	        );
	    }
	}
}
