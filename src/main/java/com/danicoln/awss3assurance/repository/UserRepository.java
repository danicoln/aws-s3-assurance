package com.danicoln.awss3assurance.repository;

import com.danicoln.awss3assurance.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, String> {
}
