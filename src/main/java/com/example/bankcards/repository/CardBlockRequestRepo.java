package com.example.bankcards.repository;

import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardBlockRequest;
import com.example.bankcards.entity.user.MyUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardBlockRequestRepo extends JpaRepository<CardBlockRequest,Long> {
    void deleteAllByCard(Card card);
    void deleteAllByRequestedBy(MyUser user);
}
