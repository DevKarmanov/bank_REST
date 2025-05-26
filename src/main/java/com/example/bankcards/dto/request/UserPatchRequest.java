package com.example.bankcards.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Optional;

@Schema(description = "Request body for patching user details. Fields are optional.")
public record UserPatchRequest(
        @Schema(description = "The username of the user", example = "newUsername")
        Optional<String> name
) {
}
