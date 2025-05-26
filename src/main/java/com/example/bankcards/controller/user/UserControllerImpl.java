package com.example.bankcards.controller.user;

import com.example.bankcards.dto.request.UserPatchRequest;
import com.example.bankcards.service.user.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class UserControllerImpl implements UserController {

    private final UserService userService;

    public UserControllerImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ResponseEntity<?> deleteUser(String username) {
        userService.delUser(username);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<?> updateCurrentUser(UserPatchRequest request) {
        userService.updateUser(request);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<?> blockUser(String username,
                                       LocalDateTime unlockAt,
                                       String reason) {
        userService.blockUser(username, unlockAt, reason);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> unblockUser(String username) {
        userService.unblockUser(username);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> toggleAdminRole(String username) {
        String result = userService.toggleUserAuthorities(username);
        return ResponseEntity.ok(result);
    }
}
