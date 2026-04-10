package br.com.knowledge.stockonyou.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // desabilita CSRF para facilitar testes
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated() // exige autenticação em todos os endpoints
                )
                .httpBasic(withDefaults()); // habilita autenticação básica
        return http.build();
    }
}
