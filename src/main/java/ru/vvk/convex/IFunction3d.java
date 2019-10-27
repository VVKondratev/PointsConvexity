package ru.vvk.convex;

/**
 * Интерфейс вычисления провзиольной функции.
 */
public interface IFunction3d {
	/**
	 *
	 * @param x - координата икс
	 * @param y - координата игрек
	 * @param z - координата зет
	 * @return - результат вычисления функции.
	 */
	public double get(double x, double y, double z);
}
