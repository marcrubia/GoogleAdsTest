package googleadstest.application.service;

import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.ads.googleads.v8.errors.GoogleAdsError;
import com.google.ads.googleads.v8.errors.GoogleAdsException;
import com.google.ads.googleads.v8.resources.CampaignCriterion;
import com.google.ads.googleads.v8.services.*;
import googleadstest.domain.model.GoogleIpBlockResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class GoogleIpBlocksListService {

    private static final Logger log = LoggerFactory.getLogger(GoogleIpBlocksListService.class);

    private static final int PAGE_SIZE = 1_000;

    public List<GoogleIpBlockResponse> getIpBlocks(Long managerId, Long accountId, Long campaignId) {
        List<GoogleIpBlockResponse>  response = null;
        Properties adsProperties = OAuth2Service.clientProperties;
        adsProperties.put(GoogleAdsClient.Builder.ConfigPropertyKey.LOGIN_CUSTOMER_ID.getPropertyKey(), Long.toString(managerId));


        GoogleAdsClient googleAdsClient = GoogleAdsClient.newBuilder().fromProperties(adsProperties).build();

        try {
            response = getIpBlocks(googleAdsClient, accountId, campaignId);
        } catch (GoogleAdsException gae) {

            log.error("Error getting google ads ip blocks by the next errors: ");
            int i = 0;
            for (GoogleAdsError googleAdsError : gae.getGoogleAdsFailure().getErrorsList()) {
                log.error("  Error %d"+(i++)+": "+ googleAdsError);
            }
        }

        return response;
    }

    private List<GoogleIpBlockResponse> getIpBlocks(GoogleAdsClient googleAdsClient, Long customerId, Long campaignId) {
        List<GoogleIpBlockResponse> ipBlocks = null;

        try (GoogleAdsServiceClient googleAdsServiceClient = googleAdsClient.getLatestVersion().createGoogleAdsServiceClient()) {

            SearchGoogleAdsRequest request = getRequest(customerId, campaignId);

            GoogleAdsServiceClient.SearchPagedResponse searchPagedResponse = googleAdsServiceClient.search(request);

            ipBlocks = new ArrayList<>();

            for (GoogleAdsRow googleAdsRow : searchPagedResponse.iterateAll()) {
                CampaignCriterion campaignCriterion = googleAdsRow.getCampaignCriterion();
                GoogleIpBlockResponse ipBlock = new GoogleIpBlockResponse(campaignCriterion.getCriterionId(), campaignCriterion.getIpBlock().getIpAddress());
                ipBlocks.add(ipBlock);
            }
        }
        return ipBlocks;
    }

    private SearchGoogleAdsRequest getRequest(Long customerId, Long campaignId) {
        return SearchGoogleAdsRequest.newBuilder()
                .setCustomerId(Long.toString(customerId))
                .setPageSize(PAGE_SIZE)
                .setQuery(
                        String.format(
                                "SELECT campaign.id, campaign_criterion.campaign, "
                                        + " campaign_criterion.criterion_id,"
                                        + " campaign_criterion.ip_block.ip_address"
                                        + " FROM campaign_criterion"
                                        + " WHERE campaign.id = %s"
                                        + " and campaign_criterion.ip_block.ip_address is not null"
                                        + " and campaign_criterion.negative = true",
                                Long.toString(campaignId)))
                .build();
    }
}
