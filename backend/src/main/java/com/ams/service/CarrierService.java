package com.ams.service;

import com.ams.dto.CarrierRequest;
import com.ams.dto.CarrierResponse;
import com.ams.entity.Carrier;

import java.util.List;

public interface CarrierService {

    Carrier createCarrier(CarrierRequest request);

    List<Carrier> getAllCarriers();

    Carrier getCarrierById(Integer carrierId);

    Carrier updateCarrier(Integer carrierId, CarrierRequest request);
    void deleteCarrier(Integer carrierId);

    // DTO methods to avoid Hibernate proxy serialization issues
    CarrierResponse createCarrierResponse(CarrierRequest request);

    List<CarrierResponse> getAllCarriersAsResponse();

    CarrierResponse getCarrierByIdAsResponse(Integer carrierId);

    CarrierResponse updateCarrierResponse(Integer carrierId, CarrierRequest request);
}
