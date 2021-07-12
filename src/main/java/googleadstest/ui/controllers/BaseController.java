package googleadstest.ui.controllers;

import com.beust.jcommander.internal.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import googleadstest.application.service.*;
import googleadstest.domain.model.GoogleCampaignResponse;
import googleadstest.domain.model.GoogleHierarchyResponse;
import googleadstest.domain.model.GoogleIpBlockResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.List;

@Singleton
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class BaseController {

    @Inject
    private OAuth2Service oAuth2Service;

    @Inject
    private GoogleCampaignListService googleClientService;

    @Inject
    private GoogleAccountHierarchy getAccountHierarchy;

    @Inject
    private GoogleIpBlocksListService googleIpBlocksListService;

    @Inject
    private GoogleIpBlocksCreateService googleIpBlocksCreateService;
    @Inject
    private GoogleCriteriaRemoveService googleCriteriaRemoveService;

    @GET
    @Path("/campaigns")
    public Response campaigns(@QueryParam("accountId") String accountId, @QueryParam("managerId") String managerId) {
        Response response;
        List<GoogleCampaignResponse> campaigns = googleClientService.execute(accountId, managerId);
        if (campaigns != null) {
            response = Response.ok(campaigns).build();
        } else {
            response = Response.serverError().build();
        }
        return response;
    }

    @GET
    @Path("/hierarchy")
    public Response hierarchy(@QueryParam("accountId") Long accountId, @QueryParam("managerId") Long managerId) {
        Response response;
        GoogleHierarchyResponse hierarchy = getAccountHierarchy.getAccounts(managerId, accountId);
        if (hierarchy != null) {
            response = Response.ok(hierarchy).build();
        } else {
            response = Response.serverError().build();
        }
        return response;
    }

    @GET
    @Path("/ipblocks")
    public Response criteria(@QueryParam("accountId") Long accountId, @QueryParam("managerId") Long managerId, @QueryParam("campaignId") Long campaignId) {
        Response response;
        List<GoogleIpBlockResponse> ipBlocks = googleIpBlocksListService.getIpBlocks(managerId, accountId, campaignId);

        if (ipBlocks != null) {
            response = Response.ok(ipBlocks).build();
        } else {
            response = Response.serverError().build();
        }

        return response;
    }

    @POST
    @Path("/ipblocks")
    public Response addIpBlock(@QueryParam("accountId") Long accountId, @QueryParam("managerId") Long managerId, @QueryParam("campaignId") Long campaignId, List<String> ips) {
        Response response;
        List<GoogleIpBlockResponse> ipBlocks = googleIpBlocksCreateService.createIpBlocks(managerId, accountId, campaignId, ips);

        if (ipBlocks != null) {
            response = Response.ok(ipBlocks).build();
        } else {
            response = Response.serverError().build();
        }

        return response;
    }

    @DELETE
    @Path("/ipblocks")
    public Response deleteIpBlock(@QueryParam("accountId") Long accountId, @QueryParam("managerId") Long managerId, @QueryParam("campaignId") Long campaignId, List<String> ids) {
        googleCriteriaRemoveService.removeCriteria(managerId, accountId, campaignId, ids);
        return Response.ok().build();
    }

    @GET
    @Path("/test")
    public Response test() {

        return Response.ok(Lists.newArrayList("test")).build();
    }


    @GET
    @Path("/login")
    public Response login() {
        URL url = oAuth2Service.execute();
        if (url != null) {
            return Response.ok(url.toString()).build();
        }
        return Response.serverError().build();
    }

    @GET
    @Path("/oauth2callback")
    public Response callback(@QueryParam("code") String code, @QueryParam("state") String state, @QueryParam("scope") String scope) {
        if (oAuth2Service.processCallback(code, state, scope)) {
            return Response.ok("account connected successfully").build();
        }
        return Response.serverError().build();
    }
}
