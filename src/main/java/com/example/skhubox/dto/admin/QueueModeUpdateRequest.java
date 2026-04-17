package com.example.skhubox.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QueueModeUpdateRequest {
    
    @JsonProperty("enabled")
    private boolean enabled;
}
