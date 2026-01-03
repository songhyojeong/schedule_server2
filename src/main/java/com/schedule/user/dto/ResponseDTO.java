package com.schedule.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "set")
public class ResponseDTO<D> {
    private boolean result;
    private String message;
    private D data;

    // 성공 시 메시지만 포함하는 정적 메서드
    public static <D> ResponseDTO<D> setSuccess(String message) {
        return ResponseDTO.set(true, message, null);
    }

    // 실패 시 메시지만 포함하는 정적 메서드
    public static <D> ResponseDTO<D> setFailed(String message) {
        return ResponseDTO.set(false, message, null);
    }

    // 성공 시 메시지와 데이터를 포함하는 정적 메서드
    public static <D> ResponseDTO<D> setSuccessData(String message, D data) {
        return ResponseDTO.set(true, message, data);
    }

    // 실패 시 메시지와 데이터를 포함하는 정적 메서드
    public static <D> ResponseDTO<D> setFailedData(String message, D data) {
        return ResponseDTO.set(false, message, data);
    }


}
