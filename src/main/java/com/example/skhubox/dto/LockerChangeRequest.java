package com.example.skhubox.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LockerChangeRequest {

    private Long userId;
    private Long currentLockerId;
    private Long newLockerId;

    public LockerChangeRequest() {
    }

}