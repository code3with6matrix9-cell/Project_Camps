package com.vof.controller;
import com.vof.dto.response.CommonApiResponse;
import com.vof.dto.response.PackageResponse;
import com.vof.service.PackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/packages") @RequiredArgsConstructor
public class PackageController {
    private final PackageService packageService;
    @GetMapping
    public ResponseEntity<CommonApiResponse> getAllPackages() {
        List<PackageResponse> packages = packageService.getAllPackages();
        return ResponseEntity.ok(CommonApiResponse.builder().success(true).message("Packages retrieved successfully").data(packages).build());
    }
    @GetMapping("/{id}")
    public ResponseEntity<CommonApiResponse> getPackageById(@PathVariable Long id) {
        PackageResponse pkg = packageService.getPackageById(id);
        return ResponseEntity.ok(CommonApiResponse.builder().success(true).message("Package details retrieved successfully").data(pkg).build());
    }
}
