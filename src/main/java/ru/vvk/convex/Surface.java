package ru.vvk.convex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.Polygon;

public class Surface {

	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;

	protected Grid grid;
	protected IFunction3d f;
	protected boolean[][][] points;
	protected int[][][][] links;

	protected Voxel[][][] voxels;

	/**
	 * Сетка знаков функции сдвинута относительно сетки точек на полшага назад по
	 * всем осям Число точек по оси на одну больше чем в сетке grid
	 */
	protected int[][][] signs;

	protected List<int[]> inners;
	protected List<int[]> outers;

	protected List<int[]>[][][] neighbors;

	protected double s;
	protected double sv = -1;
	protected double s1;
	protected double s2;

	private List<AbstractDrawable> figures = new Vector<>();

	public Surface(Grid grid, IFunction3d f) {
		this.grid = grid;
		this.f = f;

		init();
	}

	public Surface(Grid grid, boolean[][][] points, int[][][] signs) {
		this.grid = grid;
		this.points = points;
		this.signs = signs;
	}

	private void init() {

		// Определение знака функции в точках сетки
		signs = new int[grid.count[0] + 1][grid.count[1] + 1][grid.count[2] + 1];

		double x = grid.c.X() - grid.step2 - grid.step;
		double y = grid.c.Y() - grid.step2 - grid.step;
		double z = grid.c.Z() - grid.step2 - grid.step;

		for (int i = 0; i < signs[0].length; i++) {
			x += grid.step;
			y = grid.c.Y() - grid.step2 - grid.step;
			for (int j = 0; j < signs[1].length; j++) {
				y += grid.step;
				z = grid.c.Z() - grid.step2 - grid.step;
				for (int k = 0; k < signs[2].length; k++) {
					z += grid.step;
					signs[i][j][k] = (int) Math.signum(f.get(x, y, z));
				}
			}
		}

		// Определение принадлежности точки к функции - знаки ближайших точек
		// должны
		// отличаться
		points = new boolean[grid.count[0]][grid.count[1]][grid.count[2]];

		for (int i = 0; i < grid.count[0]; i++) {
			for (int j = 0; j < grid.count[1]; j++) {
				for (int k = 0; k < grid.count[2]; k++) {
					points[i][j][k] = Math.abs(signs[i][j][k] + signs[i + 1][j][k] + signs[i][j + 1][k]
							+ signs[i][j][k + 1] + signs[i + 1][j + 1][k] + signs[i + 1][j][k + 1]
							+ signs[i][j + 1][k + 1] + signs[i + 1][j + 1][k + 1]) < 8;
				}
			}
		}
	}

	public String getSummary() {
		if (inners == null) {
			analizePoints();
		}

		final StringBuilder sb = new StringBuilder();
		sb.append("Число внутренних точек = ").append(inners.size()).append("\n").append("Число граничных точек = ")
				.append(outers.size()).append("\n").append("S = ").append(getS()).append("\n").append("Sconvex = ")
				.append(sv).append("\n").append("delta = ").append(s * s - sv);

		return sb.toString();
	}

	/**
	 * Нахождение внутренних и граничных точек
	 */
	@SuppressWarnings("unchecked")
	private void analizePoints() {
		inners = new ArrayList<>();
		outers = new ArrayList<>();
		neighbors = new List[grid.count[X] - 1][grid.count[Y] - 1][grid.count[Z] - 1];

		links = new int[grid.count[X] - 1][grid.count[Y] - 1][grid.count[Z] - 1][];

		voxels = new Voxel[grid.count[X] - 1][grid.count[Y] - 1][grid.count[Z] - 1];
		s = 0d;

		for (int i = 0; i < grid.count[X] - 1; i++) {
			for (int j = 0; j < grid.count[Y] - 1; j++) {
				for (int k = 0; k < grid.count[Z] - 1; k++) {
					if (points[i][j][k]) {
						final Voxel v = Voxel.getVoxel(signs[i][j][k], signs[i + 1][j][k], signs[i][j + 1][k],
								signs[i + 1][j + 1][k], signs[i][j][k + 1], signs[i + 1][j][k + 1],
								signs[i][j + 1][k + 1], signs[i + 1][j + 1][k + 1]);
						voxels[i][j][k] = v;
						s += v.getS().apply(grid.step);

						// Каждую точку ограничивают 6 плоскостей куба
						// Если 4 точки ограничивающей плоскости имеют разные
						// знаки и следующая за
						// плоскостью точка не принадлежит поверхности,то
						// в эпсилон окрестности нашей точки существуют две
						// точки (соседние) не
						// принадлежащие поверхности имеющие разные знаки
						// вектора нормали
						// т.е. текущая точка является граничной
						if ((Math.abs(
								signs[i][j][k] + signs[i][j + 1][k] + signs[i][j][k + 1] + signs[i][j + 1][k + 1]) < 4
								&& (i == 0 || !points[i - 1][j][k])) || // левая относительно x плоскость
								(Math.abs(signs[i + 1][j][k] + signs[i + 1][j + 1][k] + signs[i + 1][j][k + 1]
										+ signs[i + 1][j + 1][k + 1]) < 4
										&& (i == (grid.count[X] - 2) || !points[i + 1][j][k]))
								|| // правая относительно x плоскость
								(Math.abs(signs[i][j][k] + signs[i + 1][j][k] + signs[i][j][k + 1]
										+ signs[i + 1][j][k + 1]) < 4 && (j == 0 || !points[i][j - 1][k]))
								|| // левая относительно y плоскость
								(Math.abs(signs[i][j + 1][k] + signs[i + 1][j + 1][k] + signs[i][j + 1][k + 1]
										+ signs[i + 1][j + 1][k + 1]) < 4
										&& (j == (grid.count[Y] - 2) || !points[i][j + 1][k]))
								|| // правая относительно y плоскость
								(Math.abs(signs[i][j][k] + signs[i + 1][j][k] + signs[i][j + 1][k]
										+ signs[i + 1][j + 1][k]) < 4 && (k == 0 || !points[i][j][k - 1]))
								|| // левая относительно z плоскость
								(Math.abs(signs[i][j][k + 1] + signs[i + 1][j][k + 1] + signs[i][j + 1][k + 1]
										+ signs[i + 1][j + 1][k + 1]) < 4
										&& (k == (grid.count[Z] - 2) || !points[i][j][k + 1])) // правая
																								// относительно
																								// z
																								// плоскость
						) {
							outers.add(new int[] { i, j, k });
						} else {
							inners.add(new int[] { i, j, k });
						}

						links[i][j][k] = new int[] { i, j, k };
						// neighbors[i][j][k] = getNeighbors(i, j, k);
					}
				}
			}
		}

		for (int i = 0; i < grid.count[X] - 1; i++) {
			for (int j = 0; j < grid.count[Y] - 1; j++) {
				for (int k = 0; k < grid.count[Z] - 1; k++) {
					if (points[i][j][k]) {
						neighbors[i][j][k] = getNeighbors(i, j, k);
					}
				}
			}
		}

		System.out.println("outers=" + outers.size());
		System.out.println("inners=" + inners.size());
		System.out.println("s=" + s);
	}

