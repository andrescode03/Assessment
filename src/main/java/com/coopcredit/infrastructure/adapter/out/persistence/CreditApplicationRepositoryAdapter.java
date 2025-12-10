package com.coopcredit.infrastructure.adapter.out.persistence;

import com.coopcredit.domain.model.CreditApplication;
import com.coopcredit.domain.model.RiskEvaluation;
import com.coopcredit.domain.port.out.CreditApplicationRepositoryPort;
import com.coopcredit.infrastructure.adapter.out.persistence.entity.CreditApplicationEntity;
import com.coopcredit.infrastructure.adapter.out.persistence.repository.SpringDataCreditApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CreditApplicationRepositoryAdapter implements CreditApplicationRepositoryPort {

        private final SpringDataCreditApplicationRepository repository;

        @Override
        public CreditApplication save(CreditApplication application) {
                CreditApplicationEntity entity = toEntity(application);
                CreditApplicationEntity saved = repository.save(entity);
                return toDomain(saved);
        }

        @Override
        public Optional<CreditApplication> findById(Long id) {
                return repository.findById(id).map(this::toDomain);
        }

        @Override
        public java.util.List<CreditApplication> findAll() {
                return repository.findAll().stream()
                                .map(this::toDomain)
                                .collect(java.util.stream.Collectors.toList());
        }

        @Override
        public java.util.List<CreditApplication> findByStatus(CreditApplication.CreditStatus status) {
                return repository.findByStatus(status.name()).stream()
                                .map(this::toDomain)
                                .collect(java.util.stream.Collectors.toList());
        }

        @Override
        public java.util.List<CreditApplication> findByAffiliateDocument(String document) {
                return repository.findByAffiliateDocument(document).stream()
                                .map(this::toDomain)
                                .collect(java.util.stream.Collectors.toList());
        }

        private CreditApplicationEntity toEntity(CreditApplication domain) {
                com.coopcredit.infrastructure.adapter.out.persistence.entity.AffiliateEntity aff = com.coopcredit.infrastructure.adapter.out.persistence.entity.AffiliateEntity
                                .builder()
                                .id(domain.getAffiliate().getId()) // Assuming ID is present for linking
                                .build();

                return CreditApplicationEntity.builder()
                                .id(domain.getId())
                                .affiliate(aff)
                                .requestedAmount(domain.getRequestedAmount())
                                .termMonths(domain.getTermMonths())
                                .proposedRate(domain.getProposedRate())
                                .applicationDate(domain.getApplicationDate())
                                .status(domain.getStatus().name())
                                .riskScore(domain.getRiskEvaluation() != null ? domain.getRiskEvaluation().getScore()
                                                : null)
                                .riskLevel(domain.getRiskEvaluation() != null
                                                ? domain.getRiskEvaluation().getRiskLevel()
                                                : null)
                                .decisionReason(
                                                domain.getRiskEvaluation() != null
                                                                ? domain.getRiskEvaluation().getDecisionReason()
                                                                : null)
                                .evaluationDate(
                                                domain.getRiskEvaluation() != null
                                                                ? domain.getRiskEvaluation().getEvaluationDate()
                                                                : null)
                                .build();
        }

        private CreditApplication toDomain(CreditApplicationEntity entity) {
                RiskEvaluation risk = null;
                if (entity.getRiskScore() != null) {
                        risk = RiskEvaluation.builder()
                                        .score(entity.getRiskScore())
                                        .riskLevel(entity.getRiskLevel())
                                        .decisionReason(entity.getDecisionReason())
                                        .evaluationDate(entity.getEvaluationDate())
                                        .build();
                }

                // Note: Affiliate mapping here is partial/weak.
                // In a real app we might want to fetch the full affiliate or map it fully.
                // For now we map basic fields required or load it if eager.
                // Lazy loading might be an issue if we access it outside transaction.
                // We will assume "view_in_open" or transactional service.
                com.coopcredit.domain.model.Affiliate affiliate = com.coopcredit.domain.model.Affiliate.builder()
                                .id(entity.getAffiliate().getId())
                                .document(entity.getAffiliate().getDocument())
                                .name(entity.getAffiliate().getName())
                                // .salary(...) not mapped if lazy? Assuming EAGER or transactional session.
                                .build();

                return CreditApplication.builder()
                                .id(entity.getId())
                                .affiliate(affiliate)
                                .requestedAmount(entity.getRequestedAmount())
                                .termMonths(entity.getTermMonths())
                                .proposedRate(entity.getProposedRate())
                                .applicationDate(entity.getApplicationDate())
                                .status(CreditApplication.CreditStatus.valueOf(entity.getStatus()))
                                .riskEvaluation(risk)
                                .build();
        }
}
