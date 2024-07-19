package com.ecommerce.SneakerStore.repositories;

import com.ecommerce.SneakerStore.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role,Long> {
}
