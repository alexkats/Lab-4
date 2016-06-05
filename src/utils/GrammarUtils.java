/**
 * @author Alexey Katsman
 * @since 02.06.16
 */

package utils;

@SuppressWarnings("unused")
public class GrammarUtils {
    public static boolean isToken(String s) {
        return Character.isUpperCase(s.charAt(0));
    }

    public static boolean isRule(String s) {
        return Character.isLowerCase(s.charAt(0));
    }

    public static boolean isCodeBlock(String s) {
        return s.charAt(0) == '{';
    }

    public static boolean isValidId(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_';
    }

    public static String firstToUpperCase(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static boolean isInParams(int type, int ... types) {
        for (int i : types) {
            if (i == type) {
                return true;
            }
        }

        return false;
    }
}
