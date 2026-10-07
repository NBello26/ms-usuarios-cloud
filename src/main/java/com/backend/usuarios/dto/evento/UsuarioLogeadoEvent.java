package com.backend.usuarios.dto.evento;

import java.io.Serializable;
import java.time.LocalDateTime;

public record UsuarioLogeadoEvent(
        Long usuarioId,
        String correo,
        LocalDateTime fechaHoraLogin
) implements Serializable {}