package googleadstest.infrastructure.injection;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class GuiceInfrastructureModule extends AbstractModule {

    private final Integer port;
    private final String serverUrl;

    public GuiceInfrastructureModule(Integer port, String serverUrl) {
        this.port = port;
        this.serverUrl = serverUrl;
    }

    @Override
    protected void configure() {
        bindConstant().annotatedWith(Names.named(InjectionNames.PORT)).to(port);
        bindConstant().annotatedWith(Names.named(InjectionNames.SERVER_URL)).to(serverUrl);

    }
}