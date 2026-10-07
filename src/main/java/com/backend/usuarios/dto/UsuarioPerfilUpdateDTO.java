package com.backend.usuarios.dto;

import jakarta.validation.constraints.*;

public record UsuarioPerfilUpdateDTO(
    @NotBlank @Size(min = 3, max = 50) String nombre,
    @NotNull @Min(18) @Max(100) Integer edad,
    @NotBlank String genero,
    String telefono,
    String fotoUrl,
    String ocupacion,
    String direccion
) {}