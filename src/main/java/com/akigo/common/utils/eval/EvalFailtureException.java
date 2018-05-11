package com.akigo.common.utils.eval;

public class EvalFailtureException extends Exception {

    public EvalFailtureException() {
        super();
    }

    public EvalFailtureException(String message) {
        super(message);
    }

    public EvalFailtureException(String message, Throwable cause) {
        super(message, cause);
    }

    public EvalFailtureException(Throwable cause) {
        super(cause);
    }
}
