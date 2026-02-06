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
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);
        return "user/profile";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute("user") User updatedUser, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String currentUsername = authentication.getName();
        User currentUser = userService.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only update allowed fields
        currentUser.setFullName(updatedUser.getFullName());
        currentUser.setPhone(updatedUser.getPhone());
        currentUser.setAddress(updatedUser.getAddress());
        // Do NOT update username/password/email/roles here for safety

        userService.save(currentUser);
        return "redirect:/profile?success";
    }
}
