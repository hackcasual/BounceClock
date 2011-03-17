package net.hackcasual;

public class PointI {
	public final int x;
	public final int y;
	
	public PointI(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof PointI))
			return false;
		
		
		PointI other = (PointI)o;
		
		return other.x == x && other.y == y;
	}
	
	@Override
	public int hashCode() {
		return x + 23 * y;
	}
	
	@Override
	public String toString() {
		return String.format("[%dx%d]", x, y);
	}
}
