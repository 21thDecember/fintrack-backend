package com.personalfinance.account.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountStatusRequest {

    @JsonAlias({"active", "status"})
    private Boolean isActive;
}
