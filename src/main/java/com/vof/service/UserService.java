package com.vof.service;

import com.vof.dto.request.UpdateUserProfileRequest;
import com.vof.dto.response.UserProfileResponse;

public interface UserService {

    UserProfileResponse updateMyProfile(UpdateUserProfileRequest request);

    UserProfileResponse getMyProfile();

}