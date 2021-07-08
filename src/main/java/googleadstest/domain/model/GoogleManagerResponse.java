package googleadstest.domain.model;

import com.google.ads.googleads.v8.resources.CustomerClient;

import java.util.ArrayList;
import java.util.List;

public class GoogleManagerResponse extends GoogleAccountResponse {

    private final List<GoogleAccountResponse> childAccounts = new ArrayList<>();

    public GoogleManagerResponse(CustomerClient acc) {
        super(acc);
    }

    public List<GoogleAccountResponse> getChildAccounts() {
        return childAccounts;
    }
}
