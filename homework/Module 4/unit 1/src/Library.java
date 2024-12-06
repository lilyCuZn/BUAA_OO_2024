import com.oocourse.library1.LibrarySystem;
import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryMoveInfo;
import com.oocourse.library1.LibraryRequest;
import com.oocourse.library1.LibraryPrinter;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Library {
    private LibraryPrinter printer = LibrarySystem.PRINTER;
    private AppointmentOffice appointmentOffice = new AppointmentOffice();
    private BorrowReturnOffice borrowReturnOffice = new BorrowReturnOffice();
    private HashMap<LibraryBookId, Integer> books;
    private HashMap<LibraryBookId, Integer> shelfBooks;
    private HashMap<String, User> users = new HashMap<>();
    private ArrayList<BookToAppoint> booksToAppoint = new ArrayList<>();

    public Library(Map<LibraryBookId, Integer> books) {
        this.books = (HashMap<LibraryBookId, Integer>) books;
        this.shelfBooks = (HashMap<LibraryBookId, Integer>) books;
    }

    public void manage(LocalDate date, boolean isClose) {
        ArrayList<LibraryMoveInfo> moveInfos = new ArrayList<>();
        //开闭馆后整理图书
        //预约处逾期书籍回到书架上,并且移动新的书到预约处
        manageAppointmentOffice(date, isClose, moveInfos);

        //借还处的书回到书架上
        manageBorrowReturnOffice(moveInfos);

        printer.move(date, moveInfos);
    }

    public void dealWithRequest(LocalDate date, LibraryRequest request) {
        LibraryRequest.Type type = request.getType();
        String studendId = request.getStudentId();
        LibraryBookId bookId = request.getBookId();
        if (!users.containsKey(studendId)) {
            users.put(studendId, new User(studendId));
        }
        //QUERIED, BORROWED, ORDERED, RETURNED, PICKED;
        if (type.equals(LibraryRequest.Type.QUERIED)) {
            if (!shelfBooks.containsKey(bookId)) {
                printer.info(date, bookId, 0);
            } else {
                printer.info(date, bookId, shelfBooks.get(bookId));
            }
        } else if (type.equals(LibraryRequest.Type.BORROWED)) {
            tryBorrow(date, studendId, bookId, request);
        } else if (type.equals(LibraryRequest.Type.ORDERED)) { //预约
            tryOrder(date, studendId, bookId, request);
        } else if (type.equals(LibraryRequest.Type.RETURNED)) {
            tryReturn(date, studendId, bookId, request);
        } else if (type.equals(LibraryRequest.Type.PICKED)) {
            //去预约处取书
            tryPick(date, studendId, bookId, request);
        }
    }

    public void tryBorrow(LocalDate date, String studentId,
                          LibraryBookId bookId, LibraryRequest request) {
        if (!shelfBooks.containsKey(bookId) || shelfBooks.get(bookId) == 0) {
            printer.reject(date, request);
        } else if (bookId.isTypeA()) {
            printer.reject(date, request);
        } else {
            if (!users.get(studentId).canBorrow(bookId)) {
                borrowReturnOffice.addBook(bookId);
                int sum = shelfBooks.get(bookId);
                shelfBooks.put(bookId, sum - 1);
                printer.reject(date, request);
            } else {
                users.get(studentId).borrow(bookId);
                int sum = shelfBooks.get(bookId);
                shelfBooks.put(bookId, sum - 1);
                printer.accept(date, request);
            }
        }
    }

    public void tryOrder(LocalDate date, String studentId,
                         LibraryBookId bookId, LibraryRequest request) {
        if (bookId.isTypeA()) {
            printer.reject(date, request);
        } else if (!users.get(studentId).canOrder(bookId)) {
            printer.reject(date, request);
        } else {
            BookToAppoint book = new BookToAppoint(bookId, users.get(studentId), date);
            booksToAppoint.add(book); //等待被放到预约处
            printer.accept(date, request); //预约成功
        }
    }

    public void tryReturn(LocalDate date, String studentId,
                          LibraryBookId bookId, LibraryRequest request) {
        borrowReturnOffice.addBook(bookId);
        users.get(studentId).delBook(bookId);
        printer.accept(date, request);
    }

    public void tryPick(LocalDate date, String studentId,
                        LibraryBookId bookId, LibraryRequest request) {
        if (appointmentOffice.haveBookFor(bookId, users.get(studentId))
            && users.get(studentId).canBorrow(bookId)) {
            appointmentOffice.delBook(bookId, studentId);
            //取走标号为bookId的为studentId保留的最早的一本书
            users.get(studentId).borrow(bookId);
            printer.accept(date, request);
        } else {
            printer.reject(date, request);
        }
    }

    public void manageBorrowReturnOffice(ArrayList<LibraryMoveInfo> moveInfos) {
        //把借还书的书上书架
        HashMap<LibraryBookId, Integer> broBooks = borrowReturnOffice.getBooks();
        Iterator<Map.Entry<LibraryBookId, Integer>> iterator =
                broBooks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<LibraryBookId, Integer> entry = iterator.next();
            LibraryBookId bookId = entry.getKey();
            int sum = entry.getValue();
            int sum1 = shelfBooks.get(bookId);
            shelfBooks.put(bookId, sum + sum1);
            for (int i = 0; i < sum; i++) {
                moveInfos.add(new LibraryMoveInfo(bookId, "bro", "bs"));
            }
            iterator.remove();
        }
    }

    public void manageAppointmentOffice(LocalDate date, boolean isClose,
                                        ArrayList<LibraryMoveInfo> moveInfos) {
        //把逾期的书放上书架,要判断是否是在关门的时候整理的
        ArrayList<BookToAppoint> booksAtAppoint = appointmentOffice.getBooks();
        Iterator<BookToAppoint> it = booksAtAppoint.iterator();
        while (it.hasNext()) {
            BookToAppoint book = it.next();
            long dayDiff = ChronoUnit.DAYS.between(book.getDate(), date);
            if (dayDiff >= 5) {
                shelfBooks.merge(book.getBookId(), 1, Integer::sum);
                moveInfos.add(new LibraryMoveInfo(book.getBookId(), "ao", "bs"));
                it.remove();
            }
        }
        //把被预约的书放过来
        Iterator<BookToAppoint> it2 = booksToAppoint.iterator();
        while (it2.hasNext()) {
            BookToAppoint book = it2.next();
            if (isClose) {
                book.setDate(date.plusDays(1));
            } else {
                book.setDate(date);
            }
            if (shelfBooks.get(book.getBookId()) > 0) {
                int sum = shelfBooks.get(book.getBookId());
                shelfBooks.put(book.getBookId(), sum - 1);
                appointmentOffice.addBook(book);
                moveInfos.add(new LibraryMoveInfo(book.getBookId(), "bs", "ao", book.getUserId()));
                it2.remove();
            } else if (borrowReturnOffice.haveBook(book.getBookId())) {
                borrowReturnOffice.delBook(book.getBookId());
                appointmentOffice.addBook(book);
                moveInfos.add(new LibraryMoveInfo(book.getBookId(), "bro", "ao", book.getUserId()));
                it2.remove();
            }
            //否则，啥也做不了
        }
    }
}