	/**
	 * Список соседних точек принадлежащих поверхности
	 * 
	 * @param iX индекс матрицы по оси X
	 * @param iY
	 * @param iZ
	 * @return
	 */
	private List<int[]> getNeighbors(int iX, int iY, int iZ) {
		List<int[]> n = new LinkedList<>();

		for (int i = Math.max(0, iX - 1); i < Math.min(grid.count[0] - 2, iX + 2); i++) {
			for (int j = Math.max(0, iY - 1); j < Math.min(grid.count[1] - 2, iY + 2); j++) {
				for (int k = Math.max(0, iZ - 1); k < Math.min(grid.count[2] - 2, iZ + 2); k++) {
					if (points[i][j][k]) {
						// n.add(new int[] {i, j, k});
						n.add(links[i][j][k]);
					}
				}
			}
		}
		// System.out.println(n.size());
		return n;
	}

	// public List<int[]> getInners(){
	// if(inners == null) {
	// analizePoints();
	// }
	// return inners;
	// }

	public List<Coord3d> getInners() {
		List<Coord3d> coords = new ArrayList<>();

		if (inners == null) {
			analizePoints();
		}
		for (int[] p : inners) {
			coords.add(new Coord3d((float) grid.xi[p[0]], (float) grid.yi[p[1]], (float) grid.zi[p[2]]));
		}

		return coords;
	}

	public List<Coord3d> getOuters() {
		List<Coord3d> coords = new ArrayList<>();

		if (outers == null) {
			analizePoints();
		}
		for (int[] p : outers) {
			coords.add(new Coord3d((float) grid.xi[p[0]], (float) grid.yi[p[1]], (float) grid.zi[p[2]]));
		}

		return coords;
	}

	// public float[] arX() {
	// return grid.arCoord(0);
	// }
	//
	// public float[] arY() {
	// return grid.arCoord(1);
	// }

	// public float getZ(int iX, int iY) {
	// for (int iZ = 0; iZ < grid.count[2] - 1; iZ++) {
	// if (points[iX][iY][iZ]) {
	// return grid.getZ(iZ);
	// }
	// }
	//
	// return Float.NaN;
	// }

	// public float[] arZ() {
	// float[] z = new float[(grid.count[0] - 1) * (grid.count[1] - 1)];
	//
	// for (int i = 0; i < grid.count[1] - 1; i++) {
	// for (int j = 0; j < grid.count[0] - 1; j++) {
	// z[j + ((grid.count[0] - 1) * i)] = getZ(j, i);
	// }
	// }
	// return z;
	// }

	public List<Coord3d> getCoords() {
		List<Coord3d> coordinates = new ArrayList<>();
		for (int i = 0; i < points.length; i++) {
			for (int j = 0; j < points[i].length; j++) {
				for (int k = 0; k < points[i][j].length; k++) {
					if (points[i][j][k]) {
						coordinates.add(new Coord3d((float) grid.xi[i], (float) grid.yi[j], (float) grid.zi[k]));
					}
				}
			}
		}
		return coordinates;
	}

	public List<AbstractDrawable> getExt() {
		return figures;
	}

