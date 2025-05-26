package com.example.bankcards.dto.request;

import java.util.List;

public record UserDtoRequest(String name,
                             String password,
                             List<String> role) {
}
