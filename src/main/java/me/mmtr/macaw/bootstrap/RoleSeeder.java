package me.mmtr.macaw.bootstrap;

import me.mmtr.macaw.data.Role;
import me.mmtr.macaw.repository.RoleRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RoleSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private final RoleRepository roleRepository;

    public RoleSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        seedRoles();
    }

    private void seedRoles() {
        Optional<Role> roleOptional = roleRepository.findByName("USER");
        if(roleOptional.isEmpty()) {
            Role role = new Role();
            role.setName("USER");

            roleRepository.save(role);
        }
    }
}
