/**
 * @author Alexey Katsman
 * @since 02.06.16
 */

package utils;

@SuppressWarnings("unused")
public class Token {
    private String name;
    private int type;

    public Token(String name, int type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }
}
