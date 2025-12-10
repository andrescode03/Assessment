package com.riskmock.controller;

import com.riskmock.dto.RiskEvaluationRequest;
import com.riskmock.dto.RiskEvaluationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/risk-evaluation")
public class RiskEvaluationController {

    @PostMapping
    public ResponseEntity<RiskEvaluationResponse> evaluate(@RequestBody RiskEvaluationRequest request) {
        // Validaciones
        if (request.getDocumento() == null || request.getDocumento().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (request.getMonto() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (request.getPlazo() == null) {
            return ResponseEntity.badRequest().build();
        }

        double monto = request.getMonto().doubleValue();
        int plazo = request.getPlazo();
        String documento = request.getDocumento();

        // Deterministic score based on document hash (300-950 range)
        int docHash = Math.abs(documento.hashCode() % 1000);
        int baseScore = 300 + (docHash * 650 / 1000); // Maps 0-999 to 300-950

        // Factores de riesgo según monto y plazo
        // Montos altos (> 30M) aumentan riesgo (reducen score)
        double montoFactor = monto > 30_000_000 ? -50 : (monto > 10_000_000 ? -20 : 0);

        // Plazos largos (> 60 meses) aumentan riesgo (reducen score)
        double plazoFactor = plazo > 60 ? -30 : (plazo > 36 ? -10 : 0);

        // Score final (mantener en rango 300-950)
        int finalScore = Math.max(300, Math.min(950, (int) (baseScore + montoFactor + plazoFactor)));

        // Clasificación según rangos especificados
        String nivelRiesgo;
        String detalle;

        if (finalScore <= 500) {
            nivelRiesgo = "ALTO";
            detalle = String.format(
                    "Historial crediticio deficiente. Score: %d. Monto/plazo representan alto riesgo.",
                    finalScore);
        } else if (finalScore <= 700) {
            nivelRiesgo = "MEDIO";
            detalle = String.format("Historial crediticio moderado. Score: %d.", finalScore);
        } else {
            nivelRiesgo = "BAJO";
            detalle = String.format("Historial crediticio excelente. Score: %d. Apto para aprobación.",
                    finalScore);
        }

        RiskEvaluationResponse response = RiskEvaluationResponse.builder()
                .documento(documento)
                .score(finalScore)
                .nivelRiesgo(nivelRiesgo)
                .detalle(detalle)
                .build();

        return ResponseEntity.ok(response);
    }
}
