package googleadstest.ui.controllers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import googleadstest.application.service.GetAccountHierarchy;
import googleadstest.application.service.GoogleCampaignListService;
import googleadstest.application.service.OAuth2Service;
import googleadstest.domain.model.GoogleAccountResponse;
import googleadstest.domain.model.GoogleCampaignResponse;
import googleadstest.domain.model.GoogleHierarchyResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
    private GetAccountHierarchy getAccountHierarchy;

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
        GoogleHierarchyResponse hierarchy = getAccountHierarchy.getAccounts(accountId, managerId);
        if (hierarchy != null) {
            response = Response.ok(hierarchy).build();
        } else {
            response = Response.serverError().build();
        }
        return response;
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
