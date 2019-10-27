package ru.vvk.convex;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.joor.Reflect;
import org.jzy3d.maths.Coord3d;

public class Utils {
	private static int functionIndex = 0;
	
	public static void error(String message) {
		JOptionPane.showMessageDialog(new JFrame(), message, "Ошибка", JOptionPane.ERROR_MESSAGE);
	}
	
	public static double convert(JTextField tb, String message) throws Exception {
		try {
			return Double.parseDouble(tb.getText());
		}catch(Exception e) {
			throw new Exception(message);
		}
	}
	
	public static List<Coord3d> getSphereGraph(int n, Point o, double r){
		final List<Coord3d> points = new ArrayList<Coord3d>();
		
		double angXY = 0, angZ = 0;
		double step = (Math.PI / (2 * n));
		
		for (double i = 0; i < Math.PI; i += step) {			
			angXY = 0.0;
			for (double j = 0; j < Math.PI; j += step) {
				
				points.add(new Coord3d(
						r*Math.sin(angZ) * Math.cos(angXY) + o.X(),
						r*Math.sin(angZ) * Math.sin(angXY) + o.Y(),
						r*Math.cos(angZ) + o.Z()));				
				
				
				angXY = angXY + step;

			}
			angZ = angZ + step;
		}
		
		return points;
	}
	
	public static IFunction3d getFunction(String src) throws InstantiationException, IllegalAccessException{
		final StringBuilder sb = new StringBuilder();
		sb.append("package ru.vvk;\n")
		  .append("import ru.vvk.convex.IFunction3d;\n\n")
		  .append("class F").append(++functionIndex).append(" implements IFunction3d{\n")
		  .append(" public double get(double x, double y, double z){")		 
		  .append(" return ").append(src).append(";\n")
		  .append("}};\n");
		
		return Reflect.compile("ru.vvk.F" + functionIndex, sb.toString()).create().get();
	}
}
