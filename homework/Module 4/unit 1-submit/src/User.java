import com.oocourse.library1.LibraryBookId;

import java.util.HashMap;

public class User {
    private String id;
    private HashMap<LibraryBookId, Integer> books = new HashMap<>();

    public User(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void delBook(LibraryBookId bookId) {
        if (books.get(bookId) == 1) {
            books.remove(bookId);
        } else if (books.get(bookId) > 1) {
            int sum = books.get(bookId);
            books.put(bookId, sum - 1);
        }
    }

    public boolean canBorrow(LibraryBookId bookId) {
        if (haveBtype() && bookId.isTypeB()) {
            return false;
        } else if (bookId.isTypeC() && books.containsKey(bookId) &&
                books.get(bookId) >= 1) {
            return false;
        }
        return true;
    }

    public boolean canOrder(LibraryBookId bookId) {
        if (haveBtype() && bookId.isTypeB()) {
            return false;
        } else if (bookId.isTypeC() && books.containsKey(bookId) &&
                books.get(bookId) >= 1) {
            return false;
        }
        return true;
    }

    public void borrow(LibraryBookId bookId) {
        //直接借走
        if (books.containsKey(bookId)) {
            int sum = books.get(bookId);
            books.put(bookId, sum);
        } else {
            books.put(bookId, 1);
        }
    }

    private boolean haveBtype() {
        //判断有没有B类书
        for (LibraryBookId bookId : books.keySet()) {
            if (bookId.isTypeB()) {
                return true;
            }
        }
        return false;
    }
}
