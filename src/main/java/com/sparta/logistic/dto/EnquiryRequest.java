package com.sparta.logistic.dto;

public class EnquiryRequest {
    private String firstName;
    private String email;
    private String phoneNumber;
    private String city;
    private String inquiry;
    private String message;

    public EnquiryRequest() {}

    // getters and setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getInquiry() { return inquiry; }
    public void setInquiry(String inquiry) { this.inquiry = inquiry; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    @Override
    public String toString() {
        return "EnquiryRequest{" +
                "firstName='" + firstName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", city='" + city + '\'' +
                ", enquiryType='" + inquiry + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
