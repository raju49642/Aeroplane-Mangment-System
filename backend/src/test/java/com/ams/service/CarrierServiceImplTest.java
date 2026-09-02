package com.ams.service;

import com.ams.dto.CarrierRequest;
import com.ams.entity.Carrier;
import com.ams.exception.DuplicateResourceException;
import com.ams.exception.InvalidBookingException;
import com.ams.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CarrierServiceImplTest {

    @Autowired private CarrierService carrierService;

    private CarrierRequest buildRequest(String name) {
        CarrierRequest r = new CarrierRequest();
        r.setCarrierName(name);
        r.setCarrierCode("AI");
        r.setDiscount30DaysAdvance(10.0);
        r.setDiscount60DaysAdvance(20.0);
        r.setDiscount90DaysAdvance(30.0);
        r.setBulkBookingDiscount(10.0);
        r.setSilverUserDiscount(10.0);
        r.setGoldUserDiscount(20.0);
        r.setPlatinumUserDiscount(30.0);
        r.setRefund2DaysBefore(75.0);
        r.setRefund10DaysBefore(85.0);
        r.setRefund20DaysOrMore(95.0);
        return r;
    }

    @Test
    void createCarrier_withUniqueNameAndCode_succeeds() {
        Carrier c = carrierService.createCarrier(buildRequest("Air India"));
        assertNotNull(c.getCarrierId());
        assertEquals("Air India", c.getCarrierName());
        assertEquals("AI", c.getCarrierCode());
    }

    @Test
    void createCarrier_duplicateName_throwsException() {
        carrierService.createCarrier(buildRequest("IndiGo"));
        assertEquals("Carrier name already exists.", assertThrows(DuplicateResourceException.class,
                () -> carrierService.createCarrier(buildRequest("IndiGo"))).getMessage());
    }

    @Test
    void updateCarrier_nonExistent_throwsException() {
        assertThrows(ResourceNotFoundException.class, () -> carrierService.updateCarrier(9999, buildRequest("Ghost")));
    }

    @Test
    void createCarrier_acceptsDiscountBoundariesAndIncreasingSequence() {
        Carrier carrier = carrierService.createCarrier(buildRequest("Boundary Air"));

        assertEquals(10.0, carrier.getDiscount30DaysAdvance());
        assertEquals(30.0, carrier.getDiscount90DaysAdvance());
    }

    @Test
    void createCarrier_rejectsDiscountBelowTenPercent() {
        CarrierRequest request = buildRequest("Below Ten Air");
        request.setDiscount30DaysAdvance(9.99);

        assertEquals("Enter a discount between 10% and 30%.", assertThrows(InvalidBookingException.class,
                () -> carrierService.createCarrier(request)).getMessage());
    }

    @Test
    void createCarrier_rejectsDiscountAboveThirtyPercent() {
        CarrierRequest request = buildRequest("Above Thirty Air");
        request.setDiscount90DaysAdvance(30.01);

        assertEquals("Enter a discount between 10% and 30%.", assertThrows(InvalidBookingException.class,
                () -> carrierService.createCarrier(request)).getMessage());
    }

    @Test
    void createCarrier_rejectsThirtyDayDiscountGreaterThanOrEqualToSixtyDayDiscount() {
        CarrierRequest request = buildRequest("Invalid Thirty Air");
        request.setDiscount30DaysAdvance(20.0);
        request.setDiscount60DaysAdvance(20.0);

        assertEquals("30-day discount must be less than 60-day discount.", assertThrows(InvalidBookingException.class,
                () -> carrierService.createCarrier(request)).getMessage());
    }

    @Test
    void createCarrier_rejectsSixtyDayDiscountGreaterThanOrEqualToNinetyDayDiscount() {
        CarrierRequest request = buildRequest("Invalid Sixty Air");
        request.setDiscount60DaysAdvance(30.0);
        request.setDiscount90DaysAdvance(30.0);

        assertEquals("60-day discount must be less than 90-day discount.", assertThrows(InvalidBookingException.class,
                () -> carrierService.createCarrier(request)).getMessage());
    }

    @Test
    void createCarrier_acceptsCustomerCategoryDiscountBoundaries() {
        Carrier carrier = carrierService.createCarrier(buildRequest("Customer Discount Boundaries Air"));

        assertEquals(10.0, carrier.getSilverUserDiscount());
        assertEquals(30.0, carrier.getPlatinumUserDiscount());
    }

    @Test
    void createCarrier_rejectsCustomerCategoryDiscountBelowTenPercent() {
        CarrierRequest request = buildRequest("Customer Discount Below Air");
        request.setSilverUserDiscount(9.99);

        assertEquals("Enter the discount between 10% and 30%.", assertThrows(InvalidBookingException.class,
                () -> carrierService.createCarrier(request)).getMessage());
    }

    @Test
    void createCarrier_rejectsCustomerCategoryDiscountAboveThirtyPercent() {
        CarrierRequest request = buildRequest("Customer Discount Above Air");
        request.setGoldUserDiscount(30.01);

        assertEquals("Enter the discount between 10% and 30%.", assertThrows(InvalidBookingException.class,
                () -> carrierService.createCarrier(request)).getMessage());
    }

    @Test
    void createCarrier_caseInsensitiveDuplicateName_throwsException() {
        carrierService.createCarrier(buildRequest("IndiGo"));
        CarrierRequest duplicate = buildRequest("indigo");
        duplicate.setCarrierCode("IN");

        assertEquals("Carrier name already exists.", assertThrows(DuplicateResourceException.class,
                () -> carrierService.createCarrier(duplicate)).getMessage());
    }

    @Test
    void createCarrier_duplicateCode_throwsException() {
        carrierService.createCarrier(buildRequest("Air India"));
        CarrierRequest duplicate = buildRequest("IndiGo");

        assertEquals("Carrier code already exists.", assertThrows(DuplicateResourceException.class,
                () -> carrierService.createCarrier(duplicate)).getMessage());
    }

    @Test
    void createCarrier_caseInsensitiveDuplicateCode_throwsException() {
        carrierService.createCarrier(buildRequest("Air India"));
        CarrierRequest duplicate = buildRequest("IndiGo");
        duplicate.setCarrierCode("ai");

        assertEquals("Carrier code already exists.", assertThrows(DuplicateResourceException.class,
                () -> carrierService.createCarrier(duplicate)).getMessage());
    }

    @Test
    void updateCarrier_withOwnNameAndCode_succeeds() {
        Carrier carrier = carrierService.createCarrier(buildRequest("IndiGo"));
        Carrier updated = carrierService.updateCarrier(carrier.getCarrierId(), buildRequest("IndiGo"));

        assertEquals(carrier.getCarrierId(), updated.getCarrierId());
    }

    @Test
    void updateCarrier_withAnotherCarriersName_throwsException() {
        carrierService.createCarrier(buildRequest("IndiGo"));
        CarrierRequest secondRequest = buildRequest("Air India");
        secondRequest.setCarrierCode("IN");
        Carrier secondCarrier = carrierService.createCarrier(secondRequest);
        CarrierRequest duplicateName = buildRequest("INDIGO");
        duplicateName.setCarrierCode("IN");

        assertEquals("Carrier name already exists.", assertThrows(DuplicateResourceException.class,
                () -> carrierService.updateCarrier(secondCarrier.getCarrierId(), duplicateName)).getMessage());
    }

    @Test
    void updateCarrier_withAnotherCarriersCode_throwsException() {
        carrierService.createCarrier(buildRequest("IndiGo"));
        CarrierRequest secondRequest = buildRequest("Air India");
        secondRequest.setCarrierCode("IN");
        Carrier secondCarrier = carrierService.createCarrier(secondRequest);
        CarrierRequest duplicateCode = buildRequest("Air India");
        duplicateCode.setCarrierCode("ai");

        assertEquals("Carrier code already exists.", assertThrows(DuplicateResourceException.class,
                () -> carrierService.updateCarrier(secondCarrier.getCarrierId(), duplicateCode)).getMessage());
    }

    @Test
    void createCarrier_acceptsRefundBoundariesAndIncreasingSequence() {
        Carrier carrier = carrierService.createCarrier(buildRequest("Refund Boundaries Air"));

        assertEquals(75.0, carrier.getRefund2DaysBefore());
        assertEquals(95.0, carrier.getRefund20DaysOrMore());
    }

    @Test
    void createCarrier_rejectsRefundBelowSeventyFivePercent() {
        CarrierRequest request = buildRequest("Refund Below Air");
        request.setRefund2DaysBefore(74.99);

        assertEquals("Enter the refund percentage between 75% and 95%.", assertThrows(InvalidBookingException.class,
                () -> carrierService.createCarrier(request)).getMessage());
    }

    @Test
    void createCarrier_rejectsRefundAboveNinetyFivePercent() {
        CarrierRequest request = buildRequest("Refund Above Air");
        request.setRefund20DaysOrMore(95.01);

        assertEquals("Enter the refund percentage between 75% and 95%.", assertThrows(InvalidBookingException.class,
                () -> carrierService.createCarrier(request)).getMessage());
    }

    @Test
    void createCarrier_rejectsRefundOrdering() {
        CarrierRequest request = buildRequest("Refund Order Air");
        request.setRefund2DaysBefore(85.0);
        request.setRefund10DaysBefore(85.0);

        assertEquals("2–9 day refund must be less than 10–19 day refund.", assertThrows(InvalidBookingException.class,
                () -> carrierService.createCarrier(request)).getMessage());
    }

    @Test
    void createCarrier_rejectsTenToNineteenDayRefundGreaterThanOrEqualToTwentyDayRefund() {
        CarrierRequest request = buildRequest("Refund Second Order Air");
        request.setRefund10DaysBefore(95.0);
        request.setRefund20DaysOrMore(95.0);

        assertEquals("10–19 day refund must be less than 20+ day refund.", assertThrows(InvalidBookingException.class,
                () -> carrierService.createCarrier(request)).getMessage());
    }

    @Test
    void createCarrier_rejectsBulkBookingDiscountOutsideAllowedRange() {
        CarrierRequest request = buildRequest("Bulk Discount Air");
        request.setBulkBookingDiscount(30.01);

        assertEquals("Enter the discount between 10% and 30%.", assertThrows(InvalidBookingException.class,
                () -> carrierService.createCarrier(request)).getMessage());
    }
}
