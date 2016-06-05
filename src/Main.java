import java.io.IOException;

/**
 * @author Alexey Katsman
 * @since 02.06.16
 */

public class Main {
    public static void main(String[] args) throws IOException {
        ClassGenerator.gen("lab2Lexer", "lab2Parser");
    }
}
