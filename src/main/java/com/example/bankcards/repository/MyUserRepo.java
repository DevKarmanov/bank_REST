package com.example.bankcards.repository;


import com.example.bankcards.entity.user.MyUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MyUserRepo extends JpaRepository<MyUser, Long> {
    Optional<MyUser> findByName(String name);

    boolean existsByNameIgnoreCase(String name);
}
