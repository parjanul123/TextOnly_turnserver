package com.textonly.backend.repository;

import com.textonly.backend.model.StoreItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StoreItemRepository extends JpaRepository<StoreItem, Long> {
    List<StoreItem> findByType(StoreItem.ItemType type);
    List<StoreItem> findByIsAvailableTrue();
}
