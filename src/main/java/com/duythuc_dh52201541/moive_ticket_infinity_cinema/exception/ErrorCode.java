package com.duythuc_dh52201541.moive_ticket_infinity_cinema.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(999,"erorr", HttpStatus.INTERNAL_SERVER_ERROR);

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
