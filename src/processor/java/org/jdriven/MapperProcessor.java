package org.jdriven;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static javax.lang.model.SourceVersion.RELEASE_17;
import static javax.lang.model.element.ElementKind.*;

@SupportedSourceVersion(RELEASE_17)
public class MapperProcessor extends AbstractProcessor {
    private Types typeUtils;
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(Mapper.class.getCanonicalName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(elementUtils.getTypeElement(Mapper.class.getName())).stream()
                .filter(element -> element.getKind() == INTERFACE)
                .map(element -> (TypeElement) element)
                .forEach(this::generateMapperClass);

        return true;
    }

    private void generateMapperClass(TypeElement interfaceElement) {
        var packageName = elementUtils.getPackageOf(interfaceElement).getQualifiedName().toString();
        var interfaceName = interfaceElement.getSimpleName().toString();
        var implClassName = interfaceName + "Impl";

        var mapMethods = interfaceElement.getEnclosedElements().stream()
                .filter(e -> e.getKind() == METHOD)
                .map(e -> (ExecutableElement) e)
                .filter(m -> m.getSimpleName().toString().equals("map"))
                .collect(toUnmodifiableSet());

        var generatedClass =
                """
                package %s;
                
                %s
                
                public class %s implements %s {
                
                %s
                }
                """.formatted(packageName,
                        generateImports(mapMethods),
                        implClassName,
                        interfaceName,
                        mapMethods.stream().map(this::generateMapMethod).collect(joining("\n")));

        writeFile(packageName + "." + implClassName, generatedClass);
    }

    private String generateImports(Set<ExecutableElement> methods) {
        return methods.stream()
                .flatMap(method -> Stream.of(
                        (TypeElement) typeUtils.asElement(method.getParameters().get(0).asType()),
                        (TypeElement) typeUtils.asElement(method.getReturnType())
                ))
                .distinct()
                .map(it -> "import " + it.getQualifiedName() + ";")
                .collect(joining("\n"));
    }

    private String generateMapMethod(ExecutableElement method) {
        TypeElement inputElement = (TypeElement) typeUtils.asElement(method.getParameters().get(0).asType());
        TypeElement outputElement = (TypeElement) typeUtils.asElement(method.getReturnType());

        return
                """
                    @Override
                    public %s map(%s input) {
                        return new %s(%s);
                    }
                """.formatted(
                        outputElement.getSimpleName(),
                        inputElement.getSimpleName(),
                        outputElement.getSimpleName(),
                        newClassArgs(inputElement, outputElement)
                );
    }

    private String newClassArgs(TypeElement input, TypeElement output) {
        var inputFields = input.getEnclosedElements().stream()
                .filter(e -> e.getKind() == RECORD_COMPONENT)
                .map(Element::getSimpleName)
                .collect(toUnmodifiableSet());

        return output.getEnclosedElements().stream()
                .filter(it -> it.getKind() == RECORD_COMPONENT)
                .map(it -> inputFields.contains(it.getSimpleName()) ? "input." + it.getSimpleName() + "()" : "null")
                .collect(joining(", "));
    }

    private void writeFile(String className, String content) {
        try {
            var file = processingEnv.getFiler().createSourceFile(className);
            try (var out = new PrintWriter(file.openWriter())) {
                out.write(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
