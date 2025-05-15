package com.team.child_be.init;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.team.child_be.dtos.enums.RoleName;
import com.team.child_be.models.Role;
import com.team.child_be.models.User;
import com.team.child_be.repositories.RoleRepository;
import com.team.child_be.repositories.UserRepository;

@Configuration
public class InitDatabase {
    @Bean
    CommandLineRunner initRole(RoleRepository roleRepository, UserRepository userRepository) {
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
            if (!userRepository.existsByUsername("bot@gmail.com")) {
                User botUser = new User();
                botUser.setName("Trợ lý ảo");
                botUser.setUsername("bot@gmail.com");
                botUser.setPhoneNumber("0123456789");
                botUser.setPassword("nE1BABFulC3BDtiE1BB83ulongC491E1BAA5umiketyson");
                botUser.setEnabled(false);
                userRepository.save(botUser);
            }
        };
    }
}
