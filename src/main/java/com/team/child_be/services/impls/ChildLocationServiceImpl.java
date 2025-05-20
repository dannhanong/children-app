package com.team.child_be.services.impls;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.team.child_be.dtos.requests.ChildLocationRequest;
import com.team.child_be.dtos.responses.ResponseMessage;
import com.team.child_be.models.ChildLocation;
import com.team.child_be.models.User;
import com.team.child_be.repositories.ChildLocationRepository;
import com.team.child_be.repositories.UserRepository;
import com.team.child_be.services.ChildLocationService;

@Service
public class ChildLocationServiceImpl implements ChildLocationService {
    @Autowired
    private ChildLocationRepository childLocationRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public ResponseMessage addChildLocation(String username, ChildLocationRequest childLocationRequest) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng");
        }

        ChildLocation childLocation = ChildLocation.builder()
                .latitude(childLocationRequest.latitude())
                .longitude(childLocationRequest.longitude())
                .user(user)
                .build();

        List<ChildLocation> existingLocations = childLocationRepository.findByUser_Username(username);

        if (existingLocations != null && !existingLocations.isEmpty()) {
            for (ChildLocation existingLocation : existingLocations) {
                childLocationRepository.delete(existingLocation);
            }
        }

        childLocationRepository.save(childLocation);
        return new ResponseMessage(200, "Thêm vị trí trẻ em thành công");
    }

    @Override
    public List<ChildLocation> getFamilyLocations(String username) {
        User user = userRepository.findByUsername(username);
        List<User> familyMembers = new ArrayList<>();

        if (user.getAccessCode() != null && !user.getAccessCode().isEmpty()) {
            familyMembers = userRepository.findByParentIdAndDeletedAtNull(user.getParentId());
            familyMembers.add(userRepository.findById(user.getParentId()).orElse(null));
        } else {
            familyMembers = userRepository.findByParentIdAndDeletedAtNull(user.getId());
            familyMembers.add(user);
        }

        List<ChildLocation> familyLocations = new ArrayList<>();
        for (User familyMember : familyMembers) {
            List<ChildLocation> locations = childLocationRepository.findByUser(familyMember);
            if (locations != null && !locations.isEmpty()) {
                familyLocations.addAll(locations);
            }
        }
        return familyLocations;
    }
}
