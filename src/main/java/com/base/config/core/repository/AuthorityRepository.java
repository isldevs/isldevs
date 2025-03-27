package com.base.config.core.repository;

import com.base.config.core.model.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author YISivlay
 */
@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Long> {

    List<Authority> findByUsername(String username);

    void deleteByUsername(String username);

}
