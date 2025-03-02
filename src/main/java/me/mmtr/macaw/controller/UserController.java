package me.mmtr.macaw.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
public class UserController {
    @GetMapping("/current-user-username")
    public Map<String, String> currentUserUsername(Principal principal) {
        return Map.of("username", principal.getName());
    }
}
