package com.hicms.dto;

import com.hicms.entity.Role;
import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO for user information display
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private Role role;
    private boolean enabled;
    private LocalDateTime createdDate;
}
