package googleadstest.ui;

import billy.core.configmanager.ArgParameter;
import billy.core.configmanager.Configuration;

public class GoogleAdsTestConfiguration extends Configuration {

    private static final String MODULE_NAME = "googleadstest";

    public GoogleAdsTestConfiguration() {
        super(MODULE_NAME);
    }

    @ArgParameter(names = {"-p", "--port"}, description = "Port to listen", required = false)
    public Integer SERVICE_PORT = null;

    @ArgParameter(names = {"-r", "--log-sampling-rate"}, description = "Log rampling rate", required = false)
    public Integer LOG_SAMPLING_RATE = null;

    @ArgParameter(names = {"-sv-url", "--server-url"}, description = "Server URL", required = false)
    public String SERVER_URL = null;
}
