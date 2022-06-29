package org.jboss.arquillian.jaxb;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;

public class JaxbExtension implements LoadableExtension {
    public void register(ExtensionBuilder builder) {
        System.out.printf("JaxbExtension registered JaxbApiJarAppender\n");
        builder.service(ApplicationArchiveProcessor.class, JaxbApiJarAppender.class);
    }
}
