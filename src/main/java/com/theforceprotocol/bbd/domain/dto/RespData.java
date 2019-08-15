package com.theforceprotocol.bbd.domain.dto;

import com.theforceprotocol.bbd.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespData<T> {
    private T data;
    private int status = 200;
    private String msg = "success";

    public T checkAndGetData() {
        if (status == 200) {
            return data;
        }
        throw new BusinessException(status, msg);
    }
}
