package com.rtr.alchemy.transmute;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.sun.tools.javac.api.JavacTaskImpl;
import org.junit.Before;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class IdentityAnnotationProcessorTest {
    private IdentityAnnotationProcessor processor;
    private RoundEnvironment roundEnvironment;
    private ProcessingEnvironment processingEnvironment;
    private Filer filer;
    private Map<String, String> generatedSources;
    private List<Element> elements;

    @Before
    public void setUp() throws IOException {
        filer = mock(Filer.class);
        processingEnvironment = mock(ProcessingEnvironment.class);
        doReturn(filer).when(processingEnvironment).getFiler();
        doReturn(mock(Messager.class)).when(processingEnvironment).getMessager();
        roundEnvironment = mock(RoundEnvironment.class);
        processor = new MockIdentityAnnotationProcessor(processingEnvironment);
        elements = Lists.newArrayList();
        generatedSources = Maps.newHashMap();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final JavaFileObject jfo = mock(JavaFileObject.class);
                final OutputStream stream = spy(new ByteArrayOutputStream());
                final String className = (String) invocation.getArguments()[0];
                doReturn(new OutputStreamWriter(stream)).when(jfo).openWriter();
                doAnswer(new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        generatedSources.put(className, stream.toString());
                        return null;
                    }
                }).when(stream).close();
                return jfo;
            }
        }).when(filer).createSourceFile(anyString());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                @SuppressWarnings("unchecked")
                final Class<? extends Annotation> clazz = (Class<? extends Annotation>) invocation.getArguments()[0];
                final List<Element> result = Lists.newArrayList();
                for (final Element element : elements) {
                    if (element.getAnnotation(clazz) != null) {
                        result.add(element);
                    }
                }

                return Sets.newHashSet(result);
            }
        }).when(roundEnvironment).getElementsAnnotatedWith(Mockito.<Class<? extends Annotation>>any());
    }

    private File getResourceFile(String resource) {
        final URL resourceUrl = getClass().getClassLoader().getResource(resource);
        if (resourceUrl == null) {
            fail(String.format("could not find resource %s", resource));
        }

        return new File(resourceUrl.getFile());
    }

    private Set<? extends TypeElement> getElementsFromSource(String resource) throws IOException {
        final JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        final StandardJavaFileManager fileManager = tool.getStandardFileManager(null, null, null);
        final File resourceFile = getResourceFile(resource);
        final List<String> options = Lists.newArrayList("-proc:none");
        final JavacTaskImpl task = (JavacTaskImpl) tool.getTask(null, fileManager, null, options, null, fileManager.getJavaFileObjects(resourceFile));
        return Sets.newHashSet(task.enter());
    }

    private boolean process(String sourceFile) {
        try {
            final Set<? extends TypeElement> elements = getElementsFromSource(String.format("sources/%s", sourceFile));
            this.elements.clear();
            this.elements.addAll(elements);
            return processor.process(elements, roundEnvironment);
        } catch (final IOException e) {
            fail(e.getMessage());
        }

        return false;
    }

    private void assertGenerated(String className) {
        final String simpleName = className.substring(className.lastIndexOf(".") + 1);
        final String resourceFile = String.format("generated-sources/%s.java", simpleName);
        final String actual = generatedSources.get(className);
        assertNotNull("no source code generated", actual);
        try {
            final String expected =
                Files
                    .asByteSource(getResourceFile(resourceFile))
                    .asCharSource(Charset.forName("UTF-8"))
                    .read();
            compareSourceCode(expected, actual);
        } catch (final IOException e) {
            fail(String.format("could not read source file: %s", e.getMessage()));
        }
    }

    private String formatSourceCode(String sourceCode) {
        // remove leading/trailing whitespaces and empty lines to make comparisons easier
        return
            Joiner
                .on(System.lineSeparator())
                .join(
                    Iterables.filter(
                        Iterables.transform(
                            Splitter
                                .on(System.lineSeparator())
                                .split(sourceCode),
                            new Function<String, Object>() {
                                @Override
                                public Object apply(String input) {
                                    return input.trim();
                                }
                            }
                        ),
                        new Predicate<Object>() {
                            @Override
                            public boolean apply(Object input) {
                                return !String.valueOf(input).isEmpty();
                            }
                        }
                    )
                );
    }

    private void compareSourceCode(String expected, String actual) {
        if (!formatSourceCode(expected).equals(formatSourceCode(actual))) {
            throw new ComparisonFailure("generated source code not the same", expected, actual);
        }
    }

    private void assertNothingGenerated() {
        assertEquals("expected no source code to generated", 0, generatedSources.size());
    }

    @Test
    public void testIdentityWithoutAnnotation() {
        process("IdentityNoAnnotation.java");
        assertNothingGenerated();
    }

    @Test
    public void testIdentityNotPublic() {
        process("IdentityNotPublic.java");
        assertNothingGenerated();
    }

    @Test
    public void testIdentityAbstract() {
        process("IdentityAbstract.java");
        assertNothingGenerated();
    }

    @Test
    public void testIdentityMultipleConstructors() {
        process("IdentityMultipleConstructors.java");
        assertNothingGenerated();
    }

    @Test
    public void testIdentityNestedClass() {
        process("IdentityNestedClass.java");
        assertNothingGenerated();
    }

    @Test
    public void testIdentityMismatchedNames() {
        process("IdentityMismatchedNames.java");
        assertNothingGenerated();
    }

    @Test
    public void testSimpleIdentity() {
        process("SimpleIdentity.java");
        assertGenerated("com.example.SimpleIdentityDto");
        assertGenerated("com.example.SimpleIdentityMapper");
    }
}
