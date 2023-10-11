package com.lecture101.dto;

import com.lecture101.constant.Role;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class MemberSearchDto {
    private String searchDateType;

    private Role searchRole;

    private String searchBy;

    private String searchQuery = "";
}
