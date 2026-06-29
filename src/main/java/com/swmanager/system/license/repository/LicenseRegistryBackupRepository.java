package com.swmanager.system.license.repository;

import com.swmanager.system.license.domain.LicenseRegistryBackup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LicenseRegistryBackupRepository extends JpaRepository<LicenseRegistryBackup, Long> {

    long countBySnapshotId(String snapshotId);
}