	public List<Point> getPoints() {
		List<Point> ps = new ArrayList<>();
		for (int i = 0; i < points.length; i++) {
			for (int j = 0; j < points[i].length; j++) {
				for (int k = 0; k < points[i][j].length; k++) {
					if (points[i][j][k]) {
						ps.add(new Point(grid.xi[i], grid.yi[j], grid.zi[k]));
					}
				}
			}
		}
		return ps;
	}

	public double getS() {
		if (inners == null) {
			analizePoints();
		}

		return s;
	}

	public Surface innerOf(Surface s) {
		boolean[][][] pn = new boolean[grid.count[0] - 1][grid.count[1] - 1][grid.count[2] - 1];
		int[][][] sn = new int[grid.count[0] - 1][grid.count[1] - 1][grid.count[2] - 1];

		for (int iX = 0; iX < grid.count[0] - 1; iX++) {
			for (int iY = 0; iY < grid.count[1] - 1; iY++) {
				for (int iZ = 0; iZ < grid.count[2] - 1; iZ++) {
					if (points[iX][iY][iZ] && s.signs[iX][iY][iZ] <= 0) {
						pn[iX][iY][iZ] = true;
					} else {
						pn[iX][iY][iZ] = false;
					}
					sn[iX][iY][iZ] = signs[iX][iY][iZ];
				}
			}
		}

		return new Surface(grid, pn, sn);
	}

