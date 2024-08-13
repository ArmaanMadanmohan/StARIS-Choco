package org.chocosolver.examples.nqueen;
//import org.chocosolver.solver.Model;
//import org.chocosolver.solver.variables.IntVar;
//import org.chocosolver.solver.Solver;
//
//public class KnightsTour {
//
//    public static void main(String[] args) {
//        int N = 5; // Board size
//
//        // Create a new model
//        // Define decision variables
//        Model model = new Model("Knight's Tour");
//        IntVar[] row = model.intVarArray("row", N*N, 0, N-1);
//        IntVar[] col = model.intVarArray("col", N*N, 0, N-1);
//        IntVar[] helper_array = model.intVarArray("helper_array", N*N, 0, N*N-1);
//        IntVar[] A = model.intVarArray("A", N*N, 0, N*N-1);
//
//        // Create decision variables and add constraints
//        for (int i = 0; i < N*N; i++) {
//            IntVar rowTimesN = model.intScaleView(row[i], N);
//            IntVar sum = model.intVar("sum", 0, N*N);
//            model.sum(new IntVar[]{rowTimesN, col[i]}, "=", sum).post();
//            model.arithm(helper_array[i], "=", sum).post();
//        }
//
//// Add constraints
//        for (int i = 1; i < N*N; i++) {
//            IntVar rowDiff = model.intVar("rowDiff", -N, N);
//            model.arithm(rowDiff, "=", row[i], "-", row[i-1]).post();
//
//            IntVar colDiff = model.intVar("colDiff", -N, N);
//            model.arithm(colDiff, "=", col[i], "-", col[i-1]).post();
//
//            IntVar absRowDiff = model.abs(rowDiff);
//            IntVar absColDiff = model.abs(colDiff);
//
//            IntVar sum = model.intVar("sum", 0, 2*N);
//            model.sum(new IntVar[]{absRowDiff, absColDiff}, "=", sum).post();
//
//            model.arithm(sum, "=", 3).post();
//            model.arithm(row[i], "!=", row[i-1]).post();
//            model.arithm(col[i], "!=", col[i-1]).post();
//        }
//// Add constraint for closed game (optional)
//// model.arithm(row[N*N-1].sub(row[0]).abs().add(col[N*N-1].sub(col[0]).abs()), "=", 3).post();
//// model.arithm(row[N*N-1], "!=", row[0]).post();
//// model.arithm(col[N*N-1], "!=", col[0]).post();
//
//        // Create solver
//        Solver solver = model.getSolver();
//
//        // Find solutions
//        int solutionCount = 0;
//        while (solver.solve()) {
//            solutionCount++;
//            System.out.println("Solution " + solutionCount + ":");
//            for (int i = 0; i < N; i++) {
//                for (int j = 0; j < N; j++) {
//                    int index = i*N + j;
//                    System.out.print(A[index].getValue() + "\t");
//                }
//                System.out.println();
//            }
//            System.out.println();
//        }
//
//        System.out.println("Total solutions: " + solutionCount);
//    }
////}
//import org.chocosolver.solver.Model;
//import org.chocosolver.solver.Solution;
//import org.chocosolver.solver.Solver;
//import org.chocosolver.solver.variables.IntVar;
//
//public class KnightsTour {
//    public static void main(String[] args) {
//        int N = 5; // Size of the array
//        Model model = new Model("Building expressions on IntVar array");
//
//        // Create an array of IntVar
//        IntVar[] array = new IntVar[N];
//        for (int i = 0; i < N; i++) {
//            array[i] = VariableFactory.enumerated("array_" + i, 0, 10, model); // Example bounds: 0 to 10
//        }
//
//        // Example expression: sum of all elements in the array
//        IntVar sum = model.intVar("sum", 0, 100); // Example bounds: 0 to 100
//        model.sum(array, "=", sum).post();
//        model.getSolver().solve();
//
//        // Print the solutions
//        for (int i = 0; i < N; i++) {
//            System.out.println("array_" + i + ": " + array[i].getValue());
//        }
//        System.out.println("sum: " + sum.getValue());
//    }
////}
//import org.chocosolver.solver.Model;
//import org.chocosolver.solver.Solution;
//import org.chocosolver.solver.Solver;
//import org.chocosolver.solver.variables.IntVar;
//
//public class KnightsTour {
//    public static void main(String[] args) {
//        int n = 8;
//        int m = 6;
//
//        Model model = new Model("KnightsTour");
//        IntVar[] r = model.intVarArray("r", m, 1, n); //make this into a 2d matrix and then flatten it
//        IntVar[] c = model.intVarArray("c", m, 1, n);
//
//        IntVar[] expressions = new IntVar[m];
//        for (int i = 0; i < m; i++) {
//            expressions[i] = model.intVar("expr_" + i, 0, n * n);
//            model.arithm(c[i], "+", model.intScaleView(r[i], n), "=", expressions[i]).post();
//        }
//
//        model.allDifferent(expressions).post();
//
//        model.arithm(r[0], "=", 1).post();
//        model.arithm(c[0], "=", 1).post();
//        model.arithm(r[1], "=", 2).post();
//        model.arithm(c[1], "=", 3).post();
//
//        model.arithm(r[m - 1], "=", 3).post();
//        model.arithm(c[m - 1], "=", 2).post();
//
//        for (int i = 0; i < m - 1; i++) {
//            IntVar jr1 = model.intOffsetView(r[i], -2);
//            IntVar jr2 = model.intOffsetView(r[i], 2);
//            IntVar kr1 = model.intOffsetView(c[i], -1);
//            IntVar kr2 = model.intOffsetView(c[i], 1);
//
//            IntVar[] jValues = new IntVar[] {jr1, jr2};
//            IntVar[] kValues = new IntVar[] {kr1, kr2};
//
//            for (IntVar j : jValues) {
//                for (IntVar k : kValues) {
//                    model.ifThen(
//                            model.arithm(r[i + 1], "=", model.intOffsetView(r[i],j)),
//                            model.arithm(c[i + 1], "=", model.intOffsetView(c[i], getk))
//                    );
//                    model.ifThen(
//                            model.arithm(r[i + 1], "=", model.intOffsetView(r[i], "+", k)),
//                            model.arithm(c[i + 1], "=", model.intOffsetView(c[i], "+", j))
//                    );
//                }
//            }
//        }// if abs row diff is 1 then col diff is abs 2. vice versa.
//
//
//        Solver solver = model.getSolver();
//        Solution solution = solver.findSolution();
//
//        if (solution != null) {
//            System.out.print("r = ");
//            for (IntVar ri : r) {
//                System.out.print(ri.getValue() + " ");
//            }
//            System.out.println();
//
//            System.out.print("c = ");
//            for (IntVar ci : c) {
//                System.out.print(ci.getValue() + " ");
//            }
//            System.out.println();
//        } else {
//            System.out.println("No solution found.");
//        }
//    }
//}

