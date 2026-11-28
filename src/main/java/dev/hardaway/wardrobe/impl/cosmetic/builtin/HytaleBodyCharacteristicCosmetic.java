package dev.hardaway.wardrobe.impl.cosmetic.builtin;

import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.cosmetics.CosmeticType;
import com.hypixel.hytale.server.core.cosmetics.PlayerSkinPart;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeContext;
import dev.hardaway.wardrobe.api.cosmetic.WardrobeCosmeticSlot;
import dev.hardaway.wardrobe.api.player.PlayerCosmetic;

public class HytaleBodyCharacteristicCosmetic extends HytaleCosmetic {
    public HytaleBodyCharacteristicCosmetic(CosmeticType type, PlayerSkinPart part) {
        super(type, part);
    }

    @Override
    public void applyCosmetic(WardrobeContext context, WardrobeCosmeticSlot slot, PlayerCosmetic playerCosmetic) {
        Model model = context.getPlayerModel();
        context.setPlayerModel(new Model(
                model.getModelAssetId(),
                model.getScale(),
                model.getRandomAttachmentIds(),
                model.getConditionalAttachmentIds(),
                model.getAttachments(),
                model.getBoundingBox(),
                this.getPart().getModel(),
                this.getPart().getGreyscaleTexture(),
                this.getPart().getGradientSet(),
                model.getGradientId(),
                model.getEyeHeight(),
                model.getCrouchOffset(),
                model.getSittingOffset(),
                model.getSleepingOffset(),
                model.getAnimationSetMap(),
                model.getCamera(),
                model.getLight(),
                model.getParticles(),
                model.getTrails(),
                model.getBlockCollisionEffects(),
                model.getPhysicsValues(),
                model.getDetailBoxes(),
                model.getPhobia(),
                model.getPhobiaModelAssetId()
        ));
    }
}
