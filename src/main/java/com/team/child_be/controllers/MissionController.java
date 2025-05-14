package com.team.child_be.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.team.child_be.security.jwt.JwtService;
import com.team.child_be.services.MissionService;

@RestController
@RequestMapping("/missions")
public class MissionController {
    @Autowired
    private MissionService missionService;
    @Autowired
    private JwtService jwtService;
}
