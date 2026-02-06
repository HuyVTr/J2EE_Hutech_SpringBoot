package fit.hutech.spring.dtos;

import fit.hutech.spring.entities.Book;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookSalesDTO {
    private Book book;
    private Long totalSold;

}
