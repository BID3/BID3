package utd;

class Base
{
	int f1 = 0;
	int f2 = 1;

	public Base() {

	}

	public Base(int a) {

	}

	public void m1(int a, int b, int c) {
		int d = 0;
		Object object = null;
		m2(a, b, c);
		object.m3(a, b);
	}

	public void m2(int a, int b, int c) {
		c = a + b;
		if (c > b) {
			c++;
		} else {
			c--;
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
		object = o2;
		object = api(object);
		Object object2 = object;
		object2 = new Object();
		object = api(object2);
		return object;
	}
	
}
