package googleadstest.application.service;

import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.ads.googleads.v8.common.IpBlockInfo;
import com.google.ads.googleads.v8.enums.ResponseContentTypeEnum;
import com.google.ads.googleads.v8.errors.GoogleAdsError;
import com.google.ads.googleads.v8.errors.GoogleAdsException;
import com.google.ads.googleads.v8.resources.CampaignCriterion;
import com.google.ads.googleads.v8.services.*;
import com.google.ads.googleads.v8.utils.ResourceNames;
import googleadstest.domain.model.GoogleIpBlockResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class GoogleCriteriaRemoveService {

    private static final Logger log = LoggerFactory.getLogger(GoogleCriteriaRemoveService.class);
    private static final String CRITERIA_BASE_PATH_1 = "customers/";
    private static final String CRITERIA_BASE_PATH_2 = "/campaignCriteria/";
    private static final String CRITERIA_BASE_PATH_3 = "~";

    public void removeCriteria(Long managerId, Long accountId, Long campaignId, List<String> ips) {
        Properties adsProperties = OAuth2Service.clientProperties;
        adsProperties.put(GoogleAdsClient.Builder.ConfigPropertyKey.LOGIN_CUSTOMER_ID.getPropertyKey(), Long.toString(managerId));


        GoogleAdsClient googleAdsClient = GoogleAdsClient.newBuilder().fromProperties(adsProperties).build();

        try {
            removeCriteria(googleAdsClient, accountId, campaignId, ips);
        } catch (GoogleAdsException gae) {

            log.error("Error removing google ads criteria by the next errors: ");
            int i = 0;
            for (GoogleAdsError googleAdsError : gae.getGoogleAdsFailure().getErrorsList()) {
                log.error("  Error %d"+(i++)+": "+ googleAdsError);
            }
        }
    }

    private void removeCriteria(GoogleAdsClient googleAdsClient, Long customerId, Long campaignId, List<String> ids) {
        String campaignResourceName = ResourceNames.campaign(customerId, campaignId);

        List<CampaignCriterionOperation> operations = new ArrayList<>();

        for (String id : ids) {
            operations.add(
                    CampaignCriterionOperation.newBuilder()
                    .setRemove(getCriteriaPath(customerId, campaignId, id))
                    .build()
            );
        }

        try (CampaignCriterionServiceClient campaignCriterionServiceClient =
                     googleAdsClient.getLatestVersion().createCampaignCriterionServiceClient()) {

            MutateCampaignCriteriaRequest request =
                    MutateCampaignCriteriaRequest.newBuilder()
                        .addAllOperations(operations)
                        .setCustomerId(String.valueOf(customerId))
                        .setResponseContentType(ResponseContentTypeEnum.ResponseContentType.MUTABLE_RESOURCE).build();
            MutateCampaignCriteriaResponse response =
                    campaignCriterionServiceClient
                    .mutateCampaignCriteria(request);



        }
    }

    private String getCriteriaPath(Long customerId, Long campaignId, String id) {

        // customers/{customer_id}/campaignCriteria/{campaign_id}~{criterion_id}
        return CRITERIA_BASE_PATH_1 + customerId + CRITERIA_BASE_PATH_2 + campaignId + CRITERIA_BASE_PATH_3 + id;
    }

    private CampaignCriterion buildNegativeIpBlockCriterion(
            String ip, String campaignResourceName) {
        return CampaignCriterion.newBuilder()
                .setCampaign(campaignResourceName)
                .setNegative(true)
                .setIpBlock(
                        IpBlockInfo.newBuilder()
                                .setIpAddress(ip)
                                .build())
                .build();
    }
}
