package com.ams.dto;

public class PassengerResponse {
    private Integer passengerId;
    private String passengerName;
    private Integer age;

    public PassengerResponse() { }
    public PassengerResponse(Integer passengerId, String passengerName, Integer age) {
        this.passengerId = passengerId; this.passengerName = passengerName; this.age = age;
    }
    public Integer getPassengerId() { return passengerId; }
    public void setPassengerId(Integer passengerId) { this.passengerId = passengerId; }
    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
}
