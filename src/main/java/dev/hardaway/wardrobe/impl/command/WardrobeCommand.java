package dev.hardaway.wardrobe.impl.command;


import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.ServerCameraSettings;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.types.AssetArgumentType;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.api.property.WardrobeCamera;
import dev.hardaway.wardrobe.impl.cosmetic.CosmeticAsset;
import dev.hardaway.wardrobe.impl.cosmetic.CosmeticSlotAsset;
import dev.hardaway.wardrobe.impl.menu.WardrobePage;
import dev.hardaway.wardrobe.impl.player.PlayerWardrobeComponent;

import javax.annotation.Nonnull;


public class WardrobeCommand extends AbstractPlayerCommand {

    public static final SingleArgumentType<CosmeticAsset> COSMETIC_ARGUMENT_TYPE = new AssetArgumentType<>("server.commands.parsing.argtype.asset.wardrobe.cosmetic.name", CosmeticAsset.class, "server.commands.parsing.argtype.asset.wardrobe.cosmetic.usage");
    public static final SingleArgumentType<CosmeticSlotAsset> COSMETIC_SLOT_ARGUMENT_TYPE = new AssetArgumentType<>("server.commands.parsing.argtype.asset.wardrobe.slot.name", CosmeticSlotAsset.class, "server.commands.parsing.argtype.asset.wardrobe.cosmetic_slot.usage");

    public WardrobeCommand() {
        super("wardrobe", "server.commands.wardrobe.description");
        this.setPermissionGroup(GameMode.Adventure);
        this.addSubCommand(new WardrobeRemoveCommand());
        this.addSubCommand(new WardrobeWearCommand());
        this.addSubCommand(new WardrobeClearCommand());
        this.addSubCommand(new WardrobeResetCommand());
        this.addSubCommand(new WardrobeUnlockCommand());
        this.addSubCommand(new WardrobeLockCommand());
        this.addSubCommand(new WardrobeUnlockAllCommand());
        this.addSubCommand(new WardrobeLockAllCommand());
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Player player = store.getComponent(ref, Player.getComponentType());

        if (player != null) {
            PlayerWardrobeComponent wardrobe = store.ensureAndGetComponent(ref, PlayerWardrobeComponent.getComponentType());
            ServerCameraSettings camera = WardrobeCamera.DEFAULT_CAMERA.toServerSettings(ref, store);
            player.getPageManager().openCustomPage(ref, store, new WardrobePage(playerRef, wardrobe, camera, true));
        }
    }
}