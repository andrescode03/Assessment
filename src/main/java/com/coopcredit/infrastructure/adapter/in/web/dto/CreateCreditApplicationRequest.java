package com.coopcredit.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateCreditApplicationRequest {

    // Optional: If user is ROLE_AFILIADO, taken from authenticated user
    // Required: If user is ADMIN/ANALISTA, must specify it
    private String affiliateDocument;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    @DecimalMin(value = "100000", message = "Minimum amount is 100,000")
    private BigDecimal amount;

    @NotNull(message = "Term is required")
    @Min(value = 6, message = "Minimum term is 6 months")
    @Max(value = 72, message = "Maximum term is 72 months")
    private Integer term;
}
