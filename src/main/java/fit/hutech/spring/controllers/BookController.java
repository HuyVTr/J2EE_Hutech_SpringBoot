package fit.hutech.spring.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import fit.hutech.spring.entities.Book;
import fit.hutech.spring.services.BookService;
import fit.hutech.spring.services.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;
    private final CategoryService categoryService;

    @GetMapping
    public String showAllBooks(
            @NotNull Model model,
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            Authentication authentication) {

        model.addAttribute("books", bookService.getAllBooks(pageNo, pageSize, sortBy));
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("categories", categoryService.getAllCategories());
        long totalBooks = bookService.countAllBooks();
        model.addAttribute("totalPages", totalBooks > 0 ? (int) Math.ceil((double) totalBooks / pageSize) - 1 : 0);
        model.addAttribute("keyword", "");

        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("STAFF"))) {
            return "book/manage"; // Giao diện quản lý cũ
        }

        return "book/list"; // Giao diện người dùng mới
    }

    // === BỔ SUNG: PHƯƠNG THỨC SEARCH (THEO ẢNH) ===
    @GetMapping("/search")
    public String searchBook(
            @NotNull Model model,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            Authentication authentication) {

        // Tối ưu: Gọi service 1 lần và lưu vào biến thay vì gọi 2 lần
        var searchResults = bookService.searchBook(keyword);
        model.addAttribute("books", searchResults);

        model.addAttribute("currentPage", pageNo);
        int totalSearchItems = searchResults.size();
        model.addAttribute("totalPages",
                totalSearchItems > 0 ? (int) Math.ceil((double) totalSearchItems / pageSize) - 1 : 0);

        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("keyword", keyword);

        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("STAFF"))) {
            return "book/manage";
        }

        return "book/list";
    }
    // ===============================================

    @GetMapping("/add")
    public String addBookForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "book/add";
    }

    @PostMapping("/add")
    public String addBook(@Valid @ModelAttribute("book") Book book,
            BindingResult bindingResult,
            @RequestParam("image") MultipartFile imageFile,
            Model model) {
        if (bindingResult.hasErrors()) {
            var errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toArray(String[]::new);
            model.addAttribute("errors", errors);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "book/add";
        }
        if (!imageFile.isEmpty()) {
            try {
                String imageName = saveImageStatic(imageFile);
                book.setImagePath("/images/books/" + imageName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        bookService.addBook(book);
        return "redirect:/books";
    }

    @GetMapping("/edit/{id}")
    public String editBookForm(@NotNull Model model, @PathVariable long id) {
        var book = bookService.getBookById(id);
        model.addAttribute("book", book.orElseThrow(() -> new IllegalArgumentException("Book not found")));
        model.addAttribute("categories", categoryService.getAllCategories());
        return "book/edit";
    }

    @PostMapping("/edit")
    public String editBook(@Valid @ModelAttribute("book") Book book,
            BindingResult bindingResult,
            @RequestParam("image") MultipartFile imageFile,
            Model model) {
        if (bindingResult.hasErrors()) {
            var errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toArray(String[]::new);
            model.addAttribute("errors", errors);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "book/edit";
        }
        if (!imageFile.isEmpty()) {
            try {
                String imageName = saveImageStatic(imageFile);
                book.setImagePath("/images/books/" + imageName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        bookService.updateBook(book);
        return "redirect:/books";
    }

    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable long id) {
        bookService.getBookById(id)
                .ifPresentOrElse(
                        book -> bookService.deleteBookById(id),
                        () -> {
                            throw new IllegalArgumentException("Book not found");
                        });
        return "redirect:/books";
    }

    @GetMapping("/detail/{id}")
    public String getBookDetail(@PathVariable long id, Model model) {
        var book = bookService.getBookById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
        model.addAttribute("book", book);
        return "book/detail";
    }

    private String saveImageStatic(MultipartFile image) throws IOException {
        Path uploadPath = Paths.get("uploads");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String filename = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);
        Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }

    // === API CẬP NHẬT SỐ LƯỢNG (STAFF ONLY) ===
    @PostMapping("/update-quantity")
    public String updateBookQuantity(@RequestParam Long bookId, @RequestParam Integer changeAmount) {
        try {
            bookService.updateBookQuantity(bookId, changeAmount);
        } catch (IllegalArgumentException e) {
            return "redirect:/books?error=" + e.getMessage();
        }
        return "redirect:/books";
    }
}