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
                    Provider.FORVO, "FORVO_BASE_URL",
                    Provider.TENCENT, "TENCENT_BASE_URL");

    // Environment variable names for each provider's API key
    private static final Map<Provider, String> PROVIDER_API_KEY_ENV_VARS =
            Map.of(
                    Provider.OPENROUTER, "OPENROUTER_API_KEY",
                    Provider.DEEPSEEK, "DEEPSEEK_API_KEY",
                    Provider.DASHSCOPE, "DASHSCOPE_API_KEY",
                    Provider.ZHIPU, "ZHIPU_API_KEY",
                    Provider.MINIMAX, "MINIMAX_API_KEY",
                    Provider.FORVO, "FORVO_API_KEY",
                    Provider.TENCENT, "TENCENT_API_KEY");

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

            // Build BASE_URL and API_KEY overrides for mocked providers
            String baseUrl = wireMockServer.getBaseUrl();
            for (Provider provider : Provider.values()) {
                if (mockConfig.isMocked(provider)) {
                    String baseUrlEnvVar = PROVIDER_BASE_URL_ENV_VARS.get(provider);
                    if (baseUrlEnvVar != null) {
                        baseUrlOverrides.put(baseUrlEnvVar, baseUrl);
                        System.out.println(
                                "[WireMock] Mocking "
                                        + provider
                                        + " via "
                                        + baseUrlEnvVar
                                        + "="
                                        + baseUrl);
                    }
                    // Also set dummy API key so the application doesn't fail validation
                    String apiKeyEnvVar = PROVIDER_API_KEY_ENV_VARS.get(provider);
                    if (apiKeyEnvVar != null) {
                        baseUrlOverrides.put(apiKeyEnvVar, "dummy-test-key-for-wiremock");
                    }
                    // MiniMax also requires GROUP_ID
                    if (provider == Provider.MINIMAX) {
                        baseUrlOverrides.put("MINIMAX_GROUP_ID", "dummy-group-id-for-wiremock");
                    }
                    // Tencent also requires SECRET_ID and REGION
                    if (provider == Provider.TENCENT) {
                        baseUrlOverrides.put("TENCENT_SECRET_ID", "dummy-secret-id-for-wiremock");
                        baseUrlOverrides.put("TENCENT_REGION", "ap-guangzhou");
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
