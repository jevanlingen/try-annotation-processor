package org.jdriven;

import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.*;
import javax.lang.model.element.TypeElement;
import java.util.Set;

import static com.sun.source.util.Trees.instance;
import static com.sun.tools.javac.code.Flags.PUBLIC;
import static com.sun.tools.javac.util.List.*;
import static java.util.stream.Collectors.joining;
import static javax.lang.model.SourceVersion.RELEASE_21;
import static javax.lang.model.element.ElementKind.CLASS;

@SupportedSourceVersion(RELEASE_21)
@SupportedAnnotationTypes("org.jdriven.ToString")
public class ToStringProcessor extends AbstractProcessor {
    private Trees treeUtils;
    private TreeMaker treeMaker;
    private Names names;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.treeUtils = instance(processingEnv);

        var ctx = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(ctx);
        this.names = Names.instance(ctx);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(ToString.class).stream()
                .filter(element -> element.getKind() == CLASS)
                .map(element -> (JCClassDecl) treeUtils.getTree(element))
                .forEach(this::addToStringMethod);

        return true;
    }

    private void addToStringMethod(JCClassDecl classDecl) {
        var fields = classDecl.defs.stream().filter(it -> it instanceof JCVariableDecl).map(JCVariableDecl.class::cast).toList();
        var fieldNames = fields.stream().map(JCVariableDecl::getName);
        var args = fields.stream().map(it -> (JCExpression) treeMaker.Select(treeMaker.Ident(names._this), it.getName())).toList();

        // ClassName{fieldX=%s, ..} => "Dog{name=%s, age=%s}"
        var literal = treeMaker.Literal(classDecl.getSimpleName() + "{" + fieldNames.map(it -> it + "=%s").collect(joining(", ")) + "}");

        // "..".formatted(fieldX, ..); => "Dog{name=%s, age=%s}".formatted(name, age)
        var classNameFormatted = treeMaker.Apply(
                nil(), // type params
                treeMaker.Select(literal, names.fromString("formatted")),
                from(args)
        );

        // return String.format(...);
        var return_ = treeMaker.Return(classNameFormatted);

        // public String toString() { return String.format(...); }
        var toStringMethod = treeMaker.MethodDef(
                treeMaker.Modifiers(PUBLIC),
                names.toString,
                treeMaker.Ident(names.fromString("String")),
                nil(), // type params
                nil(), // arguments
                nil(), // exceptions
                treeMaker.Block(0, of(return_)),
                null // default value for annotation methods
        );

        classDecl.defs = classDecl.defs.append(toStringMethod);
    }
}
