package googleadstest.domain.model;

import com.google.ads.googleads.v8.resources.CustomerClient;

import java.util.ArrayList;
import java.util.List;

public class GoogleAccountResponse {

    private String descriptiveName;
    private String timezone;
    private String currencyCode;
    private boolean manager;

    public GoogleAccountResponse() {}

    public GoogleAccountResponse(CustomerClient acc) {
        this.descriptiveName = acc.getDescriptiveName();
        this.timezone = acc.getTimeZone();
        this.currencyCode = acc.getCurrencyCode();
        this.manager = acc.getManager();
    }

    public String getDescriptiveName() {
        return descriptiveName;
    }

    public void setDescriptiveName(String descriptiveName) {
        this.descriptiveName = descriptiveName;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public boolean isManager() {
        return manager;
    }

    public void setManager(boolean manager) {
        this.manager = manager;
    }
}
