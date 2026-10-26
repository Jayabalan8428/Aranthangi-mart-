package com.aranthangimart.product;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /** Empty category means "all categories"; empty search matches everything. */
    @Query("""
            select p from Product p
            where (:category = '' or lower(p.category) = lower(:category))
              and (lower(p.name) like lower(concat('%', :search, '%'))
                   or lower(p.description) like lower(concat('%', :search, '%')))
            """)
    List<Product> filter(@Param("search") String search,
                         @Param("category") String category,
                         Sort sort);

    @Query("select distinct p.category from Product p order by p.category")
    List<String> findCategories();

    /** Row lock so two customers cannot buy the last item at the same time. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
