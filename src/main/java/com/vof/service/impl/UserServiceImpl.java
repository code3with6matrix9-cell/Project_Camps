package com.vof.service.impl;

import com.vof.dto.request.UpdateUserProfileRequest;
import com.vof.dto.response.UserProfileResponse;
import com.vof.entity.User;
import com.vof.repository.UserRepository;
import com.vof.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserProfileResponse updateMyProfile(UpdateUserProfileRequest request) {

        User user = getCurrentUser();

        user.setUsername(request.getUsername().trim());

        String mobileNumber = request.getMobileNumber().trim();

        if (!user.getMobileNumber().equals(mobileNumber)
                && userRepository.existsByMobileNumber(mobileNumber)) {

            throw new IllegalArgumentException("Mobile number is already registered.");
        }

        user.setMobileNumber(mobileNumber);

        User updatedUser = userRepository.save(user);

        return UserProfileResponse.builder()
                .id(updatedUser.getId())
                .username(updatedUser.getUsername())
                .email(updatedUser.getEmail())
                .mobileNumber(updatedUser.getMobileNumber())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile() {

        User user = getCurrentUser();

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .build();
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));
    }
}