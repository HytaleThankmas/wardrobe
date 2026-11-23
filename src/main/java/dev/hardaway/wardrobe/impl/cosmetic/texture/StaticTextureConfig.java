package dev.hardaway.wardrobe.impl.cosmetic.texture;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIPropertyTitle;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import dev.hardaway.wardrobe.api.cosmetic.appearance.TextureConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static dev.hardaway.wardrobe.impl.cosmetic.texture.AssetTextureValidator.TEXTURE;

public class StaticTextureConfig implements TextureConfig {

    public static final BuilderCodec<StaticTextureConfig> CODEC = BuilderCodec.builder(StaticTextureConfig.class, StaticTextureConfig::new)
            .append(new KeyedCodec<>("Texture", Codec.STRING, true),
                    (t, value) -> t.texture = value,
                    t -> t.texture
            )
            .addValidator(TEXTURE)
            .metadata(new UIPropertyTitle("Texture")).documentation("The texture to use.")
            .add()

            .build();

    private String texture;

    @Nonnull
    public String getTexture(@Nullable String variantId) {
        return texture;
    }

    @Override
    public String[] collectVariants() {
        return new String[0];
    }



}
