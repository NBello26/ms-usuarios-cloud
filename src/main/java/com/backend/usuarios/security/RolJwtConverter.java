package com.backend.usuarios.security;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Convierte el JWT de Azure AD en una autenticación con el ROL REAL del usuario.
 * El token de Azure no trae el rol: la fuente de verdad es la columna tipo_usuario de la BD.
 * "admin" -> ROLE_ADMIN, "cliente" -> ROLE_CLIENTE. Sin usuario en BD => sin roles.
 */
public class RolJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final Function<String, Optional<String>> rolPorCorreo;

    public RolJwtConverter(Function<String, Optional<String>> rolPorCorreo) {
        this.rolPorCorreo = rolPorCorreo;
    }

    /** Correo del usuario según los claims habituales de Azure AD (null si el token no trae ninguno). */
    public static String correoDe(Jwt jwt) {
        for (String claim : new String[] { "email", "preferred_username", "upn" }) {
            String valor = jwt.getClaimAsString(claim);
            if (valor != null && !valor.isBlank()) {
                return valor.trim();
            }
        }
        return null;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String correo = correoDe(jwt);
        List<GrantedAuthority> authorities = List.of();
        if (correo != null) {
            Optional<String> rol = rolPorCorreo.apply(correo).filter(r -> !r.isBlank());
            if (rol.isPresent()) {
                authorities = List.of(new SimpleGrantedAuthority("ROLE_" + rol.get().trim().toUpperCase()));
            }
        }
        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }
}