	public void convex(double[][] pointsC) {

		double x3 = pointsC[0][0], y3 = pointsC[0][1], z3 = pointsC[0][2];
		if (outers == null) {
			analizePoints();
		}

		sv = s * s;
		final Map<int[], Set<int[]>> exclude = new HashMap<>();

		final double error = Math.sqrt(3) * grid.step / 2;
		// System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism",
		// "1");
		System.out.println("in");
		outers.parallelStream().forEach(a1 -> {
			// Множество уже найденных выпуклых точек относительно
			// граничной точки а1
			final boolean[][][] isConvex = new boolean[grid.count[X] - 1][grid.count[Y] - 1][grid.count[Z] - 1];
			isConvex[a1[X]][a1[Y]][a1[Z]] = true; // Граничная точка
													// выпукла
													// относительно
													// себя

			final Map<int[], Set<int[]>> excludeOut = new HashMap<>();

			inners.stream().forEach(a2 -> {
				if (isConvex[a2[X]][a2[Y]][a2[Z]]) {
					// System.out.println("is_convex");
					return;
				}

				final double x1 = grid.xi[a1[X]]; // grid.c.X()
													// +
													// a1[0]*grid.step;
				final double y1 = grid.yi[a1[Y]]; // grid.c.Y()
													// +
													// a1[1]*grid.step;
				final double z1 = grid.zi[a1[Z]]; // grid.c.Z()
													// +
													// a1[2]*grid.step;

				final double x2 = grid.xi[a2[X]]; // grid.c.X()
													// +
													// a2[0]*grid.step;
				final double y2 = grid.yi[a2[Y]]; // grid.c.Y()
													// +
													// a2[1]*grid.step;
				final double z2 = grid.zi[a2[Z]]; // grid.c.Z()
													// +
													// a2[2]*grid.step;

				// Коэффициенты плоскости конуса Ax + By +
				// Cz + D = 0
				final double A = ((y2 - y1) * (z3 - z1) - (z2 - z1) * (y3 - y1));
				final double B = ((x3 - x1) * (z2 - z1) - (x2 - x1) * (z3 - z1));
				final double C = ((x2 - x1) * (y3 - y1) - (x3 - x1) * (y2 - y1));
				final double D = -1.0 * (A * x1 + B * y1 + C * z1);

				// Коэффициенты плоскости (перпендикулярной
				// плоскости конуса) луча а3а1
				// строится по координатам двух точек: а1,
				// а3 и вектору нормали к плоскости
				// конуса n(A,B,C)
				final double A1 = (C * (y3 - y1) - B * (z3 - z1));
				final double B1 = (A * (z3 - z1) - C * (x3 - x1));
				final double C1 = (B * (x3 - x1) - A * (y3 - y1));
				final double D1 = -1.0 * (A1 * x1 + B1 * y1 + C1 * z1);
				final double S1 = Math.signum(A1 * x2 + B1 * y2 + C1 * z2 + D1);

				// Функция принадлежности точки конусу -
				// принадлежит плоскости а1а2а3 и лежит
				// между лучами а3а1, а3а2
				Predicate<int[]> check = p -> {
					final double x = grid.xi[p[X]]; // grid.c.X()
													// +
													// p[X]*grid.step;
					final double y = grid.yi[p[Y]]; // grid.c.Y()
													// +
													// p[Y]*grid.step;
					final double z = grid.zi[p[Z]]; // grid.c.Z()
													// +
													// p[Z]*grid.step;

					// Точка принадлежит плоскости конуса
					// если расстояние от нее до плоскости
					// меньше
					// или равно ошибке
					final double d = Math.abs((A * x + B * y + C * z + D) / Math.sqrt(A * A + B * B + C * C));
					if (d > error) {
						return false;
					}

					final double s1 = Math.signum(A1 * x + B1 * y + C1 * z + D1);
					if (s1 != S1 && s1 != 0) {
						return false;
					}

					return true;
				};

				// final Stack<int[]> temp = new Stack<>();
				// temp.add(a1);
				//
				// //Поиск всех точек принадлежащих
				// плоскости конуса пути от граничной точки
				// а1
				// до внутренней точки а2
				// while(temp.size() > 0){
				// final int[] p = temp.pop(); //Текущая
				// точка
				//
				// this.neighbors[p[X]][p[Y]][p[Z]]
				// .stream()
				// .filter(t -> !isConvex[t[X]][t[Y]][t[Z]])
				// .filter(check)
				// .forEach(t -> {
				// temp.add(t);
				// isConvex[t[X]][t[Y]][t[Z]] = true;
				// });
				//
				//
				// };

				// Поиск всех точек принадлежащих плоскости
				// конуса пути от граничной точки а1 до
				// внутренней точки а2
				final Set<int[]> path = new HashSet<>();
				path.add(a1);

				final Set<int[]> nextPath = new HashSet<>();
				nextPath.add(a1);

				do {
					final Set<int[]> temp = new HashSet<>();
					nextPath.stream().flatMap(p -> this.neighbors[p[X]][p[Y]][p[Z]].stream())
							.filter(p -> !path.contains(p)).filter(check).forEach(p -> {
								temp.add(p);
								isConvex[p[X]][p[Y]][p[Z]] = true;
							});

					nextPath.clear();
					nextPath.addAll(temp);
					path.addAll(temp);
				} while (!nextPath.isEmpty());

				if (!isConvex[a2[X]][a2[Y]][a2[Z]]) {
					if (!excludeOut.containsKey(a2)) {
						excludeOut.put(a2, new HashSet<>());
					}

					excludeOut.get(a2).addAll(path);
				}
			});

			for (int[] key : excludeOut.keySet()) {
				if (!exclude.containsKey(key)) {
					exclude.put(key, new HashSet<>());
				}
				exclude.get(key).addAll(excludeOut.get(key));
			}
		});

		// Вычисление площади
		double delta = 0;
		int n = 0;
		for (int[] ai : exclude.keySet()) {
			// System.out.println("n=" + exclude.get(ai).size());
			if (n == 0) {
				figures.add(Draw.get(exclude.get(ai).stream()
						.map(p -> new Coord3d((float) grid.xi[p[0]], (float) grid.yi[p[1]], (float) grid.zi[p[2]]))
						.collect(Collectors.toList()), Color.GREEN));

				figures.add(Draw.get(
						new Coord3d[] {
								new Coord3d((float) grid.xi[ai[0]], (float) grid.yi[ai[1]], (float) grid.zi[ai[2]]) },
						Color.BLACK));
			}
			n++;

			// final ArrayList<int[]> inn = new ArrayList<int[]>();
			// inn.add(ai);
			// figures.add(new Figure(Color.MAGENTA, inn, grid));

			double se = 0;

			for (int[] v : exclude.get(ai)) {
				se += voxels[v[X]][v[Y]][v[Z]].getS().apply(grid.step);
			}

			delta += se * voxels[ai[X]][ai[Y]][ai[Z]].getS().apply(grid.step);
			// sv -= se*voxels[ai[X]][ai[Y]][ai[Z]].getS().apply(grid.step);
		}
		sv = s * s - delta;

		System.out.println("sv=" + sv);
		System.out.println("delta=" + delta);
		System.out.println("keySet=" + exclude.keySet().size());

		// for(int[] key : exclude.keySet()) {
		// final Set<int[]> set = exclude.get(key);
		// System.out.println("size=" + set.size() + "; distinct=" +
		// set.stream().filter(p ->
		// !(inners.contains(p)||outers.contains(p))&&!points[p[0]][p[1]][p[2]]).distinct().count());
		// }

		// AbstractDrawable draw = figures.get(0);
		// figures.clear();
		// figures.add(draw);
	}

