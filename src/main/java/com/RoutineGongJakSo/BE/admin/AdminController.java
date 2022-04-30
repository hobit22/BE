package com.RoutineGongJakSo.BE.admin;

import com.RoutineGongJakSo.BE.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    //관리자 로그인
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody AdminDto.RequestDto adminDto){
        return adminService.login(adminDto);
    }

    //전체 유저 조회
    @GetMapping("/userList")
    public List<AdminDto.ResponseDto> getAllUser(){
        return adminService.getAllUser();
    }

    //권한 변경
    @PutMapping("/{userId}")
    public String updateLevel(@PathVariable Long userId, @RequestBody AdminDto.UpdateDto update){
        return adminService.updateLevel(userId, update);
    }

    //유저 삭제
    @DeleteMapping("/{userId}")
    public String deleteUser(@PathVariable Long userId){
        return adminService.deleteUser(userId);
    }
}
