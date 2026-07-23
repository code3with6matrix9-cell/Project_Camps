package com.vof.service;
import com.vof.dto.request.CreatePackageRequest;
import com.vof.dto.response.PackageResponse;
import java.util.List;
public interface PackageService {
    PackageResponse createPackage(CreatePackageRequest request);
    PackageResponse updatePackage(Long id, CreatePackageRequest request);
    void deletePackage(Long id);
    List<PackageResponse> getAllPackages();
    PackageResponse getPackageById(Long id);
}
