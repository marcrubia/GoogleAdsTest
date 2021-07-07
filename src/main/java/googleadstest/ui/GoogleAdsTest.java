package googleadstest.ui;

import billy.core.configmanager.ConfigManager;
import billy.core.http.server.json.GuiceHttpServerJsonModule;
import billy.tracker.commons.injection.infrastructure.GuiceCommonsControllersModule;
import billy.tracker.commons.injection.infrastructure.GuiceCommonsInjectionModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceFilter;
import com.squarespace.jersey2.guice.BootstrapUtils;
import googleadstest.infrastructure.injection.GuiceControllersModule;
import googleadstest.infrastructure.injection.GuiceInfrastructureModule;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import googleadstest.infrastructure.injection.GuiceApplicationServicesModule;

import javax.servlet.DispatcherType;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class GoogleAdsTest {

    private static final Logger log = LoggerFactory.getLogger(GoogleAdsTest.class);


    public static void main(String[] args) {

        try {
            final long applicationStartTime = System.currentTimeMillis();
            GoogleAdsTestConfiguration config = new ConfigManager<>(GoogleAdsTestConfiguration.class, args).load();

            List<Module> moduleList = Arrays.asList(
                    new GuiceHttpServerJsonModule(),
                    new GuiceApplicationServicesModule(),
                    new GuiceCommonsInjectionModule(
                            applicationStartTime,
                            config.MODULE_VERSION,
                            config.MODULE_BUILD_NUMBER,
                            config.LOG_SAMPLING_RATE
                    ),
                    new GuiceCommonsControllersModule(),
                    new GuiceControllersModule(),
                    new GuiceInfrastructureModule(config.SERVICE_PORT, config.SERVER_URL)
            );

            ServiceLocator locator = BootstrapUtils.newServiceLocator();
            Injector injector = BootstrapUtils.newInjector(locator, moduleList);

            BootstrapUtils.install(locator);

            Server server = new Server(config.SERVICE_PORT);

            ResourceConfig resourceConfig = ResourceConfig.forApplication(new GoogleAdsTestApplication());

            ServletContainer servletContainer = new ServletContainer(resourceConfig);

            ServletHolder sh = new ServletHolder(servletContainer);
            ServletContextHandler context = new ServletContextHandler(
                    ServletContextHandler.SESSIONS);
            context.setContextPath("/");

            FilterHolder filterHolder = new FilterHolder(GuiceFilter.class);
            context.addFilter(filterHolder, "/*",
                    EnumSet.allOf(DispatcherType.class));

            GzipHandler gzipHandler = new GzipHandler();
            gzipHandler.setIncludedMethods("GET", "POST");
            gzipHandler.addIncludedMimeTypes("application/json");
            context.setGzipHandler(gzipHandler);

            context.addServlet(sh, "/*");
            server.setHandler(context);

            server.start();
        } catch (Exception e) {
            log.error("Error starting the server ", e);
            System.exit(1);
        }

    }
}
