package ru.vvk.convex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.jzy3d.plot3d.primitives.Polygon;

public class Voxel {

    /**
     * Номер строки в массиве содержащей точки по оси X
     */
    public static final int X = 0;
    /**
     * Номер строки в массиве содержащей точки по оси Y
     */
    public static final int Y = 1;
    /**
     * Номер строки в массиве содержащей точки по оси Z
     */
    private static final int Z = 2;
    /**
     * Сетка знаков функции сдвинута относительно сетки точек на полшага назад по
     * всем осям Число точек по оси на одну больше чем в сетке grid
     */
    private int[] signs;
    /**
     * Интерфейс вычисления произвольной функции
     */
    private Function<Double, Double> s;
    /**
     * Стороны вокселя
     */
    private int[][][] sides;

    /**
     * Матрица поворотов куба
     * каждая строка содержит перекодировку вершин куба при повороте на 90 градусов вдоль одной из осей
     * индекс столбца - номер исходной вершины куба - значение по этому индексу - номер вершины после поворота
     */
    private static final int[][] rotates = {
            {4, 5, 0, 1, 6, 7, 2, 3}, // поворот по оси X на 90
            {1, 5, 3, 7, 0, 4, 2, 6}, // поворот по оси Y на 90
            {1, 3, 0, 2, 5, 7, 4, 6}  // поворот по оси Z на 90
    };
    /**
     * Первый индекс - номер трансформации (всего 3 оси для каждой оси возможно 4 поворота на 90 градусов - итого 4^3=64 трансформации)
     * второй индекс - номер вершины куба (всего 8 вершин)
     */
    private static final int[][] transforms = new int[64][8];
	/**
	 * HashMap вокселей.
	 */
	private static final Map<String, Voxel> voxels = new HashMap<String, Voxel>();

