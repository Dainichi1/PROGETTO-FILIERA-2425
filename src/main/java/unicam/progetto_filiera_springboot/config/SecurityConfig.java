package unicam.progetto_filiera_springboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // non usiamo il login di Spring, il tuo login custom imposta l'Authentication
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**")) // utile in dev
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))            // per H2 console
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/register/**", "/login/**", "/h2-console/**", "/css/**", "/js/**", "/files/**").permitAll()
                        .anyRequest().permitAll()
                )
                .formLogin(f -> f.disable())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
