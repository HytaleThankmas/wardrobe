package dev.hardaway.wardrobe.impl.menu;

import com.hypixel.hytale.common.util.StringCompareUtil;
import dev.hardaway.wardrobe.WardrobeUtil;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmetic;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmeticSlot;
import dev.hardaway.wardrobe.api.cosmetic.appearance.Appearance;
import dev.hardaway.wardrobe.api.cosmetic.appearance.AppearanceCosmetic;
import dev.hardaway.wardrobe.api.menu.WardrobeCategory;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;
import dev.hardaway.wardrobe.api.property.WardrobeVisibility;
import dev.hardaway.wardrobe.impl.cosmetic.CosmeticAsset;
import dev.hardaway.wardrobe.impl.cosmetic.CosmeticCategoryAsset;
import dev.hardaway.wardrobe.impl.cosmetic.CosmeticSlotAsset;
import dev.hardaway.wardrobe.impl.player.CosmeticSaveData;
import dev.hardaway.wardrobe.impl.player.PlayerWardrobeComponent;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import javax.annotation.Nullable;
import java.util.*;

public class WardrobeMenu {

    private final UUID playerId;
    private final PlayerWardrobeComponent wardrobe;
    private  final PlayerWardrobeComponent baseWardrobe;

    private final List<? extends WardrobeCategory> categories;
    private final Map<String, List<WardrobeCosmeticSlot>> slotMap = new HashMap<>();
    private final Map<String, List<WardrobeCosmetic>> cosmeticMap = new HashMap<>();

    private String selectedCategory;
    private String selectedSlot;
    private String searchQuery = "";

    public WardrobeMenu(UUID playerId, PlayerWardrobeComponent wardrobe) {
        this.playerId = playerId;
        this.wardrobe = wardrobe;
        this.baseWardrobe = wardrobe.clone();

        this.categories = CosmeticCategoryAsset.getAssetMap().getAssetMap().values().stream()
                .filter(c -> c.getProperties().getWardrobeVisibility() != WardrobeVisibility.NEVER)
                .filter(c -> c.getProperties().hasPermission(playerId))
                .sorted(Comparator.comparing(WardrobeCategory::getTabOrder))
                .toList();

        buildSlots();
        buildCosmetics();

        if (!categories.isEmpty()) {
            selectCategory(categories.getFirst().getId());
        }
    }

    private void buildSlots() {
        for (WardrobeCategory category : categories) {
            slotMap.put(category.getId(), new ArrayList<>());
        }

        CosmeticSlotAsset.getAssetMap().getAssetMap().values().stream()
                .filter(g -> g.getProperties().getWardrobeVisibility() != WardrobeVisibility.NEVER)
                .filter(g -> g.getProperties().hasPermission(playerId))
                .sorted(Comparator.comparing(WardrobeCosmeticSlot::getTabOrder))
                .forEach(slot -> {
                    slotMap.computeIfAbsent(slot.getCategory().getId(), k -> new ArrayList<>()).add(slot);
                    cosmeticMap.put(slot.getId(), new ArrayList<>());
                });
    }

    private void buildCosmetics() {
        CosmeticAsset.getAssetMap().getAssetMap().values().stream()
                .filter(c -> c.getProperties().getWardrobeVisibility() != WardrobeVisibility.NEVER)
                .filter(c -> c.getProperties().getWardrobeVisibility() != WardrobeVisibility.PERMISSION || wardrobe.isUnlocked(c.getId()))
                .sorted(Comparator.comparing(dev.hardaway.wardrobe.api.cosmetic.Cosmetic::getId))
                .forEach(c -> cosmeticMap.computeIfAbsent(c.getCosmeticSlotId(), k -> new ArrayList<>()).add(c));
    }

    public List<? extends WardrobeCategory> getCategories() {
        return categories;
    }

    public List<WardrobeCosmeticSlot> getSlots() {
        return slotMap.getOrDefault(selectedCategory, List.of());
    }

    public List<WardrobeCosmetic> getCosmetics() {
        List<WardrobeCosmetic> cosmetics = cosmeticMap.getOrDefault(selectedSlot, List.of());

        if (searchQuery.isEmpty()) return cosmetics;

        Object2IntMap<WardrobeCosmetic> scores = new Object2IntOpenHashMap<>();
        for (WardrobeCosmetic c : cosmetics) {
            int score = StringCompareUtil.getFuzzyDistance(c.getId(), searchQuery, Locale.ENGLISH);
            if (score > 0) scores.put(c, score);
        }

        return scores.keySet().stream()
                .sorted(Comparator.comparingInt(scores::getInt).reversed())
                .toList();
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery == null ? "" : searchQuery;
    }

    public String getSelectedCategory() {
        return selectedCategory;
    }

    public String getSelectedSlot() {
        return selectedSlot;
    }

    public PlayerWardrobeComponent getWardrobe() {
        return wardrobe;
    }

    public PlayerWardrobeComponent getBaseWardrobe() {
        return baseWardrobe;
    }

    public void selectCategory(String categoryId) {
        selectedCategory = categoryId;
        List<WardrobeCosmeticSlot> slots = slotMap.get(categoryId);
        selectedSlot = slots == null || slots.isEmpty() ? null : slots.getFirst().getId();
    }

    public void selectSlot(String slotId) {
        selectedSlot = slotId;
    }

    public void selectCosmetic(@Nullable dev.hardaway.wardrobe.api.cosmetic.Cosmetic cosmetic, @Nullable String option, @Nullable String variant) {
        PlayerCosmetic worn = wardrobe.getCosmetic(selectedSlot);

        if (cosmetic != null && worn != null && cosmetic.getId().equals(worn.getCosmeticId())) {
            wardrobe.removeCosmetic(selectedSlot);
            wardrobe.rebuild();
            return;
        }

        if (cosmetic == null) {
            if (worn == null) return;
            cosmetic = Objects.requireNonNull(CosmeticAsset.getAssetMap().getAsset(worn.getCosmeticId())); // TODO: registry
        }

        if (!wardrobe.isUnlocked(cosmetic.getId())) {
            return;
        }

        if (cosmetic instanceof AppearanceCosmetic a) {
            Appearance appearance = a.getAppearance();
            List<String> options = List.of(appearance.collectVariants());

            if (option == null || !options.contains(option)) {
                option = worn != null && (worn.getOptionId() != null && options.contains(worn.getOptionId()))
                        ? worn.getOptionId()
                        : options.isEmpty() ? null : options.getFirst();
            }

            List<String> variants = List.of(appearance.getTextureConfig(option).collectVariants());

            if (variant == null || !variants.contains(variant)) {
                variant = worn != null && (worn.getVariantId() != null && variants.contains(worn.getVariantId()))
                        ? worn.getVariantId()
                        : variants.isEmpty() ? null : variants.getFirst();
            }
        }

        wardrobe.setCosmetic(selectedSlot, new CosmeticSaveData(cosmetic.getId(), option, variant));
        wardrobe.rebuild();
    }

    public void toggleCosmeticType() {
        CosmeticSlotAsset slot = CosmeticSlotAsset.getAssetMap().getAsset(getSelectedSlot());
        if (slot != null && slot.getHytaleCosmeticType() != null && WardrobeUtil.canBeHidden(slot.getHytaleCosmeticType())) {
            wardrobe.toggleCosmeticType(slot.getHytaleCosmeticType());
            wardrobe.rebuild();
        }

    }
}
