package com.ams.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure Bean Validation tests (no Spring context) for the backend-enforced
 * validation rules on RegisterUserRequest — password strength, phone format,
 * email format, ZIP format, and minimum age.
 */
class RegisterUserRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private RegisterUserRequest validRequest() {
        RegisterUserRequest r = new RegisterUserRequest();
        r.setUserName("john_doe");
        r.setPassword("Passw0rd!");
        r.setCustomerCategory("REGULAR");
        r.setPhone("9876543210");
        r.setEmailId("john@gmail.com");
        r.setAddress1("Address 1");
        r.setCity("Chennai");
        r.setState("Tamil Nadu");
        r.setZipCode("600001");
        r.setDob(LocalDate.of(2000, 1, 1));
        r.setFavouriteSport("Cricket");
        r.setFavouriteHobby("Reading");
        return r;
    }

    @Test
    void validRequest_hasNoViolations() {
        assertTrue(validator.validate(validRequest()).isEmpty());
    }

    @Test
    void weakPassword_missingSpecialChar_isRejected() {
        RegisterUserRequest r = validRequest();
        r.setPassword("Password1");
        Set<ConstraintViolation<RegisterUserRequest>> violations = validator.validate(r);
        assertFalse(violations.isEmpty());
    }

    @Test
    void weakPassword_tooShort_isRejected() {
        RegisterUserRequest r = validRequest();
        r.setPassword("P1!aa");
        assertFalse(validator.validate(r).isEmpty());
    }

    @Test
    void invalidPhone_wrongStartDigit_isRejected() {
        RegisterUserRequest r = validRequest();
        r.setPhone("5876543210");
        assertFalse(validator.validate(r).isEmpty());
    }

    @Test
    void invalidPhone_wrongLength_isRejected() {
        RegisterUserRequest r = validRequest();
        r.setPhone("98765432");
        assertFalse(validator.validate(r).isEmpty());
    }

    @Test
    void invalidEmail_isRejected() {
        RegisterUserRequest r = validRequest();
        r.setEmailId("not-an-email");
        assertFalse(validator.validate(r).isEmpty());
    }

    @Test
    void invalidZip_leadingZero_isRejected() {
        RegisterUserRequest r = validRequest();
        r.setZipCode("012345");
        assertFalse(validator.validate(r).isEmpty());
    }

    @Test
    void futureDob_isRejected() {
        RegisterUserRequest r = validRequest();
        r.setDob(LocalDate.now().plusDays(1));
        assertFalse(validator.validate(r).isEmpty());
    }

    @Test
    void tooYoungDob_isRejected() {
        RegisterUserRequest r = validRequest();
        r.setDob(LocalDate.now().minusYears(5));
        assertFalse(validator.validate(r).isEmpty());
    }

    @Test
    void invalidUsername_tooShort_isRejected() {
        RegisterUserRequest r = validRequest();
        r.setUserName("ab1");
        assertFalse(validator.validate(r).isEmpty());
    }

    @Test
    void invalidCustomerCategory_isRejected() {
        RegisterUserRequest r = validRequest();
        r.setCustomerCategory("VIP");
        assertFalse(validator.validate(r).isEmpty());
    }
}
