package fit.hutech.spring.services;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import fit.hutech.spring.entities.Book;
import fit.hutech.spring.repositories.IBookRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = { Exception.class, Throwable.class })
public class BookService {
    private final IBookRepository bookRepository;

    public List<Book> getAllBooks(Integer pageNo, Integer pageSize, String sortBy) {
        return bookRepository.findAllBooks(pageNo, pageSize, sortBy);
    }

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    public void addBook(Book book) {
        bookRepository.save(book);
    }

    public void updateBook(@NotNull Book book) {
        Book existingBook = bookRepository.findById(book.getId())
                .orElse(null);

        Objects.requireNonNull(existingBook).setTitle(book.getTitle());

        existingBook.setAuthor(book.getAuthor());
        existingBook.setPrice(book.getPrice());
        existingBook.setCategory(book.getCategory());
        existingBook.setImagePath(book.getImagePath());

        bookRepository.save(existingBook);
    }

    public void deleteBookById(Long id) {
        bookRepository.deleteById(id);
    }

    public long countAllBooks() {
        return bookRepository.count();
    }

    // === BỔ SUNG: Phương thức tìm kiếm theo ảnh image_e6d09d.png ===
    public List<Book> searchBook(String keyword) {
        // Lọc lại kết quả bằng Java để phân biệt dấu tiếng Việt chính xác
        // (Vì MySQL mặc định collation utf8_general_ci thường bỏ qua dấu: a == á)
        String finalKeyword = keyword.toLowerCase();
        return bookRepository.searchBook(keyword).stream()
                .filter(book -> (book.getTitle() != null && book.getTitle().toLowerCase().contains(finalKeyword)) ||
                        (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(finalKeyword)) ||
                        (book.getCategory() != null && book.getCategory().getName() != null
                                && book.getCategory().getName().toLowerCase().contains(finalKeyword)))
                .toList();
    }
    // ===============================================================

    // === Cập nhật số lượng sách (Nhập/Xuất kho) ===
    public void updateBookQuantity(Long bookId, Integer changeAmount) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + bookId));

        int newQuantity = (book.getQuantity() != null ? book.getQuantity() : 0) + changeAmount;

        if (newQuantity < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không đủ để xuất! Tồn tại: " + book.getQuantity());
        }

        book.setQuantity(newQuantity);
        bookRepository.save(book);
    }
}