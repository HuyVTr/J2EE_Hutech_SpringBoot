package fit.hutech.spring.services;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fit.hutech.spring.entities.User;
import fit.hutech.spring.repositories.IRoleRepository;
import fit.hutech.spring.repositories.IUserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public void setDefaultRole(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.getRoles().add(roleRepository.findByName("USER"));
            userRepository.save(user);
        });
    }

    public void saveOauthUser(String email, String username) {
        if (userRepository.findByUsername(username).isPresent() ||
                userRepository.findByEmail(email).isPresent())
            return;
        var user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("OAUTH2_PROVIDER"); // Mật khẩu giả cho OAuth
        user.setProvider("GOOGLE");
        userRepository.save(user);
        setDefaultRole(username);
    }
}