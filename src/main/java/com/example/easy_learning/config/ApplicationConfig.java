package com.example.easy_learning.config;

import com.example.easy_learning.security.JwtTokenFilter;
import com.example.easy_learning.security.JwtTokenProvider;
import com.example.easy_learning.security.StudentJwtUserDetailsService;
import com.example.easy_learning.security.TutorJwtUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpMethod;
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
  @Bean //Для хеширования и проверки паролей
  public PasswordEncoder passwordEncoder() { // хэширование паролей
    return new BCryptPasswordEncoder();
  }

  // Создаём DaoAuthenticationProvider для студентов
  @Bean//Подключает UserDetailsService для логина
  public DaoAuthenticationProvider studentDaoAuthenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(studentJwtUserDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  // Создаём DaoAuthenticationProvider для репетиторов
  @Bean//Подключает UserDetailsService для логина
  public DaoAuthenticationProvider tutorDaoAuthenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(tutorJwtUserDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  // Регистрируем оба провайдера в AuthenticationManager
  @Bean
  @SneakyThrows//Проверяет логин/пароль
  public AuthenticationManager authenticationManager(HttpSecurity http) {
    AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
    authBuilder.authenticationProvider(studentDaoAuthenticationProvider());
    authBuilder.authenticationProvider(tutorDaoAuthenticationProvider());
    return authBuilder.build();
  }

  // Конфигурация фильтров и остальных настроек безопасности
  @Bean
  @SneakyThrows//Главное: говорит, кто куда может, и подключает JwtTokenFilter
  public SecurityFilterChain filterChain(HttpSecurity httpSecurity) {
    httpSecurity
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .sessionManagement(sessionManagement ->
                    sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(configurer ->
                    configurer.authenticationEntryPoint( // если не авторизован
                            (request, response, exception) -> {
                              response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 401
                              response.getWriter().write("Unauthorized.");
                            }
                    ).accessDeniedHandler( // если нет прав
                            (request, response, exception) -> {
                              response.setStatus(HttpStatus.FORBIDDEN.value()); //403
                              response.getWriter().write("Unauthorized.");
                            }
                    )
            ) //Swagger и OpenAPI — доступны без авторизации всё остальное — только с валидным токеном
            .authorizeHttpRequests(auth -> auth
                    // REST‑API
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    // формы логина/регистрации
                    .requestMatchers("/login", "/login-page", "/register/**").permitAll()
                    // статика
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                    // Swagger если нужно
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    // всё остальное — с токеном
                    .anyRequest().authenticated()
            )

            .addFilterBefore(new JwtTokenFilter(tokenProvider), UsernamePasswordAuthenticationFilter.class);
//Добавляем JWT-фильтр
    return httpSecurity.build();
  }
}
