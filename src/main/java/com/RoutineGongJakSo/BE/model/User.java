package com.RoutineGongJakSo.BE.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
public class User extends Timestamped {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "weekTeamUserId")
    private WeekTeamUser weekTeamUser;

    @Column(nullable = false, unique = true)
    private String userEmail;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String userPw;

    @Column
    private String phoneNumber;

    @Column
    private int userLevel;

    @Column(unique = true)
    private Long kakaoId;

    @Column(unique = true)
    private String naverId;
}
