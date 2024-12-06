import com.oocourse.library1.LibraryBookId;

import java.util.ArrayList;

public class AppointmentOffice {
    //预约处
    private ArrayList<BookToAppoint> books;

    public AppointmentOffice() {
        books = new ArrayList<BookToAppoint>();
    }

    public ArrayList<BookToAppoint> getBooks() {
        return books;
    }

    public void addBook(BookToAppoint book) {
        books.add(book);
    }

    public boolean haveBookFor(LibraryBookId bookId, User user) {
        for (BookToAppoint book : books) {
            if (book.getUserId().equals(user.getId()) 
                && book.getBookId().equals(bookId)) {
                return true;
            }
        }
        return false;
    }
    
    public void delBook(LibraryBookId bookId, String studendId) {
        //取走标号为bookId的为studentId保留的最早的一本书
        BookToAppoint bookToDel = null;
        for (BookToAppoint book : books) {
            if (book.getUserId().equals(studendId)
                && book.getBookId().equals(bookId)) {
                bookToDel = book;
                break;
            }
        }
        books.remove(bookToDel);
    }
}
