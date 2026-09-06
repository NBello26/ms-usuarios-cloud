package com.backend.usuarios.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.backend.usuarios.dto.UsuarioDTO;
import com.backend.usuarios.dto.UsuarioRequestDTO;
import com.backend.usuarios.dto.UsuarioUpdateDTO;
import com.backend.usuarios.model.Usuario;
import com.backend.usuarios.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;

    // =========================================================================
    // 1. MÉTODOS PÚBLICOS
    // =========================================================================

    public UsuarioDTO crearUsuario(UsuarioRequestDTO dto) {
        if (repository.findByCorreo(dto.correo()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo ya está registrado");
        }

        Usuario usuario = Usuario.builder()
                .nombre(dto.nombre())
                .edad(dto.edad())
                .genero(dto.genero())
                .correo(dto.correo())
                .telefono(dto.telefono())
                .fotoUrl(dto.fotoUrl())
                .ocupacion(dto.ocupacion())
                .direccion(dto.direccion())
                .tipoUsuario("cliente")
                .build();

        return convertirADTO(repository.save(usuario));
    }

    public UsuarioDTO actualizarUsuario(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        usuario.setNombre(dto.nombre());
        usuario.setEdad(dto.edad());
        usuario.setGenero(dto.genero());
        usuario.setTelefono(dto.telefono());
        usuario.setFotoUrl(dto.fotoUrl());
        usuario.setOcupacion(dto.ocupacion());
        usuario.setDireccion(dto.direccion());

        return convertirADTO(repository.save(usuario));
    }

    // =========================================================================
    // 2. MÉTODOS GENERALES (INCLUYE LOGIN CON SSO)
    // =========================================================================

    public List<UsuarioDTO> listar() {
        return repository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public UsuarioDTO obtenerPorId(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        return convertirADTO(usuario);
    }

    public void eliminarUsuario(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        repository.delete(usuario);
    }

    // AHORA SOLO VALIDA QUE EXISTA Y DEVUELVE SUS DATOS
    public UsuarioDTO login(String correo) {
        Usuario usuario = repository.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "El usuario no está registrado en el sistema."));

        return convertirADTO(usuario);
    }

    // =========================================================================
    // 3. MÉTODOS EXCLUSIVOS PARA EL BFF (ADMIN)
    // =========================================================================

    public UsuarioDTO crearUsuarioAdmin(UsuarioRequestDTO dto) {
        if (repository.findByCorreo(dto.correo()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo ya está registrado");
        }

        Usuario usuario = Usuario.builder()
                .nombre(dto.nombre())
                .edad(dto.edad())
                .genero(dto.genero())
                .correo(dto.correo())
                .telefono(dto.telefono())
                .fotoUrl(dto.fotoUrl())
                .ocupacion(dto.ocupacion())
                .direccion(dto.direccion())
                .tipoUsuario(dto.tipoUsuario())
                .build();

        return convertirADTO(repository.save(usuario));
    }

    public UsuarioDTO actualizarUsuarioPorAdmin(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        // ... seteo de campos ...
        usuario.setNombre(dto.nombre());
        usuario.setEdad(dto.edad());
        usuario.setGenero(dto.genero());
        usuario.setTelefono(dto.telefono());
        usuario.setFotoUrl(dto.fotoUrl());
        usuario.setOcupacion(dto.ocupacion());
        usuario.setDireccion(dto.direccion());
        usuario.setTipoUsuario(dto.tipoUsuario());

        return convertirADTO(repository.save(usuario));
    }

    // =========================================================================
    // 4. MÉTODO UTILITARIO PARA DTOs
    // =========================================================================
    
    private UsuarioDTO convertirADTO(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getTelefono(),
                usuario.getCorreo(),
                usuario.getEdad(),
                usuario.getGenero(),
                usuario.getDireccion(),
                usuario.getOcupacion(),
                usuario.getFotoUrl(),
                usuario.getTipoUsuario()
        );
    }
}