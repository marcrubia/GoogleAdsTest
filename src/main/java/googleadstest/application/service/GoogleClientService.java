package googleadstest.application.service;


import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.ads.googleads.v8.errors.GoogleAdsError;
import com.google.ads.googleads.v8.errors.GoogleAdsException;
import com.google.ads.googleads.v8.services.GoogleAdsRow;
import com.google.ads.googleads.v8.services.GoogleAdsServiceClient;
import com.google.ads.googleads.v8.services.SearchGoogleAdsStreamRequest;
import com.google.ads.googleads.v8.services.SearchGoogleAdsStreamResponse;
import com.google.api.gax.rpc.ServerStream;
import com.google.auth.oauth2.ClientId;
import com.google.auth.oauth2.UserAuthorizer;
import com.google.auth.oauth2.UserCredentials;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import googleadstest.domain.model.GoogleAdsOAuth2Data;
import googleadstest.infrastructure.injection.InjectionNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Singleton
public class GoogleClientService {

    private static final Logger log = LoggerFactory.getLogger(GoogleClientService.class);

    public void execute(String customerId) {

        /*GoogleAdsClient googleAdsClient = GoogleAdsClient.newBuilder()
                .fromProperties(OAuth2Service.clientProperties)
                .build();*/

        Properties adsProperties = new Properties();
        adsProperties.put(GoogleAdsClient.Builder.ConfigPropertyKey.CLIENT_ID.getPropertyKey(), "825631670832-ed595lrvrntn0q6i15go7mgti0tggaie.apps.googleusercontent.com");
        adsProperties.put(GoogleAdsClient.Builder.ConfigPropertyKey.CLIENT_SECRET.getPropertyKey(), "cztWgabDcIbtsU_2fW5bilT8");
        adsProperties.put(
                GoogleAdsClient.Builder.ConfigPropertyKey.REFRESH_TOKEN.getPropertyKey(), "1//03hGKfqxAYWAVCgYIARAAGAMSNwF-L9IrkBdjtTXfsbtdeyOybsclPVDrUTZtKT-OVgdsn_I5GKlIbvAE0BW7RMa7TCKF7GRzobc");
        adsProperties.put(
                GoogleAdsClient.Builder.ConfigPropertyKey.DEVELOPER_TOKEN.getPropertyKey(), "hlA5BG8QG9A9Gd11McIk0A");

        GoogleAdsClient googleAdsClient = GoogleAdsClient.newBuilder()
                .fromProperties(adsProperties)
                .build();

        try {
            getCampaigns(googleAdsClient, customerId);
        } catch (GoogleAdsException gae) {
            // GoogleAdsException is the base class for most exceptions thrown by an API request.
            // Instances of this exception have a message and a GoogleAdsFailure that contains a
            // collection of GoogleAdsErrors that indicate the underlying causes of the
            // GoogleAdsException.
            log.error("Request ID "+gae.getRequestId()+" failed due to GoogleAdsException. Underlying errors:%n");
            int i = 0;
            for (GoogleAdsError googleAdsError : gae.getGoogleAdsFailure().getErrorsList()) {
                log.error("  Error "+(i++)+": "+googleAdsError+"%n");
            }
        }

    }

    /**
     * Runs the example.
     *
     * @param googleAdsClient the Google Ads API client.
     * @param customerId the client customer ID.
     * @throws GoogleAdsException if an API request failed with one or more service errors.
     */
    private void getCampaigns(GoogleAdsClient googleAdsClient, String customerId) {
        try (GoogleAdsServiceClient googleAdsServiceClient =
                     googleAdsClient.getLatestVersion().createGoogleAdsServiceClient()) {
            String query = "SELECT campaign.id, campaign.name FROM campaign ORDER BY campaign.id";
            // Constructs the SearchGoogleAdsStreamRequest.
            SearchGoogleAdsStreamRequest request =
                    SearchGoogleAdsStreamRequest.newBuilder()
                            .setCustomerId(customerId)
                            .setQuery(query)
                            .build();

            // Creates and issues a search Google Ads stream request that will retrieve all campaigns.
            ServerStream<SearchGoogleAdsStreamResponse> stream =
                    googleAdsServiceClient.searchStreamCallable().call(request);

            // Iterates through and prints all of the results in the stream response.
            for (SearchGoogleAdsStreamResponse response : stream) {
                for (GoogleAdsRow googleAdsRow : response.getResultsList()) {
                    log.info("Campaign with ID "+googleAdsRow.getCampaign().getId()+" and name '"+googleAdsRow.getCampaign().getName()+"' was found.%n");
                }
            }
        }
    }

}
