package org.wargamer2010.signshop.util;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.util.FileUtil;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.blocks.*;
import org.wargamer2010.signshop.configuration.Storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class DataConverter {
    static File sellersFile;
    static File sellersFileBackup;
    static File timingFile;
    static File timingFileBackup;

    //TODO Make a very complex 1.8 set of data to convert.
    public static void init() {
        File dataFolder = SignShop.getInstance().getDataFolder();
        sellersFile = new File(dataFolder, "sellers.yml");
        FileConfiguration sellers = new YamlConfiguration();
        try {
            sellers.load(sellersFile);
            SignShop.log("Checking data version.", Level.INFO);
            if (sellers.getInt("DataVersion") < SignShop.DATA_VERSION) {
                sellersFileBackup = new File(dataFolder, "sellersBackup" + SSTimeUtil.getDateTimeStamp() + ".yml");
                FileUtil.copy(sellersFile, sellersFileBackup);
                convertData(sellers);
                convertTiming();
            }
            else {
                SignShop.log("Your data is current.", Level.INFO);
            }
        } catch (IOException | InvalidConfigurationException ignored) {
        }
    }

    private static void convertTiming() {
        File dataFolder = SignShop.getInstance().getDataFolder();
        timingFile = new File(dataFolder, "timing.yml");
        FileConfiguration timing = new YamlConfiguration();
        try {
            timing.load(timingFile);
            ConfigurationSection expirables = timing.getConfigurationSection("expirables");
            if (expirables != null && !expirables.getKeys(false).isEmpty()) {
                timingFileBackup = new File(dataFolder, "timingBackup" + SSTimeUtil.getDateTimeStamp() + ".yml");
                FileUtil.copy(timingFile, timingFileBackup);
                for (String key : expirables.getKeys(false)) {
                    if (key.contains("sshotel")) {
                        Map<String, Object> propertyMap = expirables.getConfigurationSection(key).getValues(true);
                        timing.createSection("expirables." + key.replace("sshotel", "signshophotel"), propertyMap);
                    }
                }
                timing.save(timingFile);
            }
        } catch (InvalidConfigurationException | IOException e) {
            e.printStackTrace();
        }

    }

    private static void convertData(FileConfiguration sellers) {

        try {
            SignShop.log("Converting old data.", Level.INFO);
            ConfigurationSection section = sellers.getConfigurationSection("sellers");
            if (section == null) {
                SignShop.log("There was a problem with the sellers.yml, attempting to fix it. If the problem persists try regenerating the SignShop folder.",Level.WARNING);
                sellers.set("sellers", new ArrayList<>());
            }
            else {
                Set<String> shops = section.getKeys(false);
                for (String shop : shops) {
                    StringBuilder itemPath = new StringBuilder().append("sellers.").append(shop).append(".items");
                    StringBuilder miscPath = new StringBuilder().append("sellers.").append(shop).append(".misc");
                    //Strip old data from items
                    List<String> items = sellers.getStringList(itemPath.toString());
                    ItemStack[] itemStacks = convertOldStringsToItemStacks(items);
                    sellers.set(itemPath.toString(), itemUtil.convertItemStacksToString(itemStacks));
                    //Strip old data from misc
                    List<String> misc = sellers.getStringList(miscPath.toString());
                    if (!misc.isEmpty()) {
                        List<String> newMisc = new ArrayList<>();

                        for (String miscString : misc) {
                            String[] keyPair = miscString.split(":", 2);
                            String key = keyPair.length >= 1 ? keyPair[0] : "";
                            String data = keyPair.length == 2 ? keyPair[1] : "";
                            //This strips the old data if it exists.
                            if (data.contains("|")) {
                                String[] dataPair = data.split("\\|", 2);
                                data = dataPair.length == 2 ? dataPair[1] : dataPair[0];
                            }
                            newMisc.add(key + ":" + data);
                        }
                        sellers.set(miscPath.toString(), newMisc);
                    }
                }
                SignShop.log("Data conversion of " + shops.size() + " shops has finished.", Level.INFO);
            }
            sellers.set("DataVersion", SignShop.DATA_VERSION);
            sellers.save(sellersFile);
        } catch (IOException e) {
            SignShop.log("Error converting data!", Level.WARNING);
        }
    }

    public static ItemStack[] convertOldStringsToItemStacks(List<String> itemStringList) {
        IItemTags itemTags = BookFactory.getItemTags();
        ItemStack[] itemStacks = new ItemStack[itemStringList.size()];
        int invalidItems = 0;

        for (int i = 0; i < itemStringList.size(); i++) {
            try {
                String[] itemProperties = itemStringList.get(i).split(Storage.getItemSeperator());
                if (itemProperties.length < 4) {
                    invalidItems++;
                    continue;
                }

                if (itemProperties.length <= 7) {
                    if (i < (itemStringList.size() - 1) && itemStringList.get(i + 1).split(Storage.getItemSeperator()).length < 4) {
                        // Bug detected, the next item will be the base64 string belonging to the current item
                        // This bug will be fixed at the next save as the ~ will be replaced with a |
                        itemProperties = (itemStringList.get(i) + "|" + itemStringList.get(i + 1)).split(Storage.getItemSeperator());
                    }
                }

                if (itemProperties.length > 7) {
                    String base64prop = itemProperties[7];
                    // The ~ and | are used to differentiate between the old NBTLib and the BukkitSerialization
                    if (base64prop != null && (base64prop.startsWith("~") || base64prop.startsWith("|"))) {
                        String joined = itemUtil.Join(itemProperties, 7).substring(1);

                        ItemStack[] convertedStacks = BukkitSerialization.itemStackArrayFromBase64(joined);
                        if (convertedStacks.length > 0 && convertedStacks[0] != null) {
                            itemStacks[i] = convertedStacks[0];
                        }
                    }
                }

                if (itemStacks[i] == null) {
                    itemStacks[i] = itemTags.getCraftItemstack(
                            Material.getMaterial(itemProperties[1]),
                            Integer.parseInt(itemProperties[0]),
                            Short.parseShort(itemProperties[2])
                    );
                    //noinspection deprecation
                    itemStacks[i].getData().setData(Byte.parseByte(itemProperties[3]));

                    if (itemProperties.length > 4)
                        itemUtil.safelyAddEnchantments(itemStacks[i], signshopUtil.convertStringToEnchantments(itemProperties[4]));
                }

                if (itemProperties.length > 5) {
                    try {
                        itemStacks[i] = SignShopBooks.addBooksProps(itemStacks[i], Integer.parseInt(itemProperties[5]));
                    } catch (NumberFormatException ignored) {

                    }
                }
                if (itemProperties.length > 6) {
                    try {
                        SignShopItemMeta.setMetaForID(itemStacks[i], Integer.parseInt(itemProperties[6]));
                    } catch (NumberFormatException ignored) {

                    }
                }
            } catch (Exception ignored) {

            }
        }

        if (invalidItems > 0) {
            ItemStack[] temp = new ItemStack[itemStringList.size() - invalidItems];
            int counter = 0;
            for (ItemStack i : itemStacks) {
                if (i != null) {
                    temp[counter] = i;
                    counter++;
                }
            }

            itemStacks = temp;
        }


        return itemStacks;
    }

    //Probably won't need this but saving it anyway.
    public static String[] convertItemStacksToOldString(ItemStack[] itemStackArray) {
        List<String> itemStringList = new ArrayList<>();
        if (itemStackArray == null)
            return new String[1];

        ItemStack currentItemStack;
        for (ItemStack itemStack : itemStackArray) {
            if (itemStack != null) {
                currentItemStack = itemStack;
                String ID = "";
                if (itemUtil.isWriteableBook(currentItemStack))
                    ID = SignShopBooks.getBookID(currentItemStack).toString();
                String metaID = SignShopItemMeta.getMetaID(currentItemStack).toString();
                if (metaID.equals("-1"))
                    metaID = "";
                ItemStack[] stacks = new ItemStack[1];
                stacks[0] = currentItemStack;

                itemStringList.add(BukkitSerialization.itemStackArrayToBase64(stacks));

                //noinspection deprecation
                itemStringList.add((currentItemStack.getAmount() + Storage.getItemSeperator()
                        + currentItemStack.getType() + Storage.getItemSeperator()
                        + ((Damageable) currentItemStack.getItemMeta()).getDamage() + Storage.getItemSeperator()
                        + currentItemStack.getData().getData() + Storage.getItemSeperator()
                        + signshopUtil.convertEnchantmentsToString(currentItemStack.getEnchantments()) + Storage.getItemSeperator()
                        + ID + Storage.getItemSeperator()
                        + metaID + Storage.getItemSeperator()
                        + "|" + BukkitSerialization.itemStackArrayToBase64(stacks)));
            }

        }
        String[] items = new String[itemStringList.size()];
        itemStringList.toArray(items);
        return items;
    }
}
