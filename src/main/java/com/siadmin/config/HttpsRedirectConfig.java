package com.siadmin.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@Profile("ssl")
public class HttpsRedirectConfig {

    @Value("${siadmin.http-redirect-port:8080}")
    private int httpRedirectPort;

    @Value("${server.port:8443}")
    private int httpsPort;

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> httpToHttpsRedirect() {
        return factory -> {
            Connector httpConnector = new Connector(org.apache.coyote.http11.Http11NioProtocol.class.getName());
            httpConnector.setScheme("http");
            httpConnector.setPort(httpRedirectPort);
            httpConnector.setSecure(false);
            httpConnector.setRedirectPort(httpsPort);
            factory.addAdditionalConnectors(httpConnector);
        };
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public OncePerRequestFilter httpsEnforcementFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                    throws ServletException, IOException {
                if (!request.isSecure()) {
                    String url = "https://" + request.getServerName() + ":" + httpsPort + request.getRequestURI();
                    if (request.getQueryString() != null) {
                        url += "?" + request.getQueryString();
                    }
                    response.sendRedirect(url);
                    return;
                }
                chain.doFilter(request, response);
            }
        };
    }
}
