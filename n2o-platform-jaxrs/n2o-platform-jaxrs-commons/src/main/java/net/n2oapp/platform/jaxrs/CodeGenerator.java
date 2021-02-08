package net.n2oapp.platform.jaxrs;

import java.time.LocalDateTime;
import java.util.Objects;

public class CodeGenerator {
    /*
     * Should be between 1 and 9
     */
    private static final int SHIFT_OFFSET = 5;
    private String prefix;

    public CodeGenerator(String prefix) {
        this.prefix = prefix;
    }

    public String generate() {
        LocalDateTime dateTime = LocalDateTime.now();
        int yearOffset = dateTime.getYear() - 2021;
        int secondOfDay = 3600 * dateTime.getHour()  + 60 * dateTime.getMinute() + dateTime.getSecond();
        int millisecond = dateTime.getNano() / 1000000;

        String uid = secondOfDay + " " + millisecond + " " + yearOffset + dateTime.getDayOfYear();

        char[] intCharArray = uid.toCharArray();
        for (int i=0; i<intCharArray.length; i++) {
            if (intCharArray[i] != ' ')
                intCharArray[i] = shift(intCharArray[i]);
        }

        if (Objects.nonNull(prefix) && !prefix.isEmpty())
            return prefix + " " + String.valueOf(intCharArray);
        else return String.valueOf(intCharArray);
    }

    private static char shift(char input) {
        return input + SHIFT_OFFSET > '9'? (char)(input + SHIFT_OFFSET - 10) : (char)(input + SHIFT_OFFSET);
    }
}
