package javato.activetesting.atominfer;

import java.io.Serializable;

/**
 * Mostly a copy of hybridracedetection.CommutativePair
 * except that it stores UniqueEvents and not Integers.
 */
public class CommutativePair implements Serializable {

    private UniqueEvent x;
    private UniqueEvent y;

    public CommutativePair(UniqueEvent x, UniqueEvent y) {
        this.x = x;
        this.y = y;
    }

    public boolean contains(UniqueEvent e) {
        return x.equals(e) || y.equals(e);
    }

    public int hashCode() {
        return x.hashCode() + y.hashCode();
    }

    public boolean equals(Object object) {
        if (object == null) return false;
        if (!(object instanceof CommutativePair)) return false;
        CommutativePair c = (CommutativePair) object;
        return (x.equals(c.x) && y.equals(c.y)) || (x.equals(c.y) && y.equals(c.x));
    }

    public String toString() {
        return x + " " + y;
    }

    public UniqueEvent getFirst() {
	return x;
    }

    public UniqueEvent getSecond() {
	return y;
    }
}
