package com.mawjood.repository;

import com.mawjood.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;


public interface LocationRepository extends JpaRepository<Location, Long> {
}
