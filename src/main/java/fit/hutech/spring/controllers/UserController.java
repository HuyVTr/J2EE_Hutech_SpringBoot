package fit.hutech.spring.controllers;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import fit.hutech.spring.entities.User;
import fit.hutech.spring.services.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login() {
        return "user/login";
    }

    @GetMapping("/register")
    public String register(@NotNull Model model) {
        model.addAttribute("user", new User());
        return "user/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user,
            @NotNull BindingResult bindingResult,
            Model model) {
        // Kiểm tra lỗi validation
        if (bindingResult.hasErrors()) {
            var errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toArray(String[]::new);
            model.addAttribute("errors", errors);
            return "user/register";
        }

        // 1. Mã hóa mật khẩu trước khi lưu vào DB
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 2. Gán provider mặc định là LOCAL
        user.setProvider("LOCAL");

        // Kiểm tra trùng lặp Email và Phone thủ công
        if (!userService.findByEmail(user.getEmail()).isEmpty()) {
            bindingResult.rejectValue("email", "error.email", "Email đã tồn tại!");
        }
        if (!userService.findByPhone(user.getPhone()).isEmpty()) {
            bindingResult.rejectValue("phone", "error.phone", "Số điện thoại đã tồn tại!");
        }

        if (bindingResult.hasErrors()) {
            var errors = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toArray(String[]::new);
            model.addAttribute("errors", errors);
            return "user/register";
        }

        // Lưu user vào cơ sở dữ liệu
        userService.save(user);
        // Tích hợp từ hình ảnh: Gán role mặc định cho user sau khi đăng ký
        userService.setDefaultRole(user.getUsername());

        return "redirect:/login";
    }
}