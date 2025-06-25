package com.nurtel.vaskamailio.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Value("${ldap.url}")
    String ldapUrl;
    @Value("${ldap.domain}")
    String ldapDomain;
    @Value("${ldap.user}")
    String ldapUser;
    @Value("${ldap.password}")
    String ldapPassword;
    @Value("${ldap.base}")
    String ldapBase;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .maximumSessions(1)
                )
                //.userDetailsService(userAuthService)
                .build();
    }

    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource source = new LdapContextSource();
        source.setUrl(ldapUrl);
        source.setBase(ldapBase);

        source.setUserDn(ldapUser);
        source.setPassword(ldapPassword);

        source.afterPropertiesSet();
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        BindAuthenticator bindAuthenticator = new BindAuthenticator(contextSource());
        // Ищем пользователя по sAMAccountName
        bindAuthenticator.setUserSearch(new FilterBasedLdapUserSearch(
                "", // поиск будет по базовому DN contextSource()
                "(sAMAccountName={0})",
                contextSource()
        ));

        LdapAuthenticationProvider provider = new LdapAuthenticationProvider(bindAuthenticator);
        return new ProviderManager(provider);
    }


    private static SimpleAuthorityMapper getAuthorityMapper()
    {
        SimpleAuthorityMapper mapper = new SimpleAuthorityMapper();
        mapper.setConvertToUpperCase(true);
        return mapper;
    }
}
