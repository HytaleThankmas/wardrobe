package dev.hardaway.wardrobe.api.player;

import com.hypixel.hytale.server.core.cosmetics.CosmeticType;
import dev.hardaway.wardrobe.api.cosmetic.Cosmetic;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmeticSlot;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

public interface PlayerWardrobe {
    Map<String, PlayerCosmetic> getCosmeticMap();

    Collection<PlayerCosmetic> getCosmetics();

    boolean hasCosmetic(String id);

    default boolean hasCosmetic(Cosmetic cosmetic) {
        return this.hasCosmetic(cosmetic.getId());
    }

    PlayerCosmetic getCosmetic(String groupId);

    @Nullable
    default PlayerCosmetic getCosmetic(WardrobeCosmeticSlot group) {
        return this.getCosmetic(group.getId());
    }

    void setCosmetic(String groupId, @Nullable PlayerCosmetic cosmetic);

    default void setCosmetic(WardrobeCosmeticSlot group, @Nullable PlayerCosmetic cosmetic) {
        this.setCosmetic(group.getId(), cosmetic);
    }

    default void removeCosmetic(String groupId) {
        this.setCosmetic(groupId, null);
    }

    default void removeCosmetic(WardrobeCosmeticSlot group) {
        this.removeCosmetic(group.getId());
    }

    void clearCosmetics();

    void toggleCosmeticType(CosmeticType type);

    Collection<CosmeticType> getHiddenCosmeticTypes();

    java.util.Set<String> getUnlockedCosmetics();

    boolean isUnlocked(String id);

    default boolean isUnlocked(Cosmetic cosmetic) {
        return this.isUnlocked(cosmetic.getId());
    }

    boolean unlockCosmetic(String id);

    default boolean unlockCosmetic(Cosmetic cosmetic) {
        return this.unlockCosmetic(cosmetic.getId());
    }

    boolean lockCosmetic(String id);

    default boolean lockCosmetic(Cosmetic cosmetic) {
        return this.lockCosmetic(cosmetic.getId());
    }

    void unlockAllCosmetics();

    void lockAllCosmetics();

    void rebuild();

    PlayerWardrobe clone();
}
