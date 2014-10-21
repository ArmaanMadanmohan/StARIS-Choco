/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package parser.flatzinc.ast.searches;

import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.search.strategy.selectors.SetValueSelector;
import solver.search.strategy.selectors.VariableSelector;
import solver.search.strategy.selectors.values.SetDomainMin;
import solver.search.strategy.selectors.variables.InputOrder;
import solver.search.strategy.selectors.variables.Occurrence;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.SetStrategy;
import solver.variables.SetVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 27/01/11
 */
public class SetSearch {

    private static long seed = 29091981;

    private SetSearch() {
    }

    public static AbstractStrategy build(SetVar[] variables, VarChoice varChoice, Assignment assignment, Solver solver) {
        VariableSelector<SetVar> varsel = variableSelector(varChoice);
        if (varsel == null) { // free search
            LoggerFactory.getLogger("solver").error("% No free search defined for SetVar");
            return null;
        }
        return valueSelector(variables, varsel, assignment);
    }

    private static VariableSelector<SetVar> variableSelector(VarChoice varChoice) {
        switch (varChoice) {
            case input_order:
                return new InputOrder<>();
            case occurrence:
                return new Occurrence<>();
            case first_fail:
            case anti_first_fail:
            case smallest:
            case largest:
            case most_constrained:
            case max_regret:
            default:
                LoggerFactory.getLogger("solver").error("% No implementation for " + varChoice.name() + ". Set default.");
                return new InputOrder<>();
        }
    }

    private static SetStrategy valueSelector(SetVar[] scope, VariableSelector<SetVar> variableSelector,
                                             Assignment assignment) {
        SetValueSelector valSelector;
        switch (assignment) {
            case indomain:
            case indomain_min:
                valSelector = new SetDomainMin();
                break;
            case indomain_max:
            case indomain_middle:
            case indomain_median:
            case indomain_random:
            case indomain_split:
            case indomain_interval:
            case indomain_reverse_split:
            default:
                LoggerFactory.getLogger("solver").error("% No implementation for " + assignment.name() + ". Set default.");
                valSelector = new SetDomainMin();
        }
        return new SetStrategy(scope, variableSelector, valSelector, true);
    }


}

