package fit.hutech.spring.dtos;

import fit.hutech.spring.entities.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSpendingDTO {
    private User user;
    private Double totalSpent;
}
