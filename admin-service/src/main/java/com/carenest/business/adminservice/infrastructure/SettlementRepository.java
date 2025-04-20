package com.carenest.business.adminservice.infrastructure;

import com.carenest.business.adminservice.domain.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, UUID> {
    List<Settlement> findByCareWorkerId(UUID careWorkerId);
}
