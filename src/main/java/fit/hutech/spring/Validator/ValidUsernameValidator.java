package fit.hutech.spring.Validator;

import org.springframework.stereotype.Component;

import fit.hutech.spring.Validator.annotations.ValidUsername;
import fit.hutech.spring.services.UserService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ValidUsernameValidator implements ConstraintValidator<ValidUsername, String> {
    private final UserService userService;

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        // Nếu userService tìm thấy user (không rỗng) => username đã tồn tại => trả về
        // false (không hợp lệ)
        // Nếu userService không tìm thấy (rỗng) => trả về true (hợp lệ)
        if (username == null)
            return true; // Bỏ qua validate nếu null (để @NotNull lo)
        return userService.findByUsername(username).isEmpty();
    }
}