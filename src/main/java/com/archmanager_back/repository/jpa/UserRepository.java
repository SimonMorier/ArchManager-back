package com.archmanager_back.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.archmanager_back.model.entity.jpa.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  @Query("""
        select distinct u
        from User u
        left join fetch u.permissions p
        left join fetch p.project
        where u.username = :username
      """)
  Optional<User> findByUsernameWithPermissions(@Param("username") String username);

  @Query("""
      SELECT DISTINCT u
        FROM User u
        JOIN u.permissions perm
        JOIN perm.project p
       WHERE p.name = :projectName
      """)
  List<User> findByProjectName(@Param("projectName") String projectName);

  Optional<User> findByUsername(String username);

}
