package com.internship.tmontica.security;

import com.internship.tmontica.security.exception.UnauthorizedException;
import com.internship.tmontica.user.User;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtServiceImpl implements JwtService{

    private static final Logger log = LoggerFactory.getLogger(JwtServiceImpl.class);
    private static final Long TOKEN_EXPIRATION_TIME = 1000 * 60 * 60 * 24L; // 1day
    private SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;
    private static final String SALT = "TMONTICA";
    private static final byte[] KEY = SALT.getBytes(StandardCharsets.UTF_8);

    @Override
    public String getToken(User user) {

        return Jwts.builder()
                .setSubject(user.getId())
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION_TIME))
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("regDate", System.currentTimeMillis())
                .claim("userId", user.getId())
                .claim("role", user.getRole())
                .signWith(signatureAlgorithm, KEY)
                .compact();
    }

    @Override
    public boolean isUsable(String jwt) {
        try{
            Jwts.parser().setSigningKey(KEY).parseClaimsJws(jwt);
            return true;
        } catch (JwtException e) {
            log.info("JwtException  : " + e.getMessage());
            throw new UnauthorizedException();
        }
    }

}