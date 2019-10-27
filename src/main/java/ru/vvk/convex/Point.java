package ru.vvk.convex;

public class Point {
	
	public static final int SIZE = 3;
		
	protected double[] vector = new double[SIZE];
	
	public Point() {
		
	}
	
	public Point(double x, double y, double z) {
		vector[0] = x;
		vector[1] = y;
		vector[2] = z;
	}
	
	public double X() {
		return vector[0];
	}
	
	public double Y() {
		return vector[1];
	}
	
	public double Z() {
		return vector[2];
	}
	
	public double get(int i) {
		return vector[i];
	}
	
	public void setX(double x) {
		vector[0] = x;
	}
	
	public void setY(double y) {
		vector[1] = y;
	}
	
	public void setZ(double z) {
		vector[2] = z;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for(int i = 0; i < vector.length; i++) {
			sb.append(String.valueOf(vector[i])).append(";");
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

}
