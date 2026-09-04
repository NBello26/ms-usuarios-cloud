package com.backend.usuarios.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDTO {
    private String token;      // Tu propio JWT (no el de Microsoft)
    private String sessionId;  // El ID para controlar que no haya dos sesiones
    private UsuarioDTO usuario; // Los datos del usuario para que el Frontend los muestre
}