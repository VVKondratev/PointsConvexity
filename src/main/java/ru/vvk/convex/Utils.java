package ru.vvk.convex;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.joor.Reflect;
import org.jzy3d.maths.Coord3d;

public class Utils {
	/**
	 * Номер функции
	 */
	private static int functionIndex = 0;

	/**
	 * Вывод переданной ошибки
	 * @param message - сообщение, переденное ошибкой
	 */
	public static void error(String message) {
		JOptionPane.showMessageDialog(new JFrame(), message, "Ошибка", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 *
	 * @param tb - элемент JTextField
	 * @param message - сообщение
	 * @return - возвращает текст, содержащийся внутри JTextField
	 * @throws Exception
	 */
	public static double convert(JTextField tb, String message) throws Exception {
		try {
			return Double.parseDouble(tb.getText());
		}catch(Exception e) {
			throw new Exception(message);
		}
	}

	/**
	 *
	 * @param src - функция записанная в виде строки
	 * @return - возвращает распарсенную функцию
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
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
