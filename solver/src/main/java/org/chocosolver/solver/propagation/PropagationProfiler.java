/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.propagation;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.binary.PropNotEqualX_YC;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.EventRecorder;
import org.chocosolver.solver.search.loop.monitors.IMonitorContradiction;
import org.chocosolver.solver.search.loop.monitors.IMonitorDownBranch;
import org.chocosolver.solver.variables.IVariableMonitor;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.solver.variables.impl.BitsetIntVarImpl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;

import java.util.List;
import java.util.ArrayList;

/**
 * This class observes a {@link PropagationEngine} in order to collect
 * data relative to propagation.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 12/10/2021
 */
public class PropagationProfiler implements PropagationObserver, IMonitorDownBranch {

    private final long[] propCounters = new long[4];
    private final HashMap<Propagator<?>, Long> coarses;
    private final HashMap<Propagator<?>, Long> fines;
    private final HashMap<Propagator<?>, Long> failures;
    private final HashMap<Propagator<?>, Long> filters;
    private final HashMap<Variable, HashMap<IEventType, Long>> changes;
    private final Model model;
    private final HashMap<String, Long> varFailures;
//    private final HashMap<String, Long> varModifications;
//    private final HashMap<Propagator<?>, Long> propagatorCalls;

    /**
     * A propagation engine profiler.
     *
     * @param model the declaring model
     */
    public PropagationProfiler(Model model) {
        this.model = model;
//        this.varModifications = new HashMap<>();
        this.coarses = new HashMap<>();
        this.fines = new HashMap<>();
        this.filters = new HashMap<>();
        this.failures = new HashMap<>();
        this.changes = new HashMap<>();
        this.varFailures = new HashMap<>();
//        this.propagatorCalls = new HashMap<>();
    }

    @Override
    public void onCoarseEvent(Propagator<?> propagator) {
        coarses.compute(propagator, (k, c) -> c == null ? 1 : c + 1);
//        propagatorCalls.compute(propagator, (p, count) -> count == null ? 1 : count + 1);
//            System.out.println(propagator.getClass().getDeclaredField("x").getInt(propagator));
        propCounters[0]++;
    }

    @Override
    public void onFineEvent(Propagator<?> propagator) {
        fines.compute(propagator, (k, c) -> c == null ? 1 : c + 1);
//        propagatorCalls.compute(propagator, (p, count) -> count == null ? 1 : count + 1);
        propCounters[1]++;
    }

    @Override
    public void onFailure(ICause cause, Propagator<?> propagator) {
        failures.compute(propagator, (k, c) -> c == null ? 1 : c + 1);
        Variable variable = model.getSolver().getContradictionException().v;
        varFailures.compute("(" + variable.getName() + ")", (k, c) -> c == null ? 1 : c + 1);
        System.out.println("Failure! " + variable);
        System.out.println("VarFailures: " + varFailures);
        propCounters[3]++;
    }

    @Override
    public void onFiltering(ICause cause, Propagator<?> propagator) {
        if (cause instanceof Propagator<?> && cause.equals(propagator)) {
            filters.compute((Propagator<?>) cause, (k, c) -> c == null ? 1 : c + 1);
            propCounters[2]++;
        }
    }

    @Override
    public void onVariableModification(Variable variable, IEventType type, ICause cause) {
        HashMap<IEventType, Long> evt = changes.computeIfAbsent(variable, k -> new HashMap<>());
        evt.compute(type, (t, c) -> c == null ? 1 : c + 1);
//        System.out.println("Updating variable " + variable + " with event type " + evt + ". Cause: " + cause.toString());
//        varModifications.put(variable.getName(), varModifications.getOrDefault(variable.getName(), 0L) + 1);
    }

//    public void printVariableStats(Variable[] variables) {
//        for (Variable variable : variables) {
//            HashMap<IEventType, Long> evts = changes.getOrDefault(variable, new HashMap<>());
//
//            if (variable instanceof IntVar) {
//                long in = evts.getOrDefault(IntEventType.INSTANTIATE, 0L);
//                long lb = evts.getOrDefault(IntEventType.INCLOW, 0L);
//                long ub = evts.getOrDefault(IntEventType.DECUPP, 0L);
//                long bd = evts.getOrDefault(IntEventType.BOUND, 0L);
//                long rm = evts.getOrDefault(IntEventType.REMOVE, 0L);
//                System.out.printf("Variable %s - Inst: %d, Lower Bound: %d, Upper Bound: %d, Bounds: %d, Remove: %d%n",
//                        variable.getName(), in, lb, ub, bd, rm);
//            } else if (variable instanceof SetVar) {
//                long ka = evts.getOrDefault(SetEventType.ADD_TO_KER, 0L);
//                long re = evts.getOrDefault(SetEventType.REMOVE_FROM_ENVELOPE, 0L);
//                System.out.printf("Variable %s - Kernel Adds: %d, Envelope Removes: %d%n",
//                        variable.getName(), ka, re);
//            } else {
//                System.out.printf("Variable %s - No statistics available%n", variable.getName());
//            }
//        }
//        System.out.println("VarModifications: " + varModifications);
//    }
//
//    public void printPropagators() {
//        System.out.println("Propagator Call Statistics:");
//
//        for (Propagator<?> propagator : propagatorCalls.keySet()) {
//            long calls = propagatorCalls.getOrDefault(propagator, 0L);
//            System.out.printf("Propagator %s - Called: %d times%n",
//                    propagator.getClass().getSimpleName(), calls);
//        }
//    }

