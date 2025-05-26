package com.example.bankcards.controller.user;

import com.example.bankcards.dto.request.UserPatchRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequestMapping("/api/admin/users")
@CrossOrigin
public interface UserController {

    @DeleteMapping("/delete/{username}")
    ResponseEntity<?> deleteUser(@PathVariable String username);

    @PatchMapping("/update")
    ResponseEntity<?> updateCurrentUser(@RequestBody UserPatchRequest request);


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/block")
    ResponseEntity<?> blockUser(@RequestParam String username,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime unlockAt,
                                   @RequestParam String reason);

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/unblock")
    ResponseEntity<?> unblockUser(@RequestParam String username);

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/toggle-admin")
    ResponseEntity<?> toggleAdminRole(@RequestParam String username);
}
