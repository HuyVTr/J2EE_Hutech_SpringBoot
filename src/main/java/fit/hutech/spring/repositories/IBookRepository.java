package fit.hutech.spring.repositories;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Bổ sung import này
import org.springframework.stereotype.Repository;

import fit.hutech.spring.entities.Book;

@Repository
public interface IBookRepository extends JpaRepository<Book, Long> {

    // Hàm mặc định có sẵn của bạn
    default List<Book> findAllBooks(Integer pageNo, Integer pageSize, String sortBy) {
        return findAll(PageRequest.of(pageNo, pageSize, Sort.by(sortBy)))
                .getContent();
    }

    // === BỔ SUNG: Phương thức tìm kiếm từ ảnh image_e6cd3c.png ===
    @Query("""
            SELECT b FROM Book b
            WHERE b.title LIKE %?1%
            OR b.author LIKE %?1%
            OR b.category.name LIKE %?1%
            """)
    List<Book> searchBook(String keyword);
    // =============================================================
}