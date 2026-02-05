package fit.hutech.spring.services;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import fit.hutech.spring.daos.Cart;
import fit.hutech.spring.daos.Item;
import fit.hutech.spring.repositories.IBookRepository;
import fit.hutech.spring.repositories.IUserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = { Exception.class, Throwable.class })
public class CartService {

    private static final String CART_SESSION_KEY = "cart";

    // Inject các Repository cần thiết để thao tác với DB
    private final IBookRepository bookRepository;
    private final IUserRepository userRepository;

    public Cart getCart(@NotNull HttpSession session) {
        return Optional.ofNullable((Cart) session.getAttribute(CART_SESSION_KEY))
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    session.setAttribute(CART_SESSION_KEY, cart);
                    return cart;
                });
    }

    public void updateCart(@NotNull HttpSession session, Cart cart) {
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public void removeCart(@NotNull HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }

    public int getSumQuantity(@NotNull HttpSession session) {
        return getCart(session).getCartItems().stream()
                .mapToInt(Item::getQuantity)
                .sum();
    }

    public double getSumPrice(@NotNull HttpSession session) {
        return getCart(session).getCartItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    // === PHƯƠNG THỨC LƯU GIỎ HÀNG (SAVE CART) ===
    //
    // === PHƯƠNG THỨC LƯU GIỎ HÀNG (SAVE ORDER) ===
    public void saveOrder(@NotNull HttpSession session, String receiverName, String phoneNumber, String address,
            String note) {
        var cart = getCart(session);
        if (cart.getCartItems().isEmpty())
            return;

        // 1. Tạo và lưu hóa đơn (Order)
        var order = new fit.hutech.spring.entities.Order();
        order.setOrderDate(java.time.LocalDateTime.now());
        order.setTotalPrice(getSumPrice(session));
        order.setShippingAddress(address);
        order.setPhoneNumber(phoneNumber);
        order.setNote(note);
        order.setStatus("PENDING");

        // Gán User hiện tại cho Order
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
                String email = oauthToken.getPrincipal().getAttribute("email");
                userRepository.findByEmail(email).ifPresent(order::setUser);
            } else {
                userRepository.findByUsername(authentication.getName()).ifPresent(order::setUser);
            }
        }

        // Lưu Order trước để có ID
        fit.hutech.spring.repositories.IOrderRepository orderRepository = context
                .getBean(fit.hutech.spring.repositories.IOrderRepository.class);
        orderRepository.save(order);

        // 2. Lưu chi tiết hóa đơn (OrderDetail)
        fit.hutech.spring.repositories.IOrderDetailRepository orderDetailRepository = context
                .getBean(fit.hutech.spring.repositories.IOrderDetailRepository.class);

        cart.getCartItems().forEach(item -> {
            var orderDetail = new fit.hutech.spring.entities.OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setQuantity(item.getQuantity());
            orderDetail.setPrice(item.getPrice());

            // Tìm sách theo ID
            orderDetail.setBook(bookRepository.findById(item.getBookId()).orElseThrow());

            orderDetailRepository.save(orderDetail);
        });

        // 3. Xóa giỏ hàng sau khi đã thanh toán thành công
        removeCart(session);
    }

    // Helper để lấy ApplicationContext (tạm thời, tốt nhất là inject Repository ở
    // trên)
    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.context.ApplicationContext context;
}