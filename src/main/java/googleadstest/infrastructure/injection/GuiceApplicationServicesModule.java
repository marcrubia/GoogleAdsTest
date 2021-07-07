package googleadstest.infrastructure.injection;

import billy.core.logger.Logger;
import billy.core.logger.implementation.LoggerFactory;
import com.google.inject.AbstractModule;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

public class GuiceApplicationServicesModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(GuiceApplicationServicesModule.class);

    @Override
    protected void configure() {}

    @Provider
    public static class BadURIExceptionMapper implements ExceptionMapper<NotFoundException> {
        public Response toResponse(NotFoundException exception){
            return Response.status(Response.Status.NOT_FOUND).
                    entity("Nothing to see here.").
                    build();
        }
    }

    @Provider
    public static class InternalServerErrorExceptionMapper implements ExceptionMapper<Exception> {
        public Response toResponse(Exception exception){
            log.error(exception);
            exception.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                    entity("Server error.").
                    build();
        }
    }
}