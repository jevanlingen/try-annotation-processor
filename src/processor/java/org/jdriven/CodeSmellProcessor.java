package org.jdriven;

import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes("*") // Process all Java files
public class CodeSmellProcessor extends AbstractProcessor {

    private Trees trees;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        trees = Trees.instance(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (var element : roundEnv.getRootElements()) {
            var path = trees.getPath(element);
            if (path != null) {
                new CodeSmellScanner(processingEnv, trees).scan(path, null);
            }
        }
        return true;
    }

    static class CodeSmellScanner extends TreePathScanner<Void, Void> {
        private final Messager messager;
        private final Trees trees;

        public CodeSmellScanner(ProcessingEnvironment processingEnv, Trees trees) {
            this.messager = processingEnv.getMessager();
            this.trees = trees;
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, Void ctx) {
            String methodName = node.getMethodSelect().toString();

            if (methodName.endsWith(".get") && node.getArguments().size() == 1) {
                var argument = node.getArguments().getFirst();
                if (argument instanceof LiteralTree literal && "0".equals(literal.getValue().toString())) {
                    var cu = getCurrentPath().getCompilationUnit();
                    var position = trees.getSourcePositions().getStartPosition(cu, node);
                    var lineNumber = cu.getLineMap().getLineNumber(position);

                    messager.printError(
                            cu.getSourceFile().getName() + ": " + lineNumber + ": Use 'getFirst()' instead of 'get(0)' in Java 21+"
                    );
                }
            }

            return super.visitMethodInvocation(node, ctx);
        }
    }
}
