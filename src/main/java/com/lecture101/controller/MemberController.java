package com.lecture101.controller;

import com.lecture101.dto.MemberFormDto;
import com.lecture101.dto.MemberSearchDto;
import com.lecture101.entity.Member;
import com.lecture101.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
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
    @GetMapping(value = "/updatemember/{memberId}")
    public String showUpdateMemberForm(@PathVariable Long memberId, Model model) {
        Member member = memberService.findById(memberId);
        model.addAttribute("member", member);
        return "member/memberUpdate";
    }

    // 맴버 수정 로직 처리
    @PostMapping(value = "/checkPwd/{memberId}")


    // 회원 상세 페이지 폼 (수정 폼)
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
    @PostMapping(value = "/admin/{memberId}")
    public String memberUpdate(@Valid MemberFormDto memberFormDto, BindingResult bindingResult){
// 일반 데이터 기본 유효성 체크.
        if(bindingResult.hasErrors()){
            return "member/memberForm";
        }
        try {
            // 일반, 파일 데이터를 전달함.
            memberService.updateMember(memberFormDto);
        } catch (Exception e){
            return "imember/memberForm";
        }

        return "redirect:/";
    }

}