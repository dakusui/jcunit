package com.github.dakusui.petronia.ut;

import static com.github.dakusui.lisj.Basic.NIL;
import static com.github.dakusui.lisj.Basic.car;
import static com.github.dakusui.lisj.Basic.cdr;
import static com.github.dakusui.lisj.Basic.cons;
import static com.github.dakusui.lisj.Basic.eq;
import static com.github.dakusui.lisj.Basic.length;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import com.github.dakusui.lisj.Basic;

public class ConsTest {
	private static final Object ARR_54 = cons(1, cons(2, cons(3, cons(4, NIL))));
	private static final Object ARR_53 = cons(1, new Object[]{2, 3, 4, NIL});
	private static final Object ARR_52 = cons(1, cons(2, cons(3, cons(4, cons(NIL, NIL)))));
	private static final Object ARR_51 = new Object[]{1,2,3,4,NIL};
	private static final Object ARR_24 = cons(1, cons(NIL, cons(3, cons(4, 5))));
	private static final Object ARR_23 = cons(1, new Object[]{NIL, 3, 4, 5});
	private static final Object ARR_22 = cons(1, cons(NIL, cons(3, cons(4, cons(5, NIL)))));
	private static final Object ARR_21 = new Object[]{1,NIL,3,4,5};
	private static final Object ARR_14 = cons(NIL, cons(2, cons(3, cons(4, 5))));
	private static final Object ARR_13 = cons(NIL, new Object[]{2, 3, 4, 5});
	private static final Object ARR_12 = cons(NIL, cons(2, cons(3, cons(4, cons(5, NIL)))));
	private static final Object ARR_11 = new Object[]{NIL,2,3,4,5};
	private static final Object ARR_04 = cons(1, cons(2, cons(3, cons(4, 5))));
	private static final Object ARR_03 = cons(1, new Object[]{2, 3, 4, 5});
	private static final Object ARR_02 = cons(1, cons(2, cons(3, cons(4, cons(5, NIL)))));
	private static final Object ARR_01 = new Object[]{1,2,3,4,5};

	@Test
	public void test01() {
		Object arr = ARR_01;
		assertArrayEquals((Object[])ARR_01, walk(arr));
		assertEquals(5, length(arr));
	}
	
	@Test
	public void test02() {
		Object arr = ARR_02;
		assertArrayEquals((Object[])ARR_01, walk(arr));
		assertEquals(5, length(arr));
	}

	@Test
	public void test03() {
		Object arr = ARR_03;
		assertArrayEquals((Object[])ARR_01, walk(arr));
		assertEquals(5, length(arr));
	}
	
	@Test
	public void test04() {
		Object arr = ARR_04;
		assertArrayEquals((Object[])ARR_01, walk(arr));
		assertEquals(5, length(arr));
	}


	@Test
	public void test11() {
		Object arr = ARR_11;
		assertArrayEquals((Object[])ARR_11, walk(arr));
	}
	
	@Test
	public void test12() {
		Object arr = ARR_12;
		assertArrayEquals((Object[])ARR_11, walk(arr));
	}

	@Test
	public void test13() {
		Object arr = ARR_13;
		assertArrayEquals((Object[])ARR_11, walk(arr));
	}
	
	@Test
	public void test14() {
		Object arr = ARR_14;
		assertArrayEquals((Object[])ARR_11, walk(arr));
	}


	@Test
	public void test21() {
		Object arr = ARR_21;
		assertArrayEquals((Object[])ARR_21, walk(arr));
	}
	
	@Test
	public void test22() {
		Object arr = ARR_22;
		assertArrayEquals((Object[])ARR_21, walk(arr));
	}

	@Test
	public void test23() {
		Object arr = ARR_23;
		assertArrayEquals((Object[])ARR_21, walk(arr));
	}
	
	@Test
	public void test24() {
		Object arr = ARR_24;
		assertArrayEquals((Object[])ARR_21, walk(arr));
	}

	@Test
	public void test51() {
		Object arr = ARR_51;
		assertArrayEquals((Object[])ARR_51, walk(arr));
	}
	
	@Test
	public void test52() {
		Object arr = ARR_52;
		assertArrayEquals((Object[])ARR_51, walk(arr));
	}

	@Test
	public void test53() {
		Object arr = ARR_53;
		assertArrayEquals((Object[])ARR_51, walk(arr));
	}
	
	@Test
	public void test54() {
		Object arr = ARR_54;
		assertArrayEquals(new Object[]{1, 2, 3, 4}, walk(arr));
	}
	
	@Test
	public void null01() {
		Object arr = new Object[]{null, 1, 2};
		assertArrayEquals(new Object[]{null, 1, 2}, walk(arr));
	}

	@Test
	public void null02() {
		Object arr = new Object[]{1, null, 2};
		assertArrayEquals(new Object[]{1, null, 2}, walk(arr));
	}
	
	@Test
	public void null03() {
		Object arr = new Object[]{1, null, 2};
		assertArrayEquals(new Object[]{1, null, 2}, walk(arr));
	}

	@Test
	public void null04() {
		Object arr = cons(null, cons(1, 2));
		assertArrayEquals(new Object[]{null, 1, 2}, walk(arr));
	}

