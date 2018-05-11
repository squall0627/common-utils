package com.akigo.common.utils.eval;

import org.junit.Test;

public class EvalTest {
    private int a = 0;

    public void setA(int a) {
        this.a = 2;
    }

    @Test
    public void test001() throws EvalFailtureException, InvalidExpressionException {
        Eval eval = new Eval();
        System.out.println((Integer) eval.eval("1 + 2"));
        System.out.println((Boolean) eval.eval("\"1\".equals(\"2\")"));
//        eval = new Eval();
//        eval.execute("setA(2)");
//        System.out.println(this.a);

    }
}
