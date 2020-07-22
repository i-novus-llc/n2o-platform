package net.n2oapp.platform.jaxrs;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CodeModel {

    private final String message;
    private final Code code;

    protected CodeModel(Code code, String message) {
        this.message = message;
        this.code = code;
    }

    private static class Code {
        private static final String PREFIX = "Ошибка. Код события ";
        private String code;

        private Code(String code) {
            this.code = code;
        }

        public static Code buildCode() {
            return new Code(generateCode());
        }

        /*
         * код формируется по паттерну секунда-минута-час-день-месяц-год
         * */
        private static String generateCode() {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("ssmmhh-ddMMuu"));
        }

        public void setCode(String code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return PREFIX + code;
        }
    }

    public static CodeModel buildCode(String message) {
        return new CodeModel(Code.buildCode(), message);
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code.toString();
    }

    public void setCode(String code) {
        this.code.setCode(code);
    }

}
