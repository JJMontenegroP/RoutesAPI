package com.project202411.journeyManager.loader;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Getter
@Component
public class AirportCodeLoader {

    private final Set<String> airportCodes;

    public AirportCodeLoader(@Value("${airport.codes}") String airportCodesString) {
        this.airportCodes = new HashSet<>(Arrays.asList(airportCodesString.split(",")));
    }

}