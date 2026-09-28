package org.jdriven;

import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;

import javax.annotation.processing.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import java.util.Set;

import static com.sun.source.util.Trees.instance;
import static com.sun.tools.javac.code.TypeTag.BOT;
import static com.sun.tools.javac.tree.JCTree.Tag.EQ;
import static com.sun.tools.javac.util.List.from;
import static com.sun.tools.javac.util.List.of;
import static java.util.stream.Collectors.*;
import static javax.lang.model.SourceVersion.RELEASE_21;
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.lang.model.element.ElementKind.PARAMETER;
import static javax.lang.model.type.TypeKind.VOID;

@SupportedSourceVersion(RELEASE_21)
@SupportedAnnotationTypes("org.jdriven.NullPropagates")
public class NullPropagatesProcessor extends AbstractProcessor {
    private Trees treeUtils;
    private TreeMaker treeMaker;

    private static boolean canPropagate(Element parameter) {
        if (parameter.getKind() != PARAMETER || parameter.asType().getKind().isPrimitive()) {
            return false;
        }

        var method = (ExecutableElement) parameter.getEnclosingElement();
        return method.getKind() == METHOD && method.getReturnType().getKind() != VOID;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.treeUtils = instance(processingEnv);
        this.treeMaker = TreeMaker.instance(((JavacProcessingEnvironment) processingEnv).getContext());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(NullPropagates.class).stream()
                .filter(NullPropagatesProcessor::canPropagate)
                .collect(groupingBy(Element::getEnclosingElement, mapping(Element::getSimpleName, toSet())))
                .forEach((method, parameters) -> addNullChecks((JCMethodDecl) treeUtils.getTree(method), parameters));

        return true;
    }

    private void addNullChecks(JCMethodDecl method, Set<Name> annotatedParameters) {
        if (method.body == null) {
            return;
        }

        var nullChecks = method.params.stream()
                .filter(it -> annotatedParameters.contains(it.getName()))
                .map(this::nullCheck)
                .toList();

        method.body.stats = from(nullChecks).appendList(method.body.stats);
    }

    private JCStatement nullCheck(JCVariableDecl parameter) {
        treeMaker.at(parameter.pos);
        var isNull = treeMaker.Binary(EQ, treeMaker.Ident(parameter.getName()), treeMaker.Literal(BOT, null));
        var returnNull = treeMaker.Return(treeMaker.Literal(BOT, null));

        return treeMaker.If(isNull, treeMaker.Block(0, of(returnNull)), null);
    }
}
