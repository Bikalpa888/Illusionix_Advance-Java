package com.virinchi.demo.repository;

import com.virinchi.demo.model.signupModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface signupRepo  extends JpaRepository<signupModel, Integer> {

    boolean	existsByUsernameAndPassword(String username,String password);
    signupModel findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    @Query(value = "SELECT id, username, email, CASE WHEN created_at = '0000-00-00 00:00:00' OR created_at IS NULL THEN NULL ELSE DATE_FORMAT(created_at, '%Y-%m-%d %H:%i:%s') END AS createdAtStr FROM users", nativeQuery = true)
    List<UserSummaryView> findSummaries();

}
