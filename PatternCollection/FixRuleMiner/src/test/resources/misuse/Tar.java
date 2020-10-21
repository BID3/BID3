package utd;


public class Base
{
	int f1 = 0;
	int f2 = 1;

	public Base() {

	}

	public Base(int a) {

	}

	public void m1(int a, int b, int c) {
		int d = 0;
		Object object = m();
		m2(a, b, c);
		// add new if
		if (a == null) {
			object.m3(a, b);
			// the folling two are added
			this.f1 = 0;
			a = b + 1;
		}
	}

	public void m2(int a, int b, int c) {
		c = a + b;
		if (c > b) {
			c++;
		} else {
			c--;
		}
		// add following complete if
		if (a < 0) {
			a ++;
		}
	}
	
	public Object api(Object o) {
		return null;
	}
	
	public boolean check(Object o) {
		return false;
	}
	
	public Object m3(Object o1, Object o2) {
		Object object = api(o2);
		// add if-null check
		if (object == null) {
			object = o2;
		}
		// add if-null and dependency check
		if (check(object) && object != null) {
			object = api(object);
		}
		Object object2 = object;
		// add other if check
		if (object2 == null) {
			object2 = new Object();
			object = api(object2);
		}
		
		return object;
	}
	
}
