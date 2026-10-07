package com.backend.usuarios.dto.evento;

import java.io.Serializable;

public record UsuarioActualizadoEvent(
        Long usuarioId,
        String correo,
        String nombre,
        String telefono,
        String direccion
) implements Serializable {}