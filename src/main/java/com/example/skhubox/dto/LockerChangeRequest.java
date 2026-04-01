package com.example.skhubox.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LockerChangeRequest {
    private Long newLockerId;
}