	public void demiconvex(double[][] pointsC) {

		double[] x3 = new double[pointsC.length], y3 = new double[pointsC.length], z3 = new double[pointsC.length];
		for (int i = 0; i < pointsC.length; i++) {
			x3[i] = pointsC[i][0];
			y3[i] = pointsC[i][1];
			z3[i] = pointsC[i][2];
		}
		if (outers == null) {
			analizePoints();
		}

		sv = s * s;
		final Map<int[], Set<int[]>> exclude = new HashMap<>();

		final double error = Math.sqrt(3) * grid.step / 2;
		// System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism",
		// "1");
		System.out.println("in");
		outers.parallelStream().forEach(a1 -> {
			// Множество уже найденных выпуклых точек относительно
			// граничной точки а1
			final boolean[][][] isConvex = new boolean[grid.count[X] - 1][grid.count[Y] - 1][grid.count[Z] - 1];
			isConvex[a1[X]][a1[Y]][a1[Z]] = true; // Граничная точка
													// выпукла
													// относительно
													// себя

			final Map<int[], Set<int[]>> excludeOut = new HashMap<>();

			inners.stream().forEach(a2 -> {
				if (isConvex[a2[X]][a2[Y]][a2[Z]]) {
					// System.out.println("is_convex");
					return;
				}
				final double[] A = new double[x3.length], B = new double[x3.length], C = new double[x3.length],
						D = new double[x3.length], A1 = new double[x3.length], B1 = new double[x3.length],
						C1 = new double[x3.length], D1 = new double[x3.length], S1 = new double[x3.length];
				final double x1 = grid.xi[a1[X]]; // grid.c.X()
													// +
													// a1[0]*grid.step;
				final double y1 = grid.yi[a1[Y]]; // grid.c.Y()
													// +
													// a1[1]*grid.step;
				final double z1 = grid.zi[a1[Z]]; // grid.c.Z()
													// +
													// a1[2]*grid.step;

				final double x2 = grid.xi[a2[X]]; // grid.c.X()
													// +
													// a2[0]*grid.step;
				final double y2 = grid.yi[a2[Y]]; // grid.c.Y()
													// +
													// a2[1]*grid.step;
				final double z2 = grid.zi[a2[Z]]; // grid.c.Z()
													// +
													// a2[2]*grid.step;
				for (int i = 0; i < x3.length; i++) {
					// Коэффициенты плоскости конуса Ax + By
					// + Cz + D = 0
					A[i] = ((y2 - y1) * (z3[i] - z1) - (z2 - z1) * (y3[i] - y1));
					B[i] = ((x3[i] - x1) * (z2 - z1) - (x2 - x1) * (z3[i] - z1));
					C[i] = ((x2 - x1) * (y3[i] - y1) - (x3[i] - x1) * (y2 - y1));
					D[i] = -1.0 * (A[i] * x1 + B[i] * y1 + C[i] * z1);

					// Коэффициенты плоскости
					// (перпендикулярной плоскости конуса)
					// луча а3а1
					// строится по координатам двух точек:
					// а1, а3 и вектору нормали к плоскости
					// конуса n(A,B,C)
					A1[i] = (C[i] * (y3[i] - y1) - B[i] * (z3[i] - z1));
					B1[i] = (A[i] * (z3[i] - z1) - C[i] * (x3[i] - x1));
					C1[i] = (B[i] * (x3[i] - x1) - A[i] * (y3[i] - y1));
					D1[i] = -1.0 * (A1[i] * x1 + B1[i] * y1 + C1[i] * z1);
					S1[i] = Math.signum(A1[i] * x2 + B1[i] * y2 + C1[i] * z2 + D1[i]);
				}

				Set<int[]> pathfinal = new HashSet<>();

				for (int i = 0; i < x3.length; i++) {

					final Set<int[]> path = new HashSet<>();
					path.add(a1);

					final Set<int[]> nextPath = new HashSet<>();
					nextPath.add(a1);

					final double Alocal = A[i], Blocal = B[i], Clocal = C[i], Dlocal = D[i], A1local = A1[i],
							B1local = B1[i], C1local = C1[i], D1local = D1[i], S1local = S1[i];

					// Функция принадлежности точки конусу -
					// принадлежит плоскости а1а2а3 и лежит
					// между лучами а3а1, а3а2
					Predicate<int[]> check = p -> {
						final double x = grid.xi[p[X]]; // grid.c.X()
														// +
														// p[X]*grid.step;
						final double y = grid.yi[p[Y]]; // grid.c.Y()
														// +
														// p[Y]*grid.step;
						final double z = grid.zi[p[Z]]; // grid.c.Z()
														// +
														// p[Z]*grid.step;

						// Точка принадлежит плоскости
						// конуса если расстояние от нее до
						// плоскости меньше
						// или равно ошибке
						final double d = Math.abs((Alocal * x + Blocal * y + Clocal * z + Dlocal)
								/ Math.sqrt(Alocal * Alocal + Blocal * Blocal + Clocal * Clocal));
						if (d > error) {
							return false;
						}

						final double s1 = Math.signum(A1local * x + B1local * y + C1local * z + D1local);
						if (s1 != S1local && s1 != 0) {
							return false;
						}

						return true;
					};

					// Поиск всех точек принадлежащих
					// плоскости конуса пути от граничной
					// точки а1 до
					// внутренней точки а2

					do {
						final Set<int[]> temp = new HashSet<>();
						nextPath.stream().flatMap(p -> this.neighbors[p[X]][p[Y]][p[Z]].stream())
								.filter(p -> !path.contains(p)).filter(check).forEach(p -> {
									temp.add(p);
									isConvex[p[X]][p[Y]][p[Z]] = true;
								});
						nextPath.clear();
						nextPath.addAll(temp);
						path.addAll(temp);
					} while (!nextPath.isEmpty());
					pathfinal = path;
					if (isConvex[a2[X]][a2[Y]][a2[Z]]) {
						break;
					} else {
						for (int[] j : path) {
							isConvex[j[X]][j[Y]][j[Z]] = false;
						}
					}
					isConvex[a1[X]][a1[Y]][a1[Z]] = true;
				}
				if (!isConvex[a2[X]][a2[Y]][a2[Z]]) {
					if (!excludeOut.containsKey(a2)) {
						excludeOut.put(a2, new HashSet<>());
					}

					excludeOut.get(a2).addAll(pathfinal);
				}
			});

			for (int[] key : excludeOut.keySet()) {
				if (!exclude.containsKey(key)) {
					exclude.put(key, new HashSet<>());
				}
				exclude.get(key).addAll(excludeOut.get(key));
			}
		});

		// Вычисление площади
		double delta = 0;
		int n = 0;
		for (int[] ai : exclude.keySet()) {
			// System.out.println("n=" + exclude.get(ai).size());
			if (n == 0) {
				figures.add(Draw.get(exclude.get(ai).stream()
						.map(p -> new Coord3d((float) grid.xi[p[0]], (float) grid.yi[p[1]], (float) grid.zi[p[2]]))
						.collect(Collectors.toList()), Color.GREEN));

				figures.add(Draw.get(
						new Coord3d[] {
								new Coord3d((float) grid.xi[ai[0]], (float) grid.yi[ai[1]], (float) grid.zi[ai[2]]) },
						Color.BLACK));
			}
			n++;

			// final ArrayList<int[]> inn = new ArrayList<int[]>();
			// inn.add(ai);
			// figures.add(new Figure(Color.MAGENTA, inn, grid));

			double se = 0;

			for (int[] v : exclude.get(ai)) {
				se += voxels[v[X]][v[Y]][v[Z]].getS().apply(grid.step);
			}

			delta += se * voxels[ai[X]][ai[Y]][ai[Z]].getS().apply(grid.step);
			// sv -= se*voxels[ai[X]][ai[Y]][ai[Z]].getS().apply(grid.step);
		}
		sv = s * s - delta;

		System.out.println("sv=" + sv);
		System.out.println("delta=" + delta);
		System.out.println("keySet=" + exclude.keySet().size());

		// for(int[] key : exclude.keySet()) {
		// final Set<int[]> set = exclude.get(key);
		// System.out.println("size=" + set.size() + "; distinct=" +
		// set.stream().filter(p ->
		// !(inners.contains(p)||outers.contains(p))&&!points[p[0]][p[1]][p[2]]).distinct().count());
		// }

		// AbstractDrawable draw = figures.get(0);
		// figures.clear();
		// figures.add(draw);
	}

