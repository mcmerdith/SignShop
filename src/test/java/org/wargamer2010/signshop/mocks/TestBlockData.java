package org.wargamer2010.signshop.mocks;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TestBlockData implements BlockData {
    private final Material material;
    private final Map<String, Object> blockData = new HashMap<>();

    public TestBlockData(Material material, Map<String, Object> blockData) {
        this.material = material == null ? Material.AIR : material;
        if (blockData != null) this.blockData.putAll(blockData);
    }

    public static TestBlockData fromString(String data) {
        try {
            String[] materialData = data.split("\\[");

            if (materialData.length == 0) throw new RuntimeException("Material data string was empty");
            else if (materialData.length > 2 || (materialData.length == 2 && !materialData[1].endsWith("]")))
                throw new RuntimeException("Mal-formatted data string");

            String[] materialComponents = materialData[0].split(":");
            String material = (materialComponents.length == 2) ? materialComponents[1] : materialComponents[0];

            Map<String, Object> blockData = new HashMap<>();

            if (materialData.length == 2) {
                String[] kvPairs = materialData[1].substring(0, materialData[1].length() - 1).split(",");

                for (String kvPair : kvPairs) {
                    String[] keyValue = kvPair.split("=");

                    if (keyValue.length == 2) {
                        blockData.put(keyValue[0], keyValue[1]);
                    }
                }
            }

            return new TestBlockData(Material.valueOf(material.toUpperCase()), blockData);
        } catch (Exception e) {
            return new TestBlockData(Material.AIR, null);
        }
    }

    @NotNull
    @Override
    public Material getMaterial() {
        return material;
    }

    public Map<String, Object> getBlockData() {
        return blockData;
    }

    @NotNull
    @Override
    public String getAsString() {
        return getAsString(false);
    }

    @NotNull
    @Override
    public String getAsString(boolean hideUnspecified) {
        String data = String.format(
                "[%s]",
                blockData.entrySet().stream().map(e -> String.format("%s=%s", e.getKey(), e.getValue()))
                        .collect(Collectors.joining(","))
        );

        return String.format("minecraft:%s%s", material.name().toLowerCase(), (data.length() <= 2) ? "" : data);
    }

    @NotNull
    @Override
    public BlockData merge(@NotNull BlockData data) {
        if (!(data instanceof TestBlockData) || data.getMaterial() != getMaterial()) return this;
        blockData.putAll(((TestBlockData) data).getBlockData());
        return this;
    }

    @Override
    public boolean matches(@Nullable BlockData data) {
        if (!(data instanceof TestBlockData)) return false;
        return material == data.getMaterial() && blockData.equals(((TestBlockData) data).getBlockData());
    }

    @NotNull
    @Override
    public BlockData clone() {
        try {
            return (BlockData) super.clone();
        } catch (Exception e) {
            return new TestBlockData(material, blockData);
        }
    }
}
