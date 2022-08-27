
package org.wargamer2010.signshop.blocks;

import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.Storage;

import java.io.File;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

/**
 * @deprecated Will be removed very soon
 * @see Storage#getBuiltInDatabase()
 */
@Deprecated
public class SSDatabase {
    private static final String downloadURL = "http://cloud.github.com/downloads/wargamer/SignShop/";
    private static Driver driver = null;
    private Connection conn = null;
    private String filename;

    /**
     * @deprecated Will be removed very soon
     * @see Storage#getBuiltInDatabase()
     */
    @Deprecated
    public SSDatabase(final String pFilename) {
        filename = pFilename;

        if(driver == null)
            loadLib();

        checkLegacy();
        if(!open())
            SignShop.log("Connection to: " + filename + " could not be established", Level.WARNING);
    }

    /**
     * @deprecated Will be removed very soon
     * @see Storage#getBuiltInDatabase()
     */
    @Deprecated
    private void checkLegacy() {
        String dbdirname = "db";
        File dbdir = new File(SignShop.getInstance().getDataFolder(), dbdirname);
        if(!dbdir.exists() && !dbdir.mkdirs()) {
            SignShop.log("Could not create db directory in plugin folder. Will use old path (plugins/SignShop) in stead of (plugins/SignShop/ " + dbdirname + ").", Level.WARNING);
            return;
        }
        File olddb = new File(SignShop.getInstance().getDataFolder(), filename);
        File newdb = new File(SignShop.getInstance().getDataFolder(), (dbdirname + File.separator + filename));
        if(olddb.exists()) {
            if(!newdb.exists() && !olddb.renameTo(newdb)) {
                SignShop.log("Could not move " + filename + " to (plugins/SignShop/ " + dbdirname + ") directory. Please move the file manually. Will use old path for now.", Level.WARNING);
                return;
            }
        }

        filename = (dbdirname + File.separator + filename);
    }

    /**
     * @deprecated Will be removed very soon
     * @see Storage#getBuiltInDatabase()
     */
    @Deprecated
    public Boolean tableExists(String tablename) {
        try {
            Map<Integer, Object> pars = new LinkedHashMap<>();
            pars.put(1, "table");
            pars.put(2, tablename);
            ResultSet set = (ResultSet)runStatement("SELECT name FROM sqlite_master WHERE type = ? AND name = ?;", pars, true);
            if(set != null && set.next()) {
                set.close();
                return true;
            }
        } catch (SQLException ignored) {
        }

        return false;
    }

    /**
     * @deprecated Will be removed very soon
     * @see Storage#getBuiltInDatabase()
     */
    @Deprecated
    public boolean columnExists(String needle) {
        ResultSet result = (ResultSet) runStatement("PRAGMA table_info(Book);", null, true);
        if(result == null)
            return false;

        try {
            do {
                String columnName = result.getString("name");
                if(columnName.equalsIgnoreCase(needle))
                    return true;
            } while(result.next());
        } catch (SQLException ex) {
            SignShop.log("Failed to check for column existence on Book table because: " + ex.getMessage(), Level.WARNING);
        } finally {
            close();
        }

        return false;
    }

    /**
     * @deprecated Will be removed very soon
     * @see Storage#getBuiltInDatabase()
     */
    @Deprecated
    public final void loadLib() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch(ClassNotFoundException ex) {
            SignShop.log("Could not find JDBC class in Bukkit JAR, please report this issue with details at http://tiny.cc/signshop", Level.SEVERE);
            return;
        }
        driver = new org.sqlite.JDBC();
    }

    /**
     * @deprecated Will be removed very soon
     * @see Storage#getBuiltInDatabase()
     */
    @Deprecated
    public final boolean open() {
        if(driver == null)
            return false;
        try {
            File DBFile = new File(SignShop.getInstance().getDataFolder(), filename);
            conn = driver.connect("jdbc:sqlite:" + DBFile.getPath(), new Properties());
        } catch (SQLException ignored) {

        }
        return (conn != null);
    }

    /**
     * @deprecated Will be removed very soon
     * @see Storage#getBuiltInDatabase()
     */
    @Deprecated
    public void close() {
        if (conn == null || driver == null)
            return;
        try {
            conn.close();
        } catch (SQLException ignored) {
        }
    }

    /**
     * @deprecated Will be removed very soon
     * @see Storage#getBuiltInDatabase()
     */
    @Deprecated
    public Object runStatement(String Query, Map<Integer, Object> params, Boolean expectingResult) {
        try {
            if(conn == null) {
                SignShop.log("Query: " + Query + " could not be run because the connection to: " + filename + " could not be established", Level.WARNING);
                return null;
            }
            PreparedStatement st = conn.prepareStatement(Query, PreparedStatement.RETURN_GENERATED_KEYS);

            if(params != null && !params.isEmpty()) {
                for(Map.Entry<Integer, Object> param : params.entrySet()) {
                    if (param.getValue() == null) {
                        SignShop.log("Query: " + Query + " with Key: "+param.getKey()+ " has null value. Setting the value to a String object of 'null'", Level.WARNING);
                        param.setValue("null");

                    }
                    if(param.getValue().getClass().equals(int.class) || param.getValue().getClass().equals(Integer.class)) {
                        st.setInt(param.getKey(), ((Integer)param.getValue()));
                    } else if(param.getValue().getClass().equals(String.class)) {
                        st.setString(param.getKey(), ((String)param.getValue()));
                    }
                }
            }
            if(expectingResult) {
                return st.executeQuery();
            } else {
                int result = st.executeUpdate();
                ResultSet genKeys = st.getGeneratedKeys();
                if(genKeys == null)
                    return result;
                else {
                    try {
                        return genKeys.getInt("last_insert_rowid()");
                    } catch(SQLException ex) {
                        SignShop.log("Query: " + Query + " threw exception: " + ex.getMessage(), Level.WARNING);
                        return result;
                    }
                }
            }
        } catch(SQLException ex) {
            SignShop.log("Query: " + Query + " threw exception: " + ex.getMessage(), Level.WARNING);
            return null;
        }
    }
}
