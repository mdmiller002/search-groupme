package com.search.rdbms.hibernate.repositories;

import com.search.rdbms.hibernate.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
