package com.duythuc_dh52201541.moive_ticket_infinity_cinema.service;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.request.UserUpdateRequest;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.request.UsersCreationRequest;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.respone.UsersRespone;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity.Role;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity.Users;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.enums.Roles;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.enums.UserStatus;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.exception.AppException;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.exception.ErrorCode;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.mapper.UserMapper;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.repository.RoleRepository;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class UserService {
    private final RoleRepository roleRepository;
    UserRepository userRepository;
    UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    //PasswordEncoder passwordEncoder;
    public UsersRespone createUser(UsersCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())){
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        Role role = roleRepository.save(Role.builder().name(Roles.User.name()).build());
        var roles = new HashSet<Role>();
        roles.add(role);
        Users newUsers = userMapper.toUsers(request);
        newUsers.setPassword(passwordEncoder.encode(newUsers.getPassword()));
        newUsers.setRole(roles);
        newUsers.setUserStatus(UserStatus.ACTIVE);
        return  userMapper.toUsersRespone(userRepository.save(newUsers));
    }

    public List<UsersRespone> getUsers() {
        log.info("In method get Users");
        // Lấy toàn bộ danh sách User trong DB
        return userRepository.findAll()       // 1. Truy vấn DB, lấy ra List<User>
                .stream()                     // 2. Chuyển List<User> thành Stream<User>
                .map(userMapper::toUsersRespone) // 3. Với mỗi User -> map sang UserResponse
                .toList();                    // 4. Thu thập lại thành List<UserResponse>
    }

    public UsersRespone getUser(String id) {
        // Tìm user theo id, nếu không có thì ném exception
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Map entity -> response DTO
        return userMapper.toUsersRespone(user);
    }

    public UsersRespone getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        Users user = userRepository.findByUsername(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUsersRespone(user);
    }

    // =================== UPDATE ===================
    public UsersRespone updateUser(String userId, UserUpdateRequest request) {
        // 1️⃣ Lấy user theo id từ DB (nếu không tồn tại thì ném exception USER_NOT_FOUND)
        Users newUser = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2️⃣ Dùng mapper để cập nhật các field từ request -> user entity
        userMapper.updateUser(newUser, request);

        // 3️⃣ Mã hóa password mới từ request rồi set lại cho user
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        // 4️⃣ Lấy danh sách Role từ DB theo list id trong request
        var roles = roleRepository.findAllById(request.getRoles());
        newUser.setRole(new HashSet<>(roles));

        // 5️⃣ Lưu user đã update vào DB rồi map sang DTO trả về
        return userMapper.toUsersRespone(userRepository.save(newUser));
    }
    public void deleteUser(String userId) {
        // Xoá user theo id trong DB
        userRepository.deleteById(userId);
    }
}
