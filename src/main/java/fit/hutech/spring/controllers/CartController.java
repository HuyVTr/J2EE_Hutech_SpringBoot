package fit.hutech.spring.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import fit.hutech.spring.daos.Item;
import fit.hutech.spring.services.BookService;
import fit.hutech.spring.services.CartService;
import fit.hutech.spring.services.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final BookService bookService;

    @GetMapping
    public String showCart(HttpSession session,
            @NotNull Model model) {
        model.addAttribute("cart", cartService.getCart(session));
        model.addAttribute("totalPrice",
                cartService.getSumPrice(session));
        model.addAttribute("totalQuantity",
                cartService.getSumQuantity(session));
        return "book/cart";
    }

    @GetMapping("/add/{id}")
    public String addToCart(HttpSession session,
            @PathVariable Long id) {
        var book = bookService.getBookById(id);
        if (book.isPresent()) {
            var cart = cartService.getCart(session);
            cart.addItems(new Item(book.get().getId(), book.get().getTitle(), book.get().getPrice(), 1,
                    book.get().getImagePath()));
            cartService.updateCart(session, cart);
        }
        return "redirect:/books";
    }

    @GetMapping("/removeFromCart/{id}")
    public String removeFromCart(HttpSession session,
            @PathVariable Long id) {
        var cart = cartService.getCart(session);
        cart.removeItems(id);
        return "redirect:/cart";
    }

    @GetMapping("/updateCart/{id}/{quantity}")
    public String updateCart(HttpSession session,
            @PathVariable Long id,
            @PathVariable int quantity) {
        var cart = cartService.getCart(session);
        cart.updateItems(id, quantity);
        return "book/cart";
    }

    @GetMapping("/clearCart")
    public String clearCart(HttpSession session) {
        cartService.removeCart(session);
        return "redirect:/cart";
    }

    // === BỔ SUNG: Phương thức Checkout từ ảnh image_e6b97e.png ===
    // === PHƯƠNG THỨC CHECKOUT & SUBMIT ORDER ===
    private final UserService userService;

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model,
            org.springframework.security.core.Authentication authentication) {
        var cart = cartService.getCart(session);
        if (cart.getCartItems().isEmpty()) {
            return "redirect:/cart";
        }
        model.addAttribute("cart", cart);
        model.addAttribute("totalPrice", cartService.getSumPrice(session));

        // Lấy thông tin user hiện tại
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            var user = userService.findByUsername(username).orElse(null);
            if (user != null) {
                model.addAttribute("user", user); // Truyền user xuống view
            }
        }
        return "book/checkout";
    }

    @org.springframework.web.bind.annotation.PostMapping("/submit")
    public String submitOrder(HttpSession session,
            @org.springframework.web.bind.annotation.RequestParam String receiverName,
            @org.springframework.web.bind.annotation.RequestParam String phoneNumber,
            @org.springframework.web.bind.annotation.RequestParam String address,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String note,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "COD") String paymentMethod) {
        cartService.saveOrder(session, receiverName, phoneNumber, address, note, paymentMethod);
        return "redirect:/cart/success";
    }

    @GetMapping("/success")
    public String orderSuccess() {
        return "book/order_success";
    }
    // ===========================================
    // =============================================================
}