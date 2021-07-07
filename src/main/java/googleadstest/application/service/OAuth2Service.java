package googleadstest.application.service;


import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.auth.oauth2.ClientId;
import com.google.auth.oauth2.UserAuthorizer;
import com.google.auth.oauth2.UserCredentials;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.inject.Singleton;
import googleadstest.domain.model.GoogleAdsOAuth2Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Singleton
public class OAuth2Service {
    private static final Logger log = LoggerFactory.getLogger(OAuth2Service.class);

    // Scopes for the generated OAuth2 credentials. The list here only contains the AdWords scope,
    // but you can add multiple scopes if you want to use the credentials for other Google APIs.
    private static final ImmutableList<String> SCOPES =
            ImmutableList.<String>builder().add("https://www.googleapis.com/auth/adwords").build();
    private static final String OAUTH2_CALLBACK = "/oauth2callback";

    private static final String SERVER_URL = "http://localhost:8080";
    private static final URI baseUri = URI.create(SERVER_URL);

    private static Map<String, GoogleAdsOAuth2Data> authDataMap = new HashMap<>();

    private static final Gson gson = new Gson();

    public URL execute(String clientId, String clientSecret, String loginEmailAddressHint) {
        // Creates an anti-forgery state token as described here:
        // https://developers.google.com/identity/protocols/OpenIDConnect#createxsrftoken
        String state = new BigInteger(130, new SecureRandom()).toString(32);

        UserAuthorizer userAuthorizer =
                UserAuthorizer.newBuilder()
                        .setClientId(ClientId.of(clientId, clientSecret))
                        .setScopes(SCOPES)
                        .setCallbackUri(URI.create(OAUTH2_CALLBACK))
                        .build();

        log.info("Requesting authorization");

        authDataMap.put(state, new GoogleAdsOAuth2Data(clientId, clientSecret, state, SCOPES.get(0), userAuthorizer));

        return userAuthorizer.getAuthorizationUrl(loginEmailAddressHint, state, baseUri);
    }

    public void processCallback(String code, String state, String scope) {
        GoogleAdsOAuth2Data data = validateData(code, state, scope);
        if (data != null) {

            try {
                UserAuthorizer userAuthorizer = data.getUserAuthorizer();

                // Exchanges the authorization code for credentials and print the refresh token.
                UserCredentials userCredentials =
                        userAuthorizer.getCredentialsFromCode(data.getCode(), baseUri);


                // Prints the configuration file contents.
                Properties adsProperties = new Properties();
                adsProperties.put(GoogleAdsClient.Builder.ConfigPropertyKey.CLIENT_ID.getPropertyKey(), data.getClientId());
                adsProperties.put(GoogleAdsClient.Builder.ConfigPropertyKey.CLIENT_SECRET.getPropertyKey(), data.getClientSecret());
                adsProperties.put(
                        GoogleAdsClient.Builder.ConfigPropertyKey.REFRESH_TOKEN.getPropertyKey(), userCredentials.getRefreshToken());
                adsProperties.put(
                        GoogleAdsClient.Builder.ConfigPropertyKey.DEVELOPER_TOKEN.getPropertyKey(), "INSERT_DEVELOPER_TOKEN_HERE");

                System.out.println(gson.toJson(adsProperties));
            } catch (IOException e) {
                log.error("Error getting credentials from code", e);
            }
        }



    }

    private GoogleAdsOAuth2Data validateData(String code, String state, String scope) {
        GoogleAdsOAuth2Data data = null;
        if (code != null) {

            GoogleAdsOAuth2Data tmpData = authDataMap.get(state);

            if (tmpData != null) {
                if (tmpData.getScope().equals(scope)) {
                    data = tmpData;
                } else {
                    log.error("Scope does not match");
                }
            } else {
                log.error("Failed to retrieve authorization code");
            }

        } else {
            log.error("Failed to retrieve authorization code");

        }

        return data;
    }
}
