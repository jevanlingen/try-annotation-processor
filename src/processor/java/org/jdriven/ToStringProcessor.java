package org.jdriven;

import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.Set;

import static com.sun.tools.javac.code.Flags.PUBLIC;
import static java.util.stream.Collectors.joining;
import static javax.lang.model.SourceVersion.RELEASE_21;
import static javax.lang.model.element.ElementKind.CLASS;

@SupportedSourceVersion(RELEASE_21)
public class ToStringProcessor extends AbstractProcessor {
    private Trees trees;
    private TreeMaker treeMaker;
    private Names names;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.trees = Trees.instance(processingEnv);

        var ctx = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(ctx);
        this.names = Names.instance(ctx);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(ToString.class.getCanonicalName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(ToString.class).stream()
                .filter(element -> element.getKind() == CLASS)
                .map(element -> (JCClassDecl) trees.getTree(element))
                .forEach(this::addToStringMethod);

        return true;
    }

    private void addToStringMethod(JCClassDecl classDecl) {
        var fields = classDecl.defs.stream().filter(it -> it instanceof JCVariableDecl).map(JCVariableDecl.class::cast).toList();
        var fieldNames = fields.stream().map(it -> it.getName() + "=%s").collect(joining(", "));
        var args = List.from(fields.stream().map(it -> (JCExpression) treeMaker.Select(treeMaker.Ident(names._this), it.getName())).toList());

        // ClassName{fieldX=%s, ..).formatted(fieldX, ..); => "Dog{name=%s, age=%s}".formatted(name, age);
        var formatCall = treeMaker.Apply(
                List.nil(),
                treeMaker.Select(treeMaker.Literal(classDecl.getSimpleName() + "{" + fieldNames + "}"), names.fromString("formatted")),
                args
        );

        // return String.format(...);
        var _return = treeMaker.Return(formatCall);

        // public String toString() { return String.format(...); }
        var toStringMethod = treeMaker.MethodDef(
                treeMaker.Modifiers(PUBLIC),
                names.fromString("toString"),
                treeMaker.Ident(names.fromString("String")),
                List.nil(),
                List.nil(),
                List.nil(),
                treeMaker.Block(0, List.of(_return)),
                null
        );

        classDecl.defs = classDecl.defs.append(toStringMethod);
    }
}
