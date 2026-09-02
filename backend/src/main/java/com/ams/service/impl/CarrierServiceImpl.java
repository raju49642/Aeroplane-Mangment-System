package com.ams.service.impl;

import com.ams.dto.CarrierRequest;
import com.ams.dto.CarrierResponse;
import com.ams.entity.Carrier;
import com.ams.exception.DuplicateResourceException;
import com.ams.exception.ResourceNotFoundException;
import com.ams.exception.InvalidBookingException;
import com.ams.repository.CarrierRepository;
import com.ams.repository.FlightRepository;
import com.ams.service.CarrierService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarrierServiceImpl implements CarrierService {

    private final CarrierRepository carrierRepository;
    private final FlightRepository flightRepository;

    public CarrierServiceImpl(CarrierRepository carrierRepository, FlightRepository flightRepository) {
        this.carrierRepository = carrierRepository;
        this.flightRepository = flightRepository;
    }

    @Override
    @Transactional
    public Carrier createCarrier(CarrierRequest request) {
        validateAdvanceBookingDiscounts(request);
        validateCustomerCategoryDiscounts(request);
        validateBulkBookingDiscount(request.getBulkBookingDiscount());
        validateRefundDiscounts(request);
        if (carrierRepository.existsByCarrierNameIgnoreCase(request.getCarrierName())) {
            throw new DuplicateResourceException("Carrier name already exists.");
        }
        if (carrierRepository.existsByCarrierCodeIgnoreCase(request.getCarrierCode())) {
            throw new DuplicateResourceException("Carrier code already exists.");
        }
        Carrier carrier = new Carrier();
        applyRequestToCarrier(carrier, request);
        return carrierRepository.save(carrier);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Carrier> getAllCarriers() {
        return carrierRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Carrier getCarrierById(Integer carrierId) {
        return carrierRepository.findById(carrierId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrier with ID " + carrierId + " not found"));
    }

    @Override
    @Transactional
    public Carrier updateCarrier(Integer carrierId, CarrierRequest request) {
        validateAdvanceBookingDiscounts(request);
        validateCustomerCategoryDiscounts(request);
        validateBulkBookingDiscount(request.getBulkBookingDiscount());
        validateRefundDiscounts(request);
        Carrier carrier = getCarrierById(carrierId);

        if (carrierRepository.existsByCarrierNameIgnoreCaseAndCarrierIdNot(request.getCarrierName(), carrierId)) {
            throw new DuplicateResourceException("Carrier name already exists.");
        }
        if (carrierRepository.existsByCarrierCodeIgnoreCaseAndCarrierIdNot(request.getCarrierCode(), carrierId)) {
            throw new DuplicateResourceException("Carrier code already exists.");
        }

        applyRequestToCarrier(carrier, request);
        return carrierRepository.save(carrier);
    }

    private void applyRequestToCarrier(Carrier carrier, CarrierRequest request) {
        carrier.setCarrierName(request.getCarrierName());
        carrier.setCarrierCode(request.getCarrierCode());
        carrier.setDiscount30DaysAdvance(request.getDiscount30DaysAdvance());
        carrier.setDiscount60DaysAdvance(request.getDiscount60DaysAdvance());
        carrier.setDiscount90DaysAdvance(request.getDiscount90DaysAdvance());
        carrier.setBulkBookingDiscount(request.getBulkBookingDiscount());
        carrier.setSilverUserDiscount(request.getSilverUserDiscount());
        carrier.setGoldUserDiscount(request.getGoldUserDiscount());
        carrier.setPlatinumUserDiscount(request.getPlatinumUserDiscount());
        carrier.setRefund2DaysBefore(request.getRefund2DaysBefore());
        carrier.setRefund10DaysBefore(request.getRefund10DaysBefore());
        carrier.setRefund20DaysOrMore(request.getRefund20DaysOrMore());
        carrier.setBusinessClassMultiplier(request.getBusinessClassMultiplier() == null ? 1.0 : request.getBusinessClassMultiplier());
        carrier.setExecutiveClassMultiplier(request.getExecutiveClassMultiplier() == null ? 1.0 : request.getExecutiveClassMultiplier());
    }

    private void validateAdvanceBookingDiscounts(CarrierRequest request) {
        validateAdvanceBookingDiscount(request.getDiscount30DaysAdvance());
        validateAdvanceBookingDiscount(request.getDiscount60DaysAdvance());
        validateAdvanceBookingDiscount(request.getDiscount90DaysAdvance());

        if (request.getDiscount30DaysAdvance() >= request.getDiscount60DaysAdvance()) {
            throw new InvalidBookingException("30-day discount must be less than 60-day discount.");
        }
        if (request.getDiscount60DaysAdvance() >= request.getDiscount90DaysAdvance()) {
            throw new InvalidBookingException("60-day discount must be less than 90-day discount.");
        }
    }

    private void validateAdvanceBookingDiscount(Double discount) {
        if (discount == null || discount < 10.0 || discount > 30.0) {
            throw new InvalidBookingException("Enter a discount between 10% and 30%.");
        }
    }

    private void validateCustomerCategoryDiscounts(CarrierRequest request) {
        validateCustomerCategoryDiscount(request.getSilverUserDiscount());
        validateCustomerCategoryDiscount(request.getGoldUserDiscount());
        validateCustomerCategoryDiscount(request.getPlatinumUserDiscount());
    }

    private void validateCustomerCategoryDiscount(Double discount) {
        if (discount == null || discount < 10.0 || discount > 30.0) {
            throw new InvalidBookingException("Enter the discount between 10% and 30%.");
        }
    }

    private void validateBulkBookingDiscount(Double discount) {
        if (discount == null || discount < 10.0 || discount > 30.0) {
            throw new InvalidBookingException("Enter the discount between 10% and 30%.");
        }
    }

    private void validateRefundDiscounts(CarrierRequest request) {
        validateRefundDiscount(request.getRefund2DaysBefore());
        validateRefundDiscount(request.getRefund10DaysBefore());
        validateRefundDiscount(request.getRefund20DaysOrMore());

        if (request.getRefund2DaysBefore() >= request.getRefund10DaysBefore()) {
            throw new InvalidBookingException("2–9 day refund must be less than 10–19 day refund.");
        }
        if (request.getRefund10DaysBefore() >= request.getRefund20DaysOrMore()) {
            throw new InvalidBookingException("10–19 day refund must be less than 20+ day refund.");
        }
    }

    private void validateRefundDiscount(Double refund) {
        if (refund == null || refund < 75.0 || refund > 95.0) {
            throw new InvalidBookingException("Enter the refund percentage between 75% and 95%.");
        }
    }

    // DTO conversion method
    private CarrierResponse toCarrierResponse(Carrier carrier) {
        return new CarrierResponse(
                carrier.getCarrierId(),
                carrier.getCarrierName(),
                carrier.getCarrierCode(),
                carrier.getDiscount30DaysAdvance(),
                carrier.getDiscount60DaysAdvance(),
                carrier.getDiscount90DaysAdvance(),
                carrier.getBulkBookingDiscount(),
                carrier.getSilverUserDiscount(),
                carrier.getGoldUserDiscount(),
                carrier.getPlatinumUserDiscount(),
                carrier.getRefund2DaysBefore(),
                carrier.getRefund10DaysBefore(),
                carrier.getRefund20DaysOrMore(),
                carrier.getBusinessClassMultiplier(),
                carrier.getExecutiveClassMultiplier()
        );
    }

    @Override
    @Transactional
    public CarrierResponse createCarrierResponse(CarrierRequest request) {
        return toCarrierResponse(createCarrier(request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarrierResponse> getAllCarriersAsResponse() {
        return getAllCarriers().stream()
                .map(this::toCarrierResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CarrierResponse getCarrierByIdAsResponse(Integer carrierId) {
        return toCarrierResponse(getCarrierById(carrierId));
    }

    @Override
    @Transactional
    public CarrierResponse updateCarrierResponse(Integer carrierId, CarrierRequest request) {
        return toCarrierResponse(updateCarrier(carrierId, request));
    }

    @Override
    @Transactional
    public void deleteCarrier(Integer carrierId) {
        Carrier carrier = getCarrierById(carrierId);
        if (flightRepository.existsByCarrierCarrierId(carrierId)) {
            throw new InvalidBookingException("Carrier '" + carrier.getCarrierName() + "' cannot be deleted while flights reference it");
        }
        carrierRepository.delete(carrier);
    }

}
