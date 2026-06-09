package pe.com.zippers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.zippers.domain.AuditoriaLog;

public interface AuditoriaLogRepository extends JpaRepository<AuditoriaLog, Long> {
}