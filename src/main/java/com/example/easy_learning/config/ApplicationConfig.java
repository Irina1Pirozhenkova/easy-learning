package com.example.easy_learning.config;

import com.example.easy_learning.security.JwtTokenFilter;
import com.example.easy_learning.security.JwtTokenProvider;
import com.example.easy_learning.security.StudentJwtUserDetailsService;
import com.example.easy_learning.security.TutorJwtUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class ApplicationConfig {

  private final JwtTokenProvider tokenProvider;
  private final ApplicationContext applicationContext;
  private final StudentJwtUserDetailsService studentJwtUserDetailsService;
  private final TutorJwtUserDetailsService tutorJwtUserDetailsService;

  // Можно использовать один PasswordEncoder для обоих провайдеров
  @Bean
  public PasswordEncoder passwordEncoder() { // хэширование паролей
    return new BCryptPasswordEncoder();
  }

  // Создаём DaoAuthenticationProvider для студентов
  @Bean
  public DaoAuthenticationProvider studentDaoAuthenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(studentJwtUserDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  // Создаём DaoAuthenticationProvider для репетиторов
  @Bean
  public DaoAuthenticationProvider tutorDaoAuthenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(tutorJwtUserDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  // Регистрируем оба провайдера в AuthenticationManager
  @Bean
  @SneakyThrows
  public AuthenticationManager authenticationManager(HttpSecurity http) {
    AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
    authBuilder.authenticationProvider(studentDaoAuthenticationProvider());
    authBuilder.authenticationProvider(tutorDaoAuthenticationProvider());
    return authBuilder.build();
  }

  // Конфигурация фильтров и остальных настроек безопасности
  @Bean
  @SneakyThrows
  public SecurityFilterChain filterChain(HttpSecurity httpSecurity) {
    httpSecurity
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .sessionManagement(sessionManagement ->
                    sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(configurer ->
                    configurer.authenticationEntryPoint(
                            (request, response, exception) -> {
                              response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 401
                              response.getWriter().write("Unauthorized.");
                            }
                    ).accessDeniedHandler(
                            (request, response, exception) -> {
                              response.setStatus(HttpStatus.FORBIDDEN.value());
                              response.getWriter().write("Unauthorized.");
                            }
                    )
            )
            .authorizeHttpRequests(configurer ->
                    configurer
                            .requestMatchers("/api/v1/auth/**").permitAll()
                            .requestMatchers("/swagger-ui/**").permitAll()
                            .requestMatchers("/v3/api-docs/**").permitAll()
                            .anyRequest().authenticated()
            )
            .anonymous(AbstractHttpConfigurer::disable)
            .addFilterBefore(new JwtTokenFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class);

    return httpSecurity.build();
  }
}
