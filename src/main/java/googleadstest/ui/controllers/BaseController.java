package googleadstest.ui.controllers;

import googleadstest.application.service.GoogleClientService;
import googleadstest.application.service.OAuth2Service;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URL;

@Singleton
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class BaseController {


    @Inject
    private OAuth2Service oAuth2Service;

    @Inject
    private GoogleClientService googleClientService;

    @GET
    @Path("/test")
    public Response test(@QueryParam("customerId") String customerId) {
        googleClientService.execute(customerId);
        return Response.ok().build();
    }


    @GET
    @Path("/login")
    public Response login(@QueryParam("clientId") String clientId, @QueryParam("clientSecret") String clientSecret, @QueryParam("email") String email) {
        URL url = oAuth2Service.execute(clientId, clientSecret, email);

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
