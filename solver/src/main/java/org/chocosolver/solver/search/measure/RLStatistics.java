package org.chocosolver.solver.search.measure;

import org.chocosolver.solver.*;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RLStatistics {

    protected HashMap<String, HashMap<IEventType, Long>> varEventsTotal;
    protected HashMap<String, HashMap<IEventType, Long>> varEventsCurrentBranch;
    protected List<HashMap<String, HashMap<IEventType, Long>>> varEventsAllBranches;
    protected HashMap<String, Long> varModifications;
    protected HashMap<ICause, Long> propCalls;
    protected HashMap<String, Long> varFailures;
    protected HashMap<Propagator<?>, Long> propFailures;
    protected HashMap<String, HashMap<Long, Long>> valueRemovals;
    protected long maxDepth;
    protected long totalDepth;
    protected HashMap<String, Long> varWeight;
    protected HashMap<String, Long> varActivity;
    protected Solver solver;
    protected long nodeCount;
    protected long backtrackCount;

    public RLStatistics(Solver solver) {
        this.solver = solver;
        varEventsTotal = new HashMap<>();
        varEventsAllBranches = new ArrayList<>();
        varModifications = new HashMap<>();
        propCalls = new HashMap<>();
        propFailures = new HashMap<>();
        varFailures = new HashMap<>();
        valueRemovals = new HashMap<>();
        nodeCount = 0;
        maxDepth = 0;
        backtrackCount = 0;
    }
    // keep track of the node count. or at every override close node/downbranch, update totalVarModifications
    //at every decision node, I want to be able to consult the modifications made to each variable. first make a total one. then, what would be the
    //reason to have ones associated with each decision. perhaps make a hashmap with a decision and hashmap of event types for that decision?

    public void incNodeCount() {
        nodeCount++;
    }

    public void onVarUpdate(Variable var, IEventType type, ICause cause) {
        varEventsTotal.computeIfAbsent(var.getName(), k -> new HashMap<>()).merge(type, 1L, Long::sum);
        varModifications.compute(var.getName(), (t,c) -> c == null ? 1L : c + 1L);
        propCalls.compute(cause, (t,c) -> c == null ? 1L : c + 1L);
    }

    public long getCurrentDepth() {
        return solver.getCurrentDepth() - 1;
    }

    public long getMaxDepth() {
        return solver.getMaxDepth();
    }

    public double getAverageDepth() {
        totalDepth += solver.getCurrentDepth();
        return nodeCount != 0 ? (double) totalDepth/nodeCount : 0;
    }

    public Variable[] getVars() {
        return solver.getModel().getVars();
    }

    public void onFailure(Propagator<?> lastProp) { // effectively cause can be cast to prop
        propFailures.compute(lastProp, (t,c) -> c == null ? 1L : 1L + c);
        Variable var = solver.getContradictionException().v;
        varFailures.compute(var.getName(), (t, c) -> c == null ? 1L : 1L + c); //or var.toString or simply change this to Variable HashMap
    }

    public void removeValue(int value, IntVar integers) {
        valueRemovals.computeIfAbsent(integers.getName(), k -> new HashMap<>()).merge((long) value, 1L, Long::sum);
    }

    public void newBranch() {
//        if (solver.getMeasures().backtrackCount > backtrackCount) {
//            varEventsAllBranches.add(varEventsCurrentBranch);
//            backtrackCount = solver.getMeasures().backtrackCount;
//        }
//        if (solver.getSolutionCount() > solutionCount || solver.getRestartCount() > restartCount) {
//
//        }
    }


    // backjump, solutions, restarts, fails, time for nodes (hook into Measures class through solver?)

        // hashmap of variables and list of total associated events and/or value removals
    // hashmap of variables and list of associated events for node
    // hashmap of propagators (causes) and failures
    // hashmap of causes/propagators and total times called
    // current depth, max depth, average depth

    // how to differentiate between branches (use restart/solution as a delimiter, place them into buckets. use a stack? pop when backtracking?)

    // nogoods
    // weights, activity
}