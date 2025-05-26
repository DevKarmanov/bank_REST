package com.example.bankcards.service.user;
import com.example.bankcards.dto.request.AuthRequest;
import com.example.bankcards.dto.request.UserDtoRequest;
import com.example.bankcards.dto.request.UserPatchRequest;
import com.example.bankcards.dto.response.auth.AuthResponse;
import com.example.bankcards.entity.user.MyUser;

import java.time.LocalDateTime;

public interface UserService {
    void registerUser(UserDtoRequest userDtoRequest);
    AuthResponse login(AuthRequest authRequest);
    void delUser(String name);
    void updateUser(UserPatchRequest userPatchRequest);
    void blockUser(String userName,
                   LocalDateTime unlockAt,
                   String reason);
    String toggleUserAuthorities(String userName);
    void unblockUser(String userName);
    MyUser getCurrentUser();
    MyUser getUserByName(String userName);
}
