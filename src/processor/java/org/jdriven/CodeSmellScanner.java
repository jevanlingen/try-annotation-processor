package org.jdriven;

import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

import static javax.tools.Diagnostic.Kind.ERROR;

public class CodeSmellScanner extends TreeScanner<Void, Void> {
    private final Messager messager;

    public CodeSmellScanner(ProcessingEnvironment processingEnv) {
        this.messager = processingEnv.getMessager();
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void unused) {
        String methodName = node.getMethodSelect().toString();

        if (methodName.endsWith(".get") && node.getArguments().size() == 1) {
            var argument = node.getArguments().getFirst();
            if (argument instanceof LiteralTree literal && "0".equals(literal.getValue().toString())) {
                messager.printMessage(ERROR,"Use 'getFirst()' instead of 'get(0)' in Java 21+");
            }
        }

        return super.visitMethodInvocation(node, unused);
    }
}
