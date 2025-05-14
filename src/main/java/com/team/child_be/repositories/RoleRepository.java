package com.team.child_be.repositories;

import com.team.child_be.dtos.enums.RoleName;
import com.team.child_be.models.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(RoleName name);
    boolean existsByName(RoleName name);
}
