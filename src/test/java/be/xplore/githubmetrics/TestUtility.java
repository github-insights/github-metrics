package be.xplore.githubmetrics;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TestUtility {
    public static String yesterday() {
        return LocalDate.now().minusDays(1)
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
