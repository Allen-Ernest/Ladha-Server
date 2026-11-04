package tz.co.ladha.controller.district_controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tz.co.ladha.dto.district_dto.*;
import tz.co.ladha.service.district_service.DistrictService;

import java.util.List;

@RestController
@RequestMapping("/api/districts")
@AllArgsConstructor
public class DistrictController {
    private final DistrictService districtService;

    @GetMapping("/get-districts")
    public ResponseEntity<List<DistrictResponseDTO>> getDistricts() {
        return ResponseEntity.ok(districtService.getAllDistricts());
    }

    @PostMapping("/add-district")
    public ResponseEntity<DistrictResponseDTO> addDistrict(@Valid @RequestBody DistrictDTO districtDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(districtService.addDistrict(districtDTO));
    }

    @PostMapping("/update-district")
    public ResponseEntity<DistrictResponseDTO> updateDistrict(@Valid @RequestBody DistrictDTO districtDTO) {
        return ResponseEntity.ok(districtService.updateDistrict(districtDTO));
    }

    @PostMapping("/delete-district")
    public ResponseEntity<Boolean> deleteDistrict(@Valid @RequestBody DeleteDistrictDTO deleteDistrictDTO) {
        return ResponseEntity.ok(districtService.deleteDistrict(deleteDistrictDTO.getDistrictId()));
    }

    @PostMapping("/add-ward")
    public ResponseEntity<WardResponseDTO> addWard(@Valid @RequestBody WardDTO wardDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(districtService.addWard(wardDTO));
    }

    @PostMapping("/update-ward")
    public ResponseEntity<WardResponseDTO> updateWard(@Valid @RequestBody WardDTO wardDTO) {
        return ResponseEntity.ok(districtService.updateWard(wardDTO));
    }

    @PostMapping("/delete-ward")
    public ResponseEntity<Boolean> deleteWard(@Valid @RequestBody DeleteWardDTO deleteWardDTO) {
        return ResponseEntity.ok(districtService.deleteWard(deleteWardDTO.getWardId()));
    }
}
