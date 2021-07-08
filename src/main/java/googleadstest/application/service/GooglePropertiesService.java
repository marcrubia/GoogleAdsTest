package googleadstest.application.service;

import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import googleadstest.infrastructure.injection.InjectionNames;

import javax.inject.Named;
import java.util.Properties;

@Singleton
public class GooglePropertiesService {

    @Inject
    @Named(InjectionNames.GOOGLE_CLIENT_ID)
    public String CLIENT_ID;

    @Inject
    @Named(InjectionNames.GOOGLE_CLIENT_SECRET)
    public String CLIENT_SECRET;

    @Inject
    @Named(InjectionNames.GOOGLE_DEVELOPER_TOKEN)
    public String DEV_TOKEN;

    public Properties getDefaultProperties() {
        Properties adsProperties = new Properties();
        adsProperties.put(GoogleAdsClient.Builder.ConfigPropertyKey.CLIENT_ID.getPropertyKey(), CLIENT_ID);
        adsProperties.put(GoogleAdsClient.Builder.ConfigPropertyKey.CLIENT_SECRET.getPropertyKey(), CLIENT_SECRET);
        adsProperties.put(
                GoogleAdsClient.Builder.ConfigPropertyKey.DEVELOPER_TOKEN.getPropertyKey(), DEV_TOKEN);

        return adsProperties;
    }

}
