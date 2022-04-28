package com.RoutineGongJakSo.BE.social;

import com.RoutineGongJakSo.BE.social.socialDto.KakaoUserInfoDto;
import com.RoutineGongJakSo.BE.social.socialService.KakaoService;
import com.RoutineGongJakSo.BE.social.socialService.NaverService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class SocialController {
    private final KakaoService kakaoService;
    private final NaverService naverService;

    //카카오 로그인
    @GetMapping("/kakao/callback")
    public KakaoUserInfoDto kakaoLogin(@RequestParam String code, HttpServletResponse response
    ) throws JsonProcessingException {
       return kakaoService.kakaoLogin(code, response);
    }

    //네이버 로그인
    @GetMapping("/naver/callback")
    public void naverLogin(@RequestParam String code, @RequestParam String state, HttpServletResponse response) throws JsonProcessingException{
        naverService.naverLogin(code, state, response);

    }

}
