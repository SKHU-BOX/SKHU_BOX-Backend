package com.example.skhubox.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LockerReserveRequest {

    private Long userId;
    private Long lockerId;

    public LockerReserveRequest() {
    }

}