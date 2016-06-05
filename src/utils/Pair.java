/**
 * @author Alexey Katsman
 * @since 02.06.16
 */

package utils;

@SuppressWarnings({"unused"})
public class Pair<A, B> implements Comparable<Pair<A, B>> {
    private A a;
    private B b;

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getFirst() {
        return a;
    }

    public B getSecond() {
        return b;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        Pair<?, ?> other = (Pair<?, ?>) obj;

        return other.getFirst().equals(this.a) && other.getSecond().equals(this.b);
    }

    @Override
    @SuppressWarnings("unchecked")
    public int compareTo(Pair<A, B> o) {
        int compareFirst = ((Comparable<A>) a).compareTo(o.getFirst());

        if (compareFirst != 0) {
            return compareFirst;
        }

        return ((Comparable<B>) b).compareTo(o.getSecond());
    }
}
