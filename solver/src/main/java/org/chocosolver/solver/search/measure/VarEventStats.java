package org.chocosolver.solver.search.measure;

import org.chocosolver.solver.variables.events.IEventType;

import java.util.HashMap;

public class VarEventStats {
    private HashMap<String, HashMap<IEventType, Long>> varEvents;

    public VarEventStats() {
        this.varEvents = new HashMap<>();
    }

    public void update(String variable, IEventType event, long count) {
        varEvents.computeIfAbsent(variable, k -> new HashMap<>()).merge(event, count, Long::sum);
    }

    public HashMap<IEventType, Long> getEventsForVar(String varName) {
        return varEvents.getOrDefault(varName, new HashMap<>());
    }

    public HashMap<String, HashMap<IEventType, Long>> getAllEvents() {
        return varEvents;
    }

}
