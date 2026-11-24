package com.backend.usuarios.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class RolJwtConverterTest {

    private Jwt jwt(String claim, String valor) {
        Jwt.Builder b = Jwt.withTokenValue("t").header("alg", "none").subject("sub-1");
        if (claim != null) {
            b.claim(claim, valor);
        } else {
            b.claim("otro", "x");
        }
        return b.build();
    }

    private RolJwtConverter con(String correoEsperado, String rol) {
        Function<String, Optional<String>> f = c ->
                c.equalsIgnoreCase(correoEsperado) ? Optional.ofNullable(rol) : Optional.empty();
        return new RolJwtConverter(f);
    }

    @Test
    void adminRecibeRoleAdmin() {
        var auth = con("a@x.cl", "admin").convert(jwt("preferred_username", "a@x.cl"));
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void clienteNoRecibeRoleAdmin() {
        var auth = con("a@x.cl", "cliente").convert(jwt("email", "A@X.cl"));
        assertEquals(1, auth.getAuthorities().size());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE")));
    }

    @Test
    void usuarioNoRegistradoNoTieneRoles() {
        var auth = con("a@x.cl", "admin").convert(jwt("preferred_username", "otro@x.cl"));
        assertTrue(auth.getAuthorities().isEmpty());
    }

    @Test
    void tokenSinCorreoNoTieneRoles() {
        Jwt j = jwt(null, null);
        assertNull(RolJwtConverter.correoDe(j));
        assertTrue(con("a@x.cl", "admin").convert(j).getAuthorities().isEmpty());
    }
}
