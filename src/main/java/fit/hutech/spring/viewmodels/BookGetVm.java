package fit.hutech.spring.viewmodels;

import fit.hutech.spring.entities.Book;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookGetVm {
    private Long id;
    private String title;
    private String author;
    private Double price;
    private String category;
    private String imagePath;
    private Integer quantity;

    public static BookGetVm from(Book book) {
        return BookGetVm.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .price(book.getPrice())
                .category(book.getCategory() != null ? book.getCategory().getName() : null)
                .imagePath(book.getImagePath())
                .quantity(book.getQuantity() != null ? book.getQuantity() : 0)
                .build();
    }
}