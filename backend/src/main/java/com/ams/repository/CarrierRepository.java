package com.ams.repository;

import com.ams.entity.Carrier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarrierRepository extends JpaRepository<Carrier, Integer> {

    boolean existsByCarrierNameIgnoreCase(String carrierName);
    boolean existsByCarrierCodeIgnoreCase(String carrierCode);
    boolean existsByCarrierNameIgnoreCaseAndCarrierIdNot(String carrierName, Integer carrierId);
    boolean existsByCarrierCodeIgnoreCaseAndCarrierIdNot(String carrierCode, Integer carrierId);

    Optional<Carrier> findByCarrierName(String carrierName);
}
