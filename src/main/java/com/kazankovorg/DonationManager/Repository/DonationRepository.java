package com.kazankovorg.DonationManager.Repository;

import com.kazankovorg.DonationManager.Models.Donation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    @Query("SELECT COUNT(dn) FROM donations dn WHERE dn.donater.id = :donaterId")
    Long countDonationsByDonaterId(@Param("donaterId") Long donaterId);
    @Query("SELECT dn.currency, SUM(dn.amount) FROM donations dn WHERE dn.donater.id = :donaterId GROUP BY dn.currency")
    List<Object[]> findTotalAmountByCurrencyAndDonaterId(@Param("donaterId") Long donaterId);
    Page<Donation> findByDonaterId(Long donaterId, Pageable pageable);
}
