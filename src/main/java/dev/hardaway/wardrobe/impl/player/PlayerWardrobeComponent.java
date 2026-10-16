package dev.hardaway.wardrobe.impl.player;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.cosmetics.CosmeticType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.WardrobePlugin;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.api.player.PlayerWardrobe;
import dev.hardaway.wardrobe.impl.cosmetic.CosmeticAsset;
import dev.hardaway.wardrobe.impl.cosmetic.PlayerModelCosmetic;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerWardrobeComponent implements PlayerWardrobe, Component<EntityStore> {

    public static final BuilderCodec<PlayerWardrobeComponent> CODEC = BuilderCodec.builder(PlayerWardrobeComponent.class, PlayerWardrobeComponent::new)
            .append(new KeyedCodec<>("Cosmetics", new MapCodec<>(CosmeticSaveData.CODEC, HashMap::new, false), true),
                    PlayerWardrobeComponent::setCosmetics,
                    t -> t.cosmetics
            ).add()
            .append(new KeyedCodec<>("HiddenCosmeticTypes", new SetCodec<>(new EnumCodec<>(CosmeticType.class), HashSet::new, false), false),
                    (t, value) -> t.hiddenCosmeticTypes = value,
                    t -> t.hiddenCosmeticTypes
            ).add()
            .append(new KeyedCodec<>("UnlockedCosmetics", new SetCodec<>(Codec.STRING, HashSet::new, false), false),
                    (t, value) -> t.unlockedCosmetics = value != null ? value : new HashSet<>(),
                    t -> t.unlockedCosmetics
            ).add()
            .build();

    private Map<String, CosmeticSaveData> cosmetics;
    private Set<String> cosmeticIdSet;
    private Set<CosmeticType> hiddenCosmeticTypes;
    private Set<String> unlockedCosmetics;
    private boolean dirty;

    public PlayerWardrobeComponent() {
        this(new HashMap<>(), new HashSet<>(), new HashSet<>());
    }

    public PlayerWardrobeComponent(Map<String, CosmeticSaveData> cosmetics, Set<CosmeticType> hiddenCosmeticTypes) {
        this(cosmetics, hiddenCosmeticTypes, new HashSet<>());
    }

    protected PlayerWardrobeComponent(Map<String, CosmeticSaveData> cosmetics, Set<CosmeticType> hiddenCosmeticTypes, Set<String> unlockedCosmetics) {
        this.setCosmetics(cosmetics);
        this.hiddenCosmeticTypes = hiddenCosmeticTypes != null ? hiddenCosmeticTypes : new HashSet<>();
        this.unlockedCosmetics = unlockedCosmetics != null ? unlockedCosmetics : new HashSet<>();
    }

    @Override
    public Map<String, PlayerCosmetic> getCosmeticMap() {
        return Collections.unmodifiableMap(this.cosmetics);
    }

    @Override
    public Collection<PlayerCosmetic> getCosmetics() {
        return Collections.unmodifiableCollection(this.cosmetics.values());
    }

    private void setCosmetics(Map<String, CosmeticSaveData> cosmeticMap) {
        this.cosmetics = cosmeticMap;
        this.cosmeticIdSet = this.cosmetics.values().stream().map(CosmeticSaveData::getCosmeticId).collect(Collectors.toSet());
    }

    @Override
    public boolean hasCosmetic(String id) {
        return cosmeticIdSet.contains(id);
    }

    @Override
    public PlayerCosmetic getCosmetic(String slot) {
        return cosmetics.get(slot);
    }

    @Override
    public void setCosmetic(String slot, PlayerCosmetic cosmetic) {
        if (cosmetic == null) {
            PlayerCosmetic lastCosmetic = this.cosmetics.remove(slot);
            if (lastCosmetic != null) {
                this.cosmeticIdSet.remove(lastCosmetic.getCosmeticId());
            }
            return;
        }

        if (CosmeticAsset.getAssetMap().getAsset(cosmetic.getCosmeticId()) instanceof PlayerModelCosmetic) {
            for (Map.Entry<String, CosmeticSaveData> entry : this.cosmetics.entrySet()) {
                String slotId = entry.getKey();
                CosmeticSaveData existingCosmetic = entry.getValue();

                if (CosmeticAsset.getAssetMap().getAsset(existingCosmetic.getCosmeticId()) instanceof PlayerModelCosmetic) {
                    this.cosmetics.remove(slotId);
                    this.cosmeticIdSet.remove(existingCosmetic.getCosmeticId());
                }
            }
        }

        // TODO: fix api so i dont have to do this
        CosmeticSaveData cosmeticSaveData = new CosmeticSaveData(cosmetic.getCosmeticId(), cosmetic.getOptionId(), cosmetic.getVariantId());
        PlayerCosmetic lastCosmetic = this.cosmetics.put(slot, cosmeticSaveData);
        if (lastCosmetic != null) {
            this.cosmeticIdSet.remove(lastCosmetic.getCosmeticId());
        }

        this.cosmeticIdSet.add(cosmetic.getCosmeticId());
    }

    @Override
    public void clearCosmetics() {
        this.cosmetics.clear();
    }

    @Override
    public void toggleCosmeticType(CosmeticType type) {
        if (hiddenCosmeticTypes.contains(type)) hiddenCosmeticTypes.remove(type);
        else hiddenCosmeticTypes.add(type);
    }

    @Override
    public Collection<CosmeticType> getHiddenCosmeticTypes() {
        return hiddenCosmeticTypes;
    }

    @Override
    public Set<String> getUnlockedCosmetics() {
        return Collections.unmodifiableSet(this.unlockedCosmetics);
    }

    @Override
    public boolean isUnlocked(String id) {
        return id != null && this.unlockedCosmetics.contains(id);
    }

    @Override
    public boolean unlockCosmetic(String id) {
        if (id == null) return false;
        boolean added = this.unlockedCosmetics.add(id);
        if (added) {
            this.rebuild();
        }
        return added;
    }

    @Override
    public boolean lockCosmetic(String id) {
        if (id == null) return false;
        boolean removed = this.unlockedCosmetics.remove(id);
        if (removed) {
            List<String> toRemove = new ArrayList<>();
            for (Map.Entry<String, CosmeticSaveData> entry : this.cosmetics.entrySet()) {
                if (id.equals(entry.getValue().getCosmeticId())) {
                    toRemove.add(entry.getKey());
                }
            }
            for (String slot : toRemove) {
                this.removeCosmetic(slot);
            }
            this.rebuild();
        }
        return removed;
    }

    @Override
    public void unlockAllCosmetics() {
        boolean changed = false;
        for (String id : CosmeticAsset.getAssetMap().getAssetMap().keySet()) {
            if (this.unlockedCosmetics.add(id)) {
                changed = true;
            }
        }
        if (changed) {
            this.rebuild();
        }
    }

    @Override
    public void lockAllCosmetics() {
        if (!this.unlockedCosmetics.isEmpty()) {
            this.unlockedCosmetics.clear();
            this.clearCosmetics();
            this.rebuild();
        }
    }

    @Override
    public void rebuild() {
        this.dirty = true;
    }

    protected boolean consumeDirty() {
        boolean dirty = this.dirty;
        this.dirty = false;
        return dirty;
    }

    @Nullable
    @Override
    public PlayerWardrobeComponent clone() {
        return new PlayerWardrobeComponent(new HashMap<>(this.cosmetics), new HashSet<>(this.hiddenCosmeticTypes), new HashSet<>(this.unlockedCosmetics));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PlayerWardrobeComponent that)) return false;
        return cosmetics.equals(that.cosmetics) && this.hiddenCosmeticTypes.equals(that.hiddenCosmeticTypes) && this.unlockedCosmetics.equals(that.unlockedCosmetics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cosmetics, hiddenCosmeticTypes, unlockedCosmetics);
    }

    public static ComponentType<EntityStore, PlayerWardrobeComponent> getComponentType() {
        return WardrobePlugin.get().getPlayerWardrobeComponentType();
    }
}
