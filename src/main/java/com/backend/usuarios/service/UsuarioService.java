package com.backend.usuarios.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.backend.usuarios.config.RabbitMQConfig;
import com.backend.usuarios.dto.UsuarioDTO;
import com.backend.usuarios.dto.UsuarioPerfilUpdateDTO;
import com.backend.usuarios.dto.UsuarioRequestDTO;
import com.backend.usuarios.dto.UsuarioUpdateDTO;
import com.backend.usuarios.dto.evento.UsuarioActualizadoEvent;
import com.backend.usuarios.dto.evento.UsuarioLogeadoEvent;
import com.backend.usuarios.dto.evento.UsuarioRegistradoEvent;
import com.backend.usuarios.model.Usuario;
import com.backend.usuarios.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j  
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final RabbitTemplate rabbitTemplate;

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

        Usuario usuarioGuardado = repository.save(usuario);
        publicarEventoRegistro(usuarioGuardado);

        return convertirADTO(usuarioGuardado);
    }

    public UsuarioDTO actualizarUsuario(Long id, UsuarioPerfilUpdateDTO dto) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: " + id));

        usuario.setNombre(dto.nombre());
        usuario.setEdad(dto.edad());
        usuario.setGenero(dto.genero());
        usuario.setTelefono(dto.telefono());
        usuario.setFotoUrl(dto.fotoUrl());
        usuario.setOcupacion(dto.ocupacion());
        usuario.setDireccion(dto.direccion());

        Usuario usuarioGuardado = repository.save(usuario);
        publicarEventoActualizacion(usuarioGuardado);

        return convertirADTO(usuarioGuardado);
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
        
        UsuarioLogeadoEvent evento = new UsuarioLogeadoEvent(
                usuario.getId(), 
                usuario.getCorreo(), 
                LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_USUARIOS, RabbitMQConfig.ROUTING_KEY_USUARIO_LOGEADO, evento);
        log.info("Evento de login publicado para el usuario: {}", correo);

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

        Usuario usuarioGuardado = repository.save(usuario);
        publicarEventoRegistro(usuarioGuardado);

        return convertirADTO(usuarioGuardado);
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

        Usuario usuarioGuardado = repository.save(usuario);
        publicarEventoActualizacion(usuarioGuardado);

        return convertirADTO(usuarioGuardado);
    }

    // =========================================================================
    // 4. MÉTODOS UTILITARIOS
    // =========================================================================
    
    private void publicarEventoRegistro(Usuario usuario) {
        try {
            UsuarioRegistradoEvent evento = new UsuarioRegistradoEvent(
                    usuario.getId(),
                    usuario.getCorreo(),
                    usuario.getNombre(),
                    usuario.getTipoUsuario()
            );
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_USUARIOS, RabbitMQConfig.ROUTING_KEY_USUARIO_REGISTRADO, evento);
            log.info("Evento de registro publicado para el usuario: {}", usuario.getCorreo());
        } catch (Exception e) {
            log.error("Error al publicar evento de registro para {}: {}", usuario.getCorreo(), e.getMessage());
            // No bloqueamos el registro si falla RabbitMQ
        }
    }

    private void publicarEventoActualizacion(Usuario usuario) {
        try {
            UsuarioActualizadoEvent evento = new UsuarioActualizadoEvent(
                    usuario.getId(),
                    usuario.getCorreo(),
                    usuario.getNombre(),
                    usuario.getTelefono(),
                    usuario.getDireccion()
            );
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_USUARIOS, RabbitMQConfig.ROUTING_KEY_USUARIO_ACTUALIZADO, evento);
            log.info("Evento de actualización publicado para el usuario: {}", usuario.getCorreo());
        } catch (Exception e) {
            log.error("Error al publicar evento de actualización para {}: {}", usuario.getCorreo(), e.getMessage());
        }
    }
    
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