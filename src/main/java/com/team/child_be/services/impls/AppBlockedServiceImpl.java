package com.team.child_be.services.impls;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.team.child_be.dtos.enums.AppType;
import com.team.child_be.dtos.requests.AppBlockedRequest;
import com.team.child_be.dtos.responses.ResponseMessage;
import com.team.child_be.models.AppBlocked;
import com.team.child_be.models.User;
import com.team.child_be.repositories.AppBlockedRepository;
import com.team.child_be.repositories.UserRepository;
import com.team.child_be.services.AppBlockedService;

@Service
public class AppBlockedServiceImpl implements AppBlockedService{
    @Autowired
    private AppBlockedRepository appBlockedRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public ResponseMessage addAppBlocked(String username, AppBlockedRequest appBlockedRequest) {
        User child = userRepository.findById(appBlockedRequest.childId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản trẻ"));
        
        User parent = userRepository.findById(child.getParentId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản phụ huynh"));

        if (!parent.getUsername().equals(username)) {
            return new ResponseMessage(400, "Bạn không có quyền thêm ứng dụng bị chặn cho tài khoản này");
        } else {
            appBlockedRepository.save(AppBlocked.builder()
                .appName(appBlockedRequest.appName())
                .child(child)
                .appType(AppType.APP)
                .build());
            return new ResponseMessage(200, "Thêm ứng dụng bị chặn thành công");
        }
    }

    @Override
    public ResponseMessage deleteAppBlocked(String username, Long id) {
        AppBlocked appBlocked = appBlockedRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy ứng dụng bị chặn"));

        User parent = userRepository.findById(appBlocked.getChild().getParentId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản phụ huynh"));

        if (!parent.getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền xóa ứng dụng bị chặn cho tài khoản này");
        } else {
            appBlockedRepository.delete(appBlocked);
            return new ResponseMessage(200, "Xóa ứng dụng bị chặn thành công");
        }
    }

    @Override
    public List<String> getMyChildAppBlocked(String username, Long childId) {
        User child = userRepository.findById(childId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản trẻ"));

        User parent = userRepository.findById(child.getParentId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản phụ huynh"));

        if (!parent.getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền xem ứng dụng bị chặn cho tài khoản này");
        }

        return appBlockedRepository.findByChild_IdAndAppType(childId, AppType.APP)
            .stream()
            .map(AppBlocked::getAppName)
            .toList();
    }

    @Override
    public ResponseMessage addWebBlocked(String username, AppBlockedRequest appBlockedRequest) {
        User child = userRepository.findById(appBlockedRequest.childId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản trẻ"));
        
        User parent = userRepository.findById(child.getParentId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản phụ huynh"));

        if (!parent.getUsername().equals(username)) {
            return new ResponseMessage(400, "Bạn không có quyền thêm website bị chặn cho tài khoản này");
        } else {
            appBlockedRepository.save(AppBlocked.builder()
                .appName(appBlockedRequest.appName())
                .child(child)
                .appType(AppType.WEB)
                .build());
            return new ResponseMessage(200, "Thêm website bị chặn thành công");
        }
    }

    @Override
    public List<String> getMyChildWebBlocked(String username, Long childId) {
        User child = userRepository.findById(childId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản trẻ"));

        User parent = userRepository.findById(child.getParentId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản phụ huynh"));

        if (!parent.getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền xem website bị chặn cho tài khoản này");
        }

        return appBlockedRepository.findByChild_IdAndAppType(childId, AppType.WEB)
            .stream()
            .map(AppBlocked::getAppName)
            .toList();
    }

}
