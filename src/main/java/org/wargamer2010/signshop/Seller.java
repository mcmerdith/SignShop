package org.wargamer2010.signshop;

import jakarta.persistence.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.wargamer2010.signshop.blocks.SignShopBooks;
import org.wargamer2010.signshop.blocks.SignShopItemMeta;
import org.wargamer2010.signshop.configuration.storage.database.datatype.*;
import org.wargamer2010.signshop.configuration.storage.database.models.SellerExport;
import org.wargamer2010.signshop.configuration.storage.database.util.LazyLocation;
import org.wargamer2010.signshop.player.PlayerCache;
import org.wargamer2010.signshop.player.PlayerIdentifier;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.SSTimeUtil;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "sellers")
public class Seller {

    /*
    Database specific stuff
     */

    @Id
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment", strategy = "increment")
    private Long id;

    @OneToOne(mappedBy = "seller")
    private SellerExport export;

    /*
    Seller class
     */

    @ElementCollection
    @Convert(converter = LazyLocationConverter.class)
    private List<LazyLocation> containables;

    @ElementCollection
    @Convert(converter = LazyLocationConverter.class)
    private List<LazyLocation> activatables;

    @Lob
    @Convert(converter = ItemStackConverter.class)
    @Column(name = "items", columnDefinition = "BLOB")
    private ItemStack[] isItems;

    @Column(name = "sign", unique = true)
    @Basic(optional = false)
    @Convert(converter = LazyLocationConverter.class)
    private LazyLocation signLocation;

    @SuppressWarnings("JpaAttributeTypeInspection")
    @Convert(converter = MapConverter.class)
    private Map<String, String> miscProps = new HashMap<>();

    @Transient
    private Map<String, String> volatileProperties = new LinkedHashMap<>();

    @Transient
    private Map<String, Object> serializedData = new HashMap<>();

    @Convert(converter = SignShopPlayerConverter.class)
    @Basic(optional = false)
    private SignShopPlayer owner;

    @Basic(optional = false)
    private String world;

    protected Seller() {
    }

    public Seller(PlayerIdentifier playerId, String sWorld, List<Block> pContainables, List<Block> pActivatables, ItemStack[] isChestItems, Location location,
                  Map<String, String> pMiscProps, Boolean save) {
        owner = PlayerCache.getPlayer(playerId);//new SignShopPlayer(playerId);
        world = sWorld;

        isItems = itemUtil.getBackupItemStack(isChestItems);
        containables = LazyLocation.collectionOfBlockToListOf(pContainables);
        activatables = LazyLocation.collectionOfBlockToListOf(pActivatables);
        signLocation = new LazyLocation(location);
        if (pMiscProps != null)
            miscProps.putAll(pMiscProps);
        if (save)
            storeMeta(isItems);

        calculateSerialization();
    }

    public ItemStack[] getItems() {
        return getItems(true);
    }

    public ItemStack[] getItems(boolean backup) {
        if (backup)
            return itemUtil.getBackupItemStack(isItems);
        else
            return isItems;
    }

    public void setItems(ItemStack[] items) {
        isItems = items;
        calculateSerialization();
    }

    public List<Block> getContainables() {
        return LazyLocation.collectionOfToBlockList(containables);
    }

    public void setContainables(List<Block> blocklist) {
        containables = LazyLocation.collectionOfBlockToListOf(blocklist);
        calculateSerialization();
    }

    public List<Block> getActivatables() {
        return LazyLocation.collectionOfToBlockList(activatables);
    }

    public void setActivatables(List<Block> blocklist) {
        activatables = LazyLocation.collectionOfBlockToListOf(blocklist);
        calculateSerialization();
    }

    public SignShopPlayer getOwner() {
        return owner;
    }

    public void setOwner(SignShopPlayer newowner) {
        owner = newowner;
        calculateSerialization();
    }

    public boolean isOwner(SignShopPlayer player) {
        return player.compareTo(owner);
    }

    public String getWorld() {
        return world;
    }

    public boolean hasMisc(String key) {
        return miscProps.containsKey(key);
    }

    public void removeMisc(String key) {
        miscProps.remove(key);
        calculateSerialization();
    }

