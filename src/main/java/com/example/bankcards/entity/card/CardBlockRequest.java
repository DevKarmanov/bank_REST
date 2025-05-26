package com.example.bankcards.entity.card;

import com.example.bankcards.entity.user.MyUser;
import jakarta.persistence.*;


import java.time.LocalDateTime;

@Entity
public class CardBlockRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @ManyToOne
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private MyUser requestedBy;

    private LocalDateTime requestDate;

    private String reason;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public MyUser getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(MyUser requestedBy) {
        this.requestedBy = requestedBy;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
