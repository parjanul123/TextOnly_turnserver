package com.textonly.backend.repository;

import com.textonly.backend.model.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServerRepository extends JpaRepository<Server, Long> {
    List<Server> findByOwnerId(Long ownerId);
    List<Server> findByMembers_Id(Long userId);
}
