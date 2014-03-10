package com.github.dakusui.jcunit.generators.ipo;

/**
 * A class that represents the value pair.
 * In IPO algorithm, all the possible value pairs(that are represented by
 * this class) must be covered by output result (CA; Covering Array).
 * 
 * @author hiroshi
 */
public class ValuePair {
	int A;
	Object r;
	int B;
	Object s;

	/**
	 * Creates an object of this class.
	 * 
	 * @param A
	 * @param r
	 * @param B
	 * @param s
	 */
	public ValuePair(int A, Object r, int B, Object s) {
		if (A == B)
			throw new IllegalArgumentException();
		if (A < B) {
			this.A = A;
			this.r = r;
			this.B = B;
			this.s = s;
		} else {
			this.A = B;
			this.r = s;
			this.B = A;
			this.s = r;
		}
	}

	/**
	 * Returns the hashCode of this object.
	 */
	public int hashCode() {
		return this.A + ((this.r == null) ? 0 : this.r.hashCode())
				+ this.B + ((this.s == null) ? 0 : this.s.hashCode());
	}

	/**
	 * Checks if the given object is <code>equals</code> to this object.
	 * Note that since the intention of IPO algorithm is to generate all the
	 * possible pairs in the test space, the order of A.r and B.s must be
	 * ignored. That is if we swap the values of <code>A</code> and </code>B</code>,
	 * and also <code>r</code> and <code>s</code>, it should still remain
	 * equal to the original object.
	 * 
	 * @return true - anotherObject is equal to this object / false - otherwise.
	 */
	public boolean equals(Object anotherObject) {
		if (!(anotherObject instanceof ValuePair)) {
			////
			// since null is not an instance of any class, anotherObject
			// can't be null later on in this method.
			return false;
		}
		ValuePair another = (ValuePair) anotherObject;
		if (this.A != another.A)
			return false;
		if (this.B != another.B)
			return false;
		if (this.r == null) {
			if (another.r != null)
				return false;
		} else {
			if (!this.r.equals(another.r))
				return false;
		}
		if (this.s == null) {
			if (another.s != null)
				return false;
		} else {
			if (!this.s.equals(another.s))
				return false;
		}
		return true;
	}

	public Object r() {
		return this.r;
	}

	public Object s() {
		return this.s;
	}

	public int A() {
		return this.A;
	}

	public int B() {
		return this.B;
	}

	public String toString() {
		return String.format("(F%d=%s,F%d=%s)", A, r, B, s);
	}
}