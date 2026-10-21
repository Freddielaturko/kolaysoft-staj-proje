package com.kolaysoft.projedurum.repository;

import com.kolaysoft.projedurum.entity.WeeklyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, Long>, JpaSpecificationExecutor<WeeklyReport> {

    List<WeeklyReport> findByProjectIdOrderByRaporHaftasiDesc(Long projectId);

    Optional<WeeklyReport> findByProjectIdAndRaporHaftasi(Long projectId, LocalDate raporHaftasi);

    Optional<WeeklyReport> findFirstByProjectIdOrderByRaporHaftasiDesc(Long projectId);

    List<WeeklyReport> findByProjectIdInOrderByRaporHaftasiDesc(Collection<Long> projectIds);

    // CTO dashboard icin: her projenin en son raporu
    @Query(
        "SELECT wr FROM WeeklyReport wr WHERE wr.raporHaftasi = " +
        "(SELECT MAX(wr2.raporHaftasi) FROM WeeklyReport wr2 WHERE wr2.project = wr.project)"
    )
    List<WeeklyReport> findLatestReportPerProject();
}
