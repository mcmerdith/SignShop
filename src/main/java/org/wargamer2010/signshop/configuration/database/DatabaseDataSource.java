package org.wargamer2010.signshop.configuration.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.configuration.database.queries.DatabaseQuery;
import org.wargamer2010.signshop.configuration.database.queries.DatabaseUpdate;
import org.wargamer2010.signshop.configuration.database.queries.QuerySelect;
import org.wargamer2010.signshop.player.PlayerIdentifier;
import org.wargamer2010.signshop.player.SignShopPlayer;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

public class DatabaseDataSource extends Storage {
    private DataSource source;
    private Connection connection;

    private DatabaseUpdateWorker worker = new DatabaseUpdateWorker();

    private BukkitTask dbUpdateWorker;
    private final Timer fallbackUpdateWorker = new Timer();

    private Map<Location, Seller> sellers = new HashMap<>();

    /**
     * Construct a new Database Data Source. Database is connected and ready as soon as the class is created
     * <br>Database configuration is read from "database.properties" in the plugin data folder
     * @throws IllegalStateException If the database is unavailable
     */
    public DatabaseDataSource() {
        SignShop.getInstance().saveResource("database.properties", false);
        File propertiesFile = new File(SignShop.getInstance().getDataFolder(), "database.properties");

        source = new HikariDataSource(new HikariConfig(propertiesFile.getPath()));

        if (!verifyConnection()) throw new IllegalStateException("Failed to connect to database!");

        initUpdateWorker();
    }

    /**
     * Construct a new Database Data Source. Database is connected and ready as soon as the class is created
     * <br>This constructor is used mainly for testing
     * @param source The data source to use for the database
     * @throws IllegalStateException If the database is unavailable
     */
    public DatabaseDataSource(DataSource source) {
        this.source = source;

        if (!verifyConnection()) throw new IllegalStateException("Failed to connect to database!");

        fallbackUpdateWorker.schedule(worker, 250, 250);
    }

    private void initUpdateWorker() {
        dbUpdateWorker = Bukkit.getScheduler().runTaskTimerAsynchronously(SignShop.getInstance(), worker, 5L, 5L);
    }

    @Override
    public boolean Load() {
        return false;
    }

    @Override
    public boolean Save() {
        return false;
    }

    @Override
    public void dispose() {
        worker.dispose();
        fallbackUpdateWorker.cancel();
        if (dbUpdateWorker != null && !dbUpdateWorker.isCancelled()) dbUpdateWorker.cancel();

        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // Nothing we can do about that
                sqlError(e);
            }
        }
    }

    @Override
    public int shopCount() {
        return sellers.size();
    }

    @Override
    public void addSeller(PlayerIdentifier playerId, String sWorld, Block bSign, List<Block> containables, List<Block> activatables, ItemStack[] isItems, Map<String, String> misc, boolean save) {
        sellers.put(bSign.getLocation(), new Seller(playerId, sWorld, containables, activatables, isItems, bSign.getLocation(), misc, save));
        // we don't handle the saving
    }

    @Override
    public void updateSeller(Block bSign, List<Block> containables, List<Block> activatables, ItemStack[] isItems) {
        // TODO prepare query and queue it on the worker
    }

    @Override
    public Seller getSeller(Location lKey) {
        return sellers.getOrDefault(lKey, null);
    }

    @Override
    public Collection<Seller> getSellers() {
        return sellers.values();
    }

    @Override
    public void removeSeller(Location lKey) {
        // Remove the seller from our local map
        sellers.remove(lKey);

        // TODO prepare query and queue it on the worker
    }

    @Override
    public int countLocations(SignShopPlayer player) {
        return 0;
    }

    @Override
    public List<Block> getSignsFromHolder(Block bHolder) {
        return null;
    }

    @Override
    public List<Seller> getShopsByBlock(Block bBlock) {
        return null;
    }

    @Override
    public List<Block> getShopsWithMiscSetting(String key, String value) {
        return null;
    }

    private ResultSet getAllSellers() {
        QuerySelect query = new QuerySelect();
        try (Connection connection = source.getConnection(); PreparedStatement statement = query.prepareStatement(connection, "sellers")) {
            return statement.executeQuery();
        } catch (SQLException e) {
            sqlError(e);
            return null;
        }
    }

    public static void sqlError(SQLException e) {
        SignShop.log(String.format("Database Error %d (%s): %s", e.getErrorCode(), e.getSQLState(), e.getMessage()), Level.SEVERE);
    }

    public Connection newConnection() throws SQLException {
        return source.getConnection();
    }

    public Connection getConnection() {
        return (verifyConnection()) ? connection : null;
    }

    public boolean verifyConnection() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(1)) {
                if (connection != null && !connection.isClosed()) {
                    // Connection is invalid
                    connection.close();
                }

                connection = newConnection();

                return connection != null && !connection.isClosed();
            }

            return true;
        } catch (SQLException e) {
            sqlError(e);
            return false;
        }
    }
}
