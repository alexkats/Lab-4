/**
 * @author Alexey Katsman
 * @since 02.06.16
 */

package utils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Type {
    private static final int NORMAL_TYPE = 0;
    private static final int ARRAY_TYPE = 1;
    private static final int GENERIC_TYPE = 2;

    private int type;
    private String typeName;
    private List<Type> subTypes;

    private Type(int type, String typeName, List<Type> subTypes) {
        this.type = type;
        this.typeName = typeName;
        this.subTypes = subTypes;
    }

    public static Type normal(String typeName) {
        return new Type(NORMAL_TYPE, typeName, null);
    }

    public static Type array(Type type1) {
        return new Type(ARRAY_TYPE, null, Collections.singletonList(type1));
    }

    public static Type generic(String typeName, List<Type> subTypes) {
        return new Type(GENERIC_TYPE, typeName, subTypes);
    }

    @Override
    public String toString() {
        switch (type) {
            case NORMAL_TYPE:
                return typeName;
            case ARRAY_TYPE:
                return subTypes.get(0) + "[]";
            case GENERIC_TYPE:
                return typeName + "<" + subTypes.stream().map(e -> "" + e).collect(Collectors.joining(", ")) + ">";
            default:
                return "Unknown type";
        }
    }
}
