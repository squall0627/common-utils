package com.akigo.common.utils.eval;

import java.util.Map;

public class Eval extends AbstractEval {

    public Eval() {
        super();
    }

    public synchronized Object eval(String expression) throws EvalFailtureException, InvalidExpressionException {
        Map<String, Object> results = doEval(expression);
        return results.get(expression);
    }
}
