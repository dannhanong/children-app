package com.team.child_be.services.impls;

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
        User child = userRepository.findByUsername(username);

        if (child == null) {
            throw new RuntimeException("Không tìm thấy người dùng");
        }

        ChildLocation childLocation = ChildLocation.builder()
                .latitude(childLocationRequest.latitude())
                .longitude(childLocationRequest.longitude())
                .child(child)
                .build();

        childLocationRepository.save(childLocation);
        return new ResponseMessage(200, "Thêm vị trí trẻ em thành công");
    }
}
