package dev.hardaway.wardrobe.impl.cosmetic.texture;

import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;

public class AssetTextureValidator extends CommonAssetValidator {
    public static final CommonAssetValidator TEXTURE = new CommonAssetValidator("png", new String[]{"Characters", "NPC", "Items", "Cosmetics", "Items", "NPC", "Resources", "Blocks"});
}
