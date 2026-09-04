package com.backend.usuarios.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    // Lee la clave secreta desde el application.properties
    @Value("${jwt.secret:UnaClaveSecretaMuyLargaYSeguraParaPoderFirmarElToken256BitsMinimo!}")
    private String secret;

    // Lee el tiempo de expiración desde el properties (por defecto 24 horas)
    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generarToken(String correo, String sessionId, Long id, String tipoUsuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sessionId", sessionId);
        claims.put("id", id);
        claims.put("tipoUsuario", tipoUsuario);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(correo)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}