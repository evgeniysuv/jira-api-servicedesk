package executable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class App {
    public static void main(String[] args) {
        String date = "2018-04-09T16:39:10.000+0300";
        LocalDateTime localDateTime = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX"));
        System.out.println(localDateTime);
        System.out.println(localDateTime.getDayOfWeek());
        System.out.println(localDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
        localDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }
}
