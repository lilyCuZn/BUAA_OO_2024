import com.oocourse.library2.LibrarySystem;
import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryMoveInfo;
import com.oocourse.library2.LibraryPrinter;
import com.oocourse.library2.LibraryCommand;
import com.oocourse.library2.annotation.Trigger;

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
    private HashMap<LibraryBookId, Integer> cornerBooks = new HashMap<>();
    private HashMap<LibraryBookId, Integer> corBorTime = new HashMap<>();
    private HashMap<String, User> users = new HashMap<>();
    private ArrayList<BookToAppoint> booksToAppoint = new ArrayList<>();
    private ArrayList<BorrowInfo> borrowInfos = new ArrayList<>();

    public Library(Map<LibraryBookId, Integer> books) {
        this.books = (HashMap<LibraryBookId, Integer>) books;
        this.shelfBooks = (HashMap<LibraryBookId, Integer>) books;
    }

    public void putUser(String studentId) {
        if (!users.containsKey(studentId)) {
            users.put(studentId, new User(studentId));
        }
    }

    @Trigger(from = "TEMPSTORED", to = "STORED")
    public void manage(LocalDate date, boolean isClose) {
        ArrayList<LibraryMoveInfo> moveInfos = new ArrayList<>();
        //开闭馆后整理图书
        //预约处逾期书籍回到书架上,并且移动新的书到预约处
        manageAppointmentOffice(date, isClose, moveInfos);
        //借还处的书回到书架上
        manageBorrowReturnOffice(moveInfos);
        manageCornerBooks(date, moveInfos);
        printer.move(date, moveInfos);
    }

    public void tryQuery(LocalDate date, String studentId,
                         LibraryBookId bookId, LibraryCommand command) {
        if (bookId.isFormal()) {
            printer.info(date, bookId, shelfBooks.get(bookId));
        } else {
            printer.info(date, bookId, cornerBooks.get(bookId));
        }
    }

    @Trigger(from = "STORED", to = "USER")
    public void tryBorrow(LocalDate date, String studentId,
                          LibraryBookId bookId, LibraryCommand command) {
        if (bookId.isFormal()) {
            if (!shelfBooks.containsKey(bookId) || shelfBooks.get(bookId) == 0) {
                printer.reject(command);
            } else if (bookId.isTypeA()) {
                printer.reject(command);
            } else {
                if (!users.get(studentId).canBorrow(bookId)) {
                    borrowReturnOffice.addBook(bookId);
                    int sum = shelfBooks.get(bookId);
                    shelfBooks.put(bookId, sum - 1);
                    printer.reject(command);
                } else {
                    users.get(studentId).borrow(bookId);
                    borrowInfos.add(new BorrowInfo(studentId, bookId, date));
                    int sum = shelfBooks.get(bookId);
                    shelfBooks.put(bookId, sum - 1);
                    printer.accept(command);
                }
            }
        } else {
            if (bookId.isTypeAU() || !cornerBooks.containsKey(bookId)
                    || cornerBooks.get(bookId) == 0) {
                printer.reject(command);
            } else {
                if (!users.get(studentId).canBorrow(bookId)) {
                    borrowReturnOffice.addBook(bookId);
                    int sum = cornerBooks.get(bookId);
                    cornerBooks.put(bookId, sum - 1);
                    printer.reject(command);
                } else {
                    users.get(studentId).borrow(bookId);
                    borrowInfos.add(new BorrowInfo(studentId, bookId, date));
                    int sum = cornerBooks.get(bookId);
                    cornerBooks.put(bookId, sum - 1);
                    printer.accept(command);
                }
            }
        }

    }

    public void tryOrder(LocalDate date, String studentId,
                         LibraryBookId bookId, LibraryCommand command) {
        if (bookId.isTypeAU() || bookId.isTypeBU() || bookId.isTypeCU()) {
            printer.reject(command);
        } else if (bookId.isTypeA()) {
            printer.reject(command);
        } else if (!users.get(studentId).canOrder(bookId)) {
            printer.reject(command);
        } else {
            BookToAppoint book = new BookToAppoint(bookId, users.get(studentId), date);
            booksToAppoint.add(book); //等待被放到预约处
            printer.accept(command); //预约成功
        }
    }

    @Trigger(from = "USER", to = "TEMPSTORED")
    public void tryReturn(LocalDate date, String studentId,
                          LibraryBookId bookId, LibraryCommand command) {
        borrowReturnOffice.addBook(bookId);
        users.get(studentId).delBook(bookId);
        int flag = 0;
        BorrowInfo borrowInfo = null;
        for (BorrowInfo b : borrowInfos) {
            if (b.getBookId().equals(bookId) && b.getStudentId().equals(studentId)) {
                if (b.getBorrowDate().isBefore(date) || b.getBorrowDate().equals(date)) {
                    borrowInfo = b;
                    if (b.getReturnDate().isAfter(date) || b.getReturnDate().equals(date)) {
                        flag = 1; //在期限内
                    }
                    break;
                }
            }
        }
        borrowInfos.remove(borrowInfo); //有可能空指针
        String o = (flag == 0) ? "overdue" : "not overdue";
        if (bookId.isTypeAU() || bookId.isTypeBU() || bookId.isTypeCU()) {
            if (!corBorTime.containsKey(bookId)) {
                corBorTime.put(bookId, 1);
            } else {
                int time = corBorTime.get(bookId);
                corBorTime.put(bookId, time + 1);
            }
        }
        printer.accept(command, o);
    }

    @Trigger(from = "TEMPSTORED", to = "USER")
    public void tryPick(LocalDate date, String studentId,
                        LibraryBookId bookId, LibraryCommand command) {
        if (appointmentOffice.haveBookFor(bookId, users.get(studentId))
            && users.get(studentId).canBorrow(bookId)) {
            appointmentOffice.delBook(bookId, studentId);
            //取走标号为bookId的为studentId保留的最早的一本书
            users.get(studentId).borrow(bookId);
            borrowInfos.add(new BorrowInfo(studentId, bookId, date));
            printer.accept(command);
        } else {
            printer.reject(command);
        }
    }

    public void tryRenew(LocalDate date, String studentId,
                         LibraryBookId bookId, LibraryCommand command) {
        if (!bookId.isFormal()) {
            printer.reject(command);
        } else if (users.get(studentId).haveBook(bookId)) {
            if (inRenewTime(studentId, bookId, date) == null) {
                printer.reject(command);
            } else if (isBeingAppointed(bookId) && shelfBooks.get(bookId) == 0) {
                printer.reject(command);
            } else {
                printer.accept(command);
                BorrowInfo borrowInfo = inRenewTime(studentId, bookId, date);
                borrowInfo.renewReturnDate();
            }
        } else {
            printer.reject(command);
        }
    }

    public void tryDonate(LocalDate date, String studentId,
                          LibraryBookId bookId, LibraryCommand command) {
        printer.accept(command);
        if (!cornerBooks.containsKey(bookId)) {
            cornerBooks.put(bookId, 1);
        } else {
            int sum = cornerBooks.get(bookId);
            cornerBooks.put(bookId, sum + 1);
        }
    }

    public BorrowInfo inRenewTime(String studentId, LibraryBookId bookId, LocalDate date) {
        for (BorrowInfo borrowInfo : borrowInfos) {
            String s = borrowInfo.getStudentId();
            LibraryBookId b = borrowInfo.getBookId();
            LocalDate d = borrowInfo.getReturnDate();
            if (s.equals(studentId) && b.equals(bookId)) {
                if (Math.abs(ChronoUnit.DAYS.between(d, date)) < 5 &&
                        (date.isBefore(d) || date.equals(d))) {
                    return borrowInfo;
                }
            }
        }
        return null;
    }

    public boolean isBeingAppointed(LibraryBookId bookId) {
        //正在生效的预约
        for (BookToAppoint book : booksToAppoint) {
            if (book.getBookId().equals(bookId)) {
                return true;
            }
        }
        if (appointmentOffice.haveBook(bookId)) {
            return true;
        }
        return false;
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
            if (bookId.isFormal()) {
                int sum1 = shelfBooks.get(bookId);
                shelfBooks.put(bookId, sum + sum1);
                for (int i = 0; i < sum; i++) {
                    moveInfos.add(new LibraryMoveInfo(bookId, "bro", "bs"));
                }
            } else { //是漂流的书
                if (corBorTime.containsKey(bookId) && corBorTime.get(bookId) == 2) {
                    corBorTime.remove(bookId);
                    LibraryBookId bookId1 = bookId.toFormal();
                    int sum1 = (shelfBooks.containsKey(bookId1)) ? shelfBooks.get(bookId1) : 0;
                    shelfBooks.put(bookId1, sum + sum1);
                    for (int i = 0; i < sum; i++) {
                        moveInfos.add(new LibraryMoveInfo(bookId, "bro", "bs"));
                    }
                } else {
                    int sum1 = cornerBooks.get(bookId);
                    cornerBooks.put(bookId, sum + sum1);
                    for (int i = 0; i < sum; i++) {
                        moveInfos.add(new LibraryMoveInfo(bookId, "bro", "bdc"));
                    }
                }
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

    public void manageCornerBooks(LocalDate date, ArrayList<LibraryMoveInfo> moveInfos) {
        //整理图书漂流角
        for (LibraryBookId bookId : cornerBooks.keySet()) {
            if (corBorTime.containsKey(bookId) && corBorTime.get(bookId) == 2) {
                LibraryBookId bookId1 = bookId.toFormal();
                int sum = cornerBooks.get(bookId);
                int sum1 = (shelfBooks.containsKey(bookId1)) ? shelfBooks.get(bookId1) : 0;
                shelfBooks.put(bookId1, sum + sum1);
                cornerBooks.put(bookId, 0);
                corBorTime.remove(bookId);
                for (int i = 0; i < sum; i++) {
                    moveInfos.add(new LibraryMoveInfo(bookId, "bdc", "bs"));
                }
            }
        }
    }
}
