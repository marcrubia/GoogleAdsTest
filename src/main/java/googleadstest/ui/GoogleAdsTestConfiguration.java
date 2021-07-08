package googleadstest.ui;

import billy.core.configmanager.ArgParameter;
import billy.core.configmanager.Configuration;

public class GoogleAdsTestConfiguration extends Configuration {

    private static final String MODULE_NAME = "googleadstest";

    public GoogleAdsTestConfiguration() {
        super(MODULE_NAME);
    }

    @ArgParameter(names = {"-p", "--port"}, description = "Port to listen", required = true)
    public Integer SERVICE_PORT = null;

    @ArgParameter(names = {"-r", "--log-sampling-rate"}, description = "Log rampling rate", required = true)
    public Integer LOG_SAMPLING_RATE = null;

    @ArgParameter(names = {"-sv-url", "--server-url"}, description = "Server URL", required = true)
    public String SERVER_URL = null;

    @ArgParameter(names = {"-gcid", "--google-clientid"}, description = "Google Client Id", required = true)
    public String GOOGLE_CLIENT_ID = null;

    @ArgParameter(names = {"-gcs", "--google-clientsecret"}, description = "Google Client Secret", required = true)
    public String GOOGLE_CLIENT_SECRET = null;

    @ArgParameter(names = {"-gdt", "--google-dev-token"}, description = "Google Dev Token", required = true)
    public String GOOGLE_DEV_TOKEN = null;
}
