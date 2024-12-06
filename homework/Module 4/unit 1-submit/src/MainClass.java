import com.oocourse.library1.LibrarySystem;
import com.oocourse.library1.LibraryRequest;
import com.oocourse.library1.LibraryCommand;
import com.oocourse.library1.LibraryScanner;

import java.time.LocalDate;

public class MainClass {
    public static void main(String[] args) {
        //LibrarySystem librarySystem = LibrarySystem.INSTANCE;
        LibraryScanner scanner = LibrarySystem.SCANNER;
        //LibraryPrinter PRINTER = LibrarySystem.PRINTER;
        Library library = new Library(scanner.getInventory());
        while (true) {
            LibraryCommand<?> command = scanner.nextCommand();
            if (command == null) { break; }
            LocalDate date = command.getDate(); //什么是localDate（？）
            if (command.getCmd().equals("OPEN")) {
                library.manage(date, false);// 在图书馆开门之前干点什么
            } else if (command.getCmd().equals("CLOSE")) {
                library.manage(date, true);// 在图书馆关门之后干点什么
            } else {
                LibraryRequest request = (LibraryRequest) command.getCmd();
                // 对 request 干点什么
                library.dealWithRequest(date, request);
            }
        }

    }
}
