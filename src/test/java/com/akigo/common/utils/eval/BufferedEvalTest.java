package com.akigo.common.utils.eval;

import org.junit.Test;

import java.util.Map;

public class BufferedEvalTest {

    @Test
    public void test001() throws EvalFailtureException, InvalidExpressionException {
        BufferedEval eval = new BufferedEval();
        eval.addExpression("1 + 2");
        eval.addExpression("\"1\".equals(\"2\")");
        Map<String, Object> results = eval.evalAll();
        System.out.println((Integer) results.get("1 + 2"));
        System.out.println((Boolean) results.get("\"1\".equals(\"2\")"));

    }

    @Test
    public void test002() throws EvalFailtureException, InvalidExpressionException {
        BufferedEval eval = new BufferedEval();
        eval.evalAll();
    }

    @Test
    public void test003() throws EvalFailtureException, InvalidExpressionException {
        BufferedEval eval = new BufferedEval();
        eval.addExpression(null);
        eval.evalAll();
    }
}
