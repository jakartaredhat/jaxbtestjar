package container;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

public class MockContainer implements DeployableContainer<MockConfiguration> {
    @Override
    public Class<MockConfiguration> getConfigurationClass() {
        return MockConfiguration.class;
    }

    @Override
    public void setup(MockConfiguration configuration) {

    }

    @Override
    public void start() throws LifecycleException {

    }

    @Override
    public void stop() throws LifecycleException {

    }

    @Override
    public ProtocolDescription getDefaultProtocol() {
        return new ProtocolDescription("Local");
    }

    @Override
    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
        System.out.printf("deploy: %s\n", archive);
        WebArchiveClassLoader classLoader = new WebArchiveClassLoader(MockContainer.class.getClassLoader(), archive);
        Thread.currentThread().setContextClassLoader(classLoader);
        return new ProtocolMetaData();
    }

    @Override
    public void undeploy(Archive<?> archive) throws DeploymentException {

    }

    @Override
    public void deploy(Descriptor descriptor) throws DeploymentException {

    }

    @Override
    public void undeploy(Descriptor descriptor) throws DeploymentException {

    }
}
