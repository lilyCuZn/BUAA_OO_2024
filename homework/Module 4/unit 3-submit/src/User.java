import com.oocourse.library3.LibraryBookId;

import java.util.HashMap;

public class User {
    private String id;
    private HashMap<LibraryBookId, Integer> books = new HashMap<>();
    private int point;

    public User(String id) {
        this.id = id;
        this.point = 10;
    }

    public String getId() {
        return id;
    }

    public int getPoint() {
        return point;
    }

    public boolean haveBook(LibraryBookId bookId) {
        return books.containsKey(bookId);
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
        } else if (haveBUtype() && bookId.isTypeBU()) {
            return false;
        } else if (bookId.isTypeCU() && books.containsKey(bookId) &&
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

    public boolean haveBtype() {
        //判断有没有B类书
        for (LibraryBookId bookId : books.keySet()) {
            if (bookId.isTypeB()) {
                return true;
            }
        }
        return false;
    }

    public boolean haveBUtype() {
        for (LibraryBookId bookId : books.keySet()) {
            if (bookId.isTypeBU()) {
                return true;
            }
        }
        return false;
    }

    public void addPoint(int add) {
        this.point += add;
        if (this.point > 20) {
            this.point = 20;
        }
    }
}
