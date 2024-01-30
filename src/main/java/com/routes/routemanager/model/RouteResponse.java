package com.routes.routemanager.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class RouteResponse {

    private String id;
    private LocalDateTime createdAt;

}
