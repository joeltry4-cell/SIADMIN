package com.siadmin.repository;

import com.siadmin.model.Karyawan;
import com.siadmin.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<User> findByKaryawan(Karyawan karyawan);

    @Query("select u from User u left join fetch u.karyawan order by u.username")
    List<User> findAllWithKaryawan();

    @Query("select u from User u left join fetch u.karyawan where u.username = :username")
    Optional<User> findByUsernameWithKaryawan(String username);
}
