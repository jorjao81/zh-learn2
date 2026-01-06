package com.zhlearn.e2e.wiremock;

import java.nio.file.Path;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

/**
 * Manages WireMock server lifecycle for e2e tests. Runs in standard mode and redirects provider
 * traffic via BASE_URL environment variable overrides.
 */
public class E2EWireMockServer {

    private final WireMockServer server;
    private final Path wireMockRoot;

    public E2EWireMockServer(Path testResourcesDir) {
        this.wireMockRoot = testResourcesDir.resolve("wiremock");

        // Use standard WireMock mode (not browser proxy) for simplicity
        // We'll redirect traffic by setting BASE_URL env vars for each provider
        this.server =
                new WireMockServer(
                        WireMockConfiguration.options()
                                .dynamicPort()
                                .withRootDirectory(wireMockRoot.toString()));
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop();
    }

    public int getPort() {
        return server.port();
    }

    public WireMockServer getServer() {
        return server;
    }

    public Path getWireMockRoot() {
        return wireMockRoot;
    }

    /** Returns the base URL for WireMock server. */
    public String getBaseUrl() {
        return "http://localhost:" + getPort();
    }
}
