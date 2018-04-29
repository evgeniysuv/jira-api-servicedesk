package executable;

public class App {
    public static void main(String[] args) {
        ThreadGroup group = new ThreadGroup("stub");
        Thread thread1 = new Thread(group, () -> {
            throw new Error();
        });
        thread1.setUncaughtExceptionHandler((t, e) -> {
            group.interrupt();
            System.out.println("Dead");
        });

        new Thread(group, () -> {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                System.out.println("Killed");
            }
        }).start();
        thread1.start();

//
//
//        String date = "2018-04-09T16:39:10.000+0300";
//        LocalDateTime localDateTime = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX"));
//        System.out.println(localDateTime);
//        System.out.println(localDateTime.getDayOfWeek());
//        System.out.println(localDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
//        localDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }
}
