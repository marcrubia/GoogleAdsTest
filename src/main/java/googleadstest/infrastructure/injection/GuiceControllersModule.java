package googleadstest.infrastructure.injection;

import com.google.inject.AbstractModule;
import googleadstest.ui.controllers.BaseController;

public class GuiceControllersModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(BaseController.class);
    }
}
