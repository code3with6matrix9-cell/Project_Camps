package com.vof.controller;
import com.vof.dto.request.CreatePackageRequest;
import com.vof.dto.response.CommonApiResponse;
import com.vof.dto.response.PackageResponse;
import com.vof.service.PackageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/admin/packages") @RequiredArgsConstructor @SecurityRequirement(name = "bearerAuth")
public class AdminPackageController {
    private final PackageService packageService;
    @PostMapping
    public ResponseEntity<CommonApiResponse> createPackage(@Valid @RequestBody CreatePackageRequest request) {
        PackageResponse createdPackage = packageService.createPackage(request);
        return new ResponseEntity<>(CommonApiResponse.builder().success(true).data(createdPackage).message("Package created successfully.").build(), HttpStatus.CREATED);
    }
    @PutMapping("/{id}")
    public ResponseEntity<CommonApiResponse> updatePackage(@PathVariable Long id, @Valid @RequestBody CreatePackageRequest request) {
        return ResponseEntity.ok(CommonApiResponse.builder().success(true).data(packageService.updatePackage(id, request)).message("Package updated successfully.").build());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonApiResponse> deletePackage(@PathVariable Long id) {
        packageService.deletePackage(id);
        return ResponseEntity.ok(CommonApiResponse.builder().success(true).message("Package deleted successfully.").build());
    }
}
