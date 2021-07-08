package googleadstest.domain.model;

import java.util.ArrayList;
import java.util.List;

public class GoogleHierarchyResponse {

    private List<Long> accountsWithNoInfo = new ArrayList<>();
    private List<GoogleAccountResponse> hierarchy;

    public GoogleHierarchyResponse() {}

    public List<Long> getAccountsWithNoInfo() {
        return accountsWithNoInfo;
    }

    public List<GoogleAccountResponse> getHierarchy() {
        return hierarchy;
    }

    public void setHierarchy(List<GoogleAccountResponse> hierarchy) {
        this.hierarchy = hierarchy;
    }
}
