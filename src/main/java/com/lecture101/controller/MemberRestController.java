package com.lecture101.controller;

import com.lecture101.entity.Member;
import com.lecture101.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/members/rest")
@Controller
@RequiredArgsConstructor
public class MemberRestController {

    private final MemberService memberService;

    //회원정보 수정 전 비밀번호 확인
    @GetMapping("/checkPwd")
    public boolean checkPassword(@AuthenticationPrincipal Member member,
                                 @RequestParam String checkPassword,
                                 Model model){

        System.out.println("checkPwd 진입");
        Long member_id = member.getId();

        return memberService.checkPassword(member_id, checkPassword);
    }
}
