package com.ams.dto;
public class SuggestedFlightNumberResponse {
    private String suggestedFlightNumber;
    public SuggestedFlightNumberResponse() {}
    public SuggestedFlightNumberResponse(String value) { suggestedFlightNumber = value; }
    public String getSuggestedFlightNumber() { return suggestedFlightNumber; }
    public void setSuggestedFlightNumber(String value) { suggestedFlightNumber = value; }
}
