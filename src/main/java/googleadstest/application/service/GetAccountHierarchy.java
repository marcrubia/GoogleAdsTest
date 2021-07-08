package googleadstest.application.service;

import com.google.ads.googleads.lib.GoogleAdsClient;
import com.google.ads.googleads.v8.errors.GoogleAdsError;
import com.google.ads.googleads.v8.errors.GoogleAdsException;
import com.google.ads.googleads.v8.resources.CustomerClient;
import com.google.ads.googleads.v8.resources.CustomerName;
import com.google.ads.googleads.v8.services.*;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import googleadstest.domain.model.GoogleAccountResponse;
import googleadstest.domain.model.GoogleHierarchyResponse;
import googleadstest.domain.model.GoogleManagerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Gets the account hierarchy of the specified manager account and login customer ID. If you don't
 * specify manager ID and login customer ID, the example will instead print the hierarchies of all
 * accessible customer accounts for your authenticated Google account. Note that if the list of
 * accessible customers for your authenticated Google account includes accounts within the same
 * hierarchy, this example will retrieve and print the overlapping portions of the hierarchy for
 * each accessible customer.
 */
public class GetAccountHierarchy {

    private static final Logger log = LoggerFactory.getLogger(GetAccountHierarchy.class);

    public GoogleHierarchyResponse getAccounts() {
        return getAccounts(null, null);
    }

    public GoogleHierarchyResponse getAccounts(Long managerId, Long accountId) {
        GoogleHierarchyResponse response = null;
        Properties adsProperties = OAuth2Service.clientProperties;
        if (managerId != null) {
            adsProperties.put(GoogleAdsClient.Builder.ConfigPropertyKey.LOGIN_CUSTOMER_ID.getPropertyKey(), Long.toString(managerId));
        }

        GoogleAdsClient googleAdsClient = GoogleAdsClient.newBuilder().fromProperties(adsProperties).build();

        try {
            response = getHierarchy(googleAdsClient, managerId, accountId);
        } catch (GoogleAdsException gae) {
            // GoogleAdsException is the base class for most exceptions thrown by an API request.
            // Instances of this exception have a message and a GoogleAdsFailure that contains a
            // collection of GoogleAdsErrors that indicate the underlying causes of the
            // GoogleAdsException.
            log.error("Error getting google ads hierarchy by the next errors: ");
            int i = 0;
            for (GoogleAdsError googleAdsError : gae.getGoogleAdsFailure().getErrorsList()) {
                log.error("  Error %d"+(i++)+": "+ googleAdsError);
            }
        } catch (IOException ioe) {
            log.error("Error getting google ads hierarchy", ioe);
        }

        return response;
    }

    private GoogleHierarchyResponse getHierarchy(GoogleAdsClient googleAdsClient, Long managerId, Long loginCustomerId)
            throws IOException {
        List<Long> seedCustomerIds = new ArrayList<>();
        if (managerId == null) {
            // Gets the account hierarchies for all accessible customers.
            seedCustomerIds = getAccessibleCustomers(googleAdsClient);
        } else {
            // Only gets the hierarchy for the provided manager ID if provided.
            seedCustomerIds.add(managerId);
        }

        GoogleHierarchyResponse response = new GoogleHierarchyResponse();

        Map<CustomerClient, Multimap<Long, CustomerClient>> allHierarchies = new HashMap<>();
        // Constructs a map of account hierarchies.
        for (Long seedCustomerId : seedCustomerIds) {
            Map<CustomerClient, Multimap<Long, CustomerClient>> customerClientToHierarchy =
                    createCustomerClientToHierarchy(loginCustomerId, seedCustomerId);

            if (customerClientToHierarchy == null) {
                response.getAccountsWithNoInfo().add(seedCustomerId);
            } else {
                allHierarchies.putAll(customerClientToHierarchy);
            }
        }

        int depth = 0;
        // Prints the hierarchy information for all accounts for which there is hierarchy information
        // available.
        List<GoogleAccountResponse> hierarchy = new ArrayList<>();
        for (CustomerClient rootCustomerClient : allHierarchies.keySet()) {
            GoogleManagerResponse manager = new GoogleManagerResponse(rootCustomerClient);
            Multimap<Long, CustomerClient> rootHierarchy = allHierarchies.get(rootCustomerClient);

            mapChilds(manager, rootCustomerClient.getId(), rootHierarchy);

            hierarchy.add(manager);
        }

        response.setHierarchy(hierarchy);

        return response;
    }

    private void mapChilds(GoogleManagerResponse manager, Long managerId, Multimap<Long, CustomerClient> rootHierarchy) {
        for (CustomerClient childCustomer : rootHierarchy.get(managerId)) {
            if (rootHierarchy.get(childCustomer.getId()).isEmpty()) {
                GoogleAccountResponse child = new GoogleAccountResponse(childCustomer);
                manager.getChildAccounts().add(child);
            } else {
                GoogleManagerResponse childManager = new GoogleManagerResponse(childCustomer);
                mapChilds(childManager, childCustomer.getId(), rootHierarchy);
                manager.getChildAccounts().add(childManager);
            }
        }
    }

