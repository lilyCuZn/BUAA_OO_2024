import com.oocourse.library3.LibraryBookId;

import java.time.LocalDate;

public class BorrowInfo {
    //用来表示谁在哪天借了什么书，这本书的应还日期是什么时候
    private String studentId;
    private LibraryBookId bookId;
    private LocalDate borrowDate;
    private LocalDate returnDate;
    private boolean delPoint; //是否对借书人扣分

    public BorrowInfo(String studentId, LibraryBookId bookId, LocalDate borrowDate) {
        this.studentId = studentId;
        this.bookId = bookId;
        this.borrowDate = borrowDate;
        if (bookId.isTypeB()) {
            returnDate = borrowDate.plusDays(30);
        } else if (bookId.isTypeC()) {
            returnDate = borrowDate.plusDays(60);
        } else if (bookId.isTypeBU()) {
            returnDate = borrowDate.plusDays(7);
        } else if (bookId.isTypeCU()) {
            returnDate = borrowDate.plusDays(14);
        }
        this.delPoint = false;
    }

    public String getStudentId() {
        return studentId;
    }

    public LibraryBookId getBookId() {
        return bookId;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void renewReturnDate() {
        returnDate = returnDate.plusDays(30);
    }

    public boolean delPoint() {
        return delPoint;
    }

    public void setDelPoint(boolean a) {
        delPoint = a;
    }
}