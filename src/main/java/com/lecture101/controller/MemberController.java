package com.lecture101.controller;

import com.lecture101.dto.MemberFormDto;
import com.lecture101.dto.MemberSearchDto;
import com.lecture101.entity.Member;
import com.lecture101.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@RequestMapping("/members")
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;




    @GetMapping(value = "/new")
    public String memberForm(Model model){
        model.addAttribute("memberFormDto", new MemberFormDto());
        return "member/memberForm";
    }


    @PostMapping(value = "/new")
    public String newMember(@Valid MemberFormDto memberFormDto, BindingResult bindingResult, Model model){

        if(bindingResult.hasErrors()){
            return "member/memberForm";
        }

        try {
            Member member = Member.createMember(memberFormDto, passwordEncoder);
            memberService.saveMember(member);
        } catch (IllegalStateException e){
            model.addAttribute("errorMessage", e.getMessage());
            return "member/memberForm";
        }

        return "redirect:/";
    }

    @GetMapping(value = "/login")
    public String loginMember(){
        return "/member/memberLoginForm";
    }

    @GetMapping(value = "/login/error")
    public String loginError(Model model){
        model.addAttribute("loginErrorMsg", "아이디 또는 비밀번호를 확인해주세요");
        return "/member/memberLoginForm";
    }


    // 회원 관리 페이지 링킹
    @GetMapping(value = {"/admin"})
    public String memberManage(MemberSearchDto memberSearchDto, @PathVariable("page") Optional<Integer> page, Model model){

        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 10);
        Page<Member> members = memberService.getAdminMemberPage(memberSearchDto, pageable);

        model.addAttribute("members", members);
        model.addAttribute("memberSearchDto", memberSearchDto);
        model.addAttribute("maxPage", 5);

        return "member/memberMng";
    }
    //마이페이지 띄우기
    @GetMapping(value = {"/mypage"})
    public String showMyPage(Model model, @AuthenticationPrincipal User user) {
        if (user != null) {
            Member member = memberService.findByEmail(user.getUsername());
            model.addAttribute("member", member);
        }
        return "member/myPage";
    }
    // 맴버 수정폼을 띄우기
    @GetMapping(value = "/updateMemberForm/{memberId}")
    public String showUpdateMemberForm(@PathVariable Long memberId, Model model) {
        Member member = memberService.findById(memberId);
        model.addAttribute("member", member);
        return "member/memberUpdate";
    }

    // 회원 정보 수정 처리
    @PostMapping("/update")
    public String updateMember(@ModelAttribute Member member,
                               String currentPassword,
                               String newPassword,
                               String confirmPassword,
                               Model model) {

        Member existingMember = memberService.findById(member.getId());

        // 현재 비밀번호 확인 로직
        //타이핑한 currentPassword 와 db에 있는 기존 existingMember.getPassword()
        // .matches 메서드를 이용해서 비교
        if (!passwordEncoder.matches(currentPassword, existingMember.getPassword())) {
            model.addAttribute("errorMessage", "현재 비밀번호가 일치하지 않습니다.");
            model.addAttribute("member", member); // 원래의 멤버 정보를 다시 모델에 추가
            return "member/memberUpdate";
        }

        // 새 비밀번호와 비밀번호 확인 일치 여부 확인 로직
        if (newPassword != null && !newPassword.isEmpty()) {
            if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("errorMessage", "새 비밀번호가 일치하지 않습니다.");
                model.addAttribute("member", member); // 원래의 멤버 정보를 다시 모델에 추가
                return "member/memberUpdate";
            }
            String encodedPassword = passwordEncoder.encode(newPassword);
            member.setPassword(encodedPassword);
        }

        try {
            memberService.updateMember(member);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("member", member); // 원래의 멤버 정보를 다시 모델에 추가
            return "member/memberUpdate";
        }

        return "redirect:/";
    }

    // 회원탈퇴
    @GetMapping("/deleteMember/{memberId}")
    public String deleteMember(@PathVariable Long memberId, HttpServletRequest request, HttpServletResponse response) {
        memberService.deleteMember(memberId);
        // 로그아웃 처리
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }


        return "redirect:/members/login";
    }




    // 회원 상세 페이지 폼
    @GetMapping(value = "/admin/{memberId}")
    public String memberDtl(@PathVariable("memberId") Long memberId, Model model){

        try {
            // 예) 맴버id로, 실제 디비에서 조회 후, 내용을 dto 담기.
            MemberFormDto memberFormDto = memberService.getMemberDtl(memberId);
            // dto 담은 내용을 모델 인스턴스에 담아서, 뷰로 전달.
            model.addAttribute("memberFormDto", memberFormDto);
        } catch(EntityNotFoundException e){
            // 유효성, 체크.
            model.addAttribute("errorMessage", "존재하지 않는 회원입니다.");
            model.addAttribute("memberFormDto", new MemberFormDto());
            return "member/memberForm";
        }

        return "member/memberForm";
    }

    // 회원 상세 페이지에서 수정 처리부분
//    @PostMapping(value = "/admin/{memberId}")
//    public String memberUpdate(@Valid MemberFormDto memberFormDto, BindingResult bindingResult){
//// 일반 데이터 기본 유효성 체크.
//        if(bindingResult.hasErrors()){
//            return "member/memberForm";
//        }
//        try {
//            // 일반, 파일 데이터를 전달함.
//            memberService.updateMember(memberFormDto);
//        } catch (Exception e){
//            return "imember/memberForm";
//        }
//
//        return "redirect:/";
//    }

}