//package org.chocosolver.examples.nqueen;
//
//import org.chocosolver.solver.Model;
//import org.chocosolver.solver.variables.IntVar;
//
//import java.util.stream.IntStream;
//
//public class KnightsTour {
//
//    public static void main(String[] args) {
//        int N = 5; // Board size
//
//        // Create a new model
//        Model model = new Model("Knight's Tour");
//
//        // Define decision variables
//        IntVar[] row = IntStream.range(0, N * N)
//                .mapToObj(i -> model.intVar("row_" + i, 0, N - 1))
//                .toArray(IntVar[]::new);
//
//        IntVar[] col = IntStream.range(0, N * N)
//                .mapToObj(i -> model.intVar("col_" + i, 0, N - 1))
//                .toArray(IntVar[]::new);
//
//        IntVar[] helper_array = IntStream.range(0, N * N)
//                .mapToObj(i -> model.intVar("helper_array_" + i, 0, N * N - 1))
//                .toArray(IntVar[]::new);
//
//        IntVar[] A = IntStream.range(0, N * N)
//                .mapToObj(i -> model.intVar("A_" + i, 0, N * N - 1))
//                .toArray(IntVar[]::new);
//
//        // Create decision variables and add constraints
//        IntStream.range(0, N * N).forEach(i -> {
//            IntVar rowTimesN = model.intScaleView(row[i], N);
//            IntVar sum = model.intVar("sum_" + i, 0, N * N);
//            model.sum(new IntVar[]{rowTimesN, col[i]}, "=", sum).post();
//            model.arithm(helper_array[i], "=", sum).post();
//        });
//
//        // Add constraints
//        IntStream.range(1, N * N).forEach(i -> {
//            IntVar rowDiff = model.intOffsetView(row[i], -row[i - 1].getValue());
//            IntVar colDiff = model.intOffsetView(col[i], -col[i - 1].getValue());
//            IntVar absRowDiff = model.intVar(model.abs(rowDiff));
//            IntVar absColDiff = model.intVar(model.abs(colDiff));
//            IntVar sum = model.intVar("sum_" + i, 0, 2 * N);
//            model.sum(new IntVar[]{absRowDiff, absColDiff}, "=", sum).post();
//            model.arithm(sum, "=", 3).post();
//            model.arithm(row[i], "!=", row[i - 1]).post();
//            model.arithm(col[i], "!=", col[i - 1]).post();
//        });
//
//        // Create solver
//        boolean solutionExists = model.getSolver().solve();
//
//        // Find solutions
//        int solutionCount = 0;
//        while (solutionExists) {
//            solutionCount++;
//            System.out.println("Solution " + solutionCount + ":");
//            IntStream.range(0, N).forEach(i -> {
//                IntStream.range(0, N).forEach(j -> {
//                    int index = i * N + j;
//                    System.out.print(A[index].getValue() + "\t");
//                });
//                System.out.println();
//            });
//            System.out.println();
//            solutionExists = model.getSolver().solve();
//        }
//
//        System.out.println("Total solutions: " + solutionCount);
//    }
//}
