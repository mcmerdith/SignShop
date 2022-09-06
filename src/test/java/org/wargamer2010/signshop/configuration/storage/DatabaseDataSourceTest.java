package org.wargamer2010.signshop.configuration.storage;

import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.hibernate.tool.schema.Action;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.DataSourceType;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.configuration.storage.database.util.DatabaseUtil;
import org.wargamer2010.signshop.mocks.TestBlock;
import org.wargamer2010.signshop.mocks.TestPlayer;
import org.wargamer2010.signshop.mocks.TestServer;
import org.wargamer2010.signshop.mocks.TestWorld;
import org.wargamer2010.signshop.player.PlayerIdentifier;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class DatabaseDataSourceTest {
    private static final boolean wipeForTest = true;

    private static final DataSourceType testingType = DataSourceType.MARIADB;
    private static final String testDBUser = "signshop";
    private static final String testDBPassword = "signshop";

    static class SellerData {
        final PlayerIdentifier playerId;
        final String sWorld;
        final Block bSign;
        final List<Block> containables;
        final List<Block> activatables;
        final ItemStack[] isItems;
        final Map<String, String> misc;

        public SellerData(PlayerIdentifier playerId, String sWorld, Block bSign, List<Block> containables, List<Block> activatables, ItemStack[] isItems, Map<String, String> misc) {
            this.playerId = playerId;
            this.sWorld = sWorld;
            this.bSign = bSign;
            this.containables = containables;
            this.activatables = activatables;
            this.isItems = isItems;
            this.misc = misc;
        }
    }

    private static final World testWorld = new TestWorld("world", "6b0dd938-e0a5-4346-a983-7b6472606da4", null, null);

    private static final Player[] testPlayers = {
            new TestPlayer(testWorld, 5, 60, 10, "a8ae1005-73e3-49ba-b94e-bbf5143451bb", "mcmerdith", true, true, false)
    };

    private static final Supplier<IntStream> signXPositions = () -> IntStream.range(1, 7);

    // Generate repeatable signs to test
    private static final List<Block> signs = signXPositions.get()
            .mapToObj(x -> new TestBlock(testWorld, x, 60, 0, Material.SIGN, null))
            .collect(Collectors.toList());

    private static final List<List<Block>> containables = signXPositions.get()
            .mapToObj(x -> {
                List<Block> c = new ArrayList<>();

                for (int z = 0; z < x; z++) {
                    c.add(new TestBlock(testWorld, x, 61, z, Material.CHEST, null));
                }

                return c;
            })
            .collect(Collectors.toList());

    private static final List<List<Block>> activatables = signXPositions.get()
            .mapToObj(x -> {
                List<Block> c = new ArrayList<>();

                for (int z = 0; z < x; z++) {
                    c.add(new TestBlock(testWorld, x, 62, z, Material.CHEST, null));
                }

                return c;
            })
            .collect(Collectors.toList());

    public static final List<ItemStack[]> isItems = signXPositions.get()
            .mapToObj(x -> {
                ItemStack[] stacks = new ItemStack[x];

                for (int z = 0; z < x; z++) {
                    stacks[z] = new ItemStack(Material.values()[z]);
                }

                return stacks;
            })
            .collect(Collectors.toList());

    private static final List<Map<String, String>> misc = Collections.nCopies(signs.size(), Collections.emptyMap());

    private static final SellerData[] testSellersData = new SellerData[signs.size()];
    private static final Seller[] testSellers = new Seller[signs.size()];

    static {
        // Make the logs easier to read
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] [%4$-7s] %5$s %n");

        // Reflect some settings into SignShopConfig
        try {
            FieldUtils.writeField(SignShopConfig.class.getDeclaredField("DataSource"), SignShop.class, testingType, true);
            FieldUtils.writeField(SignShopConfig.class.getDeclaredField("Database_Authentication_Username"), SignShop.class, testDBUser, true);
            FieldUtils.writeField(SignShopConfig.class.getDeclaredField("Database_Authentication_Password"), SignShop.class, testDBPassword, true);
        } catch (Exception e) {
            System.out.println("Failed to reflect values into SignShopConfig! This test will run with default settings");
        }

        // Fake the server
        Bukkit.setServer(TestServer.getServer());

        // Compile the signs
        for (int i = 0; i < signs.size(); i++) {
            SellerData data = new SellerData(
                    new PlayerIdentifier(testPlayers[0]),
                    testWorld.getName(),
                    signs.get(i),
                    containables.get(i),
                    activatables.get(i),
                    isItems.get(i),
                    misc.get(i)
            );

            testSellersData[i] = data;

            testSellers[i] = new Seller(
                    data.playerId,
                    data.sWorld,
                    data.containables,
                    data.activatables,
                    data.isItems,
                    data.bSign.getLocation(),
                    data.misc,
                    false
            );
        }
    }

    @Before
    public void setUp() throws Exception {
        if (wipeForTest && SignShopConfig.getDataSource() != DataSourceType.YML) {
            // Wipe the database clean
            Properties props = DatabaseUtil.getDatabaseProperties(false);
            props.put("hibernate.hbm2ddl.auto", Action.CREATE.getExternalHbm2ddlName());
            new DatabaseDataSource(SignShopConfig.getDataSource(), props).dispose();
        }

        Storage.init();
    }

    @After
    public void tearDown() throws Exception {
        try {
            Storage.get().dispose();
        } catch (Exception ignored) {
        }
    }

    @Test
    public void runTasks() {
        addSeller();
    }

    public void addSeller() {
        for (SellerData data : testSellersData) {
            Storage.get().addSeller(data.playerId, data.sWorld, data.bSign, data.containables, data.activatables, data.isItems, data.misc);
        }

        Collection<Seller> stored = Storage.get().getSellers();

        for (Seller seller : testSellers) {
            Seller storedSeller = stored.stream().filter(s -> s.getSignLocation().equals(seller.getSignLocation())).findAny().orElse(null);

            assertNotNull(String.format("Database did not contain seller %s", seller.getSignLocation().toString()),
                    storedSeller);

            assertArrayEquals("ItemStacks saved incorrectly!",
                    storedSeller.getItems(), seller.getItems());

            for (Block b : seller.getContainables()) {
                Block storedBlock = storedSeller.getContainables().stream().filter(c -> c.equals(b)).findAny().orElse(null);

                assertNotNull("Containable was not stored!", storedBlock);

                assertEquals("Containble does not match!",
                        b, storedBlock);
            }
        }
    }

    public void getSeller() {
    }

    public void updateSeller() {
    }

    public void getSellers() {
    }

    public void removeSeller() {
    }

    public void load() {
    }

    public void save() {
    }

    public void shopCount() {
    }

    public void countLocations() {
    }

    public void getSignsFromHolder() {
    }

    public void getShopsByBlock() {
    }

    public void getShopsWithMiscSetting() {
    }
}