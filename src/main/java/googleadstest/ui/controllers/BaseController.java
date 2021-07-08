package googleadstest.ui.controllers;

import googleadstest.application.service.GoogleCampaignListService;
import googleadstest.application.service.OAuth2Service;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import googleadstest.domain.model.GoogleCampaignResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.List;

@Singleton
@Path("/google-ads")
@Produces(MediaType.APPLICATION_JSON)
public class BaseController {

    @Inject
    private OAuth2Service oAuth2Service;

    @Inject
    private GoogleCampaignListService googleClientService;

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
        oAuth2Service.processCallback(code, state, scope);
        return Response.ok().build();
    }
}
