package com.danicoln.awss3assurance.repository;

import com.danicoln.awss3assurance.model.CepRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CepRequestRepository extends JpaRepository<CepRequestEntity, String> {
}
