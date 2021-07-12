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


public class GoogleIpBlocksCreateService {

    private static final Logger log = LoggerFactory.getLogger(GoogleIpBlocksCreateService.class);

    private static final int PAGE_SIZE = 1_000;

    public List<GoogleIpBlockResponse> createIpBlocks(Long managerId, Long accountId, Long campaignId, List<String> ips) {
        List<GoogleIpBlockResponse>  response = null;
        Properties adsProperties = OAuth2Service.clientProperties;
        adsProperties.put(GoogleAdsClient.Builder.ConfigPropertyKey.LOGIN_CUSTOMER_ID.getPropertyKey(), Long.toString(managerId));


        GoogleAdsClient googleAdsClient = GoogleAdsClient.newBuilder().fromProperties(adsProperties).build();

        try {
            response = addIpBlocks(googleAdsClient, accountId, campaignId, ips);
        } catch (GoogleAdsException gae) {

            log.error("Error creating google ads ip blocks by the next errors: ");
            int i = 0;
            for (GoogleAdsError googleAdsError : gae.getGoogleAdsFailure().getErrorsList()) {
                log.error("  Error %d"+(i++)+": "+ googleAdsError);
            }
        }

        return response;
    }

    private List<GoogleIpBlockResponse> addIpBlocks(GoogleAdsClient googleAdsClient, Long customerId, Long campaignId, List<String> ips) {
        List<GoogleIpBlockResponse> ipBlocks = null;

        String campaignResourceName = ResourceNames.campaign(customerId, campaignId);
        List<CampaignCriterionOperation> operations = new ArrayList<>();

        for (String ip : ips) {
            operations.add(
                    CampaignCriterionOperation.newBuilder()
                    .setCreate(buildNegativeIpBlockCriterion(ip, campaignResourceName))
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

            ipBlocks = new ArrayList<>();

            for (MutateCampaignCriterionResult result : response.getResultsList()) {
                CampaignCriterion campaignCriterion = result.getCampaignCriterion();
                GoogleIpBlockResponse ipBlock = new GoogleIpBlockResponse(campaignCriterion.getCriterionId(), campaignCriterion.getIpBlock().getIpAddress());
                ipBlocks.add(ipBlock);
            }
        }
        return ipBlocks;
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
