package me.mmtr.macaw.filter;

import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class WebSocketFilter implements ChannelInterceptor {

    private final UserDetailsService userDetailsService;

    public WebSocketFilter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor sha = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (sha != null && StompCommand.CONNECT.equals(sha.getCommand())) {
            Principal principal = sha.getUser();

            if (principal != null) {
                String username = principal.getName();
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                sha.setUser(authenticationToken);
            }
        }
        return message;
    }
}
