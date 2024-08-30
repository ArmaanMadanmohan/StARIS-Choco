package org.chocosolver.solver.search.measure;

import org.chocosolver.solver.*;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Stack;

public class RLStatistics {

    protected HashMap<String, HashMap<IEventType, Long>> varEventsTotal;
    Stack<HashMap<String, HashMap<IEventType, Long>>> varEventsCurrentBranch;
    protected HashMap<String, HashMap<IEventType, Long>> currentNodeStats;
    protected HashMap<String, Long> varModifications;
    protected HashMap<ICause, Long> propCalls;
    protected HashMap<String, Long> varFailures;
    protected HashMap<Propagator<?>, Long> propFailures;
    protected HashMap<String, HashMap<Long, Long>> valueRemovals;
    protected long maxDepth;
    protected long totalDepth;
    protected Solver solver;
    protected long nodeCount;
    protected long currentNode;
    protected Stack<Boolean> branchStats;
    protected boolean isLeft;
    protected HashMap<String, Double> activities;

    public RLStatistics(Solver solver) {
        this.solver = solver;
        varEventsTotal = new HashMap<>();
        varModifications = new HashMap<>();
        propCalls = new HashMap<>();
        propFailures = new HashMap<>();
        varFailures = new HashMap<>();
        valueRemovals = new HashMap<>();
        nodeCount = 0;
        maxDepth = 0;
        this.varEventsCurrentBranch = new Stack<>();
        currentNode = 0;
        branchStats = new Stack<>();
        isLeft = false;
        activities = new HashMap<>();
    }

    /**
    * Get total number of backtracks so far.
     **/
    public long getBacktracks() {
        return solver.getBackTrackCount();
    }

    /**
    * Get total number of failures so far.
     **/
    public long getFailures() {
        return solver.getFailCount();
    }

    /**
    * Get total resolution time.
     **/
    public float getResolutionTime() {
        return solver.getTimeCount();
    }

    /**
    * Get total number of solutions so far.
     **/
    public long getSolutionCount() {
        return solver.getSolutionCount();
    }

    /**
    * Get total number of backjumps so far.
     **/
    public long getBackjumpCount() {
        return solver.getBackjumpCount();
    }

    /**
    * Get total number of restarts so far.
     **/
    public long getRestartCount() {
        return solver.getRestartCount();
    }

    /**
    * Displays a list of constraints involved in the search.
     **/
    public Constraint[] getConstraints() {
        return solver.getModel().getCstrs();
    }

    /**
    * Pauses the solving process until the user presses Enter. Chain with BeforeOpenNode monitor.
     **/
    public void pauseSolving() {
        try {
            System.out.println("Press Enter to continue... \n ----------------------");
            do System.in.read(); // Clear buffer
            while (System.in.available() > 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the activity of a variable (implemented in ArmaanAbstract)
     */
    public void setActivity(Variable var, double activity) {
        activities.put(var.getName(), activity);
    }

    /**
     * Returns the activity of a variable.
     */
    public double getActivity(Variable var) {
        return activities.get(var.getName());
    }

    /**
    * Increases node count for calculating average depth.
     **/
    public void incNodeCount() {
        nodeCount++;
    }

    /**
    * Calculates the various (left, right) decisions taken at each node; one can view the current one too.
     **/
    public void leftBranchDetails(boolean isLeft) {
        this.isLeft = isLeft;
        branchStats.push(isLeft);
    }

    /**
    * Stores the propagators and events involved in each variable modification, as well as the events involved in
    * the current branch only.
     **/
    public void onVarUpdate(Variable var, IEventType type, ICause cause, boolean newNode) {
        varEventsTotal.computeIfAbsent(var.getName(), k -> new HashMap<>()).merge(type, 1L, Long::sum);
        varModifications.compute(var.getName(), (t,c) -> c == null ? 1L : c + 1L);
        propCalls.compute(cause, (t,c) -> c == null ? 1L : c + 1L);
        if (currentNode == var.getModel().getSolver().getNodeCount() && !varEventsCurrentBranch.isEmpty()) {
            currentNodeStats = this.varEventsCurrentBranch.pop();
            HashMap<IEventType, Long> varStats = currentNodeStats.computeIfAbsent(var.getName(), k -> new HashMap<>());
            varStats.merge(type, 1L, Long::sum);
            this.varEventsCurrentBranch.push(currentNodeStats);
        }
        if (currentNode != var.getModel().getSolver().getNodeCount()) {
            currentNode++;
            HashMap<String, HashMap<IEventType, Long>> currentNodeStats = new HashMap<>();
            varEventsCurrentBranch.push(currentNodeStats);
        }
    }

    /**
    * Gets the depth of the current decision branch.
     **/
    public long getCurrentDepth() {
        return solver.getCurrentDepth();
    }

    /**
    * Gets the maximum depth of the decision tree.
     **/
    public long getMaxDepth() {
        return solver.getMaxDepth();
    }

    /**
    * Gets the average depth of the decision tree.
     **/
    public double getAverageDepth() {
        totalDepth += solver.getCurrentDepth();
        return nodeCount != 0 ? (double) totalDepth/nodeCount : 0;
    }

    /**
    * Gets the variables involved in the search.
     **/
    public Variable[] getVars() {
        return solver.getModel().getVars();
    }

    /**
    * Gets the propagators and variables involved in the contradiction and adjusts the current branch.
     **/
    public void onFailure(Propagator<?> lastProp) {
        propFailures.compute(lastProp, (t,c) -> c == null ? 1L : 1L + c);
        Variable var = solver.getContradictionException().v;
        varFailures.compute(var.getName(), (t, c) -> c == null ? 1L : 1L + c);
        if (!varEventsCurrentBranch.isEmpty()) {
            varEventsCurrentBranch.pop();
        }
    }

    /**
    * Gets the value removals from the domain of the variable.
     **/
    public void removeValue(int value, IntVar integers) {
        valueRemovals.computeIfAbsent(integers.getName(), k -> new HashMap<>()).merge((long) value, 1L, Long::sum);
    }
}