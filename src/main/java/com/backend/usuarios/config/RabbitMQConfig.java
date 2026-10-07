package com.backend.usuarios.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_USUARIOS = "usuarios.exchange";
    
    // Routing keys que usaremos para publicar
    public static final String ROUTING_KEY_USUARIO_REGISTRADO = "usuario.registrado";
    public static final String ROUTING_KEY_USUARIO_LOGEADO = "usuario.logeado";
    public static final String ROUTING_KEY_USUARIO_ACTUALIZADO = "usuario.actualizado"; 

    @Bean
    public TopicExchange usuariosExchange() {
        return new TopicExchange(EXCHANGE_USUARIOS, true, false);
    }

    // Permite serializar automáticamente los objetos Java a JSON para enviarlos
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}