	@Test
	public void null05() {
		Object arr = cons(1, cons(null, 2));
		assertArrayEquals(new Object[]{1, null, 2}, walk(arr));
	}
	
	@Test
	public void null06() {
		Object arr = cons(1, cons(2, null));
		assertArrayEquals(new Object[]{1, 2, null}, walk(arr));
	}
	
	
	@Test
	public void length_0() {
		Object arr = new Object[]{};
		assertEquals(0, length(arr));
	}
	
	@Test
	public void length_0nil() {
		Object arr = NIL;
		assertEquals(0, length(arr));
	}

	@Test
	public void length_null() {
		Object arr = null;
		boolean passed = false;
		try {
			length(arr);
		} catch (NullPointerException e) {
			passed = true;
		}
		assertTrue(passed);
	}

	@Test
	public void length_atom() {
		Object arr = "A";
		boolean passed = false;
		try {
			length(arr);
		} catch (IllegalArgumentException e) {
			passed = true;
		}
		assertTrue(passed);
	}

	@Test
	public void length_cons() {
		Object arr = cons(1, 2);
		assertEquals(2, length(arr));
	}

	@Test
	public void length_1null() {
		Object arr = new Object[]{null};
		assertEquals(1, length(arr));
	}

	@Test
	public void length_1nil() {
		Object arr = new Object[]{NIL};
		assertEquals(1, length(arr));
	}

	@Test
	public void length_1atom() {
		Object arr = new Object[]{"A"};
		assertEquals(1, length(arr));
	}
	
	@Test
	public void length_1cons() {
		Object arr = new Object[]{cons(1, 2)};
		assertEquals(1, length(arr));
	}
	
	@Test
	public void get_01() {
		Object arr = ARR_01;
		check_get(arr);
	}

	@Test
	public void get_02() {
		Object arr = ARR_02;
		check_get(arr);
	}

	@Test
	public void get_03() {
		Object arr = ARR_03;
		check_get(arr);
	}

	@Test
	public void get_04() {
		Object arr = ARR_04;
		check_get(arr);
	}
	
	private void check_get(Object arr) {
		assertEquals(1, Basic.get(arr, 0));
		assertEquals(2, Basic.get(arr, 1));
		assertEquals(3, Basic.get(arr, 2));
		assertEquals(4, Basic.get(arr, 3));
		assertEquals(5, Basic.get(arr, 4));
	}

	@Test
	public void iter_01() {
		Object arr = ARR_01;
		check_iter(arr);
	}
	
	@Test
	public void iter_02() {
		Object arr = ARR_02;
		check_iter(arr);
	}
	
	@Test
	public void iter_03() {
		Object arr = ARR_03;
		check_iter(arr);
	}
	
	@Test
	public void iter_04() {
		Object arr = ARR_04;
		check_iter(arr);
	}
	
	@Test
	public void atom_01() {
		assertEquals(false, Basic.atom(new Object[]{1}));
	}
	
	public void atom_02() {
		assertEquals(true, Basic.atom(new Object[]{}));
	}
	
	public void atom_03() {
		assertEquals(true, Basic.atom(Basic.NIL));
	}
	
	public void atom_04() {
		assertEquals(true, Basic.atom(123));
	}
	
	public void atom_05() {
		assertEquals(true, Basic.atom(null));
	}

	public void atom_06() {
		assertEquals(false, Basic.atom(new Object[]{1,2}));
	}

	@Test
	public void constest() {
		Object cons = cons(cons("hello", "world"), "!");
		
		System.out.println("atom=" + Basic.atom(cons));
		System.out.println("car=" + ArrayUtils.toString(car(cons)));
		System.out.println("car=" + ArrayUtils.toString(cdr(cons)));
		
		System.out.println(Basic.tostr(cons));
		System.out.println(Basic.tostr(ARR_01));
		System.out.println(Basic.tostr(ARR_02));
		System.out.println(Basic.tostr(ARR_03));
		System.out.println(Basic.tostr(ARR_04));
		System.out.println(Basic.tostr(ARR_11));
		System.out.println(Basic.tostr(ARR_12));
		System.out.println(Basic.tostr(ARR_13));
		System.out.println(Basic.tostr(ARR_14));
		System.out.println(Basic.tostr(ARR_21));
		System.out.println(Basic.tostr(ARR_22));
		System.out.println(Basic.tostr(ARR_23));
		System.out.println(Basic.tostr(ARR_24));
	}
	
	private void check_iter(Object arr) {
		Iterator<Object> i = Basic.iterator(arr);
		assertEquals(true, i.hasNext()); assertEquals(1, i.next());
		assertEquals(true, i.hasNext()); assertEquals(2, i.next());
		assertEquals(true, i.hasNext()); assertEquals(3, i.next());
		assertEquals(true, i.hasNext()); assertEquals(4, i.next());
		assertEquals(true, i.hasNext()); assertEquals(5, i.next());
	}

	protected Object[] walk(Object arr) {
		List<Object> ret = new LinkedList<Object>();
		while (!eq(arr, NIL)) {
			Object car = car(arr);
			ret.add(car);
			arr = cdr(arr);
		}
		return ret.toArray();
	}
}
