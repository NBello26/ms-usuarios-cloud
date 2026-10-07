package com.backend.usuarios.dto.evento;

import java.io.Serializable;

public record UsuarioRegistradoEvent(
        Long usuarioId,
        String correo,
        String nombre,
        String tipoUsuario
) implements Serializable {}