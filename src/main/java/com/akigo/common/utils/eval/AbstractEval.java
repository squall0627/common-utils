package com.akigo.common.utils.eval;

import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractEval {
    /**
     * EvalProxyクラス名
     */
    protected final String PROXY_CLASS_NAME_BASE = "$EvalProxy";
    /**
     * コンパイル後のソースファイル保管場所
     */
    protected final String BASE_PATH = "../";
    /**
     * 親クラス名
     */
    protected Class<?> superClass = null;

    private final Map<String, String> expressionToMethodMap = new HashMap<>();

    private String proxyClassName;

    protected AbstractEval() {
        this.proxyClassName = PROXY_CLASS_NAME_BASE + ThreadLocalRandom.current().nextLong(0, Long.MAX_VALUE);
    }

    protected String getProxyClassName() {
        return this.proxyClassName;
    }

    protected Map<String, Object> doEval(String... expressions) throws EvalFailtureException, InvalidExpressionException {

        // 評価式チェック
        if (expressions == null || expressions.length == 0) {
            throw new InvalidExpressionException("評価式が指定されていない");
        }
        for (String expression : expressions) {
            checkExpression(expression);
        }

        // EvalProxyのjavaソースコードを作成
        String javaSource = createEvalProxy(expressions);

        // javaファイルを生成する
        String javaFileFullPath = storeJavaSource(javaSource);

        try {
            // javaファイルをコンパイルしてロード
            Class<?> clazz = loadProxyClass(javaFileFullPath);

            // ProxyMethod実行
            return doProxyMethod(clazz);
        } finally {
            // javaとclassファイルを削除
            new File(javaFileFullPath).delete();
            new File(javaFileFullPath.replace(".java", ".class")).delete();
        }
    }

    protected void checkExpression(String expression) throws InvalidExpressionException {
        if (expression == null) {
            throw new InvalidExpressionException("評価式はNULLが指定できません");
        }

        if (expression.contains(";")) {
            throw new InvalidExpressionException("評価式は「;」が含められません");
        }
    }

    protected String createEvalProxy(String... expressions) {
        int methodCount = 0;
        // 適当に引数の内容を返すstaticメソッドをもつクラスを定義
        StringBuilder sb = new StringBuilder();
        sb.append("public class ");
        sb.append(this.proxyClassName);
        if (superClass != null) {
            sb.append(" extends ");
            sb.append(superClass.getName());
        }
        sb.append(" { ");
        for (String expression : expressions) {
            sb.append(createProxyMethod(expression, methodCount));
            methodCount++;
        }
        sb.append("}");

        return sb.toString();
    }

    protected String createProxyMethod(String expression, int methodCount) {
        String name = "proxyMehtod" + methodCount;
        this.expressionToMethodMap.put(expression, name);

        StringBuilder sb = new StringBuilder();
        sb.append("public static Object ");
        sb.append(name);
        sb.append("()");
        sb.append(" {");
        sb.append("return ");
        sb.append(expression);
        sb.append(";}");
        return sb.toString();
    }

    protected String storeJavaSource(String javaSource) {
        String fullPath = BASE_PATH + this.proxyClassName + ".java";

        // javaファイルを保存
        File file = new File(fullPath);
        try (FileWriter filewriter = new FileWriter(file)) {
            filewriter.write(javaSource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return fullPath;
    }

    protected Class<?> loadProxyClass(String javaFileFullPath) throws EvalFailtureException {

        // javaソースをコンパイル
        int ret = ToolProvider.getSystemJavaCompiler().run(null, null,
                null, new String[]{javaFileFullPath});
        if (ret != 0) {
            throw new EvalFailtureException("JAVAコンパイルが失敗しました。");
        }

        // クラスをロード
        try {
            ClassLoader loader = URLClassLoader.newInstance(
                    new URL[]{new File(BASE_PATH).toURI().toURL()},
                    (superClass != null) ? superClass.getClassLoader() : null);
            return Class.forName(this.proxyClassName, true, loader);
        } catch (Exception e) {
            throw new EvalFailtureException(e);
        }
    }

    protected Map<String, Object> doProxyMethod(Class<?> clazz) throws EvalFailtureException {
        Map<String, Object> results = new HashMap<>();
        for (Map.Entry<String, String> entry : this.expressionToMethodMap.entrySet()) {
            Method method = null;
            try {
                method = clazz.getMethod(entry.getValue(), null);
            } catch (NoSuchMethodException e) {
                throw new EvalFailtureException("評価式 " + entry.getKey() + " が存在しません、または実行済です。");
            }
            try {
                results.put(entry.getKey(), method.invoke(null, null));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new EvalFailtureException(e);
            }
        }
        return results;
    }
}
