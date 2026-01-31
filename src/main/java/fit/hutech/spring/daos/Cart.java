package fit.hutech.spring.daos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.Data;

@Data
public class Cart {
    private List<Item> cartItems = new ArrayList<>();

    public void addItems(Item item) {
        boolean exist = false;
        for (Item i : cartItems) {
            if (Objects.equals(i.getBookId(), item.getBookId())) {
                i.setQuantity(i.getQuantity() + item.getQuantity());
                exist = true;
                break;
            }
        }
        if (!exist) {
            cartItems.add(item);
        }
    }

    public void removeItems(Long bookId) {
        cartItems.removeIf(item -> Objects.equals(item.getBookId(), bookId));
    }

    public void updateItems(Long bookId, int quantity) {
        for (Item item : cartItems) {
            if (Objects.equals(item.getBookId(), bookId)) {
                item.setQuantity(quantity);
                break;
            }
        }
    }
}