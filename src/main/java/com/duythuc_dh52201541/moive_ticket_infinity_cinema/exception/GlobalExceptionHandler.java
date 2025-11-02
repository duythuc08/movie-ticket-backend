package com.duythuc_dh52201541.moive_ticket_infinity_cinema.exception;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.respone.ApiResponse;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.Objects;

// @ControllerAdvice: đánh dấu đây là class xử lý lỗi tập trung cho toàn bộ ứng dụng.
// Thay vì try/catch ở từng Controller, Spring sẽ tự động "gom" lỗi về đây xử lý.
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    private  static  final  String MIN_ATTRIBUTES = "min";
    // @ExceptionHandler: đánh dấu method này sẽ xử lý 1 loại Exception cụ thể
    // Ở đây là RuntimeException -> mọi RuntimeException xảy ra trong Controller sẽ vào đây.
    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<ApiResponse> handlingRuntimeExceptiom(RuntimeException exception) {
        // Trả về HTTP status 400 (Bad Request) kèm theo message của exception
        ApiResponse apiRespone = new ApiResponse();
        apiRespone.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiRespone.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());
        return ResponseEntity.badRequest().body(apiRespone);
    }

    // Khi có AppException được ném ra ở bất kỳ Controller nào
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlingAppExceptiom(AppException exception) {
        // Lấy ErrorCode ra từ exception
        ErrorCode errorCode = exception.getErrorCode();
        // Tạo response chuẩn để trả về cho client
        ApiResponse apiRespone = new ApiResponse();
        apiRespone.setCode(errorCode.getCode());       // Mã lỗi (vd: 1001)
        apiRespone.setMessage(errorCode.getMessage()); // Thông báo lỗi (vd: "User existed")

        // Trả về HTTP status 400 (Bad Request) + body JSON chứa code và message
        return ResponseEntity.badRequest().body(apiRespone);
    }

//    // Xử lý ngoại lệ khi dữ liệu đầu vào vi phạm validation (@Valid, @NotNull, @Size, @DobConstraint,...)
//    @ExceptionHandler(value = MethodArgumentNotValidException.class)
//    ResponseEntity<ApiResponse> handlingValidException(MethodArgumentNotValidException validException) {
//
//        // Lấy ra message được gán trong annotation validation
//        // Ví dụ: @NotBlank(message = "USERNAME_INVALID") => lấy được "USERNAME_INVALID"
//        String enumKey = validException.getFieldError().getDefaultMessage();
//
//        // Gán mặc định ErrorCode = INVALID_KEY (phòng trường hợp enumKey không tồn tại trong ErrorCode)
//        ErrorCode errorCode = ErrorCode.INVALID_KEY;
//
//        // Map chứa metadata (attributes) của annotation validation (vd: min=18,...)
//        Map<String,Object> attributes = null;
//
//        try {
//            // Thử chuyển enumKey thành một phần tử trong ErrorCode
//            // Ví dụ: "USERNAME_INVALID" -> ErrorCode.USERNAME_INVALID
//            errorCode = ErrorCode.valueOf(enumKey);
//
//            // Lấy toàn bộ thông tin vi phạm (ConstraintViolation)
//            var constrainViolation = validException.getBindingResult()
//                    .getAllErrors().getFirst()       // lấy error đầu tiên
//                    .unwrap(ConstraintViolation.class); // ép kiểu thành ConstraintViolation
//
//            // Lấy attributes từ annotation (vd: min=18 trong @DobConstraint(min=18))
//            attributes = constrainViolation.getConstraintDescriptor().getAttributes();
//
//        } catch (IllegalArgumentException e) {
//            // Nếu enumKey không map được với ErrorCode thì giữ nguyên INVALID_KEY
//        }
//
//        // Tạo response chuẩn để trả về client
//        ApiResponse apiRespone = new ApiResponse();
//        apiRespone.setCode(errorCode.getCode()); // Gán mã lỗi (vd: 1003)
//
//        // Nếu annotation có attributes (vd: {min=18}) thì format message bằng mapAttribute()
//        // Ví dụ: "Age must be at least {min}" -> "Age must be at least 18"
//        // Ngược lại thì lấy message mặc định trong ErrorCode
//        apiRespone.setMessage(Objects.nonNull(attributes)
//                ? mapAttribute(errorCode.getMessage(), attributes)
//                : errorCode.getMessage());
//
//        // Trả về ResponseEntity với HTTP status code tương ứng (lấy từ ErrorCode)
//        return ResponseEntity
//                .status(errorCode.getStatusCode())
//                .body(apiRespone);
//    }



    // Đánh dấu method này để xử lý riêng cho lỗi AccessDeniedException (user đã đăng nhập nhưng không đủ quyền)
//    @ExceptionHandler(value = AccessDeniedException.class)
//    ResponseEntity<ApiResponse> handlingAccessDeniedException(AccessDeniedException exception) {
//        log.info("Chay");
//        // Lấy mã lỗi định nghĩa sẵn trong enum ErrorCode
//        // Ở đây dùng UNAUTHORIZED (ví dụ: 403 Forbidden)
//        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
//
//        // Trả về ResponseEntity với HTTP status lấy từ errorCode
//        // và body theo format ApiRespone (code + message)
//        return ResponseEntity.status(errorCode.getStatusCode())
//                .body(ApiResponse.builder()
//                        .code(errorCode.getCode())         // Mã lỗi định nghĩa trong hệ thống
//                        .message(errorCode.getMessage())   // Thông điệp lỗi mô tả
//                        .build()
//                );
//    }
//    // Hàm dùng để thay thế placeholder trong message bằng giá trị thực tế từ attributes
//    private String mapAttribute(String message, Map<String,Object> attributes) {
//
//        // Lấy giá trị thuộc tính có key = MIN_ATTRIBUTES trong map attributes
//        // (ví dụ: MIN_ATTRIBUTES = "min", thì lấy ra giá trị min được truyền từ annotation)
//        String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTES));
//
//        // Thay thế placeholder {min} trong message bằng giá trị thực tế
//        // Ví dụ: message = "Age must be at least {min}"
//        //        minValue = "18"
//        // Kết quả: "Age must be at least 18"
//        return message.replace("{" + MIN_ATTRIBUTES + "}", minValue);
//    }

}






