package fit.hutech.spring.repositories;

import fit.hutech.spring.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {
    // Tìm kiếm User bằng username
    // Lưu ý: Trong ảnh là trả về User, nhưng để an toàn và chuẩn (khớp với Validator dùng .isEmpty()), 
    // bạn nên dùng Optional<User>. Tuy nhiên, tôi để User theo đúng ảnh nếu bạn muốn y hệt.
    // Nếu validator của bạn dùng .isEmpty(), bạn nên đổi dòng dưới thành: Optional<User> findByUsername(String username);
    
    User findByUsername(String username);
}