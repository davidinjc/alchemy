package com.rtr.alchemy.transmute;

import javax.annotation.processing.ProcessingEnvironment;

public class MockIdentityAnnotationProcessor extends IdentityAnnotationProcessor {
    public MockIdentityAnnotationProcessor(ProcessingEnvironment environment) {
        this.processingEnv = environment;
    }
}
