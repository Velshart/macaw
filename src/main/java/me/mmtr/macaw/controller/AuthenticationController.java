package me.mmtr.macaw.controller;

import me.mmtr.macaw.data.User;
import me.mmtr.macaw.data.UserDTO;
import me.mmtr.macaw.repository.UserRepository;
import me.mmtr.macaw.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthenticationController {

    private final UserService userService;
    private final UserRepository userRepository;

    public AuthenticationController(UserService userService,
                                    UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/login")
    public String login(Model model,
                        @RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout) {

        if (error != null) {
            model.addAttribute("error", "Incorrect username or password provided");
        }
        if (logout != null) {
            model.addAttribute("logout", logout);
        }
        return "login";
    }

    @GetMapping("/register")
    public String registrationForm(Model model) {
        UserDTO userDTO = new UserDTO();
        model.addAttribute("user", userDTO);
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") UserDTO userDTO, BindingResult bindingResult, Model model) {
        User existingUser = userRepository.findByUsername(userDTO.getUsername()).orElse(null);

        if (existingUser != null && existingUser.getUsername() != null && !existingUser.getUsername().isEmpty()) {
            bindingResult.rejectValue("username", "exists", "This username is already in use");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", userDTO);
            return "register";
        }

        userService.registerUser(userDTO, "USER");
        return "redirect:/login";
    }
}
