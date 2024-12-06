import com.oocourse.library3.LibraryBookId;

import java.time.LocalDate;

public class BookToAppoint {
    private LibraryBookId bookId;
    private User user;
    private LocalDate date;

    public BookToAppoint(LibraryBookId bookId, User user, LocalDate date) {
        this.bookId = bookId;
        this.user = user;
        this.date = date;
    }

    public String getUserId() {
        return user.getId();
    }

    public LibraryBookId getBookId() {
        return bookId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
