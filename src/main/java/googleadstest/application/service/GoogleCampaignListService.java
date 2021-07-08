package googleadstest.application.service;


import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.ads.googleads.v8.errors.GoogleAdsError;
import com.google.ads.googleads.v8.errors.GoogleAdsException;
import com.google.ads.googleads.v8.services.GoogleAdsRow;
import com.google.ads.googleads.v8.services.GoogleAdsServiceClient;
import com.google.ads.googleads.v8.services.SearchGoogleAdsStreamRequest;
import com.google.ads.googleads.v8.services.SearchGoogleAdsStreamResponse;
import com.google.api.gax.rpc.ServerStream;
import com.google.inject.Singleton;
import googleadstest.domain.model.GoogleCampaignResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class GoogleCampaignListService {

    private static final Logger log = LoggerFactory.getLogger(GoogleCampaignListService.class);

    public List<GoogleCampaignResponse> execute(String accountId, String managerId) {
        List<GoogleCampaignResponse> campaigns = null;

        OAuth2Service.clientProperties.put(GoogleAdsClient.Builder.ConfigPropertyKey.LOGIN_CUSTOMER_ID.getPropertyKey(), managerId);

        // TODO get client properties from bbdd
        GoogleAdsClient googleAdsClient = GoogleAdsClient.newBuilder()
                .fromProperties(OAuth2Service.clientProperties)
                .build();

        try {
            campaigns = getCampaigns(googleAdsClient, accountId);
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
        } catch (Exception e) {
            log.error("error", e);
        }

        return campaigns;
    }

    /**
     * Runs the example.
     *
     * @param googleAdsClient the Google Ads API client.
     * @param customerId the client customer ID.
     * @throws GoogleAdsException if an API request failed with one or more service errors.
     */
    private List<GoogleCampaignResponse> getCampaigns(GoogleAdsClient googleAdsClient, String customerId) {
        List<GoogleCampaignResponse> campaigns = null;

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

            campaigns = new ArrayList<>();

            // Iterates through and prints all of the results in the stream response.
            for (SearchGoogleAdsStreamResponse response : stream) {
                for (GoogleAdsRow googleAdsRow : response.getResultsList()) {
                    GoogleCampaignResponse campaign = new GoogleCampaignResponse(googleAdsRow.getCampaign().getId(), googleAdsRow.getCampaign().getName());
                    campaigns.add(campaign);
                }
            }
        }

        return campaigns;
    }

}
