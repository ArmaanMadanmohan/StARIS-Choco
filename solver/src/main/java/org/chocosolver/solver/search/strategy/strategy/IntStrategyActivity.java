package org.chocosolver.solver.search.strategy.strategy;

import gnu.trove.list.array.TLongArrayList;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.propagation.PropagationInsight;
import org.chocosolver.solver.search.loop.monitors.IMonitorDownBranch;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.selectors.values.IntValueSelector;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.variables.IVariableMonitor;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;

import java.util.ArrayList;
import java.util.BitSet;

public class IntStrategyActivity extends IntStrategy implements IMonitorDownBranch, IVariableMonitor<IntVar> {
    private final double[] A; // activity of all variables
    private final BitSet affected; // store affected variables
    private final double g; // g for aging
    private boolean sampling; // is this still in a sampling phase
    private int currentVar = -1, currentVal = -1;

    private ArrayList<Variable> Lvars;
    private TLongArrayList Ldeltas;

    public IntStrategyActivity(IntVar[] scope, VariableSelector<IntVar> varSelector, IntValueSelector valSelector,
                                   DecisionOperator<IntVar> decOperator, double g) {
        super(scope, varSelector, valSelector, decOperator);
        this.g = g;
        this.A = new double[scope.length];
        this.affected = new BitSet(scope.length);
        this.sampling = true;
    }

    @Override
    public boolean init() {
        PropagationInsight.PickOnDom pi = new PropagationInsight.PickOnDom();

        boolean init = super.init();
        if (init) {
            Solver solver = vars[0].getModel().getSolver();
            solver.getEngine().setInsight(pi);
            Lvars = pi.getLvars();
            Ldeltas = pi.getLdeltas();
            solver.plugMonitor(this);
            for (int i = 0; i < vars.length; i++) {
                vars[i].addMonitor(this);
            }
        }
        return init;
    }

    @Override
    public void remove() {
        super.remove();
        Solver solver = vars[0].getModel().getSolver();
        solver.unplugMonitor(this);
        for (IntVar var : vars) {
            var.removeMonitor(this);
        }
    }

    @Override
    public Decision<IntVar> getDecision() {
        IntVar best = null;
        double bestVal = -1.0d;
        for (int i = 0; i < vars.length; i++) {
            if (!vars[i].isInstantiated()) {
                double a = A[i] / vars[i].getDomainSize();
                System.out.println("Activity for " + i + ": " + a);
                if (a > bestVal) {
                    best = vars[i];
                    bestVal = a;
                }
            }
        }
//        System.out.println("Best Val: " + bestVal);
//        System.out.println("Best: " + best);
        if (best != null) {
            currentVar = getIndex(best);
            currentVal = valueSelector.selectValue(best);
            return computeDecision(best);
        }
        return null;
    }

    @Override
    public void onUpdate(IntVar var, IEventType evt) {
        affected.set(getIndex(var));
    }

    @Override
    public void beforeDownBranch(boolean left) {
        if (left) {
            affected.clear();
        }
    }

    @Override
    public void afterDownBranch(boolean left) {
        if (left && currentVar >= 0) {
            for (int i = 0; i < A.length; i++) {
                if (vars[i].getDomainSize() > 1) {
                    A[i] *= sampling ? 1.0 : g;
                }
                if (affected.get(i)) {
                    A[i] += 1;
                }
            }
            currentVar = -1;
        }
    }

    private int getIndex(IntVar var) {
        for (int i = 0; i < vars.length; i++) {
            if (vars[i].equals(var)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Variable not found in scope");
    }
}
