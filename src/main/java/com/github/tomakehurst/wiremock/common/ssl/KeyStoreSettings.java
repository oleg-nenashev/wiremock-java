/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.common.ssl;

import java.security.KeyStore;

public class KeyStoreSettings {

    public static final KeyStoreSettings NO_STORE = new KeyStoreSettings(null, null, null);

    private final AbstractKeyStoreSource keyStoreSource;

    public KeyStoreSettings(AbstractKeyStoreSource keyStoreSource) {
        this.keyStoreSource = keyStoreSource;
    }

    public KeyStoreSettings(String path, String password, String type) {
        this(
            path != null && password != null && type != null ? new FileOrClasspathKeyStoreSource(
                path,
                type,
                password.toCharArray()) :
            null
        );
    }

    public String path() {
        if (keyStoreSource instanceof FileOrClasspathKeyStoreSource) {
            return ((FileOrClasspathKeyStoreSource) keyStoreSource).getPath();
        }

        return "(no path - custom keystore source)";
    }

    public String password() {
        return new String(keyStoreSource.getKeyStorePassword());
    }

    public String type() {
        return keyStoreSource.getKeyStoreType();
    }

    public KeyStore loadStore() {
        return keyStoreSource.load();
    }

    public boolean exists() {
        return keyStoreSource.exists();
    }
}
