package com.duythuc_dh52201541.moive_ticket_infinity_cinema.service;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.request.AuthenticationResquest;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.request.IntrospectResquest;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.request.LogoutResquest;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.request.RegisterRequest;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.respone.AuthenticationResponse;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.respone.IntrospectResponse;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.respone.UsersRespone;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity.InvalidatedToken;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity.Role;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity.Users;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity.VerificationToken;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.enums.Roles;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.enums.UserStatus;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.exception.AppException;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.exception.ErrorCode;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.mapper.UserMapper;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.repository.InvalidatedTokenRepository;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.repository.RoleRepository;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.repository.UserRepository;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.repository.VerificationTokenRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository  invalidatedTokenRepository;
    private final UserMapper userMapper;
    private final VerificationTokenRepository verificationTokenRepository;
    private final @Lazy EmailService emailService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    @NonFinal
    @Value("${jwt.signerKey}") //doc bien tu file .yaml
    protected String SIGNER_KEY;

    public AuthenticationResponse authenticate(AuthenticationResquest resquest) {
        var user = userRepository.findByUsername(resquest.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated =  passwordEncoder.matches(resquest.getPassword(), user.getPassword());

        if(!authenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .enabled(user.isEnabled())
                .build();
    }

    // ✅ Hàm xử lý đăng xuất (logout) — vô hiệu hóa token đang sử dụng
    public void logout(LogoutResquest request) throws ParseException, JOSEException {
        try {
            // 1️⃣ Xác thực (verify) token nhận được từ client (FE gửi lên trong request body)
            //    - Nếu token không hợp lệ hoặc đã hết hạn → verifyToken() sẽ ném AppException
            var signToken = verifyToken(request.getToken());

            // 2️⃣ Lấy "JWT ID" (jit) từ phần claims của token
            //    → Đây là mã định danh duy nhất cho mỗi JWT, dùng để theo dõi token bị vô hiệu hóa
            String jit = signToken.getJWTClaimsSet().getJWTID();

            // 3️⃣ Lấy thời gian hết hạn (expiryTime) của token để biết khi nào có thể xóa khỏi DB
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            // 4️⃣ Tạo một bản ghi mới đánh dấu token này là "đã bị vô hiệu hóa"
            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jit)                 // Lưu lại JWT ID
                    .expiryTime(expiryTime)  // Lưu lại thời gian hết hạn
                    .build();

            // 5️⃣ Lưu token này vào cơ sở dữ liệu (bảng invalidated_token)
            //    → Khi có request mới đến, hệ thống sẽ kiểm tra xem token này có trong danh sách bị vô hiệu hóa không.
            invalidatedTokenRepository.save(invalidatedToken);

        } catch (AppException exception) {
            // 6️⃣ Nếu verifyToken() ném AppException (token hết hạn, sai định dạng, v.v.)
            //    → Chỉ log thông báo, không ném lỗi ra ngoài vì token hết hạn thì coi như đã logout xong rồi
            log.info("Token already expired");
        }
    }


    @Transactional
    public UsersRespone register(RegisterRequest request) {

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        if(userRepository.existsByUsername(request.getUsername())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        Role role = roleRepository.save(Role.builder().name(Roles.USER.name()).build());
        var roles = new HashSet<Role>();
        roles.add(role);

        // 1️⃣ Map DTO → entity
        Users user = userMapper.toRegisterUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(roles);
        user.setUserStatus(UserStatus.ACTIVE);
        user.setEnabled(false); // mặc định chưa verify
        userRepository.save(user);

        // 2️⃣ Tạo OTP 6 so dùng để xác thực email
        String otp = generateVerificationCode();
        VerificationToken token = new VerificationToken();
        token.setUser(user);
        token.setVerificationCode(otp);
        token.setVerificationCodeExpiresAt(LocalDateTime.now().plusSeconds(60));
        verificationTokenRepository.save(token);

        //Send mail
        sendMail(user,token.getVerificationCode());
        return userMapper.toUsersRespone(user);
    }

    @Transactional
    public void forgotPassword(String email) {
        Users user = userRepository.findByUsername(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Tạo OTP mới
        String otp = generateVerificationCode();
        log.info("✅ OTP tạo ra: {}", otp);
        VerificationToken token = verificationTokenRepository.findByUser(user)
                .orElse(new VerificationToken());

        token.setUser(user);
        token.setVerificationCode(otp);
        token.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(5));
        token.setInvalidated(false);
        token.setLastSentAt(LocalDateTime.now());

        verificationTokenRepository.save(token);
        verificationTokenRepository.flush(); // đảm bảo token được ghi ngay
        // Gửi mail
        emailService.sendEmail(user.getUsername(), "Reset Password OTP", otp);
    }


    @Transactional
    public void resetPassword(String email, String newPassword) {
        Users user = userRepository.findByUsername(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        VerificationToken token = verificationTokenRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.OTP_NOT_VERIFIED));

        // OTP phải đã được xác thực (invalidated = true)
        if (token.isInvalidated()) {
            // OK, OTP đã được verify
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        } else {
            throw new AppException(ErrorCode.OTP_NOT_VERIFIED);
        }

    }

    // Phương thức introspect: kiểm tra tính hợp lệ của JWT token
    public IntrospectResponse introspect(IntrospectResquest resquest)
            throws JOSEException, ParseException {   // Có thể ném ra lỗi JOSE hoặc Parse nếu token sai format

        // 1. Lấy token từ request mà client gửi lên
        var token = resquest.getToken();

        // 2. Mặc định token là không hợp lệ
        boolean inValid = true;

        try {
            // 3. Thử verify token (chữ ký, hạn,...)
            verifyToken(token);
        } catch (AppException e) {
            // 4. Nếu verify thất bại -> token không hợp lệ
            inValid = false;
        }

        // 5. Trả về kết quả introspect cho client
        return IntrospectResponse.builder()
                .valid(inValid) // true = hợp lệ, false = không hợp lệ
                .build();
    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {

        // 2. Tạo đối tượng verifier để xác minh chữ ký của token
        //    MACVerifier dùng HMAC + secret key (SIGNER_KEY) để verify
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        // 3. Parse chuỗi token thành đối tượng SignedJWT (Nimbus cung cấp)
        SignedJWT signedJWT = SignedJWT.parse(token);

        // 4. Lấy thời gian hết hạn (expiration time - exp claim) trong JWT
        Date expityTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        // 5. Xác minh chữ ký JWT bằng secret key
        var verified = signedJWT.verify(verifier);
        if (!(verified && expityTime.after(new Date())))
            throw new AppException(ErrorCode.AUTHENTICATED);

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.AUTHENTICATED);

        // 6. Trả về IntrospectRespone:
        //    - valid = true nếu:
        //        (a) chữ ký hợp lệ
        //        (b) token chưa hết hạn
        //    - valid = false trong các trường hợp còn lại
        return signedJWT;
    }

    private String generateToken(Users user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("infinity_cinema.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now()
                                .plus(30, ChronoUnit.MINUTES)
                                .toEpochMilli()
                ))
                .claim("scope", buildScope(user))
                .jwtID(UUID.randomUUID().toString())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject  jwsObject = new JWSObject(jwsHeader,payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildScope(Users user) {
        StringJoiner scope = new StringJoiner(" ");
        if(!CollectionUtils.isEmpty(user.getRole()))
            user.getRole().forEach(role -> {
                scope.add(role.getName());
            });

        return scope.toString();
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000);
        return String.valueOf(code);
    }

    @Transactional
    public void verifyEmail(String otp, String userName) {
        // 1️⃣ Tìm user theo email
        Users user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2️⃣ Lấy token (OTP) gắn với user
        VerificationToken verificationToken = verificationTokenRepository
                .findByUserAndVerificationCode(user, otp)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_OTP));

        // 3️⃣ Kiểm tra OTP còn hạn không
        if (verificationToken.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        // 4️⃣ Kiểm tra OTP đã bị vô hiệu chưa
        if (verificationToken.isInvalidated()) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        // 5️⃣ Xác thực thành công → enable user
        if (!user.isEnabled()) {
            user.setEnabled(true);
            userRepository.save(user);
        }

        // 6️⃣ Vô hiệu hóa OTP sau khi dùng (đánh dấu chứ không xóa)
        verificationToken.setInvalidated(true);
        verificationTokenRepository.save(verificationToken);
        log.info("🔍 Đang xác thực OTP: {}, expires at: {}, now: {}",
                otp, verificationToken.getVerificationCodeExpiresAt(), LocalDateTime.now());

        // 7️⃣ (Tùy chọn) Xóa token cũ sau vài phút
        // verificationTokenRepository.delete(verificationToken);
    }


    @Transactional
    public void resendOtp(String userName) {
        Users user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        VerificationToken token = verificationTokenRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.OTP_NOT_FOUND));

        // Kiểm tra thời gian resend (30s)
        if (token.getLastSentAt() != null &&
                token.getLastSentAt().isAfter(LocalDateTime.now().minusSeconds(30))) {
            throw new AppException(ErrorCode.OTP_RESEND_TOO_SOON);
        }

        // Tạo OTP mới
        String otp = generateVerificationCode();
        token.setVerificationCode(otp);
        token.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(5)); // OTP mới còn hạn 5 phút
        token.setInvalidated(false);
        token.setLastSentAt(LocalDateTime.now());
        verificationTokenRepository.save(token);

        //Send mail
        sendMail(user,token.getVerificationCode());
    }

    private void sendMail(Users user,String otp){
        //Send Email
        String verificationCode = "VERIFICATION CODE " + otp;
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendEmail(user.getUsername(), "Xác thực tài khoản", htmlMessage);
            log.info("Email sent successfully");
        } catch (Exception e) {
            log.error("Failed to send email", e);
        }
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void deleteUnverifiedUsers() {

        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);
        List<Users> unverifiedUsers = userRepository.findAllByEnabledFalseAndCreatedAtBefore(cutoff);

        if (unverifiedUsers.isEmpty()) {
            log.info("✅ No unverified users to delete.");
            return;
        }

        for (Users user : unverifiedUsers) {
            try {
                log.info("Deleting unverified user: {}", user.getUsername());
                verificationTokenRepository.deleteByUser(user);
                user.getRole().clear();
                userRepository.save(user);
                userRepository.delete(user);
            } catch (Exception e) {
                log.error("❌ Error deleting user {}: {}", user.getUsername(), e.getMessage());
            }
        }
    }
}
