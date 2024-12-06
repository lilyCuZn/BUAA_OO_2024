import com.oocourse.library2.LibrarySystem;
import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryReqCmd;
import com.oocourse.library2.LibraryRequest;
import com.oocourse.library2.LibraryScanner;
import com.oocourse.library2.LibraryCommand;
import com.oocourse.library2.LibraryOpenCmd;
import com.oocourse.library2.LibraryCloseCmd;

import java.time.LocalDate;

public class MainClass {
    public static void main(String[] args) {
        LibraryScanner scanner = LibrarySystem.SCANNER;
        Library library = new Library(scanner.getInventory());

        while (true) {
            LibraryCommand command = scanner.nextCommand();
            if (command == null) { break; }
            LocalDate date = command.getDate(); // 今天的日期
            if (command instanceof LibraryOpenCmd) {
                library.manage(date, false);
                // 在开馆时做点什么
            } else if (command instanceof LibraryCloseCmd) {
                library.manage(date, true);
                // 在闭馆时做点什么
            } else {
                LibraryReqCmd req = (LibraryReqCmd) command;
                LibraryRequest.Type type = req.getType(); // 指令对应的类型（查询/借阅/预约/还书/取书/续借/捐赠）
                LibraryBookId bookId = req.getBookId(); // 指令对应书籍编号（type-uid）
                String studentId = req.getStudentId(); // 指令对应的用户Id
                // 对指令进行处理
                library.putUser(studentId);
                //QUERIED, BORROWED, ORDERED, RETURNED, PICKED, RENEWED, DONATED;
                if (type.equals(LibraryRequest.Type.QUERIED)) {
                    library.tryQuery(date, studentId, bookId, command);
                } else if (type.equals(LibraryRequest.Type.BORROWED)) {
                    library.tryBorrow(date, studentId, bookId, command);
                } else if (type.equals(LibraryRequest.Type.ORDERED)) { //预约
                    library.tryOrder(date, studentId, bookId, command);
                } else if (type.equals(LibraryRequest.Type.RETURNED)) {
                    library.tryReturn(date, studentId, bookId, command);
                } else if (type.equals(LibraryRequest.Type.PICKED)) {
                    //去预约处取书
                    library.tryPick(date, studentId, bookId, command);
                } else if (type.equals(LibraryRequest.Type.RENEWED)) {
                    library.tryRenew(date, studentId, bookId, command);
                } else if (type.equals(LibraryRequest.Type.DONATED)) {
                    library.tryDonate(date, studentId, bookId, command);
                }
            }
        }
    }
}
