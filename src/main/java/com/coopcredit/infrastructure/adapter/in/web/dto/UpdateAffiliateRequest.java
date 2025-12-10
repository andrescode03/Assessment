package com.coopcredit.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateAffiliateRequest {

    @NotBlank(message = "Nombre es requerido")
    @Size(min = 3, max = 100, message = "Nombre debe tener entre 3 y 100 caracteres")
    private String name;

    @NotNull(message = "Salario es requerido")
    @Positive(message = "Salario debe ser mayor a 0")
    private BigDecimal salary;

    @NotBlank(message = "Estado es requerido")
    @Pattern(regexp = "ACTIVE|INACTIVE", message = "Estado debe ser ACTIVE o INACTIVE")
    private String status;
}
