package com.hicms.service;

import com.hicms.dto.UserDTO;
import com.hicms.dto.UserRegistrationDTO;
import com.hicms.entity.Role;
import com.hicms.entity.User;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for User operations
 */
public interface UserService {
    
    User registerUser(UserRegistrationDTO registrationDTO);
    
    User registerUser(UserRegistrationDTO registrationDTO, Role role);
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findById(Long userId);
    
    List<User> findAllUsers();
    
    List<User> findUsersByRole(Role role);
    
    List<User> findActiveAgents();
    
    List<User> findActiveAdjusters();
    
    UserDTO convertToDTO(User user);
    
    User updateUser(Long userId, UserDTO userDTO);
    
    void enableUser(Long userId);
    
    void disableUser(Long userId);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    void deleteUser(Long userId);

}
