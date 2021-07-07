package googleadstest.domain.model;

import com.google.auth.oauth2.UserAuthorizer;

public class GoogleAdsOAuth2Data {

    private String clientId;
    private String clientSecret;
    private String code;
    private String state;
    private String scope;
    private UserAuthorizer userAuthorizer;

    public GoogleAdsOAuth2Data(String clientId, String clientSecret, String state, String scope, UserAuthorizer userAuthorizer) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.state = state;
        this.scope = scope;
        this.userAuthorizer = userAuthorizer;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public UserAuthorizer getUserAuthorizer() {
        return userAuthorizer;
    }

    public void setUserAuthorizer(UserAuthorizer userAuthorizer) {
        this.userAuthorizer = userAuthorizer;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
