package com.swmanager.system.repository;
import com.swmanager.system.domain.MaintTpMst;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintTpMstRepository extends JpaRepository<MaintTpMst, String> {}