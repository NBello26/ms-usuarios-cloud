package com.backend.usuarios.repository;

import com.backend.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Método para buscar un usuario por su correo, utilizado en el proceso de login
    Optional<Usuario> findByCorreo(String correo);

    // Usado para resolver el rol a partir del correo del token (Azure puede variar mayúsculas)
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
}