	private Map<int[], Set<int[]>> getExcludePoints(double[] pointsC) {
		double x3 = pointsC[0], y3 = pointsC[1], z3 = pointsC[2];
		final double error = Math.sqrt(3) * grid.step / 2;
		final Map<int[], Set<int[]>> exclude = new HashMap<>();
		outers.parallelStream().forEach(a1 -> {
			// Множество уже найденных выпуклых точек относительно
			// граничной точки а1
			final boolean[][][] isConvex = new boolean[grid.count[X] - 1][grid.count[Y] - 1][grid.count[Z] - 1];
			isConvex[a1[X]][a1[Y]][a1[Z]] = true; // Граничная точка
													// выпукла
													// относительно
													// себя

			final Map<int[], Set<int[]>> excludeOut = new HashMap<>();

			inners.stream().forEach(a2 -> {
				if (isConvex[a2[X]][a2[Y]][a2[Z]]) {
					// System.out.println("is_convex");
					return;
				}

				final double x1 = grid.xi[a1[X]]; // grid.c.X()
													// +
													// a1[0]*grid.step;
				final double y1 = grid.yi[a1[Y]]; // grid.c.Y()
													// +
													// a1[1]*grid.step;
				final double z1 = grid.zi[a1[Z]]; // grid.c.Z()
													// +
													// a1[2]*grid.step;

				final double x2 = grid.xi[a2[X]]; // grid.c.X()
													// +
													// a2[0]*grid.step;
				final double y2 = grid.yi[a2[Y]]; // grid.c.Y()
													// +
													// a2[1]*grid.step;
				final double z2 = grid.zi[a2[Z]]; // grid.c.Z()
													// +
													// a2[2]*grid.step;

				// Коэффициенты плоскости конуса Ax + By +
				// Cz + D = 0
				final double A = ((y2 - y1) * (z3 - z1) - (z2 - z1) * (y3 - y1));
				final double B = ((x3 - x1) * (z2 - z1) - (x2 - x1) * (z3 - z1));
				final double C = ((x2 - x1) * (y3 - y1) - (x3 - x1) * (y2 - y1));
				final double D = -1.0 * (A * x1 + B * y1 + C * z1);

				// Коэффициенты плоскости (перпендикулярной
				// плоскости конуса) луча а3а1
				// строится по координатам двух точек: а1,
				// а3 и вектору нормали к плоскости
				// конуса n(A,B,C)
				final double A1 = (C * (y3 - y1) - B * (z3 - z1));
				final double B1 = (A * (z3 - z1) - C * (x3 - x1));
				final double C1 = (B * (x3 - x1) - A * (y3 - y1));
				final double D1 = -1.0 * (A1 * x1 + B1 * y1 + C1 * z1);
				final double S1 = Math.signum(A1 * x2 + B1 * y2 + C1 * z2 + D1);

				// Функция принадлежности точки конусу -
				// принадлежит плоскости а1а2а3 и лежит
				// между лучами а3а1, а3а2
				Predicate<int[]> check = p -> {
					final double x = grid.xi[p[X]]; // grid.c.X()
													// +
													// p[X]*grid.step;
					final double y = grid.yi[p[Y]]; // grid.c.Y()
													// +
													// p[Y]*grid.step;
					final double z = grid.zi[p[Z]]; // grid.c.Z()
													// +
													// p[Z]*grid.step;

					// Точка принадлежит плоскости конуса
					// если расстояние от нее до плоскости
					// меньше
					// или равно ошибке
					final double d = Math.abs((A * x + B * y + C * z + D) / Math.sqrt(A * A + B * B + C * C));
					if (d > error) {
						return false;
					}

					final double s1 = Math.signum(A1 * x + B1 * y + C1 * z + D1);
					if (s1 != S1 && s1 != 0) {
						return false;
					}

					return true;
				};

				// Поиск всех точек принадлежащих плоскости
				// конуса пути от граничной точки а1 до
				// внутренней точки а2
				final Set<int[]> path = new HashSet<>();
				path.add(a1);

				final Set<int[]> nextPath = new HashSet<>();
				nextPath.add(a1);

				do {
					final Set<int[]> temp = new HashSet<>();
					nextPath.stream().flatMap(p -> this.neighbors[p[X]][p[Y]][p[Z]].stream())
							.filter(p -> !path.contains(p)).filter(check).forEach(p -> {
								temp.add(p);
								isConvex[p[X]][p[Y]][p[Z]] = true;
							});

					nextPath.clear();
					nextPath.addAll(temp);
					path.addAll(temp);
				} while (!nextPath.isEmpty());

				if (!isConvex[a2[X]][a2[Y]][a2[Z]]) {
					if (!excludeOut.containsKey(a2)) {
						excludeOut.put(a2, new HashSet<>());
					}

					excludeOut.get(a2).addAll(path);
				}
			});

			for (int[] key : excludeOut.keySet()) {
				if (!exclude.containsKey(key)) {
					exclude.put(key, new HashSet<>());
				}
				exclude.get(key).addAll(excludeOut.get(key));
			}
		});
		return exclude;
	}

