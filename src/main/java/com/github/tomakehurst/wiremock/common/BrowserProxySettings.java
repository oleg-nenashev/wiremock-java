package com.github.tomakehurst.wiremock.common;

import com.github.tomakehurst.wiremock.common.ssl.AbstractKeyStoreSource;
import com.github.tomakehurst.wiremock.common.ssl.FileOrClasspathKeyStoreSource;
import com.github.tomakehurst.wiremock.common.ssl.KeyStoreSettings;

import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;

public final class BrowserProxySettings {

    public static final String DEFAULT_CA_KEYSTORE_PATH = Paths.get(
            System.getProperty("user.home"))
            .resolve(".wiremock")
            .resolve("ca-keystore.jks")
            .toFile().getAbsolutePath();
    public static final String DEFAULT_CA_KESTORE_PASSWORD = "password";

    public static BrowserProxySettings DISABLED = new Builder().build();

    private final boolean enabled;
    private final boolean trustAllProxyTargets;
    private final List<String> trustedProxyTargets;
    private final KeyStoreSettings caKeyStoreSettings;

    public BrowserProxySettings(
        boolean enabled,
        boolean trustAllProxyTargets,
        List<String> trustedProxyTargets,
        KeyStoreSettings caKeyStoreSettings
    ) {
        this.enabled = enabled;
        this.trustAllProxyTargets = trustAllProxyTargets;
        this.trustedProxyTargets = trustedProxyTargets;
        this.caKeyStoreSettings = caKeyStoreSettings;
    }

    public boolean enabled() {
        return enabled;
    }

    public boolean trustAllProxyTargets() {
        return trustAllProxyTargets;
    }

    public List<String> trustedProxyTargets() {
        return trustedProxyTargets;
    }

    public KeyStoreSettings caKeyStore() {
        return caKeyStoreSettings;
    }

    @Override
    public String toString() {
        return "BrowserProxySettings{" +
                "enabled=" + enabled +
                ", trustAllProxyTargets=" + trustAllProxyTargets +
                ", trustedProxyTargets=" + trustedProxyTargets +
                ", caKeyStore='" + caKeyStoreSettings.path() + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BrowserProxySettings that = (BrowserProxySettings) o;
        return enabled == that.enabled &&
                trustAllProxyTargets == that.trustAllProxyTargets &&
                Objects.equals(trustedProxyTargets, that.trustedProxyTargets) &&
                Objects.equals(caKeyStoreSettings, that.caKeyStoreSettings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, trustAllProxyTargets, trustedProxyTargets, caKeyStoreSettings);
    }

    public static final class Builder {

        private boolean enabled = false;
        private boolean trustAllProxyTargets = false;
        private List<String> trustedProxyTargets = emptyList();

        private KeyStoreSettings caKeyStoreSettings = KeyStoreSettings.NO_STORE;

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder trustAllProxyTargets(boolean trustAllProxyTargets) {
            this.trustAllProxyTargets = trustAllProxyTargets;
            return this;
        }

        public Builder trustedProxyTargets(List<String> trustedProxyTargets) {
            this.trustedProxyTargets = trustedProxyTargets;
            return this;
        }

        public Builder caKeyStoreSettings(KeyStoreSettings caKeyStoreSettings) {
            this.caKeyStoreSettings = caKeyStoreSettings;
            return this;
        }

        public BrowserProxySettings build() {
            return new BrowserProxySettings(enabled, trustAllProxyTargets, trustedProxyTargets, caKeyStoreSettings);
        }
    }
}
