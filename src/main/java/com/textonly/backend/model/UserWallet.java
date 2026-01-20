package com.textonly.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_wallet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Integer coins = 0;

    @Column(name = "total_spent")
    private Integer totalSpent = 0;

    @Column(name = "total_earned")
    private Integer totalEarned = 0;
}
