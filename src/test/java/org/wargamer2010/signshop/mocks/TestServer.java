package org.wargamer2010.signshop.mocks;

import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.*;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.help.HelpMap;
import org.bukkit.inventory.*;
import org.bukkit.loot.LootTable;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.CachedServerIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TestServer implements Server {
    private static TestServer _instance;

    public static TestServer getServer() {
        if (_instance == null) initialize("SignShop TestServer", "v1.0");

        return _instance;
    }

    public static void initialize(String name, String version) {
        if (_instance != null) throw new IllegalStateException("TestServer has already been initialized!");

        _instance = new TestServer(name, version);
    }

    private TestServer(String name, String version) {
        this.name = name;
        this.version = version;
        this.LOGGER = Logger.getLogger(getName());
    }

    private final Logger LOGGER;

    private final String name;
    private final String version;

    private final List<TestPlayer> playerRegistry = new ArrayList<>();
    private final List<World> worldRegistry = new ArrayList<>();

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public String getVersion() {
        return version;
    }

    @NotNull
    @Override
    public String getBukkitVersion() {
        return version;
    }

    @NotNull
    @Override
    public Collection<? extends Player> getOnlinePlayers() {
        return playerRegistry;
    }

    @Override
    public int getMaxPlayers() {
        return 20;
    }

    @Override
    public int getPort() {
        return 25565;
    }

    @Override
    public int getViewDistance() {
        return 10;
    }

    @NotNull
    @Override
    public String getIp() {
        return "127.0.0.1";
    }

    @NotNull
    @Override
    public String getServerName() {
        return name;
    }

    @NotNull
    @Override
    public String getServerId() {
        return name;
    }

    @NotNull
    @Override
    public String getWorldType() {
        return WorldType.NORMAL.getName();
    }

    @Override
    public boolean getGenerateStructures() {
        return false;
    }

    @Override
    public boolean getAllowEnd() {
        return false;
    }

    @Override
    public boolean getAllowNether() {
        return false;
    }

    @Override
    public boolean hasWhitelist() {
        return false;
    }

    @Override
    public void setWhitelist(boolean value) {

    }

    @NotNull
    @Override
    public Set<OfflinePlayer> getWhitelistedPlayers() {
        return Collections.emptySet();
    }

    @Override
    public void reloadWhitelist() {

    }

    @Override
    public int broadcastMessage(@NotNull String message) {
        return 0;
    }

    @NotNull
    @Override
    public String getUpdateFolder() {
        return getUpdateFolderFile().getName();
    }

    @NotNull
    @Override
    public File getUpdateFolderFile() {
        return new File("update");
    }

    @Override
    public long getConnectionThrottle() {
        return 0;
    }

    @Override
    public int getTicksPerAnimalSpawns() {
        return 0;
    }

    @Override
    public int getTicksPerMonsterSpawns() {
        return 0;
    }

    public void registerPlayer(TestPlayer player) {
        playerRegistry.add(player);
    }

    @Nullable
    @Override
    public Player getPlayer(@NotNull String name) {
        return playerRegistry.stream().filter(p -> p.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    @Nullable
    @Override
    public Player getPlayerExact(@NotNull String name) {
        return getPlayer(name);
    }

    @NotNull
    @Override
    public List<Player> matchPlayer(@NotNull String name) {
        return playerRegistry.stream().filter(p -> p.getName().equalsIgnoreCase(name)).collect(Collectors.toList());
    }

    @Nullable
    @Override
    public Player getPlayer(@NotNull UUID id) {
        return playerRegistry.stream().filter(p -> p.getUniqueId().equals(id)).findAny().orElse(null);
    }

    @NotNull
    @Override
    public PluginManager getPluginManager() {
        return null;
    }

    @NotNull
    @Override
    public BukkitScheduler getScheduler() {
        return null;
    }

    @NotNull
    @Override
    public ServicesManager getServicesManager() {
        return null;
    }

    @NotNull
    @Override
    public List<World> getWorlds() {
        return worldRegistry;
    }

    @Nullable
    @Override
    public World createWorld(@NotNull WorldCreator creator) {
        return null;
    }

    public void registerWorld(World world) {
        worldRegistry.add(world);
    }

    @Override
    public boolean unloadWorld(@NotNull String name, boolean save) {
        return worldRegistry.remove(worldRegistry.stream().filter(w -> w.getName().equals(name)).findAny().orElse(null));
    }

    @Override
    public boolean unloadWorld(@NotNull World world, boolean save) {
        return worldRegistry.remove(world);
    }

    @Nullable
    @Override
    public World getWorld(@NotNull String name) {
        return worldRegistry.stream().filter(w -> w.getName().equals(name)).findAny().orElse(null);
    }

    @Nullable
    @Override
    public World getWorld(@NotNull UUID uid) {
        return worldRegistry.stream().filter(w -> w.getUID().equals(uid)).findAny().orElse(null);
    }

    @Nullable
    @Override
    public MapView getMap(int id) {
        return null;
    }

    @NotNull
    @Override
    public MapView createMap(@NotNull World world) {
        return null;
    }

    @NotNull
    @Override
    public ItemStack createExplorerMap(@NotNull World world, @NotNull Location location, @NotNull StructureType structureType) {
        return null;
    }

    @NotNull
    @Override
    public ItemStack createExplorerMap(@NotNull World world, @NotNull Location location, @NotNull StructureType structureType, int radius, boolean findUnexplored) {
        return null;
    }

    @Override
    public void reload() {

    }

    @Override
    public void reloadData() {

    }

    @NotNull
    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Nullable
    @Override
    public PluginCommand getPluginCommand(@NotNull String name) {
        return null;
    }

    @Override
    public void savePlayers() {

    }

    @Override
    public boolean dispatchCommand(@NotNull CommandSender sender, @NotNull String commandLine) throws CommandException {
        return false;
    }

    @Override
    public boolean addRecipe(@Nullable Recipe recipe) {
        return false;
    }

    @NotNull
    @Override
    public List<Recipe> getRecipesFor(@NotNull ItemStack result) {
        return null;
    }

    @NotNull
    @Override
    public Iterator<Recipe> recipeIterator() {
        return null;
    }

    @Override
    public void clearRecipes() {

    }

    @Override
    public void resetRecipes() {

    }

    @NotNull
    @Override
    public Map<String, String[]> getCommandAliases() {
        return null;
    }

    @Override
    public int getSpawnRadius() {
        return 0;
    }

    @Override
    public void setSpawnRadius(int value) {

    }

    @Override
    public boolean getOnlineMode() {
        return false;
    }

    @Override
    public boolean getAllowFlight() {
        return false;
    }

    @Override
    public boolean isHardcore() {
        return false;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public int broadcast(@NotNull String message, @NotNull String permission) {
        return 0;
    }

    @NotNull
    @Override
    public OfflinePlayer getOfflinePlayer(@NotNull String name) {
        // Try to get from the registry
        Player player = getPlayer(name);
        if (player != null) return player;
        return new TestPlayer(null, null, name, false, false, true);
    }

    @NotNull
    @Override
    public OfflinePlayer getOfflinePlayer(@NotNull UUID id) {
        // Try to get from the registry
        Player player = getPlayer(id);
        if (player != null) return player;
        return new TestPlayer(null, id, "offline player", false, false, true);
    }

    @NotNull
    @Override
    public Set<String> getIPBans() {
        return Collections.emptySet();
    }

    @Override
    public void banIP(@NotNull String address) {

    }

    @Override
    public void unbanIP(@NotNull String address) {

    }

    @NotNull
    @Override
    public Set<OfflinePlayer> getBannedPlayers() {
        return Collections.emptySet();
    }

    @NotNull
    @Override
    public BanList getBanList(@NotNull BanList.Type type) {
        return null;
    }

    @NotNull
    @Override
    public Set<OfflinePlayer> getOperators() {
        return playerRegistry.stream().filter(TestPlayer::isOp).collect(Collectors.toSet());
    }

    @NotNull
    @Override
    public GameMode getDefaultGameMode() {
        return GameMode.CREATIVE;
    }

    @Override
    public void setDefaultGameMode(@NotNull GameMode mode) {

    }

    @NotNull
    @Override
    public ConsoleCommandSender getConsoleSender() {
        return null;
    }

    @NotNull
    @Override
    public File getWorldContainer() {
        return new File("worlds");
    }

    @NotNull
    @Override
    public OfflinePlayer[] getOfflinePlayers() {
        return playerRegistry.toArray(new OfflinePlayer[0]);
    }

    @NotNull
    @Override
    public Messenger getMessenger() {
        return null;
    }

    @NotNull
    @Override
    public HelpMap getHelpMap() {
        return null;
    }

    @NotNull
    @Override
    public Inventory createInventory(@Nullable InventoryHolder owner, @NotNull InventoryType type) {
        return null;
    }

    @NotNull
    @Override
    public Inventory createInventory(@Nullable InventoryHolder owner, @NotNull InventoryType type, @NotNull String title) {
        return null;
    }

    @NotNull
    @Override
    public Inventory createInventory(@Nullable InventoryHolder owner, int size) throws IllegalArgumentException {
        return null;
    }

    @NotNull
    @Override
    public Inventory createInventory(@Nullable InventoryHolder owner, int size, @NotNull String title) throws IllegalArgumentException {
        return null;
    }

    @NotNull
    @Override
    public Merchant createMerchant(@Nullable String title) {
        return null;
    }

    @Override
    public int getMonsterSpawnLimit() {
        return 0;
    }

    @Override
    public int getAnimalSpawnLimit() {
        return 0;
    }

    @Override
    public int getWaterAnimalSpawnLimit() {
        return 0;
    }

    @Override
    public int getAmbientSpawnLimit() {
        return 0;
    }

    @Override
    public boolean isPrimaryThread() {
        return false;
    }

    @NotNull
    @Override
    public String getMotd() {
        return "Wow, Bukkit is hard to Unit Test!";
    }

    @Nullable
    @Override
    public String getShutdownMessage() {
        return "Finally, it's over";
    }

    @NotNull
    @Override
    public Warning.WarningState getWarningState() {
        return Warning.WarningState.DEFAULT;
    }

    @NotNull
    @Override
    public ItemFactory getItemFactory() {
        return TestItemFactory.instance();
    }

    @Nullable
    @Override
    public ScoreboardManager getScoreboardManager() {
        return null;
    }

    @Nullable
    @Override
    public CachedServerIcon getServerIcon() {
        return null;
    }

    @NotNull
    @Override
    public CachedServerIcon loadServerIcon(@NotNull File file) throws IllegalArgumentException, Exception {
        return null;
    }

    @NotNull
    @Override
    public CachedServerIcon loadServerIcon(@NotNull BufferedImage image) throws IllegalArgumentException, Exception {
        return null;
    }

    @Override
    public void setIdleTimeout(int threshold) {

    }

    @Override
    public int getIdleTimeout() {
        return 0;
    }

    @NotNull
    @Override
    public ChunkGenerator.ChunkData createChunkData(@NotNull World world) {
        return null;
    }

    @NotNull
    @Override
    public BossBar createBossBar(@Nullable String title, @NotNull BarColor color, @NotNull BarStyle style, @NotNull BarFlag... flags) {
        return null;
    }

    @NotNull
    @Override
    public KeyedBossBar createBossBar(@NotNull NamespacedKey key, @Nullable String title, @NotNull BarColor color, @NotNull BarStyle style, @NotNull BarFlag... flags) {
        return null;
    }

    @NotNull
    @Override
    public Iterator<KeyedBossBar> getBossBars() {
        return null;
    }

    @Nullable
    @Override
    public KeyedBossBar getBossBar(@NotNull NamespacedKey key) {
        return null;
    }

    @Override
    public boolean removeBossBar(@NotNull NamespacedKey key) {
        return false;
    }

    @Nullable
    @Override
    public Entity getEntity(@NotNull UUID uuid) {
        return null;
    }

    @Nullable
    @Override
    public Advancement getAdvancement(@NotNull NamespacedKey key) {
        return null;
    }

    @NotNull
    @Override
    public Iterator<Advancement> advancementIterator() {
        return null;
    }

    @NotNull
    @Override
    public BlockData createBlockData(@NotNull Material material) {
        return new TestBlockData(material);
    }

    @NotNull
    @Override
    public BlockData createBlockData(@NotNull Material material, @Nullable Consumer<BlockData> consumer) {
        BlockData data = new TestBlockData(material);
        if (consumer != null) consumer.accept(data);
        return data;
    }

    @NotNull
    @Override
    public BlockData createBlockData(@NotNull String data) throws IllegalArgumentException {
        return TestBlockData.fromString(data);
    }

    @NotNull
    @Override
    public BlockData createBlockData(@Nullable Material material, @Nullable String data) throws IllegalArgumentException {
        return new TestBlockData(material);
    }

    @Nullable
    @Override
    public <T extends Keyed> Tag<T> getTag(@NotNull String registry, @NotNull NamespacedKey tag, @NotNull Class<T> clazz) {
        return null;
    }

    @NotNull
    @Override
    public <T extends Keyed> Iterable<Tag<T>> getTags(@NotNull String registry, @NotNull Class<T> clazz) {
        return null;
    }

    @Nullable
    @Override
    public LootTable getLootTable(@NotNull NamespacedKey key) {
        return null;
    }

    @NotNull
    @Override
    public List<Entity> selectEntities(@NotNull CommandSender sender, @NotNull String selector) throws IllegalArgumentException {
        return null;
    }

    @NotNull
    @Override
    public UnsafeValues getUnsafe() {
        return TestUnsafeValues.instance();
    }

    @NotNull
    @Override
    public Spigot spigot() {
        return null;
    }

    @Override
    public void sendPluginMessage(@NotNull Plugin source, @NotNull String channel, @NotNull byte[] message) {

    }

    @NotNull
    @Override
    public Set<String> getListeningPluginChannels() {
        return null;
    }
}
