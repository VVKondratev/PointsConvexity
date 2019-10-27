package ru.vvk.convex;

public class Grid {
	
	/**
	 * Шаг сетки
	 */
	protected double step;
	
	/**
	 * Базовая точка области сетки
	 */
	protected Point c;
	
	/**
	 * Количество точек сетки по осям (полное количество точек сетки = count[0]*count[1]*count[2])
	 */
	protected int[] count; //
	
	/**
	 * Координаты x по индексу точки. Например, координата x i-ой точки сетки = xi[i]
	 */
	protected double[] xi;
	
	/**
	 * Координаты y по индексу точки
	 */
	protected double[] yi; 
	
	/**
	 * Координаты z по индексу точки
	 */
	protected double[] zi; 
	
	/**
	 * Половина шага сетки
	 */
	protected double step2;
	
	/**
	 * Конструктор декартовой сетки точек
	 * @param c координаты базовой точки области сетки
	 * @param length размеры по осям области сетки
	 * @param step шаг сетки
	 */
	public Grid(Point c, Point length, double step) {
		this.c = c;
		this.step = step;
		step2 = step/2;
		
		count = new int[Point.SIZE];
		for(int i = 0; i < Point.SIZE; i++) {
			
			//Если длины отрицательные - пересчитываем точку с относительно которой они положительны
			if(length.vector[i] < 0) {
				length.vector[i] = Math.abs(length.vector[i]);
				c.vector[i] -= length.vector[i];
			}
			count[i] = (int) (Math.round(length.vector[i] / step) + 1);
		}
		
		xi = new double[count[0]];
		yi = new double[count[1]];
		zi = new double[count[2]];
		
		for(int i = 0; i < count[0]; i++) {
			xi[i] = c.X() + i*step;
		}
		for(int i = 0; i < count[1]; i++) {
			yi[i] = c.Y() + i*step;
		}
		for(int i = 0; i < count[2]; i++) {
			zi[i] = c.Z() + i*step;
		}
	}
}
