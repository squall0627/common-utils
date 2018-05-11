package com.akigo.common.utils.eval;

import org.junit.Test;

public class EvalTest {

    @Test
    public void test001() throws EvalFailtureException, InvalidExpressionException {
        Eval eval = new Eval();
        System.out.println((Integer) eval.eval("1 + 2"));
        System.out.println((Boolean) eval.eval("\"1\".equals(\"2\")"));
    }

    @Test
    public void test002() throws EvalFailtureException, InvalidExpressionException {
        Eval eval = new Eval();
        System.out.println((Integer) eval.eval(null));
    }
}
