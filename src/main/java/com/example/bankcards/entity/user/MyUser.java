package com.example.bankcards.entity.user;

import com.example.bankcards.entity.card.Card;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class MyUser{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "my_user_roles",
            joinColumns = @JoinColumn(name = "my_user_id")
    )
    @Column(name = "roles")
    private List<String> roles;

    private String password;
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Card> cards = new ArrayList<>();

    private boolean isEnable;

    private LocalDateTime unlockAt;

    private String blockReason;

    public MyUser(String name, List<String> roles, String password, boolean isEnable, LocalDateTime unlockAt) {
        this.name = name;
        this.roles = roles;
        this.password = password;
        this.isEnable = isEnable;
        this.unlockAt = unlockAt;
    }

    public MyUser() {}

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    public LocalDateTime getUnlockAt() {
        return unlockAt;
    }

    public void setUnlockAt(LocalDateTime unlockAt) {
        this.unlockAt = unlockAt;
    }

    public String getBlockReason() {
        return blockReason;
    }

    public void setBlockReason(String blockReason) {
        this.blockReason = blockReason;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        MyUser user = (MyUser) object;
        return isEnable == user.isEnable && Objects.equals(id, user.id) && Objects.equals(name, user.name) && Objects.equals(roles, user.roles) && Objects.equals(password, user.password) && Objects.equals(cards, user.cards) && Objects.equals(unlockAt, user.unlockAt) && Objects.equals(blockReason, user.blockReason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, roles, password, cards, isEnable, unlockAt, blockReason);
    }
}
