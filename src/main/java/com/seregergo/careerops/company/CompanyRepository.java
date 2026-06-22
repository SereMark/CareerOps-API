package com.seregergo.careerops.company;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {

	boolean existsByNormalizedName(String normalizedName);

	boolean existsByNormalizedNameAndIdNot(String normalizedName, UUID id);

	List<Company> findAllByArchivedFalseOrderByNormalizedNameAsc();

	List<Company> findAllByOrderByNormalizedNameAsc();
}
