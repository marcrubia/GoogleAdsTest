package googleadstest.domain.model;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractGoogleAdsResponse {
    public List<GoogleAdsError> errors;

    public AbstractGoogleAdsResponse() {
        this.errors = new ArrayList<>();
    }

    public List<GoogleAdsError> getErrors() {
        return errors;
    }
}
