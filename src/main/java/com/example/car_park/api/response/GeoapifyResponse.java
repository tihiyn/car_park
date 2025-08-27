package com.example.car_park.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoapifyResponse {
    private List<Result> results;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private String country;
        private String state;
        private String city;
        private String street;
        @JsonProperty("housenumber")
        private String houseNumber;

        @Override
        public String toString() {
            return String.format("%s, %s, %s, %s, %s",
                    country,
                    state,
                    city,
                    street,
                    houseNumber);
        }
    }
}