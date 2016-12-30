package com.distelli.europa;

import java.io.File;
import java.io.ByteArrayOutputStream;
import com.distelli.europa.guice.EuropaInjectorModule;
import com.distelli.persistence.impl.PersistenceModule;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.distelli.europa.EuropaConfiguration;
import com.distelli.ventura.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.distelli.europa.handlers.RegistryVersionCheck;
import com.distelli.europa.handlers.RegistryBase;
import com.distelli.ventura.RequestContext;
import com.distelli.ventura.WebResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import com.distelli.ventura.HTTPMethod;
import static org.mockito.Mockito.*;

public class TestRegistryAPI {
    
    private static Injector INJECTOR = createInjector();
    private static ObjectMapper OM = new ObjectMapper();

    private static Injector createInjector() {
        String path = System.getenv("EUROPA_CONFIG");
        if ( null == path ) return null;
        File file = new File(path);
        if ( ! file.exists() ) return null;
        EuropaConfiguration config = EuropaConfiguration.fromFile(file);
        return Guice.createInjector(
            new PersistenceModule(),
            new EuropaInjectorModule(
                config));
    }

    private WebServlet servlet;
    
    @Before
    public void before() throws Exception {
        if ( null == INJECTOR ) {
            throw new RuntimeException("EUROPA_CONFIG environment variable must point to valid file");
        }
        servlet = new WebServlet(
            Routes.getRouteMatcher(),
            (route) -> INJECTOR.getInstance(route.getRequestHandler()));
    }

    @Test
    public void testVersionCheck() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        ServletByteArrayOutputStream out = new ServletByteArrayOutputStream();
        when(request.getRequestURI()).thenReturn("/v2/");
        when(response.getOutputStream()).thenReturn(out);

        servlet.handleRequest(HTTPMethod.GET, request, response);

        verify(response).setStatus(200);
        verify(response).setHeader("Docker-Distribution-API-Version", "registry/2.0");

        assertEquals(
            "",
            out.toString());
    }

    public static class ErrorMessage {
        public int code;
        public String message;
        public Object detail;
    }

    @Test
    public void testISE() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        RegistryVersionCheck rvc = INJECTOR.getInstance(RegistryVersionCheck.class);
        rvc.overrideImplementation(
            new RegistryBase() {
                public WebResponse handleRegistryRequest(RequestContext requestContext) {
                    throw new RuntimeException("system failure");
                }
            });
        try {
            ServletByteArrayOutputStream out = new ServletByteArrayOutputStream();
            when(request.getRequestURI()).thenReturn("/v2/");
            when(response.getOutputStream()).thenReturn(out);

            servlet.handleRequest(HTTPMethod.GET, request, response);

            verify(response).setStatus(500);

            List<ErrorMessage> msgs =
                OM.readValue(out.toString(), new TypeReference<List<ErrorMessage>>(){});
            assertEquals(1, msgs.size());
            assertEquals(500, msgs.get(0).code);
            assertEquals("java.lang.RuntimeException: system failure", msgs.get(0).message);
            assertEquals("class java.lang.RuntimeException", msgs.get(0).detail.toString());
        } finally {
            rvc.overrideImplementation(null);
        }
    }
}
