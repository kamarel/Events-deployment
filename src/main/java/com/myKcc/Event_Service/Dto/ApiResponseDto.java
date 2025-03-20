package com.myKcc.Event_Service.Dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponseDto {


    private List<MembersDto> membersDtoList;



    public List<MembersDto> getMembersDtoList() {
        return membersDtoList;
    }

    public void setMembersDtoList(List<MembersDto> membersDtoList) {
        this.membersDtoList = membersDtoList;
    }
}
