/**
 * @author Alexey Katsman
 * @since 02.06.16
 */

public enum NodeType {
    E, E_ {
        @Override
        public String toString() {
            return "E'";
        }
    },
    T, T_ {
        @Override
        public String toString() {
            return "T'";
        }
    },
    F, F_ {
        @Override
        public String toString() {
            return "F'";
        }
    },
    A, VAR, AND, OR, XOR, NOT, LEFT, RIGHT
}