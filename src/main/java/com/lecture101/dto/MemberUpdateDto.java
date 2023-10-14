package com.lecture101.dto;

import com.lecture101.entity.Member;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class MemberUpdateDto {
    private Long id;

    @NotBlank(message = "이름은 필수 입력 값입니다.")
    private String name;

    @NotEmpty(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "이메일 형식으로 입력해주세요.")
    private String email;

    @NotEmpty(message = "주소는 필수 입력 값입니다.")
    private String address;

    @NotEmpty(message = "비밀번호는 필수 입력 값입니다.")
    @Length(min=8, max=16, message = "비밀번호는 8자 이상, 16자 이하로 입력해주세요")
    private String CurrentPassword;

    @NotEmpty(message = "새 비밀번호는 필수 입력 값입니다.")
    @Length(min=8, max=16, message = "비밀번호는 8자 이상, 16자 이하로 입력해주세요")
    private String newPassword;

    @NotEmpty(message = "새 비밀번호 확인은 필수 입력 값입니다.")
    @Length(min=8, max=16, message = "비밀번호는 8자 이상, 16자 이하로 입력해주세요")
    private String ConfirmPassword;


    public static MemberUpdateDto of(Member member) {
        MemberUpdateDto memberUpdateDto = new MemberUpdateDto();
        memberUpdateDto.setId(member.getId());
        memberUpdateDto.setName(member.getName());
        memberUpdateDto.setEmail(member.getEmail());
        memberUpdateDto.setAddress(member.getAddress());

        // You might want to set the password-related fields to null or some default values here
        memberUpdateDto.setCurrentPassword(member.getPassword());
        memberUpdateDto.setNewPassword(null);
        memberUpdateDto.setConfirmPassword(null);

        return memberUpdateDto;
    }
}
