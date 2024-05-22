package com.kazankovorg.DonationManager.Repository;

import com.kazankovorg.DonationManager.Models.Donater;
import com.kazankovorg.DonationManager.Models.Donation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DonaterRepository extends JpaRepository<Donater, Long> {

    List<Donater> findByUserIdAndUsername(Long userId, String name);

    @Query("SELECT d FROM donaters d LEFT JOIN d.donations ds WHERE d.user.id = :userId GROUP BY d ORDER BY SUM(ds.amount) DESC")
    List<Donater> findByUserIdOrderByTotalDonationAmount(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(d) FROM donaters d WHERE d.user.id = :userId")
    int countByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(dn.amount) FROM donaters d JOIN d.donations dn WHERE d IN :ds GROUP BY d")
    List<Double> getTotalDonationsByDonaters(@Param("ds") List<Donater> ds);

}
