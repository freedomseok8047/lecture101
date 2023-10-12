package com.lecture101.service;


import com.lecture101.dto.MemberFormDto;
import com.lecture101.dto.MemberSearchDto;
import com.lecture101.entity.Member;
import com.lecture101.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;


@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder; // PasswordEncoder를 주입



    public Member saveMember(Member member){
        validateDuplicateMember(member);
        return memberRepository.save(member);
    }

    private void validateDuplicateMember(Member member){
        Member findMember = memberRepository.findByEmail(member.getEmail());
        if(findMember != null){
            throw new IllegalStateException("이미 가입된 회원입니다.");
        }
    }


    public boolean checkPassword(Long id, String checkPassword) {
        Member member = memberRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
        String realPassword = member.getPassword();
        boolean matches = passwordEncoder.matches(checkPassword, realPassword);
        return matches;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Member member = memberRepository.findByEmail(email);

        if(member == null){
            throw new UsernameNotFoundException(email);
        }

        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .roles(member.getRole().toString())
                .build();
    }


    @Transactional(readOnly = true)
    public Page<Member> getAdminMemberPage(MemberSearchDto memberSearchDto, Pageable pageable){
        return memberRepository.getAdminMemberPage(memberSearchDto, pageable);
    }

    @Transactional(readOnly = true)
    public MemberFormDto getMemberDtl(Long memberId){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(EntityNotFoundException::new);
        MemberFormDto memberFormDto = MemberFormDto.of(member);
        return memberFormDto;
    }
    //회원정보 수정 처리 로직
    public Long updateMember(MemberFormDto memberFormDto) throws Exception{
        //회원정보 수정,
        // 기존 회원 정보를 불러오기.
        Member member = memberRepository.findById(memberFormDto.getId())
                .orElseThrow(EntityNotFoundException::new);
        // 기존 아이템에 내용에 , 더티 체킹. 변경사항에 대해서, 영속성이 알아서 자동으로 처리.
        member.updateMember(memberFormDto);
        memberRepository.save(member);


        return member.getId();
    }
    //이메일로 맴버 정보불러오기
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }
    //id로 맴버 정보 불러오기
    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElse(null); // 회원이 없을 경우 null을 반환하거나 예외 처리를 수행하세요.
    }




}