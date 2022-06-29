package container;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;

public class MockConfiguration implements ContainerConfiguration {
    @Override
    public void validate() throws ConfigurationException {

    }
}
