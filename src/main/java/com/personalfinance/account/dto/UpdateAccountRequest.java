package com.personalfinance.account.dto;

import com.personalfinance.account.entity.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountRequest {

    @NotBlank(message = "Tên tài khoản không được để trống")
    private String name;

    @NotNull(message = "Loại tài khoản không được để trống")
    private AccountType accountType;

    private String bankCode;

    private String accountNumber;

    @NotNull(message = "Số dư ban đầu không được để trống")
    private Long openingBalance;

    private Boolean isActive;
}
