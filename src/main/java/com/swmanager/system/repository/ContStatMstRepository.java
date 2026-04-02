package com.swmanager.system.repository;
import com.swmanager.system.domain.ContStatMst;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContStatMstRepository extends JpaRepository<ContStatMst, String> {}