package tz.co.ladha.service.vendor_service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tz.co.ladha.dto.vendor_dto.VendorRequestDTO;
import tz.co.ladha.exception.Exception;
import tz.co.ladha.model.vendor.RequestStatus;
import tz.co.ladha.model.vendor.VendorRequest;
import tz.co.ladha.repository.vendorRepository.VendorRepository;
import tz.co.ladha.repository.vendorRepository.VendorRequestRepository;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class VendorRequestService {
    private VendorRequestRepository vendorRequestRepository;
    private VendorRepository vendorRepository;

    public VendorRequest addVendorRequest(VendorRequestDTO vendorRequestDTO) {
        if (vendorRequestDTO.getPhoneNumber().trim().length() != 11) {
            throw new Exception("Invalid phone number");
        }

        if (vendorRepository.existsByRegistrationNumber(vendorRequestDTO.getRegistrationNumber())) {
            throw new Exception("Vendor request already exists");
        }

        if (vendorRepository.existsByVendorEmail(vendorRequestDTO.getEmail().trim())) {
            throw new Exception("Vendor request already exists");
        }

        if (vendorRepository.existsByVendorPhone(vendorRequestDTO.getPhoneNumber())) {
            throw new Exception("Vendor request already exists");
        }

        VendorRequest vendorRequest = new VendorRequest();

        vendorRequest.setVendorName(vendorRequestDTO.getVendorName().trim());
        vendorRequest.setRegistrationNumber(vendorRequestDTO.getRegistrationNumber());
        vendorRequest.setVendorEmail(vendorRequestDTO.getEmail().trim());
        vendorRequest.setVendorPhone(vendorRequestDTO.getPhoneNumber());
        vendorRequest.setRequestStatus(RequestStatus.PENDING);
        vendorRequest.setDateRequested(LocalDateTime.now());
        vendorRequest.setVendorAddress(vendorRequest.getVendorAddress());

        vendorRequestRepository.save(vendorRequest);
        //generate OTP
        //send it via phone number
        return vendorRequest;
    }

    //public Boolean verifyPhoneNumber(String phoneNumber) {
    //    OTP should be sent to the number
    //}
}
