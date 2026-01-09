package com.zhlearn.e2e.wiremock;

import java.util.EnumSet;
import java.util.Set;

/**
 * Configuration for which providers should be mocked vs called.
 *
 * <p>Usage examples: - CI: mock ALL providers - Local dev: mock expensive providers, call cheap
 * ones - New provider testing: mock all EXCEPT the new one
 */
public class ProviderMockConfig {

    public enum Provider {
        OPENROUTER("openrouter.ai"),
        DEEPSEEK("api.deepseek.com"),
        DASHSCOPE("dashscope-intl.aliyuncs.com"),
        ZHIPU("open.bigmodel.cn"),
        MINIMAX("api.minimax.chat"),
        FORVO("apifree.forvo.com"),
        TENCENT("tts.tencentcloudapi.com");

        private final String hostname;

        Provider(String hostname) {
            this.hostname = hostname;
        }

        public String getHostname() {
            return hostname;
        }
    }

    private final Set<Provider> mockedProviders;
    private final boolean recordUnmocked;

    private ProviderMockConfig(Set<Provider> mockedProviders, boolean recordUnmocked) {
        this.mockedProviders =
                mockedProviders.isEmpty()
                        ? EnumSet.noneOf(Provider.class)
                        : EnumSet.copyOf(mockedProviders);
        this.recordUnmocked = recordUnmocked;
    }

    public static ProviderMockConfig mockAll() {
        return new ProviderMockConfig(EnumSet.allOf(Provider.class), false);
    }

    public static ProviderMockConfig mockNone() {
        return new ProviderMockConfig(EnumSet.noneOf(Provider.class), false);
    }

    public static ProviderMockConfig mockAllExcept(Provider... realProviders) {
        Set<Provider> mocked = EnumSet.allOf(Provider.class);
        for (Provider p : realProviders) {
            mocked.remove(p);
        }
        return new ProviderMockConfig(mocked, false);
    }

    public static ProviderMockConfig mockOnly(Provider... mockedProviders) {
        if (mockedProviders.length == 0) {
            return new ProviderMockConfig(EnumSet.noneOf(Provider.class), false);
        }
        return new ProviderMockConfig(EnumSet.copyOf(Set.of(mockedProviders)), false);
    }

    public ProviderMockConfig withRecording() {
        return new ProviderMockConfig(this.mockedProviders, true);
    }

    public boolean isMocked(Provider provider) {
        return mockedProviders.contains(provider);
    }

    public boolean shouldRecord() {
        return recordUnmocked;
    }

    public Set<Provider> getMockedProviders() {
        return mockedProviders.isEmpty()
                ? EnumSet.noneOf(Provider.class)
                : EnumSet.copyOf(mockedProviders);
    }

    public boolean hasAnyMocked() {
        return !mockedProviders.isEmpty();
    }

    /**
     * Parse mock configuration from system property.
     *
     * <p>Examples: -Dzhlearn.e2e.mock=all (mock all providers) -Dzhlearn.e2e.mock=none (call all
     * real APIs) -Dzhlearn.e2e.mock=all-except:MINIMAX (mock all except MiniMax)
     * -Dzhlearn.e2e.mock=only:OPENROUTER,DEEPSEEK (mock only these) -Dzhlearn.e2e.record=true
     * (enable recording for unmocked)
     */
    public static ProviderMockConfig fromSystemProperties() {
        String mockProp = System.getProperty("zhlearn.e2e.mock", "all");
        boolean record = Boolean.parseBoolean(System.getProperty("zhlearn.e2e.record", "false"));

        ProviderMockConfig config;

        if (mockProp.equals("all")) {
            config = mockAll();
        } else if (mockProp.equals("none")) {
            config = mockNone();
        } else if (mockProp.startsWith("all-except:")) {
            String[] providers = mockProp.substring("all-except:".length()).split(",");
            Provider[] realProviders = new Provider[providers.length];
            for (int i = 0; i < providers.length; i++) {
                realProviders[i] = Provider.valueOf(providers[i].trim().toUpperCase());
            }
            config = mockAllExcept(realProviders);
        } else if (mockProp.startsWith("only:")) {
            String[] providers = mockProp.substring("only:".length()).split(",");
            Provider[] mockedProviders = new Provider[providers.length];
            for (int i = 0; i < providers.length; i++) {
                mockedProviders[i] = Provider.valueOf(providers[i].trim().toUpperCase());
            }
            config = mockOnly(mockedProviders);
        } else {
            throw new IllegalArgumentException(
                    "Invalid mock config: "
                            + mockProp
                            + ". Valid values: all, none, all-except:<providers>, only:<providers>");
        }

        return record ? config.withRecording() : config;
    }

    @Override
    public String toString() {
        return "ProviderMockConfig{"
                + "mockedProviders="
                + mockedProviders
                + ", recordUnmocked="
                + recordUnmocked
                + '}';
    }
}
