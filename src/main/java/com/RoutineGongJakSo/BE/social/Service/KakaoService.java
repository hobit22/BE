package com.RoutineGongJakSo.BE.social.Service;

import com.RoutineGongJakSo.BE.model.User;
import com.RoutineGongJakSo.BE.repository.UserRepository;
import com.RoutineGongJakSo.BE.security.UserDetailsImpl;
import com.RoutineGongJakSo.BE.security.jwt.JwtTokenUtils;
import com.RoutineGongJakSo.BE.social.Dto.KakaoUserInfoDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoService {


    @Value("${kakao.client_id}")
    String kakaoClientId;

    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository repository;

    public KakaoUserInfoDto kakaoLogin(String code, HttpServletResponse response) throws JsonProcessingException {
        // 1. "인가코드" 로 "액세스 토큰" 요청
        String accessToken = getAccessToken(code);

        // 2. 토큰으로 카카오 API 호출
        KakaoUserInfoDto kakaoUserInfo = getKakaoUserInfo(accessToken);

        // 3. 카카오ID로 회원가입 처리
        User kakaoUser = signupKakaoUser(kakaoUserInfo);

        // 4. 강제 로그인 처리
        Authentication authentication = forceLoginKakaoUser(kakaoUser);

        // 5. response Header에 JWT 토큰 추가
        kakaoUsersAuthorizationInput(authentication, response);
        return kakaoUserInfo;
    }


    //header 에 Content-type 지정
    //1번
    public String getAccessToken(String code) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        System.out.println("getCode : " + code);

        //HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoClientId);
        body.add("redirect_uri", "http://localhost:3000/api/user/kakao/callback");
        body.add("code", code);

        //HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        //HTTP 응답 (JSON) -> 액세스 토큰 파싱
        //JSON -> JsonNode 객체로 변환
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("access_token").asText();
    }

    //2번
    private KakaoUserInfoDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
// HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoUserInfoRequest,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        Long id = jsonNode.get("id").asLong();
        String nickname = jsonNode.get("properties")
                .get("nickname").asText();
        String email = jsonNode.get("kakao_account")
                .get("email").asText();

        log.info("카카오 사용자 정보 id: {},{},{}", id, nickname, email);

        return new KakaoUserInfoDto(id, nickname, email);
    }

    // 3번
    private User signupKakaoUser(KakaoUserInfoDto kakaoUserInfoDto) {
        // DB 에 중복된 Kakao Id 가 있는지 확인
        Long kakaoId = kakaoUserInfoDto.getKakaoId();
        User findKakao = repository.findByKakaoId(kakaoId)
                .orElse(null);

        Optional<User> findCheck = repository.findByUserEmail(kakaoUserInfoDto.getEmail());

        // 이미 가입된 사용자인지 확인
        if (findCheck.isPresent()){
            throw new NullPointerException("이미 가입된 아이디가 존재합니다.");
        }

        if (findKakao == null) {
            //회원가입
            //username = kakaoNickname
            String nickName = kakaoUserInfoDto.getUserName();

            //password : random UUID
            String password = UUID.randomUUID().toString();
            String encodedPassword = passwordEncoder.encode(password);
            // email : kakao email
            String email = kakaoUserInfoDto.getEmail();

            User kakaoUser = User.builder()
                    .userName(nickName)
                    .userEmail(email)
                    .kakaoId(kakaoId)
                    .userPw(encodedPassword)
                    .userLevel(0)
                    .build();

            repository.save(kakaoUser);
            return kakaoUser;

        }
        return findKakao;
    }

    // 4번
    private Authentication forceLoginKakaoUser(User kakaoUser) {
        UserDetails userDetails = new UserDetailsImpl(kakaoUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    // 5번
    private void kakaoUsersAuthorizationInput(Authentication authentication, HttpServletResponse response) {
        // response header에 token 추가
        UserDetailsImpl userDetailsImpl = ((UserDetailsImpl) authentication.getPrincipal());
        String token = JwtTokenUtils.generateJwtToken(userDetailsImpl);
        System.out.println("JWT토큰 : " + token);
        response.addHeader("Authorization", "BEARER" + " " + token);
    }
}