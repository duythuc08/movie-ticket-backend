package com.duythuc_dh52201541.moive_ticket_infinity_cinema.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(999,"erorr", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1000,"Message invalid key",HttpStatus.BAD_REQUEST),
    USER_EXISTED(1001,"User existed",HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1002,"Email existed",HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1003,"User Not Found",HttpStatus.BAD_REQUEST),
    INVALID_OTP(1004,"Message invalid otp",HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(1005,"OTP is expired",HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1006,"User must be at least {min} characters",HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1007,"Password must be at least {min} characters",HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1008,"User not existed",HttpStatus.NOT_FOUND),
    AUTHENTICATED(1009,"Authenticated",HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1010,"You do not have permission",HttpStatus.FORBIDDEN),
    INVALID_DOB(1011,"Your age must be at least {min}",HttpStatus.BAD_REQUEST),
    OTP_NOT_FOUND(1012,"OTP Not Found",HttpStatus.BAD_REQUEST),
    OTP_RESEND_TOO_SOON(1012,"Please wait for 30 second before resend OTP",HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1009,"Unauthenticated",HttpStatus.UNAUTHORIZED),
    OTP_NOT_VERIFIED(1013,"OTP is not verified",HttpStatus.BAD_REQUEST),
    GENRE_NOT_FOUND(1014,"Genre not found",HttpStatus.NOT_FOUND),;
    // Constructor để gán giá trị cho từng phần tử enum
    ErrorCode(int code, String message,HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
    private int code;
    private String message;
    private HttpStatusCode statusCode;

}
