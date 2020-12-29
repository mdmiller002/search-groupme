package com.search.rdbms.hibernate.repositories;

import com.search.rdbms.hibernate.models.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
}
