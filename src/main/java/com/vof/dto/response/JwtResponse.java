package com.vof.dto.response;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
@Getter @Setter
public class JwtResponse {
    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private Long id;
    private String email;
    private List<String> roles;
    public JwtResponse(String accessToken, String refreshToken, Long id, String email, List<String> roles) {
        this.token = accessToken;
        this.refreshToken = refreshToken;
        this.id = id;
        this.email = email;
        this.roles = roles;
    }
}
