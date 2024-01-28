package com.project202411.journeyManager.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class JourneyResponse {

    private String id;
    private LocalDateTime createdAt;

}
