package org.jboss.arquillian.jaxb;

import java.io.File;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class JaxbApiJarAppender implements ApplicationArchiveProcessor {
    public void process(Archive<?> applicationArchive, TestClass testClass) {
        System.out.printf("JaxbApiJarAppender, %s\n", applicationArchive);
        if(applicationArchive instanceof WebArchive) {
            WebArchive war = (WebArchive) applicationArchive;
            File jarFile = new File("target/dependency/lib/jakarta.xml.bind-api.jar");
            System.out.printf("jakarta.xml.bind-api.jar: %s\n", jarFile.getAbsolutePath());
            war.addAsLibraries(jarFile);
            System.out.println(war.toString(true));
        }
    }
}
