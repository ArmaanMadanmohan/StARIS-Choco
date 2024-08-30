/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.nqueen;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.propagation.PropagationProfiler;
import org.chocosolver.solver.search.loop.monitors.*;
import org.chocosolver.solver.search.measure.RLStatistics;
import org.chocosolver.solver.variables.Variable;

import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;

import static org.chocosolver.solver.search.strategy.Search.*;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */

public class NQueenBinary extends AbstractNQueen {

    private PropagationProfiler profiler;


    @Override
    public void buildModel() {
        model = new Model("NQueen");
        n = 4;
        vars = model.intVarArray("Q", n, 1, n);
        IntStream.range(0, n-1).forEach(i ->
                IntStream.range(i+1, n).forEach(j ->{
                    model.arithm(vars[i], "!=", vars[j]).post();
                    model.arithm(vars[i], "!=", vars[j], "-", j - i).post();
                    model.arithm(vars[i], "!=", vars[j], "+", j - i).post();
                })
        );
    }

    @Override
    public void configureSearch() {
//        model.getSolver().setSearch(inputOrderLBSearch(vars));
        model.getSolver().setSearch(armaanSearch(vars));
    }

    @Override
    public void solve() {
        Solver s = model.getSolver();
        RLStatistics statistics = s.getStatProfiler();
        s.printCstrs();
        s.printShortFeatures();
        s.plugMonitor(new IMonitorOpenNode() {
            @Override
            public void beforeOpenNode() {
                s.printShortStatistics();
                System.out.print("Variables: ");
                for (Variable var : statistics.getVars()) {
                    s.log().print("'" + var.toString() + "'  ");
                }
                System.out.println("");
                System.out.printf("%.0f\n", statistics.getAverageDepth());
                System.out.println("Current/Max: " + statistics.getCurrentDepth() + "/" + statistics.getMaxDepth());
                statistics.pauseSolving();
            }
        });
//        s.plugMonitor((IMonitorContradiction) cex -> pauseSolving());
        s.plugMonitor(new NogoodFromRestarts(model));
        //signed clauses, no-goods
        profiler = s.profilePropagation();
        s.findAllSolutions();
        try {
            profiler.writeTo(new File("NQueenBinary_Profiling_inputOrderLBSearch.txt"), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        model.getSolver().printShortStatistics();

    }

    public static void main(String[] args) {
        new NQueenBinary().execute(args);
    }


}





//        try (CPProfiler profiler = new CPProfiler(model.getSolver(), true)) {
//            while (model.getSolver().solve());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

