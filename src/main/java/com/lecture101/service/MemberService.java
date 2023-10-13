package com.lecture101.service;


import com.lecture101.dto.MemberFormDto;
import com.lecture101.dto.MemberSearchDto;
import com.lecture101.dto.MemberUpdateDto;
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
    //회원 정보 수정 처리 로직
    public void updateMember(MemberUpdateDto memberUpdateDto , PasswordEncoder passwordEncoder) {
        Member existingMember = memberRepository.findById(memberUpdateDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));

        // 이메일이 변경되었는지 확인하고, 변경되었다면 중복 체크를 수행합니다.
        // 이메일 readOnly = true 라서 필요없는 부분
//        if(!existingMember.getEmail().equals(member.getEmail())) {
//            validateDuplicateMember(member);
//        }

//        existingMember.setName(memberUpdateDto.getName());
//        existingMember.setEmail(memberUpdateDto.getEmail());
//        existingMember.setAddress(memberUpdateDto.getAddress());

        // 비밀번호가 널이 아니고 비어 있지 않다면 업데이트합니다.
        if (memberUpdateDto.getCurrentPassword() != null && !memberUpdateDto.getCurrentPassword().isEmpty()) {
            //existingMember.setPassword(memberUpdateDto.getNewPassword());
            // 원래는 데이터를 하나하나 넣으려고 했는데 Member entity에
            // updateMember 메서드를 정의해서 한번에 넣기
            existingMember.updateMember(memberUpdateDto,passwordEncoder);
        }

        // 저장하지 않아도, JPA가 트랜잭션 종료 시점에 변경 감지 (Dirty Checking)를 하여 업데이트 쿼리를 실행합니다.
        // 만약 다른 로직이 추가로 필요하다면 memberRepository.save(existingMember);를 호출하세요.
    }


    // 회원탈퇴(삭제) 로직
    public void deleteMember(Long memberId) {
        Member memberToDelete = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));
        memberRepository.delete(memberToDelete);
    }


    //이메일로 맴버 정보불러오기
    public MemberUpdateDto findByEmail(String email) {
        Member member = memberRepository.findByEmail(email);
        MemberUpdateDto memberUpdateDto = MemberUpdateDto.of(member);
        return memberUpdateDto;
    }
    //id로 맴버 정보 불러오기
//    public MemberUpdateDto findById(Long memberId) {
//        Member member = memberRepository.findById(memberId);
//        MemberUpdateDto memberUpdateDto = MemberUpdateDto.of(member);
//        return memberUpdateDto;
//    }

    @Transactional(readOnly = true)
    public MemberUpdateDto getMemberDtl2(Long memberId){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(EntityNotFoundException::new);
        MemberUpdateDto memberUpdateDto = MemberUpdateDto.of(member);
        return memberUpdateDto;
    }



}