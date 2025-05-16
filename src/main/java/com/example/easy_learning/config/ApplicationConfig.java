package com.example.easy_learning.config;

import com.example.easy_learning.security.JwtTokenFilter;
import com.example.easy_learning.security.UserJwtUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class ApplicationConfig implements WebMvcConfigurer {

  private final UserJwtUserDetailsService userDetailsService;
  private final JwtTokenFilter jwtFilter;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
    AuthenticationManagerBuilder builder =
            http.getSharedObject(AuthenticationManagerBuilder.class);
    builder.authenticationProvider(authenticationProvider());
    return builder.build();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                    // Если не аутентифицирован (нет токена) — редирект на /frontend/login
                    .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/frontend/login"))
                    // Если аутентифицирован, но нет нужной роли — редирект на /frontend?denied
                    .accessDeniedHandler((request, response, denied) ->
                            response.sendRedirect("/frontend?denied")
                    )
            )
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(authz -> authz
                    .requestMatchers("/frontend/login", "/frontend/login-page", "/frontend/register").permitAll()
                    .requestMatchers(HttpMethod.POST, "/frontend/tasks/assign").hasRole("TUTOR")
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                    .requestMatchers("/uploads/**").permitAll()
                    .requestMatchers("/frontend/**").authenticated()
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    .requestMatchers("/api/**").authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .logout(logout -> logout
                    //  при логауте
                    .logoutUrl("/frontend/logout")
                    // после успешного логаута — на страницу входа
                    .logoutSuccessUrl("/frontend/login")
                    // очищаем куку с именем accessToken
                    .deleteCookies("accessToken")
                    .permitAll()
            );

    return http.build();
  }

//  @Override
//  public void addResourceHandlers(ResourceHandlerRegistry registry) {
//    // Все запросы /uploads/** будут уходить в файловую систему uploadDir
//    String absolutePath = Paths.get(System.getProperty("user.dir"), "uploads")
//            .toUri().toString();
//    registry
//            .addResourceHandler("/uploads/**")
//            .addResourceLocations(absolutePath);
//  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:/app/uploads/");
  }

}
