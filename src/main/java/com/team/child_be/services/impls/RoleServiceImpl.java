package com.team.child_be.services.impls;

import com.team.child_be.dtos.enums.RoleName;
import com.team.child_be.models.Role;
import com.team.child_be.repositories.RoleRepository;
import com.team.child_be.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public Role getByName(RoleName name) {
        return roleRepository.findByName(name);
    }
}
