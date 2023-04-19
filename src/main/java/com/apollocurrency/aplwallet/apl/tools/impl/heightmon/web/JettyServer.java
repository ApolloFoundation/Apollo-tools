/*
 *  Copyright © 2018-2019 Apollo Foundation
 */

package com.apollocurrency.aplwallet.apl.tools.impl.heightmon.web;

import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.HeightMonitorService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.weld.environment.servlet.Listener;
import org.jboss.weld.module.web.servlet.WeldInitialListener;

import java.util.Objects;

@Slf4j
@NoArgsConstructor
public class JettyServer {
    private Server server;
    public static final String rootPathSpec = "/rest/*";

    private HeightMonitorService heightMonitorService;

    public JettyServer(HeightMonitorService heightMonitorService) {
        Objects.requireNonNull(heightMonitorService, "heightMonitorService is NULL");
        this.heightMonitorService = heightMonitorService;
        server = new Server();
        HttpConfiguration configuration = new HttpConfiguration();
        configuration.setSendDateHeader(false);
        configuration.setSendServerVersion(false);
        HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(configuration);

        ServerConnector connector = new ServerConnector(server, httpConnectionFactory);
        connector.setPort(this.heightMonitorService.getConfig().getPeersConfig().getJettyServerPort());
        connector.setHost("0.0.0.0");
        connector.setReuseAddress(true);
        server.addConnector(connector);
        log.debug("Main Jetty ServerConnector = {}", connector);

        ServletContextHandler servletHandler = new ServletContextHandler();
        // --------- ADD REST support servlet (RESTEasy)
        ServletHolder restEasyServletHolder = new ServletHolder(new HttpServletDispatcher());
        restEasyServletHolder.setInitParameter("resteasy.servlet.mapping.prefix", "/rest");
        restEasyServletHolder.setInitParameter("resteasy.injector.factory", "org.jboss.resteasy.cdi.CdiInjectorFactory");

        String restEasyAppClassName = RestEasyApplication.class.getName();
        restEasyServletHolder.setInitParameter("jakarta.ws.rs.Application", restEasyAppClassName);
        servletHandler.addServlet(restEasyServletHolder, rootPathSpec);
        log.debug("Main Jetty REST API root path = '{}'", rootPathSpec);
        // init Weld here
        servletHandler.addEventListener(new WeldInitialListener());
        //need this listener to support scopes properly
        servletHandler.addEventListener(new Listener());

        server.setHandler(servletHandler);
    }

    public void start() {
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public void shutdown() {
        try {
            server.stop();
        } catch (Exception e) {
            throw new RuntimeException(e.toString(), e);
        }
    }
}
