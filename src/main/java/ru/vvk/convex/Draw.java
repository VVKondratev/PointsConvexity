package ru.vvk.convex;

import java.util.List;

import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.Polygon;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.primitives.Shape;

public class Draw {
	/**
	 *
	 * @param points - координаты точек
	 * @param color - цвет
	 * @return - возвращается построенное разбиение
	 */
	public static AbstractDrawable get(Coord3d[] points, Color color) {
		final Scatter build = new Scatter(points, color);
		build.setWidth(3);
		build.setBoundingBoxDisplayed(false);
		return build;
	}

	/**
	 *
	 * @param points - список координат точек
	 * @param color - цвет
	 * @return - - возвращается построенное разбиение
	 */
	public static AbstractDrawable get(List<Coord3d> points, Color color) {
		return get(points.toArray(new Coord3d[0]), color);		
	}

	/**
	 *
	 * @param polygons - список полигонов
	 * @param color - цвет
	 * @return - создает форму
	 */
	public static AbstractDrawable getShape(List<Polygon> polygons, Color color) {
		Shape build = new Shape(polygons);
		build.setColor(color);
		build.setBoundingBoxDisplayed(false);
		return build;
	}

}
