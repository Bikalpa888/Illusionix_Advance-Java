package com.virinchi.demo.repository;

import com.virinchi.demo.model.signupModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface signupRepo  extends JpaRepository<signupModel, Integer> {

    boolean	existsByUsernameAndPassword(String username,String password);
    signupModel findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

}