    public void addMisc(String key, String value) {
        miscProps.put(key, value);
        calculateSerialization();
    }

    public String getMisc(String key) {
        if (miscProps.containsKey(key))
            return miscProps.get(key);
        return null;
    }

    public Map<String, String> getRawMisc() {
        return miscProps;
    }

    public static void storeMeta(ItemStack[] stacks) {
        if (stacks == null)
            return;
        for (ItemStack stack : stacks) {
            if (itemUtil.isWriteableBook(stack)) {
                SignShopBooks.addBook(stack);
            }
            SignShopItemMeta.storeMeta(stack);
        }
    }

    public String getVolatile(String key) {
        if (volatileProperties.containsKey(key))
            return volatileProperties.get(key);
        return null;
    }

    public void setVolatile(String key, String value) {
        volatileProperties.put(key, value);
    }

    public Block getSign() {
        return getSignLocation().getBlock();
    }

    public Location getSignLocation() {
        return signLocation.get();
    }

    public String getOperation() {
        Block block = getSign();
        if (block == null)
            return "";
        if (itemUtil.clickedSign(block)) {
            Sign sign = (Sign) block.getState();
            return signshopUtil.getOperation(sign.getLine(0));
        }
        return "";
    }

    public void reloadBlocks() {
        containables = containables.stream().map((c) -> {
            Block block = c.get().getBlock();
            return new LazyLocation(block.getWorld().getBlockAt(block.getX(), block.getY(), block.getZ()));
        }).collect(Collectors.toList());

        activatables = activatables.stream().map((a) -> {
            Block block = a.get().getBlock();
            return new LazyLocation(block.getWorld().getBlockAt(block.getX(), block.getY(), block.getZ()));
        }).collect(Collectors.toList());

        calculateSerialization();
    }

    public Map<String, Object> getSerializedData() {
        return serializedData;
    }

    private void calculateSerialization() {
        Map<String, Object> temp = new HashMap<>();

        temp.put("shopworld", getWorld());
        temp.put("owner", getOwner().GetIdentifier().toString());
        temp.put("items", itemUtil.convertItemStacksToString(getItems(false)));

        String[] sContainables = containables.stream().map(LazyLocation::getStringRepresentation).toArray(String[]::new);
        temp.put("containables", sContainables);

        String[] sActivatables = activatables.stream().map(LazyLocation::getStringRepresentation).toArray(String[]::new);
        temp.put("activatables", sActivatables);

        temp.put("sign", signshopUtil.convertLocationToString(getSignLocation()));

        Map<String, String> misc = miscProps;
        if (misc.size() > 0)
            temp.put("misc", MapToList(misc));

        serializedData = temp;
    }

    /**
     * This method really doesn't need to be here. Should probably be in a util class
     *
     * @deprecated Will be migrated at some point
     */
    @Deprecated
    private List<String> MapToList(Map<String, String> map) {
        List<String> returnList = new LinkedList<>();
        for (Map.Entry<String, String> entry : map.entrySet())
            returnList.add(entry.getKey() + ":" + entry.getValue());
        return returnList;
    }


    public String getInfo() {
        String newLine = "\n";
        StringBuilder sb = new StringBuilder();
        sb.append("--ShopInfo--").append(newLine)
                .append("  Owner: ").append(owner.getName()).append(" LastSeen: ").append(SSTimeUtil.getDateTimeFromLong(owner.getOfflinePlayer().getLastPlayed())).append(newLine)
                .append("  Sign Location: ").append(signshopUtil.convertLocationToString(getSignLocation())).append(newLine)
                .append("  Container Locations: ");
        for (LazyLocation location : containables) {
            sb.append("  ").append(location.getStringRepresentation()).append(" ");
        }
        sb.append(newLine)
                .append("  Activatable Locations: ");
        for (LazyLocation location : activatables) {
            sb.append("  ").append(location.getStringRepresentation()).append(" ");
        }
        sb.append(newLine)
                .append("  Misc: ");
        for (String key : miscProps.keySet())
            sb.append("  ").append(key).append(": ").append(miscProps.get(key)).append(" ");

        return sb.toString();
    }
}
