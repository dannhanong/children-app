package com.team.child_be.init;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.team.child_be.dtos.enums.RoleName;
import com.team.child_be.models.Role;
import com.team.child_be.repositories.RoleRepository;

@Configuration
public class InitDatabase {
    @Bean
    CommandLineRunner initRole(RoleRepository roleRepository) {
        return args -> {
            if (!roleRepository.existsByName(RoleName.ADMIN)) {
                Role adminRole = new Role();
                adminRole.setName(RoleName.ADMIN);
                roleRepository.save(adminRole);
            }
            if (!roleRepository.existsByName(RoleName.PARENT)) {
                Role parentRole = new Role();
                parentRole.setName(RoleName.PARENT);
                roleRepository.save(parentRole);
            }
            if (!roleRepository.existsByName(RoleName.CHILD)) {
                Role childRole = new Role();
                childRole.setName(RoleName.CHILD);
                roleRepository.save(childRole);
            }
        };
    }
}
