package fit.hutech.spring.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fit.hutech.spring.daos.Cart;
import fit.hutech.spring.daos.Item;
import fit.hutech.spring.entities.Category;
import fit.hutech.spring.services.BookService;
import fit.hutech.spring.services.CartService;
import fit.hutech.spring.services.CategoryService;
import fit.hutech.spring.viewmodels.BookGetVm;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ApiController {
    private final BookService bookService;
    private final CategoryService categoryService;
    private final CartService cartService;

    @GetMapping("/books")
    public ResponseEntity<List<BookGetVm>> getAllBooks(Integer pageNo, Integer pageSize, String sortBy) {
        return ResponseEntity.ok(bookService.getAllBooks(
                pageNo == null ? 0 : pageNo,
                pageSize == null ? 20 : pageSize,
                sortBy == null ? "id" : sortBy)
                .stream()
                .map(BookGetVm::from)
                .toList());
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<BookGetVm> getBookById(@PathVariable Long id) {
        return bookService.getBookById(id)
                .map(BookGetVm::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> deleteBookById(@PathVariable Long id) {
        bookService.deleteBookById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/books/search")
    public ResponseEntity<List<BookGetVm>> searchBooks(String keyword) {
        return ResponseEntity.ok(bookService.searchBook(keyword)
                .stream()
                .map(BookGetVm::from)
                .toList());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    // Cart APIs
    @GetMapping("/cart")
    public ResponseEntity<Cart> getCart(HttpSession session) {
        return ResponseEntity.ok(cartService.getCart(session));
    }

    @PostMapping("/cart/add/{bookId}")
    public ResponseEntity<Cart> addToCart(@PathVariable Long bookId, @RequestParam(defaultValue = "1") int quantity,
            HttpSession session) {
        var cart = cartService.getCart(session);
        var book = bookService.getBookById(bookId).orElseThrow(() -> new IllegalArgumentException("Book not found"));
        cart.addItems(new Item(book.getId(), book.getTitle(), book.getPrice(), quantity, book.getImagePath()));
        cartService.updateCart(session, cart);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/cart/update/{bookId}")
    public ResponseEntity<Cart> updateCart(@PathVariable Long bookId, @RequestParam int quantity, HttpSession session) {
        var cart = cartService.getCart(session);
        cart.updateItems(bookId, quantity);
        cartService.updateCart(session, cart);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/cart/remove/{bookId}")
    public ResponseEntity<Cart> removeFromCart(@PathVariable Long bookId, HttpSession session) {
        var cart = cartService.getCart(session);
        cart.removeItems(bookId);
        cartService.updateCart(session, cart);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/cart/clear")
    public ResponseEntity<Void> clearCart(HttpSession session) {
        cartService.removeCart(session);
        return ResponseEntity.ok().build();
    }
}