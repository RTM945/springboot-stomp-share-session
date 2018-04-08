package me.rtmsoft.springboot_stomp_share_session;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodReturnValueHandler;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurationSupport;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.messaging.SubProtocolWebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends WebSocketMessageBrokerConfigurationSupport implements WebSocketMessageBrokerConfigurer {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
        // default "/user"
        // registry.setUserDestinationPrefix("/user");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS()
			.setInterceptors(new HttpSessionHandshakeInterceptor());//this can set all http session attribute into websocket session
	}
	
	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(new ChannelInterceptorAdapter() {
			@Override
			public Message<?> preSend(Message<?> message, MessageChannel channel) {
				StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
				if(accessor.getSessionAttributes().get("user") != null) {
					if(accessor.getUser() == null) {
						String user =  (String) accessor.getSessionAttributes().get("user");
						accessor.setUser(new Principal() {
							
							@Override
							public String getName() {
								return user;
							}
						});
						
					}
					return message;
				}else {
					return null;
				}
				
			}
		});
	}

	@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
		super.configureWebSocketTransport(registry);
	}

	@Override
	public void configureClientOutboundChannel(ChannelRegistration registration) {
		super.configureClientOutboundChannel(registration);
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		super.addArgumentResolvers(argumentResolvers);
	}

	@Override
	public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
		super.addReturnValueHandlers(returnValueHandlers);
	}

	@Override
	public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
		return super.configureMessageConverters(messageConverters);
	}
	
	@Bean
    public WebSocketHandler subProtocolWebSocketHandler() {
        return new CustomSubProtocolWebSocketHandler(clientInboundChannel(), clientOutboundChannel());
    }
	
	public class CustomSubProtocolWebSocketHandler extends SubProtocolWebSocketHandler {

	    public CustomSubProtocolWebSocketHandler(MessageChannel clientInboundChannel, SubscribableChannel clientOutboundChannel) {
	        super(clientInboundChannel, clientOutboundChannel);
	    }

	    @SuppressWarnings("unchecked")
		@Override
	    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
	    	if(session.getAttributes().get("user") == null) {
	    		session.close(CloseStatus.NOT_ACCEPTABLE);
	    		return;
	    	}
	    	Map<String, Object> shareAttr = (Map<String, Object>) session.getAttributes().get("shareAttr");
	    	shareAttr.put("WEBSOCKET.SESSION", session);
	    	
	        super.afterConnectionEstablished(session);
	    }
	}

}
