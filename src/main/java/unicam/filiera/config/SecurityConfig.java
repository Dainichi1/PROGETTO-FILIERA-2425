package unicam.filiera.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import unicam.filiera.security.RoleCheckingAuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final RoleCheckingAuthenticationSuccessHandler successHandler;

    public SecurityConfig(RoleCheckingAuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/h2-console/**",
                                "/trasformatore/elimina/**",
                                "/distributore/elimina/**",
                                "/produttore/elimina/**"   // disabilito CSRF per DELETE prodotto
                        )
                )
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/register",
                                "/login",
                                "/h2-console/**",
                                "/css/**",
                                "/js/**",
                                "/marketplace/**",       //  Marketplace visibile a tutti
                                "/api/social/**",        //  Social Feed visibile a tutti
                                "/gestore/markers/api",  //  Marker mappa
                                "/gestore/paths/api"     //  Path trasformati
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                .formLogin(f -> f
                        .loginPage("/login")
                        .loginProcessingUrl("/doLogin")
                        .successHandler(successHandler)      // verifica ruolo selezionato
                        .failureUrl("/login?error=credenziali")
                        .permitAll()
                )
                .logout(l -> l
                        // accetta GET, POST, DELETE su /logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }
}
