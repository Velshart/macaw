package me.mmtr.macaw.controller;

import me.mmtr.macaw.data.User;
import me.mmtr.macaw.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/current-user-username")
    public Map<String, String> currentUserUsername(Principal principal) {
        return Map.of("username", principal.getName());
    }

    @GetMapping("/all-users-usernames")
    public List<String> allUsersUsernames() {
        return userRepository.findAll()
                .stream()
                .map(User::getUsername)
                .toList();
    }
}