    /**
     * Write profiling statistics to the file.
     * It constructs a {@code FileWriter} given the {@code File} to write,
     * then creates a new {@code PrintWriter}.
     *
     * @param file      output file
     * @param rawValues set to <i>true</i> to print raw values, <i>false</i> to print normalised values.
     * @throws IOException if the file exists but is a directory rather than a regular file,
     *                     does not exist but cannot be created, or cannot be opened for any other reason
     */
    public void writeTo(File file, boolean rawValues) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        PrintWriter writer = new PrintWriter(fileWriter);
        writeTo(writer, rawValues);
        writer.close();
    }

    /**
     * <p>Write profiling statistics to the print writer.
     * <code>writer</code> is not closed at the end of the call.
     * </p>
     * <p>
     * Example of usages, print profiling data to <code>System.out</code>:
     * </p>
     * <pre> {@code
     * PropagationProfiler profiler = solver.profilePropagation();
     * solver.findAllSolutions();
     * PrintWriter pw = new PrintWriter(System.out);
     * profiler.writeTo(pw, true);
     * pw.flush();
     * }</pre>
     *
     * @param writer    a print writer
     * @param rawValues set to <i>true</i> to print raw values, <i>false</i> to print normalised values.
     */
    public void writeTo(PrintWriter writer, boolean rawValues) {
        profilePropagators(writer, rawValues);
        profileVariables(writer);
    }

    private void profilePropagators(PrintWriter writer, boolean rawValues) {
        writer.println("Propagators\n" +
                " \n" +
                "* id      : row id\n" +
                "* coarse  : for a given propagator, number of coarse propagations, i.e., calls to `propagate(int)`\n" +
                "* fine    : for a given propagator, number of fine propagations, i.e., calls to `propagate(int,int)`\n" +
                "* filter  : for a given propagator, number of times a call to propagation removes a value from a variable's domain\n" +
                "* fails   : for a given propagator, number of times it throws a failure\n" +
                "* name    : name of the given propagator \n" +
                " \n" +
                " id        coarse      fine    filter     fails  name");
        Propagator<?>[] propagators = Stream.of(model.getCstrs()).flatMap(c -> Stream.of(c.getPropagators())).toArray(Propagator[]::new);
        for (int i = 0; i < propagators.length; i++) {
            Propagator<?> p = propagators[i];
            long c = coarses.getOrDefault(p, 0L);
            long fi = fines.getOrDefault(p, 0L);
            long fl = filters.getOrDefault(p, 0L);
            long fa = failures.getOrDefault(p, 0L);
            if (rawValues) {
                writer.printf(" %-6d %9d %9d %9d %9d  \"%s\"%n",
                        i, c, fi, fl, fa, p.toString()
                );
            } else {
                writer.printf(" %-6d %8.2f%% %8.2f%% %8.2f%% %8.2f%%  \"%s\"%n",
                        i, c * 100d / propCounters[0], fi * 100d / propCounters[1], fl * 100d / propCounters[2], fa * 100d / propCounters[3], p.toString()
                );
            }
        }
        writer.printf("Total   %9d %9d %9d %9d%n",
                propCounters[0],
                propCounters[1],
                propCounters[2],
                propCounters[3]
        );
        writer.println();
    }

    private void profileVariables(PrintWriter writer) {
        IntVar[] ivars = model.retrieveIntVars(true);
        int k = 0;
        if (ivars.length > 0) {
            writer.println("Integer variables\n" +
                    " \n" +
                    "* id      : row id\n" +
                    "* inst    : for a given integer variable, number of instantiation events\n" +
                    "* lower   : for a given integer variable, number of lower bound increasing events\n" +
                    "* upper   : for a given integer variable, number of upper bound decreasing events\n" +
                    "* bounds  : for a given integer variable, number of bounds modification events\n" +
                    "* remove  : for a given integer variable, number of value removal events\n" +
                    "* name    : name of the given variable \n" +
                    " \n" +
                    " id          inst     lower     upper    bounds    remove  name");
            for (int i = 0; i < ivars.length; i++) {
                HashMap<IEventType, Long> evts = changes.getOrDefault(ivars[i], new HashMap<>());
                long in = evts.getOrDefault(IntEventType.INSTANTIATE, 0L);
                long lb = evts.getOrDefault(IntEventType.INCLOW, 0L);
                long ub = evts.getOrDefault(IntEventType.DECUPP, 0L);
                long bd = evts.getOrDefault(IntEventType.BOUND, 0L);
                long rm = evts.getOrDefault(IntEventType.REMOVE, 0L);
                writer.printf(" %-6d %9d %9d %9d %9d %9d  \"%s\"%n",
                        k++, in, lb, ub, bd, rm, ivars[i].getName()
                );
            }
            writer.println();
        }
        SetVar[] svars = model.retrieveSetVars();
        if (svars.length > 0) {
            writer.println("Set variables\n" +
                    " \n" +
                    "* id      : row id\n" +
                    "* kernel  : for a given integer variable, number of instantiation events\n" +
                    "* envel   : for a given integer variable, number of lower bound increasing events\n" +
                    "* name    : name of the given variable \n" +
                    " \n" +
                    " id       kernel     envel  name");

            writer.println("Set variables");
            writer.printf(" id       kernel     envel  name%n");
            for (int i = 0; i < svars.length; i++) {
                HashMap<IEventType, Long> evts = changes.getOrDefault(svars[i], new HashMap<>());
                long ka = evts.getOrDefault(SetEventType.ADD_TO_KER, 0L);
                long re = evts.getOrDefault(SetEventType.REMOVE_FROM_ENVELOPE, 0L);
                writer.printf(" %-6d %9d %9d  \"%s\"%n",
                        k++, ka, re, svars[i].getName()
                );
            }
            writer.println();
        }
    }
}
