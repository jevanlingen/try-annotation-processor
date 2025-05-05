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
import static javax.lang.model.SourceVersion.RELEASE_21;
import static javax.lang.model.element.ElementKind.*;

@SupportedSourceVersion(RELEASE_21)
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
        roundEnv.getElementsAnnotatedWith(Mapper.class).stream()
                .filter(element -> element.getKind() == INTERFACE)
                .forEach(this::generateMapperClass);

        return true;
    }

    private void generateMapperClass(Element interface_) {
        var packageName = elementUtils.getPackageOf(interface_).getQualifiedName().toString();
        var interfaceName = interface_.getSimpleName().toString();
        var implClassName = interfaceName + "Impl";
        var mapMethods = interface_.getEnclosedElements().stream()
                .filter(e -> e.getKind() == METHOD)
                .filter(m -> "map".equals(m.getSimpleName().toString()))
                .map(e -> (ExecutableElement) e)
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
                        generateMapMethods(mapMethods));

        writeFile(packageName + "." + implClassName, generatedClass);
    }

    private String generateImports(Set<ExecutableElement> methods) {
        return methods.stream()
                .flatMap(it -> Stream.of(getReturnType(it), getFirstArgType(it)))
                .distinct()
                .map(it -> "import " + it.getQualifiedName() + ";")
                .collect(joining("\n"));
    }

    private String generateMapMethods(Set<ExecutableElement> methods) {
        return methods.stream().map(this::generateMapMethod).collect(joining("\n"));
    }

    private String generateMapMethod(ExecutableElement method) {
        TypeElement input = getFirstArgType(method);
        TypeElement output = getReturnType(method);

        return
                """
                    @Override
                    public %s map(%s input) {
                        return new %s(%s);
                    }
                """.formatted(
                        output.getSimpleName(), // Car
                        input.getSimpleName(), // CarEntity
                        output.getSimpleName(), // Car
                        newClassArgs(input, output) // null, input.model(), input.year()
                );
    }

    private String newClassArgs(TypeElement input, TypeElement output) {
        var inputFields = input.getEnclosedElements().stream()
                .filter(e -> e.getKind() == RECORD_COMPONENT)
                .map(Element::getSimpleName)
                .toList();

        return output.getEnclosedElements().stream()
                .filter(it -> it.getKind() == RECORD_COMPONENT)
                .map(it -> inputFields.contains(it.getSimpleName()) ? "input." + it.getSimpleName() + "()" : "null")
                .collect(joining(", "));
    }

    private void writeFile(String fullyQualifiedClassName, String content) {
        try {
            var file = processingEnv.getFiler().createSourceFile(fullyQualifiedClassName);
            try (var out = new PrintWriter(file.openWriter())) {
                out.write(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // -- Types Utils Helper Methods -- //
    private TypeElement getReturnType(ExecutableElement method) {
        return (TypeElement) typeUtils.asElement(method.getReturnType());
    }

    private TypeElement getFirstArgType(ExecutableElement method) {
        return (TypeElement) typeUtils.asElement(method.getParameters().getFirst().asType());
    }
}
