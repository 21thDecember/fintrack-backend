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
public class CreateAccountRequest {

    @NotBlank(message = "Tên tài khoản không được để trống")
    private String name;

    @NotNull(message = "Loại tài khoản không được để trống")
    private AccountType accountType;

    private String bankCode;

    private String accountNumber;

    @Builder.Default
    private Long openingBalance = 0L;
}