	public void fractioconvex(double[][] pointsC) {

		double x3 = pointsC[0][0], y3 = pointsC[0][1], z3 = pointsC[0][2];
		if (outers == null) {
			analizePoints();
		}

		sv = s * s;
		// final Map<int[], Set<int[]>> exclude = new HashMap<>();

		System.out.println("in");
		// Нахождение точек для каждой из точек С
		Map<int[], Set<int[]>>[] excludeForPoints = new HashMap[pointsC.length];
		// getExcludePoints(pointsC[0])
		final Map<int[], Set<int[]>> exclude = new HashMap<>();
		for (int i = 0; i < pointsC.length; i++) {
			excludeForPoints[i] = getExcludePoints(pointsC[i]);
		}
		boolean[] isEverywhere = new boolean[pointsC.length];
		// For all sets of excluded points
		// for (int i = 0; i < pointsC.length; i++) {
		// For all their outer points
		for (int[] key : excludeForPoints[0].keySet()) {
			// For all inner points
			for (int[] v : excludeForPoints[0].get(key)) {
				for (int b = 0; b < pointsC.length; b++) {
					isEverywhere[b] = false;
				}
				// Check for all other sets of excluded points
				for (int j = 0; j < pointsC.length; j++) {
					if (j != 0) {
						// For all their outer points
						for (int[] ai : excludeForPoints[j].keySet()) {
							// if all of them contain this one
							if (ai.equals(key)) {
							for (int[] c : excludeForPoints[j].get(ai)) {
								// if all of them contain this one
								
									if ((v[X] == c[X]) & (v[Y] == c[Y]) & (v[Z] == c[Z])) {
										// we will put it in the resulting set
										isEverywhere[j] = true;
										break;
									}
								}
								// If we have already found one, then go next
								if (isEverywhere[j]) {
									break;
								}
							}
						}
					} else {
						isEverywhere[j] = true;
					}
				}
				boolean every = true;
				for (int b = 0; b < isEverywhere.length; b++) {
					if (!isEverywhere[b]) {
						every = false;
						break;
					}
				}
				if (every) {
					if (!exclude.containsKey(key)) {
						exclude.put(key, new HashSet<>());
					}

					final Set<int[]> temp = new HashSet<>();
					temp.add(v);
					/*
					 * for (int[] key1 : exclude.keySet()) { if (exclude.get(key1).contains(temp)) {
					 * every = false; } }
					 */
					// if (every) {
					exclude.get(key).addAll(temp);
					// }
				}
			}
		}
		// }
		int i = exclude.size();

		

		if (excludeForPoints.length > 1) {
			i = excludeForPoints[0].size();

			i = excludeForPoints[1].size();
			
			s1 = convexSize(excludeForPoints[0]);

			s2 = convexSize(excludeForPoints[1]);
		}
		convexSize(exclude);
	}

