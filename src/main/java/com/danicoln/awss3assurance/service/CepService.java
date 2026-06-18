package com.danicoln.awss3assurance.service;

import com.danicoln.awss3assurance.exception.ResourceNotFoundException;
import com.danicoln.awss3assurance.model.CepRequestEntity;
import com.danicoln.awss3assurance.model.CepRequestStatus;
import com.danicoln.awss3assurance.repository.CepRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class CepService {

    private static final Logger log = LoggerFactory.getLogger(CepService.class);

    private final CepRequestRepository cepRequestRepository;
    private final SqsService sqsService;

    public CepService(CepRequestRepository cepRequestRepository, SqsService sqsService) {
        this.cepRequestRepository = cepRequestRepository;
        this.sqsService = sqsService;
    }

    @Transactional
    public CepRequestResponse create(String cep) {
        String normalizedCep = normalizeCep(cep);
        String procedureId = UUID.randomUUID().toString();
        Instant requestedAt = Instant.now();

        CepRequestEntity entity = cepRequestRepository.save(new CepRequestEntity(
                procedureId,
                normalizedCep,
                CepRequestStatus.RECEIVED,
                requestedAt
        ));

        SqsService.MessageResult messageResult = sqsService.sendMessage(new CepMessage(
                procedureId,
                normalizedCep,
                requestedAt
        ));

        log.info("CEP request created: procedureId={}, cep={}, messageId={}",
                procedureId,
                normalizedCep,
                messageResult.messageId());

        return toCreateResponse(entity, messageResult.messageId());
    }

    @Transactional(readOnly = true)
    public CepRequestResponse getById(String procedureId) {
        if (procedureId == null || procedureId.isBlank()) {
            throw new IllegalArgumentException("Procedure id is required");
        }
        return toResponse(findById(procedureId));
    }

    @Transactional(readOnly = true)
    public List<CepRequestResponse> list() {
        return cepRequestRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(CepRequestEntity::getRequestedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    private CepRequestEntity findById(String procedureId) {
        return cepRequestRepository.findById(procedureId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CEP request with procedure id '" + procedureId + "' was not found"
                ));
    }

    private String normalizeCep(String cep) {
        if (cep == null || cep.isBlank()) {
            throw new IllegalArgumentException("CEP is required");
        }

        String normalized = cep.replaceAll("\\D", "");
        if (normalized.length() != 8) {
            throw new IllegalArgumentException("CEP must contain exactly 8 digits");
        }
        return normalized;
    }

    private CepRequestResponse toCreateResponse(CepRequestEntity entity, String messageId) {
        return new CepRequestResponse(
                entity.getProcedureId(),
                entity.getCep(),
                entity.getStatus(),
                entity.getRequestedAt(),
                entity.getProcessedAt(),
                entity.getLogradouro(),
                entity.getBairro(),
                entity.getCidade(),
                entity.getUf(),
                entity.getErrorMessage(),
                messageId
        );
    }

    private CepRequestResponse toResponse(CepRequestEntity entity) {
        return new CepRequestResponse(
                entity.getProcedureId(),
                entity.getCep(),
                entity.getStatus(),
                entity.getRequestedAt(),
                entity.getProcessedAt(),
                entity.getLogradouro(),
                entity.getBairro(),
                entity.getCidade(),
                entity.getUf(),
                entity.getErrorMessage(),
                null
        );
    }

    public record CepMessage(String procedureId, String cep, Instant requestedAt) {
    }

    public record CepRequestResponse(
            String procedureId,
            String cep,
            CepRequestStatus status,
            Instant requestedAt,
            Instant processedAt,
            String logradouro,
            String bairro,
            String cidade,
            String uf,
            String errorMessage,
            String messageId
    ) {
    }
}
