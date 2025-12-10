package com.coopcredit.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateAffiliateRequest {

    @NotBlank(message = "Documento es requerido")
    @Pattern(regexp = "\\d{5,12}", message = "Documento debe tener entre 5 y 12 dígitos")
    private String document;

    @NotBlank(message = "Nombre es requerido")
    @Size(min = 3, max = 100, message = "Nombre debe tener entre 3 y 100 caracteres")
    private String name;

    @NotNull(message = "Salario es requerido")
    @Positive(message = "Salario debe ser mayor a 0")
    private BigDecimal salary;

    @NotNull(message = "Fecha de afiliación es requerida")
    @PastOrPresent(message = "Fecha de afiliación no puede ser futura")
    private LocalDate affiliationDate;
}
