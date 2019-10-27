package ru.vvk.convex;

import java.util.List;

import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.Polygon;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.primitives.Shape;

public class Draw {
	
	private static Color defColor = new Color(0.5f, 0.5f, 0.5f);
	
	public static AbstractDrawable get(Coord3d[] points, Color color) {
		final Scatter build = new Scatter(points, color);
		build.setWidth(3);
		build.setBoundingBoxDisplayed(false);
		
		return build;
	}
	
	public static AbstractDrawable get(List<Coord3d> points, Color color) {
		return get(points.toArray(new Coord3d[0]), color);		
	}
	
	public static AbstractDrawable get(List<Coord3d> points) {		
		return get(points, defColor);
	}
	
	public static AbstractDrawable get(Surface surface, Color color) {		
		return get(surface.getCoords(), color);
	}
	
	public static AbstractDrawable get(Surface surface) {
		return get(surface, defColor);
	}
	
	public static AbstractDrawable getShape(Surface surface, Color color) {
		return getShape(surface.getPolygons(), color);
	}
	
	public static AbstractDrawable getShape(List<Polygon> polygons, Color color) {
		Shape build = new Shape(polygons);
		//build.setColorMapper(new ColorMapper(new ColorMapRainbow(), build.getBounds().getZmin(), build.getBounds().getZmax(), new org.jzy3d.colors.Color(1,1,1,1f)));
		build.setColor(color);
		//build.setWireframeDisplayed(true);
		//build.setWireframeColor(org.jzy3d.colors.Color.BLACK);
		build.setBoundingBoxDisplayed(false);
		
		return build;
	}

}
