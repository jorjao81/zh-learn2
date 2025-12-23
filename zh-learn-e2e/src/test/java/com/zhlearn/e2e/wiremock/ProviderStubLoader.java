package com.zhlearn.e2e.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.zhlearn.e2e.wiremock.ProviderMockConfig.Provider;

/** Loads pre-recorded stubs and configures proxy pass-through for each provider. */
public class ProviderStubLoader {

    private final WireMockServer server;
    private final Path mappingsDir;

    public ProviderStubLoader(WireMockServer server, Path mappingsDir) {
        this.server = server;
        this.mappingsDir = mappingsDir;
    }

    /**
     * Configures WireMock based on the mock configuration. Mocked providers: Load stubs from files
     */
    public void configure(ProviderMockConfig config) throws Exception {
        for (Provider provider : Provider.values()) {
            if (config.isMocked(provider)) {
                loadStubsForProvider(provider);
            }
        }
    }

    private void loadStubsForProvider(Provider provider) throws Exception {
        Path providerStubsDir = mappingsDir.resolve(provider.name().toLowerCase());

        int loadedCount = 0;
        if (Files.exists(providerStubsDir) && Files.isDirectory(providerStubsDir)) {
            try (Stream<Path> files = Files.list(providerStubsDir)) {
                var jsonFiles = files.filter(p -> p.toString().endsWith(".json")).toList();
                for (Path jsonFile : jsonFiles) {
                    String json = Files.readString(jsonFile);
                    StubMapping mapping = StubMapping.buildFrom(json);
                    server.addStubMapping(mapping);
                    loadedCount++;
                }
            }
        }

        if (loadedCount > 0) {
            System.out.println("[WireMock] Loaded " + loadedCount + " stubs for " + provider);
        } else {
            System.out.println(
                    "[WireMock] Warning: No stubs found for "
                            + provider
                            + " at "
                            + providerStubsDir);
        }
    }

    /** Adds a dynamic stub for a specific provider. */
    public void addStub(Provider provider, StubMapping stub) {
        server.addStubMapping(stub);
    }

    /** Removes all stubs for a provider. */
    public void clearStubs(Provider provider) {
        server.removeStubsByMetadata(
                matchingJsonPath("$.metadata.provider", equalTo(provider.name())));
    }
}
