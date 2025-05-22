package com.team.child_be.services.impls;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.team.child_be.models.ChildBattery;
import com.team.child_be.repositories.ChildBatteryRepository;
import com.team.child_be.repositories.UserRepository;
import com.team.child_be.services.ChildBatteryService;

@Service
public class ChildBatteryServiceImpl implements ChildBatteryService{
    @Autowired
    private ChildBatteryRepository childBatteryRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public ChildBattery createChildBattery(String username, ChildBattery childBattery) {
        if (childBatteryRepository.findByChild(userRepository.findByUsername(username)).isPresent()) {
            childBatteryRepository.deleteByChild(userRepository.findByUsername(username));
        }
        childBattery.setChild(userRepository.findByUsername(username));
        childBattery.setTime(LocalDateTime.now());
        return childBatteryRepository.save(childBattery);
    }

}
