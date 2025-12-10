package com.coopcredit.domain.port.in;

import com.coopcredit.domain.model.CreditApplication;

import java.util.List;
import java.util.Optional;

public interface ProcessCreditApplicationUseCase {
    /**
     * Registra una nueva solicitud de crédito con estado PENDING
     * Valida: afiliado activo, antigüedad mínima, capacidad de pago
     */
    CreditApplication registerApplication(CreditApplication application, String affiliateDocument);

    /**
     * Evalúa una solicitud pendiente llamando al servicio de riesgo
     * Cambia el estado a APPROVED o REJECTED según el resultado
     */
    CreditApplication evaluateApplication(Long applicationId);

    Optional<CreditApplication> getApplication(Long id);

    List<CreditApplication> getAllApplications();

    /**
     * Obtiene solicitudes filtradas por estado (PENDING, APPROVED, REJECTED)
     */
    List<CreditApplication> getApplicationsByStatus(CreditApplication.CreditStatus status);

    /**
     * Obtiene solicitudes de un afiliado específico por su documento
     */
    List<CreditApplication> getApplicationsByAffiliateDocument(String document);
}

