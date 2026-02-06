package fit.hutech.spring.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import fit.hutech.spring.entities.User;
import fit.hutech.spring.services.UserService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping
    public String viewProfile(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = null;
        if (authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken) {
            // Nếu là OAuth (FB, Google), tìm theo email
            String email = oauthToken.getPrincipal().getAttribute("email");
            if (email != null) {
                user = userService.findByEmail(email).orElse(null);
            }
            // Fallback nếu không có email, thử tìm theo name hoặc login
            if (user == null) {
                String name = oauthToken.getPrincipal().getAttribute("name"); // FB return name
                if (name != null)
                    user = userService.findByUsername(name).orElse(null);
            }
        } else {
            // Login thường
            user = userService.findByUsername(authentication.getName()).orElse(null);
        }

        if (user == null) {
            return "redirect:/"; // Hoặc trang lỗi
        }

        model.addAttribute("user", user);
        return "user/profile";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute("user") User updatedUser, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        User currentUser = null;
        if (authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken) {
            String email = oauthToken.getPrincipal().getAttribute("email");
            if (email != null)
                currentUser = userService.findByEmail(email).orElse(null);
            if (currentUser == null) {
                String name = oauthToken.getPrincipal().getAttribute("name");
                if (name != null)
                    currentUser = userService.findByUsername(name).orElse(null);
            }
        } else {
            currentUser = userService.findByUsername(authentication.getName()).orElse(null);
        }

        if (currentUser == null) {
            return "redirect:/login";
        }

        // Only update allowed fields
        currentUser.setFullName(updatedUser.getFullName());
        currentUser.setPhone(updatedUser.getPhone());
        currentUser.setAddress(updatedUser.getAddress());
        // Do NOT update username/password/email/roles here for safety

        userService.save(currentUser);
        return "redirect:/profile?success";
    }
}
