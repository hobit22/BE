package com.RoutineGongJakSo.BE.social.socialDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NaverUserInfoDto {
    private Long naverId;
    private String userName;
    private String email;
}
