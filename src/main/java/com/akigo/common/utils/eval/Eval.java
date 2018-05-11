package com.akigo.common.utils.eval;

import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class Eval {
    /**
     * EvalProxyクラス名
     */
    private final String PROXY_CLASS_NAME = "$EvalProxy";
    /**
     * コンパイル後のソースファイル保管場所
     */
    private final String BASE_PATH = "../";
    /**
     * 親クラス名
     */
    private Class<?> superClass = null;

    public Eval() {
    }

//    public Eval(Class<?> superClass) {
//        this.superClass = superClass;
//    }

    public Object eval(String expression) throws EvalFailtureException, InvalidExpressionException {
        // 評価式チェック
        checkExpression(expression);

        // EvalProxyのjavaソースコードを作成
        String javaSource = createEvalProxyWithReturn(expression);

        // javaファイルを生成する
        String javaFileFullPath = storeJavaSource(javaSource);

        // javaファイルをコンパイルして実行
        return doEval(javaFileFullPath);
    }

//    public void execute(String expression) throws EvalFailtureException, InvalidExpressionException {
//        // 評価式チェック
//        checkExpression(expression);
//
//        // EvalProxyのjavaソースコードを作成
//        String javaSource = createEvalProxyWithoutReturn(expression);
//
//        // javaファイルを生成する
//        String javaFileFullPath = storeJavaSource(javaSource);
//
//        // javaファイルをコンパイルして実行
//        doEval(javaFileFullPath);
//    }

    private void checkExpression(String expression) throws InvalidExpressionException {
        if (expression == null) {
            throw new InvalidExpressionException("評価式はNULLが指定できません");
        }

        if (expression.contains(";")) {
            throw new InvalidExpressionException("評価式は「;」が含められません");
        }
    }

    private String createEvalProxyWithReturn(String expression) {
        // 適当に引数の内容を返すstaticメソッドをもつクラスを定義
        StringBuilder sb = new StringBuilder();
        sb.append("public class ");
        sb.append(PROXY_CLASS_NAME);
        if (superClass != null) {
            sb.append(" extends ");
            sb.append(superClass.getName());
        }
        sb.append(" { public static Object doEval(){");
        sb.append("return ");
        sb.append(expression);
        sb.append(";}}");

        return sb.toString();
    }

    private String createEvalProxyWithoutReturn(String expression) {
        // 適当に引数の内容を返すstaticメソッドをもつクラスを定義
        StringBuilder sb = new StringBuilder();
        sb.append("public class ");
        sb.append(PROXY_CLASS_NAME);
        if (superClass != null) {
            sb.append(" extends ");
            sb.append(superClass.getName());
        }
        sb.append(" { public static void doEval(){");
        sb.append(expression);
        sb.append(";}}");

        return sb.toString();
    }

    private String storeJavaSource(String javaSource) {
        String fullPath = BASE_PATH + PROXY_CLASS_NAME + ".java";

        // javaファイルを保存
        File file = new File(fullPath);
        try (FileWriter filewriter = new FileWriter(file)) {
            filewriter.write(javaSource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return fullPath;
    }

    private Object doEval(String javaFileFullPath) throws EvalFailtureException {

        // javaソースをコンパイル
        int ret = ToolProvider.getSystemJavaCompiler().run(null, null,
                null, new String[]{javaFileFullPath});
        if (ret != 0) {
            throw new EvalFailtureException("JAVAコンパイルが失敗しました。");
        }
        try {
            ClassLoader loader = URLClassLoader.newInstance(
                    new URL[]{new File(BASE_PATH).toURI().toURL()},
                    (superClass != null) ? superClass.getClassLoader() : null);
            Class<?> clazz = Class.forName(PROXY_CLASS_NAME, true, loader);
            Method method = clazz.getMethod("doEval", null);
//            Object proxy = clazz.newInstance();
            return method.invoke(null, null);
        } catch (Exception e) {
            throw new EvalFailtureException(e);
        } finally {
            // javaとclassファイルを削除
            new File(javaFileFullPath).delete();
            new File(javaFileFullPath.replace(".java", ".class")).delete();
        }
    }
}
