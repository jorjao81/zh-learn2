package com.zhlearn.e2e;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.zhlearn.e2e.wiremock.E2EWireMockServer;
import com.zhlearn.e2e.wiremock.ProviderMockConfig;
import com.zhlearn.e2e.wiremock.ProviderMockConfig.Provider;
import com.zhlearn.e2e.wiremock.ProviderStubLoader;

import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;

/** Cucumber hooks for WireMock lifecycle management. */
public class WireMockHooks {

    // Shared state for the test run
    private static E2EWireMockServer wireMockServer;
    private static ProviderMockConfig mockConfig;
    private static boolean initialized = false;
    private static Map<String, String> baseUrlOverrides = new HashMap<>();

    // Environment variable names for each provider's base URL
    private static final Map<Provider, String> PROVIDER_BASE_URL_ENV_VARS =
            Map.of(
                    Provider.OPENROUTER, "OPENROUTER_BASE_URL",
                    Provider.DEEPSEEK, "DEEPSEEK_BASE_URL",
                    Provider.DASHSCOPE, "DASHSCOPE_BASE_URL",
                    Provider.ZHIPU, "ZHIPU_BASE_URL",
                    Provider.MINIMAX, "MINIMAX_BASE_URL",
                    Provider.FORVO, "FORVO_BASE_URL");

    @BeforeAll
    public static void startWireMock() throws Exception {
        if (initialized) {
            return;
        }
        initialized = true;

        // Parse configuration
        mockConfig = ProviderMockConfig.fromSystemProperties();
        System.out.println("[WireMock] Configuration: " + mockConfig);

        // Only start WireMock if we're mocking something
        if (mockConfig.hasAnyMocked() || mockConfig.shouldRecord()) {
            Path testResourcesDir = Paths.get("src/test/resources");

            wireMockServer = new E2EWireMockServer(testResourcesDir);
            wireMockServer.start();

            System.out.println("[WireMock] Started on port " + wireMockServer.getPort());

            // Build BASE_URL overrides for mocked providers
            String baseUrl = wireMockServer.getBaseUrl();
            for (Provider provider : Provider.values()) {
                if (mockConfig.isMocked(provider)) {
                    String envVar = PROVIDER_BASE_URL_ENV_VARS.get(provider);
                    if (envVar != null) {
                        baseUrlOverrides.put(envVar, baseUrl);
                        System.out.println(
                                "[WireMock] Mocking "
                                        + provider
                                        + " via "
                                        + envVar
                                        + "="
                                        + baseUrl);
                    }
                }
            }

            // Load stubs
            ProviderStubLoader stubLoader =
                    new ProviderStubLoader(
                            wireMockServer.getServer(),
                            testResourcesDir.resolve("wiremock/mappings"));
            stubLoader.configure(mockConfig);
        } else {
            System.out.println("[WireMock] Disabled - all providers will use real APIs");
        }
    }

    @AfterAll
    public static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
            System.out.println("[WireMock] Stopped");
        }
    }

    /** Returns the BASE_URL environment variable overrides for mocked providers. */
    public static Map<String, String> getBaseUrlOverrides() {
        return new HashMap<>(baseUrlOverrides);
    }

    /** Returns the current mock configuration. */
    public static ProviderMockConfig getMockConfig() {
        return mockConfig;
    }

    /** Returns true if WireMock is active and mocking requests. */
    public static boolean isActive() {
        return wireMockServer != null;
    }

    /** Returns the WireMock server instance for advanced configuration. */
    public static E2EWireMockServer getServer() {
        return wireMockServer;
    }
}
