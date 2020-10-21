package utd.examples;

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
		m2(a, b, c);
		m3(a, b);
	}

	public void m2(int a, int b, int c) {
		c = a + b;
		if (c > b) {
			c--;
		} else {
			c--;
		}
	}

	public void m3(int a, int b, int c) {
		this.f1 = a;
		this.f2 = b;
	}

	public void m3(int a, int b) {
		this.f1 = -a;
		this.f2 = b;
	}

	public void m4(int a, int b, int c) {
		Base b = new Base();
		Base b2 = new Base(a);
	}

	public void m5(int a) {
		int res = 0;
		switch (a) {
		case 1:
			res = 1;
			break;
		case 2:
			res = 2;
			break;
		case 3:
			res = 3;
			break;
		default:
			res = 0;
			break;
		}
	}
	public int m6(int a){
		return a;
	}
	public int m7(int a){
		return m6(a);
	}

}
