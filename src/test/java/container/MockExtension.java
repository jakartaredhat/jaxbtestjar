package container;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.core.spi.LoadableExtension;

public class MockExtension implements LoadableExtension {
        @Override
        public void register(ExtensionBuilder builder) {
            System.out.printf("Register MockContainer\n");
            builder.service(DeployableContainer.class, MockContainer.class);
        }
    }
