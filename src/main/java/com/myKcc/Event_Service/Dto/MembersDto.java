package com.myKcc.Event_Service.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MembersDto {
    private Long id;
    private String memberId;
    private String fullName;
    private String email;
    private String dateOfBirth;
    private String sex;
    private String phoneNumber;
    private String maritalStatus;
    private String memberRank;
    private String memberRole;
    private String nationality;
    private String placeOfBirth;
    private String address;
    private String state;
    private String city;
    private String zipCode;
    private String parishName;
}