	private double convexSize(final Map<int[], Set<int[]>> exclude) {
		// Вычисление площади
		double delta = 0;
		int n = 0;
		for (int[] ai : exclude.keySet()) {
			if (n == 0) {
				figures.add(Draw.get(exclude.get(ai).stream()
						.map(p -> new Coord3d((float) grid.xi[p[0]], (float) grid.yi[p[1]], (float) grid.zi[p[2]]))
						.collect(Collectors.toList()), Color.GREEN));

				figures.add(Draw.get(
						new Coord3d[] {
								new Coord3d((float) grid.xi[ai[0]], (float) grid.yi[ai[1]], (float) grid.zi[ai[2]]) },
						Color.BLACK));
			}
			n++;

			double se = 0;

			for (int[] v : exclude.get(ai)) {
				se += voxels[v[X]][v[Y]][v[Z]].getS().apply(grid.step);
			}

			delta += se * voxels[ai[X]][ai[Y]][ai[Z]].getS().apply(grid.step);
		}
		sv = s * s - delta;
		System.out.println("sv=" + sv);
		System.out.println("delta=" + delta);
		System.out.println("keySet=" + exclude.keySet().size());
		return sv;
	}

	public String[] getData() {
		String[] data = new String[5];
		data[0] = String.valueOf(s * s);
		data[1] = String.valueOf(sv);
		data[2] = String.valueOf(s1);
		data[3] = String.valueOf(s2);
		data[4] = String.valueOf(100 * sv / (s * s));
		return data;
	}

	public List<Polygon> getPolygons() {
		final List<Polygon> polygons = new ArrayList<>();

		polygons.addAll(getInnerPolygons());
		polygons.addAll(getOutersPolygons());

		return polygons;
	}

	public List<Polygon> getInnerPolygons() {
		final List<Polygon> polygons = new ArrayList<>();

		if (inners == null) {
			analizePoints();
		}

		for (int[] p : inners) {
			polygons.addAll(
					voxels[p[X]][p[Y]][p[Z]].getPolygons(grid.step, grid.xi[p[X]], grid.xi[p[Y]], grid.xi[p[Z]]));
		}
		return polygons;
	}

	public List<Polygon> getOutersPolygons() {
		final List<Polygon> polygons = new ArrayList<>();

		if (outers == null) {
			analizePoints();
		}

		for (int[] p : outers) {
			polygons.addAll(
					voxels[p[X]][p[Y]][p[Z]].getPolygons(grid.step, grid.xi[p[X]], grid.xi[p[Y]], grid.xi[p[Z]]));
		}
		return polygons;
	}

	// public void convex(double x3, double y3, double z3) {
	// if(outers == null) {
	// analizePoints();
	// }
	//
	// for(int[] a1 : inners) {
	// for(int[] a2 : outers) {
	// double x1 = grid.c.X() + a1[0]*grid.step;
	// double y1 = grid.c.Y() + a1[1]*grid.step;
	// double z1 = grid.c.Z() + a1[2]*grid.step;
	//
	// double x2 = grid.c.X() + a2[0]*grid.step;
	// double y2 = grid.c.Y() + a2[1]*grid.step;
	// double z2 = grid.c.Z() + a2[2]*grid.step;
	//
	// Function<Point, Double> f = p ->{
	// return (p.X() - x1)*((y2 - y1)*(z3 - z1) - (z2 - z1)*(y3 - y1)) -
	// (p.Y() - y1)*((x2 - x1)*(z3 - z1) - (x3 - x1)*(z2 - z1)) +
	// (p.Z() - z1)*((x2 -x1)*(y3 - y1) - (x3 - x1)*(y2 - y1));
	// };
	// final List<Point> ps = intersectPoints(f);
	// }
	// }
	// }
	//
	// private List<Point> intersectPoints(Function<Point, Double> f) {
	// //final Surface s = new Surface(grid, f);
	// final List<Point> res = new ArrayList<>();
	//
	// // for(int iX = 0; iX < grid.count[0] - 1; iX++) {
	// // for(int iY = 0; iY < grid.count[1] - 1; iY++) {
	// // for(int iZ = 0; iZ < grid.count[2] - 1; iZ++) {
	// // if(points[iX][iY][iZ] && s.points[iX][iY][iZ]) {
	// // res.add(new Point(grid.c.X() + iX*grid.step, grid.c.Y() +
	// iY*grid.step,
	// grid.c.Z() + iZ*grid.step));
	// // }
	// // }
	// // }
	// // }
	//
	// return res;
	// }

	// public void test(JFrame frame) {
	// List<AbstractDrawable> draws = new ArrayList<>();
	//
	// if(outers == null) {
	// analizePoints();
	// }
	//
	// for(int[] p : outers) {
	// final List<int[]> n = getNeighbors(p[0], p[1], p[2]);
	// if(n.size() < 2) {
	//
	//
	// List<Coord3d> l0 = new ArrayList<>();
	// l0.add(new Coord3d((float)grid.xi[p[0]], (float)grid.yi[p[1]],
	// (float)grid.zi[p[2]]));
	// draws.add(Draw.get(l0, Color.RED));
	// }
	// }
	//
	// if(draws.size() > 0) {
	// try {
	//
	// View3D dialog = new View3D(frame, draws.to);
	// dialog.setVisible(true);
	// }catch(Exception e) {
	// Utils.error(e.getMessage());
	// }
	// }
	// }
}