	/**
	 * Поворот вершин куба относительно оси axis count раз
	 * @param vector - вершины куба
	 * @param axis - ось
	 * @param count - число повротов
	 * @return - Новое положениевершин
	 */
	private static int[] rotate(int[] vector, int axis, int count) {
        int[] v = Arrays.copyOf(vector, vector.length);
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < 8; j++) {
                v[j] = rotates[axis][v[j]];
            }
        }
        return v;
    }



    /**
     * Для приминения алгоритма марширующих кубов, инициализируются частные случаи пересечения плоксоти и вокселя.
     */
    static {
        //Инициализация списка трансформаций
        int num = 0;
        for (int xCount = 0; xCount < 4; xCount++) {
            for (int yCount = 0; yCount < 4; yCount++) {
                for (int zCount = 0; zCount < 4; zCount++) {
                    int[] t = {0, 1, 2, 3, 4, 5, 6, 7}; //Начальное - тождественное преобразование
                    t = rotate(t, X, xCount);
                    t = rotate(t, Y, yCount);
                    t = rotate(t, Z, zCount);
                    transforms[num++] = t;
                }
            }
        }
        //Инициализация типов вокселей
        addCase(new int[]{0, 0, 0, 0, 0, 0, 0, 0}, h -> 0d, new int[][][]{});
        addCase(new int[]{1, 0, 0, 0, 0, 0, 0, 0}, h -> h * h * Math.sqrt(3) / 8d, new int[][][]{{{0, 1}, {0, 2}, {0, 4}}});
        addCase(new int[]{1, 1, 0, 0, 0, 0, 0, 0}, h -> h * h / Math.sqrt(2), new int[][][]{{{0, 4}, {1, 5}, {1, 3}, {0, 2}}});
        addCase(new int[]{1, 0, 0, 0, 0, 1, 0, 0}, h -> h * h * Math.sqrt(3) / 4d, new int[][][]{{{0, 1}, {0, 2}, {0, 4}}, {{1, 5}, {4, 5}, {5, 7}}});
        addCase(new int[]{1, 0, 0, 0, 0, 0, 0, 1}, h -> h * h * Math.sqrt(3) / 4d, new int[][][]{{{0, 1}, {0, 2}, {0, 4}}, {{3, 7}, {5, 7}, {6, 7}}});
        addCase(new int[]{0, 1, 1, 1, 0, 0, 0, 0}, h -> {
            double p = (Math.sqrt(0.5) + 1 + Math.sqrt(3 / 2d)) * h / 2d;
            double s1 = Math.sqrt(p * (p - h / Math.sqrt(2)) * (p - h) * (p - h * Math.sqrt(3 / 2d)));

            p = (Math.sqrt(3 / 2d) + Math.sqrt(2) + Math.sqrt(0.5)) * h / 2d;
            double s2 = Math.sqrt(p * (p - h * Math.sqrt(3 / 2d)) * (p - Math.sqrt(2) * h) * (p - Math.sqrt(0.5) * h));

            p = (Math.sqrt(2) + 1 + Math.sqrt(0.5)) * h / 2d;
            double s3 = Math.sqrt(p * (p - Math.sqrt(2) * h) * (p - h) * (p - Math.sqrt(0.5) * h));

            return s1 + s2 + s3;
        }, new int[][][]{{{0, 2}, {0, 1}, {2, 6}}, {{2, 6}, {0, 1}, {1, 5}}, {{2, 6}, {1, 5}, {3, 7}}});

        addCase(new int[]{1, 1, 0, 0, 0, 0, 0, 1}, h -> h * h * (Math.sqrt(0.5) + Math.sqrt(3) / 8d), new int[][][]{{{0, 4}, {1, 5}, {1, 3}, {0, 2}}, {{5, 7}, {6, 7}, {3, 7}}});
        addCase(new int[]{0, 1, 0, 0, 1, 0, 0, 1}, h -> h * h * 3 * Math.sqrt(3) / 8d, new int[][][]{{{0, 4}, {4, 5}, {4, 6}}, {{0, 1}, {1, 5}, {1, 3}}, {{3, 7}, {5, 7}, {6, 7}}});
        addCase(new int[]{1, 1, 1, 1, 0, 0, 0, 0}, h -> h * h / Math.sqrt(2), new int[][][]{{{0, 4}, {1, 5}, {3, 7}, {2, 6}}});
        addCase(new int[]{1, 0, 1, 1, 0, 0, 1, 0}, h -> {
            double p = h * (Math.sqrt(3 / 2d) + Math.sqrt(2)) / 2d;
            double s1 = Math.sqrt(p * (p - h * Math.sqrt(0.5)) * (p - h * Math.sqrt(3 / 2d)) * (p - h * Math.sqrt(0.5)));

            p = h * (Math.sqrt(3 / 2d) + Math.sqrt(2) + Math.sqrt(0.5)) / 2d;
            double s2 = Math.sqrt(p * (p - h * Math.sqrt(3 / 2d)) * (p - h * Math.sqrt(2)) * (p - h * Math.sqrt(0.5)));

            p = h * (Math.sqrt(3 / 2d) + 1 + Math.sqrt(0.5)) / 2d;
            double s4 = Math.sqrt(p * (p - h * Math.sqrt(3 / 2d)) * (p - h * Math.sqrt(0.5)) * (p - h));

            return s1 + 2 * s2 + s4;
        }, new int[][][]{{{0, 4}, {4, 6}, {6, 7}}, {{6, 7}, {0, 4}, {0, 1}}, {{0, 1}, {3, 7}, {6, 7}}, {{3, 7}, {0, 1}, {1, 3}}});

        addCase(new int[]{1, 0, 0, 1, 1, 0, 0, 1}, h -> h * h * Math.sqrt(2), new int[][][]{{{0, 1}, {4, 5}, {4, 6}, {0, 2}}, {{1, 3}, {5, 7}, {6, 7}, {2, 3}}});
        addCase(new int[]{1, 0, 1, 1, 0, 0, 0, 1}, h -> {
            double p = h * (Math.sqrt(3 / 2d) + 1 + Math.sqrt(0.5)) / 2d;
            double s1 = Math.sqrt(p * (p - h * Math.sqrt(0.5)) * (p - h * Math.sqrt(3 / 2d)) * (p - h));

            p = 3 * h * Math.sqrt(2) / 2d;
            double s3 = Math.sqrt(p * Math.pow(p - Math.sqrt(2) * h, 3));

            p = h * (Math.sqrt(3 / 2d) + 1 + Math.sqrt(0.5)) / 2d;
            double s4 = Math.sqrt(p * (p - h * Math.sqrt(0.5)) * (p - h * Math.sqrt(3 / 2d)) * (p - h));

            return s1 + h * h * Math.sqrt(3) / 8d + s3 + s4;
        }, new int[][][]{{{0, 4}, {0, 1}, {2, 6}}, {{2, 6}, {6, 7}, {5, 7}}, {{5, 7}, {2, 6}, {0, 1}}, {{0, 1}, {1, 3}, {5, 7}}});

        addCase(new int[]{0, 1, 1, 1, 1, 0, 0, 0}, h -> {

            double p = h * (Math.sqrt(3 / 2d) + Math.sqrt(2)) / 2d;
            double s2 = Math.sqrt(p * (p - h * Math.sqrt(0.5)) * (p - h * Math.sqrt(3 / 2d)) * (p - h * Math.sqrt(0.5)));

            p = h * (Math.sqrt(3 / 2d) + Math.sqrt(2) + Math.sqrt(0.5)) / 2d;
            double s3 = Math.sqrt(p * (p - h * Math.sqrt(0.5)) * (p - h * Math.sqrt(3 / 2d)) * (p - h * Math.sqrt(2)));

            p = h * (Math.sqrt(2) + 2) / 2d;
            double s4 = Math.sqrt(p * (p - h * Math.sqrt(2)) * (p - h) * (p - h));

            return h * h * Math.sqrt(3) / 8d + s2 + s3 + s4;
        }, new int[][][]{{{0, 4}, {4, 6}, {4, 5}}, {{0, 1}, {0, 2}, {1, 5}}, {{0, 2}, {1, 5}, {2, 6}}, {{2, 6}, {1, 5}, {3, 7}}});

        addCase(new int[]{1, 0, 0, 1, 0, 1, 1, 0}, h -> h * h * Math.sqrt(3) / 2d, new int[][][]{{{0, 1}, {0, 2}, {0, 4}}, {{5, 1}, {5, 2}, {5, 7}}, {{3, 1}, {3, 2}, {3, 7}}, {{6, 2}, {6, 4}, {6, 7}}});

        addCase(new int[]{0, 1, 1, 1, 0, 0, 1, 0}, h -> {
            double p = h * (Math.sqrt(3 / 2d) + Math.sqrt(0.5) + 1) / 2d;
            double s1 = Math.sqrt(p * (p - h * Math.sqrt(0.5)) * (p - h * Math.sqrt(3 / 2d)) * (p - h));

            p = h * (Math.sqrt(3 / 2d) + Math.sqrt(0.5) + 1) / 2d;
            double s3 = Math.sqrt(p * (p - h * Math.sqrt(0.5)) * (p - h * Math.sqrt(3 / 2d)) * (p - h));

            p = h * (Math.sqrt(3 / 2d) + Math.sqrt(2)) / 2d;
            double s4 = Math.sqrt(p * (p - h * Math.sqrt(0.5)) * (p - h * Math.sqrt(3 / 2d)) * (p - h * Math.sqrt(0.5)));

            return s1 + h * h * 3d / (4d * Math.sqrt(2)) + s3 + s4;
        }, new int[][][]{{{0, 2}, {0, 1}, {4, 6}}, {{4, 6}, {0, 1}, {3, 7}}, {{0, 1}, {3, 7}, {1, 5}}, {{4, 6}, {6, 7}, {7, 3}}});

        //Оставшиеся варианты
        StringBuilder sb = new StringBuilder();
        for (int n = 1; n < 256; n++) {
            sb.append("[");
            for (int i = 7; i >= 0; i--) {
                if ((n & (1 << i)) != 0) {
                    sb.append("1, ");
                } else {
                    sb.append("0, ");
                }
            }
            sb.setLength(sb.length() - 2);
            sb.append("]");
            final String key = sb.toString();
            sb.setLength(0);
            if (!voxels.containsKey(key)) {
                System.out.println(key);
                break;
            }
        }

        System.out.println(voxels.size());
    }

    /**
     * Добавляется новый случай пересечения вокселя с поверхностью.
     *
     * @param signs - знаки вершин вокселя.
     * @param s     - площадь треугольника, который апроксимирует поверхность.
     * @param sides - перечень сторон вокселя, через которые проходит плоскость.
     */
    private static void addCase(int[] signs, Function<Double, Double> s, int[][][] sides) {
        if (!voxels.containsKey(Arrays.toString(signs))) {
            //Применяем всевозможные трансформации к исходному вокселю
            for (int iTransform = 0; iTransform < transforms.length; iTransform++) {
                final int[] t = transforms[iTransform];

                int[] newSigns = new int[8];
                for (int i = 0; i < 8; i++) {
                    newSigns[t[i]] = signs[i];
                }

                final String key = Arrays.toString(newSigns);
                if (!voxels.containsKey(key)) {
                    //Трансформация треугольников поверхностей
                    int[][][] newSides = new int[sides.length][][];
                    for (int iSide = 0; iSide < sides.length; iSide++) {
                        newSides[iSide] = new int[sides[iSide].length][2];
                        for (int iPoint = 0; iPoint < sides[iSide].length; iPoint++) {
                            newSides[iSide][iPoint][0] = t[sides[iSide][iPoint][0]];
                            newSides[iSide][iPoint][1] = t[sides[iSide][iPoint][1]];
                        }
                    }

                    voxels.put(key, new Voxel(newSigns, s, newSides));
                    //Инвертированный случай дает тотже результат
                    newSigns = invert(newSigns);
                    voxels.put(Arrays.toString(newSigns), new Voxel(newSigns, s, newSides));
                }
            }
        }
    }

    /**
     * Знаки инвертируется.
     *
     * @param signs - старые знаки.
     * @return - инвертированные знаки.
     */
    private static int[] invert(int[] signs) {
        final int[] newSigns = new int[signs.length];
        for (int i = 0; i < signs.length; i++) {
            newSigns[i] = 1 - signs[i];
        }
        return newSigns;
    }

    /**
     * Воксель инициализируется.
     *
     * @param signs - знаки вершин вокселя.
     * @param s     - площадь треугольника, который апроксимирует поверхность.
     * @param sides - перечень сторон вокселя, через которые проходит плоскость.
     */
    public Voxel(int[] signs, Function<Double, Double> s, int[][][] sides) {
        this.signs = signs;
        this.s = s;
        this.sides = sides;
    }

    /**
     * @return - Возвращает функцию.
     */
    public Function<Double, Double> getS() {
        return this.s;
    }

    /**
     * @param signs - массив знаков.
     * @return - возвращяется воксель.
     */
    public static Voxel getVoxel(int... signs) {
        final String key = Arrays.toString(signs).replaceAll("-1", "0");
        return voxels.get(key);
    }

    /**
     * @param l - шаг разбиения сетки.
     * @param x - координата x
     * @param y - координата y
     * @param z - координата z
     * @return - возвращается список полигонов.
     */
    public List<Polygon> getPolygons(double l, double x, double y, double z) {
        final double l2 = l / 2d;
        x -= l2;
        y -= l2;
        z -= l2;

        final List<Polygon> polygons = new ArrayList<>();

        for (int[][] plain : sides) {
            final Polygon p = new Polygon();

            for (int[] rebro : plain) {
                if (Arrays.equals(rebro, new int[]{0, 1}) || Arrays.equals(rebro, new int[]{1, 0})) {
                    p.add((float) (x + l2), (float) y, (float) z);
                } else if (Arrays.equals(rebro, new int[]{0, 2}) || Arrays.equals(rebro, new int[]{2, 0})) {
                    p.add((float) x, (float) (y + l2), (float) z);
                } else if (Arrays.equals(rebro, new int[]{0, 4}) || Arrays.equals(rebro, new int[]{4, 0})) {
                    p.add((float) x, (float) y, (float) (z + l2));
                } else if (Arrays.equals(rebro, new int[]{1, 5}) || Arrays.equals(rebro, new int[]{5, 1})) {
                    p.add((float) (x + l), (float) y, (float) (z + l2));
                } else if (Arrays.equals(rebro, new int[]{4, 5}) || Arrays.equals(rebro, new int[]{5, 4})) {
                    p.add((float) (x + l2), (float) y, (float) (z + l));
                } else if (Arrays.equals(rebro, new int[]{2, 6}) || Arrays.equals(rebro, new int[]{6, 2})) {
                    p.add((float) x, (float) (y + l), (float) (z + l2));
                } else if (Arrays.equals(rebro, new int[]{4, 6}) || Arrays.equals(rebro, new int[]{6, 4})) {
                    p.add((float) x, (float) (y + l2), (float) (z + l));
                } else if (Arrays.equals(rebro, new int[]{2, 3}) || Arrays.equals(rebro, new int[]{3, 2})) {
                    p.add((float) (x + l2), (float) (y + l), (float) z);
                } else if (Arrays.equals(rebro, new int[]{1, 3}) || Arrays.equals(rebro, new int[]{3, 1})) {
                    p.add((float) (x + l), (float) (y + l2), (float) z);
                } else if (Arrays.equals(rebro, new int[]{3, 7}) || Arrays.equals(rebro, new int[]{7, 3})) {
                    p.add((float) (x + l), (float) (y + l), (float) (z + l2));
                } else if (Arrays.equals(rebro, new int[]{6, 7}) || Arrays.equals(rebro, new int[]{7, 6})) {
                    p.add((float) (x + l2), (float) (y + l), (float) (z + l));
                } else if (Arrays.equals(rebro, new int[]{5, 7}) || Arrays.equals(rebro, new int[]{7, 5})) {
                    p.add((float) (x + l), (float) (y + l2), (float) (z + l));
                } else {
                    throw new RuntimeException("Не обрабатываемое ребро " + Arrays.toString(rebro));
                }
            }
            polygons.add(p);

        }


        return polygons;
    }

}
