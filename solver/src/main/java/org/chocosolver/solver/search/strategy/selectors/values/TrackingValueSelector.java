package org.chocosolver.solver.search.strategy.selectors.values;

import org.chocosolver.solver.variables.IntVar;

public interface TrackingValueSelector {
    int activityValue(IntVar variable);
}
