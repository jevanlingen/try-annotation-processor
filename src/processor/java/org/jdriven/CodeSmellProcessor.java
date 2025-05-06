package org.jdriven;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes("*") // Process all Java files
public class CodeSmellProcessor extends AbstractProcessor {

    private Trees treeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        treeUtils = Trees.instance(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (var rootElement : roundEnv.getRootElements()) {
            var rootPath = treeUtils.getPath(rootElement);
            if (rootPath != null) {
                new CodeSmellScanner().scan(rootPath, null);
            }
        }
        return false;
    }

    private class CodeSmellScanner extends TreePathScanner<Void, Void> { // TreePathScanner<ReturnType, ContextParam>
        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, Void ctx) {
            var methodName = node.getMethodSelect().toString();

            if (methodName.endsWith(".get") && node.getArguments().size() == 1) {
                if ("0".equals(node.getArguments().getFirst().toString())) {
                    var cu = getCurrentPath().getCompilationUnit();
                    var position = treeUtils.getSourcePositions().getStartPosition(cu, node);
                    var lineNumber = cu.getLineMap().getLineNumber(position);

                    processingEnv.getMessager().printError(
                            cu.getSourceFile().getName() + ": " + lineNumber + ": Use 'getFirst()' instead of 'get(0)' in Java 21+"
                    );
                }
            }

            return super.visitMethodInvocation(node, ctx);
        }
    }
}
