package googleadstest.infrastructure.injection;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class GuiceInfrastructureModule extends AbstractModule {

    private final Integer port;
    private final String serverUrl;
    private final String googleClientId;
    private final String googleClientSecret;
    private final String googleDevToken;

    public GuiceInfrastructureModule(Integer port, String serverUrl, String googleClientId, String googleClientSecret, String googleDevToken) {
        this.port = port;
        this.serverUrl = serverUrl;
        this.googleClientId = googleClientId;
        this.googleClientSecret = googleClientSecret;
        this.googleDevToken = googleDevToken;
    }

    @Override
    protected void configure() {
        bindConstant().annotatedWith(Names.named(InjectionNames.PORT)).to(port);
        bindConstant().annotatedWith(Names.named(InjectionNames.SERVER_URL)).to(serverUrl);
        bindConstant().annotatedWith(Names.named(InjectionNames.GOOGLE_CLIENT_ID)).to(googleClientId);
        bindConstant().annotatedWith(Names.named(InjectionNames.GOOGLE_CLIENT_SECRET)).to(googleClientSecret);
        bindConstant().annotatedWith(Names.named(InjectionNames.GOOGLE_DEVELOPER_TOKEN)).to(googleDevToken);
    }
}