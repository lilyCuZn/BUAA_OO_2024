import com.oocourse.library2.LibraryBookId;

import java.util.HashMap;

public class BorrowReturnOffice {
    private HashMap<LibraryBookId, Integer> books;

    public BorrowReturnOffice() {
        books = new HashMap<>();
    }

    public HashMap<LibraryBookId, Integer> getBooks() {
        return books;
    }

    public void addBook(LibraryBookId bookId) {
        if (books.containsKey(bookId)) {
            int sum = books.get(bookId);
            books.put(bookId, sum + 1);
        } else {
            books.put(bookId, 1);
        }
    }

    public void delBook(LibraryBookId bookId) {
        int sum = books.get(bookId);
        if (sum == 1) {
            books.remove(bookId);
        } else if (sum > 1) {
            books.put(bookId, sum - 1);
        }
    }

    public boolean haveBook(LibraryBookId bookId) {
        return books.containsKey(bookId);
    }
}
