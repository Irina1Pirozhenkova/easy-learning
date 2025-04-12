package com.example.easy_learning.security;

import com.example.easy_learning.dto.JwtResponse;
import com.example.easy_learning.model.Student;
import com.example.easy_learning.model.Tutor;
import com.example.easy_learning.service.StudentService;
import com.example.easy_learning.service.TutorService;
import com.example.easy_learning.service.props.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final StudentJwtUserDetailsService studentJwtUserDetailsService;
     private final TutorJwtUserDetailsService tutorJwtUserDetailsService;
    private final StudentService studentService;
    private final TutorService tutorService;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }
    //мы генерируем секретный ключ из строки в application.yml
    public String createAccessToken(Long userId, String username, String userType) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.getAccess(), ChronoUnit.HOURS);

        return Jwts.builder()
                .setSubject(username)
                .claim("id", userId)
                .claim("userType", userType)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(key, SignatureAlgorithm.HS256)//подпись (signWith) с нашим секретом
                .compact();
    }

    public String createRefreshToken(Long userId, String username, String userType) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.getRefresh(), ChronoUnit.DAYS);

        return Jwts.builder()
                .setSubject(username)
                .claim("id", userId)
                .claim("userType", userType)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public JwtResponse refreshStudentTokens(String refreshToken) {
        if (!isValid(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        Long studentId = Long.valueOf(getId(refreshToken));
        Student student = studentService.getStudentById(studentId.intValue());

        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setId(student.getId().longValue());
        jwtResponse.setUsername(student.getEmail());
        jwtResponse.setAccessToken(createAccessToken(
                student.getId().longValue(),
                student.getEmail(),
                "student"
        ));
        jwtResponse.setRefreshToken(createRefreshToken(
                student.getId().longValue(),
                student.getEmail(),
                "student"
        ));
        return jwtResponse;
    }

    public JwtResponse refreshTutorTokens(String refreshToken) {
        if (!isValid(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        Long tutorId = Long.valueOf(getId(refreshToken));
        Tutor tutor = tutorService.getById(tutorId.intValue());

        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setId(tutor.getId().longValue());
        jwtResponse.setUsername(tutor.getEmail());
        jwtResponse.setAccessToken(createAccessToken(
                tutor.getId().longValue(),
                tutor.getEmail(),
                "tutor"
        ));
        jwtResponse.setRefreshToken(createRefreshToken(
                tutor.getId().longValue(),
                tutor.getEmail(),
                "tutor"
        ));
        return jwtResponse;
    }

    public boolean isValid(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            return claimsJws.getBody().getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private String getId(final String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("id", String.class);
    }

    private String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String getUserType(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userType", String.class);
    }


    public Authentication getAuthentication(String token) {
        String userType = getUserType(token);

        if ("student".equals(userType)) {
            return getAuthenticationForStudent(token);
        } else if ("tutor".equals(userType)) {
            return getAuthenticationForTutor(token);
        } else {
            return null;
        }
    }

    public Authentication getAuthenticationForStudent(String token) {// Кто сделал запрос?
        String username = getUsername(token);
        UserDetails userDetails = studentJwtUserDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public Authentication getAuthenticationForTutor(String token) {// Кто сделал запрос?
        String username = getUsername(token);
        UserDetails userDetails = tutorJwtUserDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
}
