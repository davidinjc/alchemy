package com.rtr.alchemy.transmute;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rtr.alchemy.identity.IdentityType;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SupportedAnnotationTypes("com.rtr.alchemy.identity.IdentityType")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class IdentityAnnotationProcessor extends AbstractProcessor {
    private final Template dtoTemplate;
    private final Template mapperTemplate;
    private static final Pattern GETTER_PATTERN = Pattern.compile("(is|has|get)([A-Z].*)");

    public IdentityAnnotationProcessor() {
        final Properties properties = new Properties();

        final URL url = this.getClass().getClassLoader().getResource("velocity.properties");

        if (url == null) {
            throw new IllegalStateException("could not find velocity.properties file");
        }

        try {
            properties.load(url.openStream());
        } catch (final IOException e) {
            throw new IllegalStateException("failed to init annotation processor: %s", e);
        }

        final VelocityEngine velocity = new VelocityEngine(properties);
        velocity.init();
        dtoTemplate = velocity.getTemplate("Dto.vm");
        mapperTemplate = velocity.getTemplate("Mapper.vm");
    }

    private void note(String formatString, Object ... args) {
        processingEnv.getMessager().printMessage(Kind.NOTE, String.format(formatString, args));
    }

    private void error(String formatString, Object ... args) {
        processingEnv.getMessager().printMessage(Kind.ERROR, String.format(formatString, args));
    }

    private static String lowerCamelCase(String str) {
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    @Override
    public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment roundEnvironment) {
        note("Generating DTOs for types:");
        boolean result = true;

        try {
            for (final Element element : roundEnvironment.getElementsAnnotatedWith(IdentityType.class)) {
                boolean sawConstructor = false;

                if (element.getKind() != ElementKind.CLASS ||
                    element.getModifiers().contains(Modifier.ABSTRACT) ||
                    !element.getModifiers().contains(Modifier.PUBLIC) ||
                    element.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
                    continue;
                }

                final TypeElement typeElement = (TypeElement) element;
                final PackageElement packageElement = (PackageElement) typeElement.getEnclosingElement();
                final String className = typeElement.getSimpleName().toString();
                final String fullClassName = typeElement.getQualifiedName().toString();
                final String packageName = packageElement.getQualifiedName().toString();
                final Map<String, String> fields = Maps.newLinkedHashMap(); // name => type
                final Map<String, String> getters = Maps.newLinkedHashMap(); // field name => getter name
                final String boType = typeElement.getQualifiedName().toString();
                final String dtoType = boType + "Dto";
                final List<String> boCtorArgs = Lists.newArrayList();
                final List<String> dtoCtorArgs = Lists.newArrayList();
                final String identityType = typeElement.getAnnotation(IdentityType.class).value();

                note(className);

                // fields and methods
                for (final Element member : typeElement.getEnclosedElements()) {
                    if (member.getKind() == ElementKind.METHOD) {
                        final ExecutableElement method = (ExecutableElement) member;
                        final String methodName = member.getSimpleName().toString();
                        final Matcher match = GETTER_PATTERN.matcher(methodName);
                        if (method.getParameters().size() > 0 ||
                            method.getAnnotation(Override.class) != null ||
                            !match.matches()) {
                            continue;
                        }

                        final String fieldName = lowerCamelCase(match.group(2));
                        fields.put(fieldName, method.getReturnType().toString());
                        getters.put(fieldName, methodName);
                        dtoCtorArgs.add(methodName);
                    }
                }

                // constructor args for mapper
                boolean errors = false;
                for (final Element member : typeElement.getEnclosedElements()) {
                    if( member.getKind() != ElementKind.CONSTRUCTOR ||
                        !member.getModifiers().contains(Modifier.PUBLIC)) {
                        continue;
                    }

                    if (sawConstructor) {
                        error("cannot generate DTO for %s, ambiguous construction: multiple public constructors", boType);
                        errors = true;
                        break;
                    }
                    sawConstructor = true;

                    final ExecutableElement constructor = (ExecutableElement) member;
                    for (final VariableElement arg : constructor.getParameters()) {
                        final String argName = arg.getSimpleName().toString();
                        final String getterMethod = getters.get(argName);
                        if (getterMethod == null) {
                            error("cannot generate Mapper for %s, could not find getter for constructor arg %s", boType, argName);
                            errors = true;
                            break;
                        }

                        boCtorArgs.add(getterMethod);
                    }
                }

                result &= !errors;
                if (errors) {
                    continue;
                }

                final VelocityContext context = new VelocityContext();
                context.put("packageName", packageName);
                context.put("className", className);
                context.put("fields", fields);
                context.put("getters", getters);
                context.put("boType", boType);
                context.put("dtoType", dtoType);
                context.put("boCtorArgs", boCtorArgs);
                context.put("dtoCtorArgs", dtoCtorArgs);
                context.put("identityType", identityType);
                context.put("generatedMessage", String.format("generated by %s", getClass()));

                writeSourceFile(dtoTemplate, context, String.format("%sDto", fullClassName));
                writeSourceFile(mapperTemplate, context, String.format("%sMapper", fullClassName));
            }
        } catch (final IOException e) {
            error("failed to process type: %s", e.getMessage());
            return false;
        }

        return result;
    }

    private void writeSourceFile(Template template, VelocityContext context, String sourceFile) throws IOException {
        final JavaFileObject mapperOutputFile = processingEnv.getFiler().createSourceFile(sourceFile);
        note("creating file: %s", mapperOutputFile.toUri());
        try (Writer writer = mapperOutputFile.openWriter()) {
            template.merge(context, writer);
        }
    }
}
