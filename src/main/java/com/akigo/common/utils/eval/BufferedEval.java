package com.akigo.common.utils.eval;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BufferedEval extends AbstractEval {

    private final Set<String> expressions = new HashSet<>();

    public BufferedEval() {
        super();
    }

    public synchronized void addExpression(String expression) {
        this.expressions.add(expression);
    }

    public synchronized void clearExpressions() {
        this.expressions.clear();
    }

    public synchronized Map<String, Object> evalAll() throws EvalFailtureException, InvalidExpressionException {
        return doEval(expressions.toArray(new String[expressions.size()]));
    }
}