    /**
     * Creates a map between a CustomerClient and each of its managers' mappings.
     *
     * @param loginCustomerId the loginCustomerId used to create the GoogleAdsClient.
     * @param seedCustomerId the ID of the customer at the root of the tree.
     * @return a map between a CustomerClient and each of its managers' mappings if the account
     *     hierarchy can be retrieved. If the account hierarchy cannot be retrieved, returns null.
     * @throws IOException if a Google Ads Client is not successfully created.
     */
    private Map<CustomerClient, Multimap<Long, CustomerClient>> createCustomerClientToHierarchy(
            Long loginCustomerId, long seedCustomerId) throws IOException {
        Queue<Long> managerAccountsToSearch = new LinkedList<>();
        CustomerClient rootCustomerClient = null;

        // Creates a GoogleAdsClient with the specified loginCustomerId. See
        // https://developers.google.com/google-ads/api/docs/concepts/call-structure#cid for more
        // information.
        GoogleAdsClient googleAdsClient =
                GoogleAdsClient.newBuilder()
                        .fromPropertiesFile()
                        .setLoginCustomerId(loginCustomerId == null ? seedCustomerId : loginCustomerId)
                        .build();

        // Creates the Google Ads Service client.
        try (GoogleAdsServiceClient googleAdsServiceClient =
                     googleAdsClient.getLatestVersion().createGoogleAdsServiceClient()) {
            // Creates a query that retrieves all child accounts of the manager specified in search
            // calls below.
            String query =
                    "SELECT customer_client.client_customer, customer_client.level, "
                            + "customer_client.manager, customer_client.descriptive_name, "
                            + "customer_client.currency_code, customer_client.time_zone, "
                            + "customer_client.id "
                            + "FROM customer_client "
                            + "WHERE customer_client.level <= 1";

            // Adds the seed customer ID to the list of IDs to be processed.
            managerAccountsToSearch.add(seedCustomerId);
            // Performs a breadth-first search algorithm to build a mapping of managers to their
            // child accounts.
            Multimap<Long, CustomerClient> customerIdsToChildAccounts = ArrayListMultimap.create();
            while (!managerAccountsToSearch.isEmpty()) {
                long customerIdToSearchFrom = managerAccountsToSearch.poll();
                GoogleAdsServiceClient.SearchPagedResponse response;
                try {
                    // Issues a search request.
                    response =
                            googleAdsServiceClient.search(
                                    SearchGoogleAdsRequest.newBuilder()
                                            .setQuery(query)
                                            .setCustomerId(Long.toString(customerIdToSearchFrom))
                                            .build());

                    // Iterates over all rows in all pages to get all customer clients under the specified
                    // customer's hierarchy.
                    for (GoogleAdsRow googleAdsRow : response.iterateAll()) {
                        CustomerClient customerClient = googleAdsRow.getCustomerClient();

                        // Gets the CustomerClient object for the root customer in the tree.
                        if (customerClient.getId() == seedCustomerId) {
                            rootCustomerClient = customerClient;
                        }

                        // The steps below map parent and children accounts. Continue here so that manager
                        // accounts exclude themselves from the list of their children accounts.
                        if (customerClient.getId() == customerIdToSearchFrom) {
                            continue;
                        }

                        // For all level-1 (direct child) accounts that are manager accounts, the above
                        // query will be run against them to create a map of managers to their
                        // child accounts for printing the hierarchy afterwards.
                        customerIdsToChildAccounts.put(customerIdToSearchFrom, customerClient);
                        // Checks if the child account is a manager itself so that it can later be processed
                        // and added to the map if it hasn't been already.
                        if (customerClient.getManager()) {
                            // A customer can be managed by multiple managers, so to prevent visiting the same
                            // customer multiple times, we need to check if it's already in the map.
                            boolean alreadyVisited =
                                    customerIdsToChildAccounts.containsKey(customerClient.getId());
                            if (!alreadyVisited && customerClient.getLevel() == 1) {
                                managerAccountsToSearch.add(customerClient.getId());
                            }
                        }
                    }
                } catch (GoogleAdsException gae) {
                    log.error("Unable to retrieve hierarchy for customer ID "+customerIdToSearchFrom+": "+gae.getGoogleAdsFailure().getErrors(0).getMessage());
                    return null;
                }
            }

            // The rootCustomerClient will be null if the account hierarchy was unable to be retrieved
            // (e.g. the account is a test account or a client account with an incomplete billing setup).
            // This method returns null in these cases to add the seedCustomerId to the list of
            // customer IDs for which the account hierarchy could not be retrieved.
            if (rootCustomerClient == null) {
                return null;
            }

            Map<CustomerClient, Multimap<Long, CustomerClient>> customerClientToHierarchy =
                    new HashMap<>();
            customerClientToHierarchy.put(rootCustomerClient, customerIdsToChildAccounts);
            return customerClientToHierarchy;
        }
    }

    /**
     * Retrieves a list of accessible customers with the provided set up credentials.
     *
     * @param googleAdsClient the Google Ads API client.
     * @return a list of customer IDs.
     */
    private List<Long> getAccessibleCustomers(GoogleAdsClient googleAdsClient) {
        List<Long> seedCustomerIds = new ArrayList<>();
        // Issues a request for listing all accessible customers by this authenticated Google account.
        try (CustomerServiceClient customerServiceClient =
                     googleAdsClient.getLatestVersion().createCustomerServiceClient()) {
            ListAccessibleCustomersResponse accessibleCustomers =
                    customerServiceClient.listAccessibleCustomers(
                            ListAccessibleCustomersRequest.newBuilder().build());

            for (String customerResourceName : accessibleCustomers.getResourceNamesList()) {
                Long customer = Long.parseLong(CustomerName.parse(customerResourceName).getCustomerId());
                seedCustomerIds.add(customer);
            }
        }
        return seedCustomerIds;
    }
}
