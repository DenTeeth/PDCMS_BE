package com.dental.clinic.management.warehouse.repository;

import com.dental.clinic.management.warehouse.domain.Category;
import com.dental.clinic.management.warehouse.enums.WarehouseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByParentCategoryIsNull();

    List<Category> findByParentCategoryIsNullAndWarehouseType(WarehouseType warehouseType);

    List<Category> findByParentCategory_CategoryId(Long parentCategoryId);

    Optional<Category> findByCategoryNameAndWarehouseType(String categoryName, WarehouseType warehouseType);

    boolean existsByCategoryNameAndWarehouseType(String categoryName, WarehouseType warehouseType);

    List<Category> findByWarehouseType(WarehouseType warehouseType);
}
