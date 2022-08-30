package org.wargamer2010.signshop.configuration.storage.database;

import org.wargamer2010.signshop.configuration.storage.database.models.SignShopSchema;

public interface InternalDatabase {
    SignShopSchema getSchema();
    void saveSchema();
}
