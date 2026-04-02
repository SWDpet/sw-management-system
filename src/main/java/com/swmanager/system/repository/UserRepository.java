package com.swmanager.system.repository;

import com.swmanager.system.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * userid로 사용자 찾기
     */
    Optional<User> findByUserid(String userid);
    
    /**
     * 승인 대기 중인 사용자 목록 (enabled = false)
     */
    List<User> findByEnabledFalse();
    
    /**
     * 활성 사용자 목록 (enabled = true)
     */
    List<User> findByEnabledTrue();
    
    /**
     * 활성 사용자 목록 (enabled = true, 페이징)
     */
    Page<User> findByEnabledTrue(Pageable pageable);
    
    // ========== 검색 기능 ==========
    
    /**
     * 아이디로 검색 (활성 사용자만, LIKE 검색)
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true AND LOWER(u.userid) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchByUserid(@Param("keyword") String keyword);
    
    /**
     * 아이디로 검색 (활성 사용자만, LIKE 검색, 페이징)
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true AND LOWER(u.userid) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchByUserid(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 성명으로 검색 (활성 사용자만, LIKE 검색)
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true AND LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchByUsername(@Param("keyword") String keyword);
    
    /**
     * 성명으로 검색 (활성 사용자만, LIKE 검색, 페이징)
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true AND LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchByUsername(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 소속기관으로 검색 (활성 사용자만, LIKE 검색)
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true AND LOWER(u.orgNm) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchByOrgNm(@Param("keyword") String keyword);
    
    /**
     * 소속기관으로 검색 (활성 사용자만, LIKE 검색, 페이징)
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true AND LOWER(u.orgNm) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchByOrgNm(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 부서로 검색 (활성 사용자만, LIKE 검색)
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true AND LOWER(u.deptNm) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchByDeptNm(@Param("keyword") String keyword);
    
    /**
     * 부서로 검색 (활성 사용자만, LIKE 검색, 페이징)
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true AND LOWER(u.deptNm) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchByDeptNm(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 팀으로 검색 (활성 사용자만, LIKE 검색)
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true AND LOWER(u.teamNm) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchByTeamNm(@Param("keyword") String keyword);
    
    /**
     * 팀으로 검색 (활성 사용자만, LIKE 검색, 페이징)
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true AND LOWER(u.teamNm) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchByTeamNm(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 연락처로 검색 (활성 사용자만, LIKE 검색)
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true AND LOWER(u.tel) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchByTel(@Param("keyword") String keyword);
    
    /**
     * 연락처로 검색 (활성 사용자만, LIKE 검색, 페이징)
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true AND LOWER(u.tel) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchByTel(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 이메일로 검색 (활성 사용자만, LIKE 검색)
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true AND LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchByEmail(@Param("keyword") String keyword);
    
    /**
     * 이메일로 검색 (활성 사용자만, LIKE 검색, 페이징)
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true AND LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchByEmail(@Param("keyword") String keyword, Pageable pageable);
}
