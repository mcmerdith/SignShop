package org.wargamer2010.signshop.configuration.storage;

import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;

public class DatabaseDataSourceTest {
    private final SignShopConfig.DataSourceType testingType = SignShopConfig.DataSourceType.MARIADB;
    private final String testDBUser = "signshop";
    private final String testDBPassword = "signshop";

    @Before
    public void setUp() throws Exception {
        // Make the logs easier to read
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] [%4$-7s] %5$s %n");

        // Reflect some settings into SignShopConfig
        FieldUtils.writeField(SignShopConfig.class.getDeclaredField("DataSource"), SignShop.class, testingType, true);
        FieldUtils.writeField(SignShopConfig.class.getDeclaredField("Database_Authentication_Username"), SignShop.class, testDBUser, true);
        FieldUtils.writeField(SignShopConfig.class.getDeclaredField("Database_Authentication_Password"), SignShop.class, testDBPassword, true);

        Storage.init();
    }

    @After
    public void tearDown() throws Exception {
        try {
            Storage.get().dispose();
        } catch (Exception ignored) {}
    }

    @Test
    public void load() {
    }

    @Test
    public void save() {
    }

    @Test
    public void dispose() {
    }

    @Test
    public void shopCount() {
    }

    @Test
    public void addSeller() {
    }

    @Test
    public void updateSeller() {
    }

    @Test
    public void getSeller() {
    }

    @Test
    public void getSellers() {
    }

    @Test
    public void removeSeller() {
    }

    @Test
    public void countLocations() {
    }

    @Test
    public void getSignsFromHolder() {
    }

    @Test
    public void getShopsByBlock() {
    }

    @Test
    public void getShopsWithMiscSetting() {
    }
}