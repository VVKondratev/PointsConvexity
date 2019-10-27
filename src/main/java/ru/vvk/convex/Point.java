package ru.vvk.convex;

public class Point {
	/**
	 * Количество измерений
	 */
	public static final int SIZE = 3;
	/**
	 * вектор координат точки
	 */
	protected double[] vector = new double[SIZE];
	
	public Point(double x, double y, double z) {
		vector[0] = x;
		vector[1] = y;
		vector[2] = z;
	}

	/**
	 *
	 * @return - возвращает X координату точки.
	 */
	public double X() {
		return vector[0];
	}

	/**
	 *
	 * @return - возвращает Y координату точки.
	 */
	public double Y() {
		return vector[1];
	}

	/**
	 *
	 * @return - возвращает Z координату точки.
	 */
	public double Z() {
		return vector[2];
	}

	/**
	 *
	 * @param x - значение координаты X точки
	 */
	public void setX(double x) {
		vector[0] = x;
	}

	/**
	 *
	 * @param y - значение координаты y точки
	 */
	public void setY(double y) {
		vector[1] = y;
	}

	/**
	 *
	 * @param z - значение координаты z точки
	 */
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
