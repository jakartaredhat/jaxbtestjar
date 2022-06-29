package war;

import java.security.CodeSource;
import java.util.Arrays;

import container.WebArchiveClassLoader;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ArquillianExtension.class)
public class CheckJarTest {
    @Deployment
    public static Archive<?> deploy() {
        System.out.printf("CheckJarTest.deploy\n");
        WebArchive war = ShrinkWrap.create(WebArchive.class, CheckJarTest.class.getSimpleName() + ".war")
                // add this class into the deployment so that it's subject to discovery
                .addClasses(CheckJarTest.class);
        System.out.println(war.toString(true));
        return war;
    }

    @Test
    public void testJaxb() throws Exception {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        System.out.printf("ClassLoader: %s\n", loader);
        WebArchiveClassLoader warLoader = (WebArchiveClassLoader) loader;
        System.out.printf("ClassLoader.urls: %s\n", Arrays.asList(warLoader.getURLs()));
        Class<?> jaxbElement = warLoader.loadClass("jakarta.xml.bind.JAXBElement");
        CodeSource cs = jaxbElement.getProtectionDomain().getCodeSource();
        System.out.printf("JAXBElement.cs: %s\n", cs);
    }
}
