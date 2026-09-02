package com.ams.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PassengerRequest {
    @NotBlank(message = "passengerName is required")
    @Size(min = 2, max = 100, message = "passengerName must be between 2 and 100 characters")
    private String passengerName;

    @Min(value = 1, message = "passenger age must be at least 1")
    @Max(value = 120, message = "passenger age must not exceed 120")
    private Integer age;

    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
}
