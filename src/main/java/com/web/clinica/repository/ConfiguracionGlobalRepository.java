package com.web.clinica.repository;

import com.web.clinica.model.ConfiguracionGlobal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfiguracionGlobalRepository extends JpaRepository<ConfiguracionGlobal, String> {
}
