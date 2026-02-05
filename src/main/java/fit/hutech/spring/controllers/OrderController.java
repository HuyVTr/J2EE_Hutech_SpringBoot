package fit.hutech.spring.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import fit.hutech.spring.entities.User;
import fit.hutech.spring.services.OrderService;
import fit.hutech.spring.services.UserService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final UserService userService;

    @GetMapping("/history")
    public String orderHistory(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;

        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
                String email = oauthToken.getPrincipal().getAttribute("email");
                currentUser = userService.findByEmail(email).orElse(null);
            } else {
                currentUser = userService.findByUsername(authentication.getName()).orElse(null);
            }
        }

        if (currentUser != null) {
            model.addAttribute("orders", orderService.getOrdersByUserId(currentUser.getId()));
        } else {
            return "redirect:/login";
        }

        return "order/history";
    }
}
