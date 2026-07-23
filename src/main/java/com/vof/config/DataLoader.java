package com.vof.config;

import com.vof.constant.RoleConstant;
import com.vof.entity.Role;
import com.vof.entity.User;
import com.vof.repository.RoleRepository;
import com.vof.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

@Component
public class DataLoader {
    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String bootstrapAdminEmail;
    private final String bootstrapAdminPassword;
    private final String bootstrapAdminMobileNumber;

    public DataLoader(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder,
                      @Value("${app.bootstrap-admin.email:}") String bootstrapAdminEmail,
                      @Value("${app.bootstrap-admin.password:}") String bootstrapAdminPassword,
                      @Value("${app.bootstrap-admin.mobile-number:}") String bootstrapAdminMobileNumber) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapAdminEmail = bootstrapAdminEmail;
        this.bootstrapAdminPassword = bootstrapAdminPassword;
        this.bootstrapAdminMobileNumber = bootstrapAdminMobileNumber;
    }

    @PostConstruct
    public void init() {
        createRoleIfMissing(RoleConstant.ROLE_USER);
        createRoleIfMissing(RoleConstant.ROLE_ADMIN);
        createBootstrapAdminIfConfigured();
    }

    private void createRoleIfMissing(String roleName) {
        if (roleRepository.findByName(roleName).isPresent()) return;
        Role role = new Role();
        role.setName(roleName);
        roleRepository.save(role);
        log.info("Initialized role {}", roleName);
    }

    private void createBootstrapAdminIfConfigured() {
        if (!StringUtils.hasText(bootstrapAdminEmail) || !StringUtils.hasText(bootstrapAdminPassword)
                || !StringUtils.hasText(bootstrapAdminMobileNumber)) return;
        if (userRepository.existsByEmail(bootstrapAdminEmail)) return;
        Role adminRole = roleRepository.findByName(RoleConstant.ROLE_ADMIN).orElseThrow();
        User user = new User(bootstrapAdminEmail, bootstrapAdminMobileNumber, passwordEncoder.encode(bootstrapAdminPassword));
        user.setRoles(java.util.Set.of(adminRole));
        userRepository.save(user);
        log.warn("Created configured bootstrap administrator {}. Remove bootstrap credentials after initial deployment.", bootstrapAdminEmail);
    }
}
