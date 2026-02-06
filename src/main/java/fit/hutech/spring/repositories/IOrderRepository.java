package fit.hutech.spring.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fit.hutech.spring.entities.Order;

@Repository
public interface IOrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);

    @org.springframework.data.jpa.repository.Query("SELECT new fit.hutech.spring.dtos.UserSpendingDTO(o.user, SUM(o.totalPrice)) "
            +
            "FROM Order o GROUP BY o.user ORDER BY SUM(o.totalPrice) DESC")
    java.util.List<fit.hutech.spring.dtos.UserSpendingDTO> findTopSpenders(
            org.springframework.data.domain.Pageable pageable);
}
