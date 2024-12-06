import com.oocourse.library3.*;
import com.oocourse.library3.annotation.Trigger;

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
    private HashMap<LibraryBookId, String> donaters = new HashMap<>();

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

    public void tryQcs(LibraryCommand command) {
        String studentId = ((LibraryQcsCmd) command).getStudentId();
        if (!users.containsKey(studentId)) {
            printer.info(command, 10);
        } else {
            printer.info(command, users.get(studentId).getPoint());
        }
    }

    public void tryQuery(LocalDate date, String studentId,
                         LibraryBookId bookId, LibraryCommand command) {
        if (bookId.isFormal()) {
            printer.info(command, shelfBooks.get(bookId));
        } else {
            printer.info(command, cornerBooks.get(bookId));
        }
    }

    @Trigger(from = "STORED", to = "USER")
    public void tryBorrow(LocalDate date, String studentId,
                          LibraryBookId bookId, LibraryCommand command) {
        if (bookId.isFormal()) {
            if (users.get(studentId).getPoint() < 0) {
                //扣留在借还处
                borrowReturnOffice.addBook(bookId);
                int sum = shelfBooks.get(bookId);
                shelfBooks.put(bookId, sum - 1);
                printer.reject(command);
            } else if (!shelfBooks.containsKey(bookId) || shelfBooks.get(bookId) == 0) {
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
        if (users.get(studentId).getPoint() < 0) {
            printer.reject(command);
            //System.out.println("1");
        } else if (bookId.isTypeAU() || bookId.isTypeBU() || bookId.isTypeCU()) {
            printer.reject(command);
            //System.out.println("2");
        } else if (bookId.isTypeA()) {
            printer.reject(command);
            //System.out.println("3");
        } else if (!users.get(studentId).canOrder(bookId)) {
            printer.reject(command);
            //System.out.println("4");
        } else if (bookId.isTypeB() && hasAppointedB(studentId)) {
            printer.reject(command);
            //System.out.println("5");
        } else if (bookId.isTypeC() && hasAppointed(studentId, bookId)) {
            printer.reject(command);
            //System.out.println("6");
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
        if (flag == 1) {
            users.get(studentId).addPoint(1);
            //System.out.println(studentId + "add point 1");
        }
        if (!bookId.isFormal()) {
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
        if (users.get(studentId).getPoint() < 0) {
            printer.reject(command);
        } else if (!bookId.isFormal()) {
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
        users.get(studentId).addPoint(2);
        //System.out.println(studentId + "addPoint 2");
        //同书号的书最多被捐献一次
        donaters.put(bookId, studentId);
        cornerBooks.put(bookId, 1);
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

    public boolean isBeingAppointedB() {
        //有正在生效的对B类书的预约
        for (BookToAppoint book : booksToAppoint) {
            if (book.getBookId().isTypeB()) {
                return true;
            }
        }
        if (appointmentOffice.haveBbook()) {
            return true;
        }
        return false;
    }

    public boolean hasAppointed(String studentId, LibraryBookId bookId) {
        for (BookToAppoint book : booksToAppoint) {
            if (book.getBookId().equals(bookId) && book.getUserId().equals(studentId)) {
                return true;
            }
        }
        if (appointmentOffice.haveBookFor(bookId, users.get(studentId))) {
            return true;
        }
        return false;
    }

    public boolean hasAppointedB(String studentId) {
        for (BookToAppoint book : booksToAppoint) {
            if (book.getBookId().isTypeB() && book.getUserId().equals(studentId)) {
                return true;
            }
        }
        if (appointmentOffice.haveBBookFor(studentId)) {
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
                    users.get(donaters.get(bookId)).addPoint(2);
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
                users.get(book.getUserId()).addPoint(-3);
                //System.out.println(book.getUserId() + "addPoint -3");
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
                users.get(donaters.get(bookId)).addPoint(2);
                //System.out.println(donaters.get(bookId) + "addPoint 2");
                corBorTime.remove(bookId);
                for (int i = 0; i < sum; i++) {
                    moveInfos.add(new LibraryMoveInfo(bookId, "bdc", "bs"));
                }
            }
        }
    }

    public void manageUserPoint(LocalDate date) {
        //(开）闭馆后，判断有哪个用户没有归还当日应还的书，给ta扣分！
        for (BorrowInfo b : borrowInfos) {
            if (date.isAfter(b.getReturnDate()) && !b.delPoint()) {
                //逾期且还没处理
                users.get(b.getStudentId()).addPoint(-2);
                //System.out.println("b.getReturnDate:" + b.getReturnDate());
                //System.out.println("date:" + date);
                //System.out.println(b.getStudentId() + "add Point -2");
                b.setDelPoint(true);
            }
        }
    }

    public void orderNewBook(LocalDate date, String studentId,
                             LibraryBookId bookId, LibraryCommand command) {
        tryOrder(date, studentId, bookId, command);
    }
}
