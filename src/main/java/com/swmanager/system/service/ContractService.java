package com.swmanager.system.service;

import com.swmanager.system.domain.Infra;
import com.swmanager.system.domain.User;
import com.swmanager.system.domain.workplan.Contract;
import com.swmanager.system.domain.workplan.ContractParticipant;
import com.swmanager.system.domain.workplan.ContractTarget;
import com.swmanager.system.domain.workplan.InspectCycle;
import com.swmanager.system.dto.ContractDTO;
import com.swmanager.system.repository.InfraRepository;
import com.swmanager.system.repository.UserRepository;
import com.swmanager.system.repository.workplan.ContractRepository;
import com.swmanager.system.repository.workplan.ContractParticipantRepository;
import com.swmanager.system.repository.workplan.ContractTargetRepository;
import com.swmanager.system.repository.workplan.InspectCycleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ContractService {

    @Autowired private ContractRepository contractRepository;
    @Autowired private ContractParticipantRepository participantRepository;
    @Autowired private ContractTargetRepository targetRepository;
    @Autowired private InspectCycleRepository inspectCycleRepository;
    @Autowired private InfraRepository infraRepository;
    @Autowired private UserRepository userRepository;

    /**
     * 계약 목록 조회 (검색)
     */
    @Transactional(readOnly = true)
    public Page<Contract> searchContracts(Integer year, String status, String keyword, Pageable pageable) {
        if (keyword != null && keyword.trim().isEmpty()) keyword = null;
        return contractRepository.searchContracts(year, status, keyword, pageable);
    }

    /**
     * 연도 목록 조회 (필터용)
     */
    @Transactional(readOnly = true)
    public List<Integer> getDistinctYears() {
        return contractRepository.findDistinctYears();
    }

    /**
     * 전체 계약 목록 (대시보드용)
     */
    @Transactional(readOnly = true)
    public List<Contract> getAllContracts() {
        return contractRepository.findAll();
    }

    /**
     * 연도별 계약 목록 (대시보드용)
     */
    @Transactional(readOnly = true)
    public List<Contract> getContractsByYear(Integer year) {
        return contractRepository.findByContractYearOrderByCreatedAtDesc(year);
    }

    /**
     * 계약 상세 조회
     */
    @Transactional(readOnly = true)
    public Contract getContractById(Integer id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("계약 정보가 없습니다. ID: " + id));
    }

    /**
     * 계약 저장 (등록/수정)
     */
    public Contract saveContract(ContractDTO dto) {
        Contract contract;

        if (dto.getContractId() != null) {
            contract = contractRepository.findById(dto.getContractId())
                    .orElseThrow(() -> new IllegalArgumentException("계약 정보가 없습니다."));
        } else {
            contract = new Contract();
        }

        // 인프라 연동
        if (dto.getInfraId() != null) {
            Infra infra = infraRepository.findById(dto.getInfraId()).orElse(null);
            contract.setInfra(infra);
        }

        // 기본 정보 매핑
        contract.setContractName(dto.getContractName());
        contract.setContractNo(dto.getContractNo());
        contract.setContractType(dto.getContractType());
        contract.setContractMethod(dto.getContractMethod());
        contract.setContractLaw(dto.getContractLaw());
        contract.setContractClause(dto.getContractClause());
        contract.setContractAmount(dto.getContractAmount());
        contract.setGuaranteeAmount(dto.getGuaranteeAmount());
        contract.setGuaranteeRate(dto.getGuaranteeRate());
        contract.setContractDate(parseDate(dto.getContractDate()));
        contract.setStartDate(parseDate(dto.getStartDate()));
        contract.setEndDate(parseDate(dto.getEndDate()));
        contract.setActualEndDate(parseDate(dto.getActualEndDate()));
        contract.setProgressStatus(dto.getProgressStatus());
        contract.setContractYear(dto.getContractYear());

        // 발주처 정보
        contract.setClientOrg(dto.getClientOrg());
        contract.setClientAddr(dto.getClientAddr());
        contract.setClientPhone(dto.getClientPhone());
        contract.setClientFax(dto.getClientFax());
        contract.setClientDept(dto.getClientDept());
        contract.setClientContact(dto.getClientContact());
        contract.setClientContactPhone(dto.getClientContactPhone());

        // 하도급 정보
        contract.setPrimeContractor(dto.getPrimeContractor());
        contract.setSubcontractAmount(dto.getSubcontractAmount());
        contract.setSubcontractType(dto.getSubcontractType());
        contract.setPurchaseOrderNo(dto.getPurchaseOrderNo());

        return contractRepository.save(contract);
    }

    /**
     * 계약 삭제
     */
    public void deleteContract(Integer id) {
        contractRepository.deleteById(id);
    }

    // === 과업참여자 관리 (B-03) ===

    /**
     * 과업참여자 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ContractParticipant> getParticipants(Integer contractId) {
        return participantRepository.findByContract_ContractIdOrderBySortOrder(contractId);
    }

    /**
     * 과업참여자 저장 (전체 교체 방식)
     */
    public void saveParticipants(Integer contractId, List<ContractDTO.ParticipantDTO> dtoList) {
        Contract contract = getContractById(contractId);

        // 기존 참여자 삭제
        contract.getParticipants().clear();
        contractRepository.flush();

        // 새 참여자 추가
        if (dtoList != null) {
            for (int i = 0; i < dtoList.size(); i++) {
                ContractDTO.ParticipantDTO dto = dtoList.get(i);
                ContractParticipant p = new ContractParticipant();
                p.setContract(contract);

                if (dto.getUserId() != null) {
                    User user = userRepository.findById(dto.getUserId()).orElse(null);
                    p.setUser(user);
                }

                p.setRoleType(dto.getRoleType());
                p.setTechGrade(dto.getTechGrade());
                p.setTaskDesc(dto.getTaskDesc());
                p.setIsSiteRep(dto.getIsSiteRep() != null ? dto.getIsSiteRep() : false);
                p.setSortOrder(i);
                contract.getParticipants().add(p);
            }
        }

        contractRepository.save(contract);
    }

    // === 유지보수 대상 관리 (B-04) ===

    /**
     * 유지보수 대상 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ContractTarget> getTargets(Integer contractId) {
        return targetRepository.findByContract_ContractIdOrderBySortOrder(contractId);
    }

    /**
     * 유지보수 대상 저장 (전체 교체 방식)
     */
    public void saveTargets(Integer contractId, List<ContractDTO.TargetDTO> dtoList) {
        Contract contract = getContractById(contractId);

        // 기존 대상 삭제
        contract.getTargets().clear();
        contractRepository.flush();

        // 새 대상 추가
        if (dtoList != null) {
            for (int i = 0; i < dtoList.size(); i++) {
                ContractDTO.TargetDTO dto = dtoList.get(i);
                ContractTarget t = new ContractTarget();
                t.setContract(contract);
                t.setTargetCategory(dto.getTargetCategory());
                t.setProductName(dto.getProductName());
                t.setProductDetail(dto.getProductDetail());
                t.setQuantity(dto.getQuantity() != null ? dto.getQuantity() : 1);
                t.setSortOrder(i);
                contract.getTargets().add(t);
            }
        }

        contractRepository.save(contract);
    }

    // === 점검주기 관리 ===

    @Transactional(readOnly = true)
    public List<InspectCycle> getAllInspectCycles() {
        return inspectCycleRepository.findByIsActiveTrueOrderByInfra_CityNmAscInfra_DistNmAsc();
    }

    @Transactional(readOnly = true)
    public List<InspectCycle> getInspectCyclesByInfra(Long infraId) {
        return inspectCycleRepository.findByInfra_InfraId(infraId);
    }

    public InspectCycle saveInspectCycle(InspectCycle cycle) {
        return inspectCycleRepository.save(cycle);
    }

    public void deleteInspectCycle(Integer cycleId) {
        inspectCycleRepository.deleteById(cycleId);
    }

    @Transactional(readOnly = true)
    public InspectCycle getInspectCycleById(Integer id) {
        return inspectCycleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("점검주기 정보가 없습니다. ID: " + id));
    }

    // === Utility ===

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
