package com.example.airdns.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum GlobalExceptionCode {

    INVALID_VALUE(HttpStatus.BAD_REQUEST, "COMMON-001", "값이 유효하지 않습니다."),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "COMMON-002", "파라미터가 누락되었습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-003", "내부 서버 에러 입니다."),

    // AWS S3
    AWS_S3_FILE_UPLOAD_FAIL(HttpStatus.BAD_REQUEST, "S3-001", "파일 업로드에 실패하셨습니다."),
    AWS_S3_FILE_NAME_IS_BLANK(HttpStatus.BAD_REQUEST, "S3-002", "파일 이름은 공백일 수 없습니다."),

    // Validate JWT Token
    INVALID_TOKEN_VALUE(HttpStatus.BAD_REQUEST, "JWT-001", "JWT 토큰 값이 유효하지 않습니다."),
    UNAUTHORIZED_REFRESH_TOKEN_VALUE(HttpStatus.UNAUTHORIZED, "JWT-002", "JWT Refresh 토큰값이 유효하지 않습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String message;
}
