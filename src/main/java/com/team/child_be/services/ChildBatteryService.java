package com.team.child_be.services;

import java.util.List;

import com.team.child_be.dtos.responses.ResponseMessage;
import com.team.child_be.models.ChildBattery;

public interface ChildBatteryService {
    List<ChildBattery> getMyChildBatteries(String username);
    ChildBattery createChildBattery(String username, ChildBattery childBattery);
    ChildBattery updateChildBattery(String username, Long batteryId, ChildBattery childBattery);
    ResponseMessage deleteChildBattery(Long batteryId);
}
