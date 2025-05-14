package com.team.child_be.services;

import com.team.child_be.dtos.enums.RoleName;
import com.team.child_be.models.Role;

public interface RoleService {
    Role getByName(RoleName name);
}
