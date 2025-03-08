package me.mmtr.macaw;

import me.mmtr.macaw.controller.AuthenticationController;
import me.mmtr.macaw.data.Role;
import me.mmtr.macaw.data.User;
import me.mmtr.macaw.data.UserDTO;
import me.mmtr.macaw.repository.UserRepository;
import me.mmtr.macaw.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthenticationController.class)
public class AuthenticationControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    public void shouldSuccessfullyRegisterUserAndRedirectToLogin() throws Exception {
        when(userRepository.findByUsername(Mockito.anyString())).thenReturn(Optional.empty());

        mvc.perform(post("/register")
                        .param("username", "username")
                        .param("password", "password")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        Mockito.verify(userService, times(1))
                .registerUser(new UserDTO("username", "password"), "USER");
    }

    @Test
    public void shouldReturnAnErrorWhenUserAlreadyExistsDuringRegistration() throws Exception {
        when(userRepository.findByUsername(Mockito.anyString()))
                .thenReturn(Optional.of(new User(
                        1L,
                        "username",
                        "password",
                        new Role()
                )));

        mvc.perform(post("/register")
                        .param("username", "username")
                        .param("password", "password")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("user", "username"));

        Mockito.verify(userService, never()).registerUser(any(), any());
    }

    @Test
    public void shouldReturnLoginViewWithoutErrors() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    public void shouldReturnLoginViewWithAnError() throws Exception {
        mvc.perform(get("/login").param("error", "error"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error",
                        "Incorrect username or password provided"))
                .andExpect(view().name("login"));
    }

    @Test
    public void shouldReturnLoginViewWithLogoutAttribute() throws Exception {
        mvc.perform(get("/login").param("logout", "logout"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("logout"))
                .andExpect(model().attribute("logout",
                        "logout"))
                .andExpect(view().name("login"));
    }

    @TestConfiguration
    static class TestSecurityConfiguration {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.authorizeHttpRequests(authorizeRequests ->
                            authorizeRequests.requestMatchers("/register").permitAll()
                    )
                    .formLogin(formLogin ->
                            formLogin.loginPage("/login").permitAll())
                    .csrf(AbstractHttpConfigurer::disable);

            return http.build();
        }
    }
}
