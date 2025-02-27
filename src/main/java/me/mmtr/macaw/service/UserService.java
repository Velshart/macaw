package me.mmtr.macaw.service;

import me.mmtr.macaw.data.Role;
import me.mmtr.macaw.data.User;
import me.mmtr.macaw.data.UserDTO;
import me.mmtr.macaw.repository.RoleRepository;
import me.mmtr.macaw.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(RoleRepository roleRepository,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(UserDTO userDTO, String role) {
        Optional<Role> roleOptional = roleRepository.findByName(role);

        if (roleOptional.isEmpty()) return;

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(roleOptional.get());

        userRepository.save(user);
    }
}
