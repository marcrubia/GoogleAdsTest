package googleadstest.ui;

import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/")
public class GoogleAdsTestApplication extends ResourceConfig {
    public GoogleAdsTestApplication() {
        packages(true, "googleadstest", "billy");
    }
}