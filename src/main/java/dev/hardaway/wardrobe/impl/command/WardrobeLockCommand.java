package dev.hardaway.wardrobe.impl.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hardaway.wardrobe.api.player.PlayerWardrobe;
import dev.hardaway.wardrobe.impl.cosmetic.CosmeticAsset;
import dev.hardaway.wardrobe.impl.player.PlayerWardrobeComponent;

import javax.annotation.Nonnull;

public class WardrobeLockCommand extends CommandBase {

    private final RequiredArg<PlayerRef> playerArg;
    private final RequiredArg<CosmeticAsset> cosmeticArg;

    public WardrobeLockCommand() {
        super("lock", "server.commands.wardrobe.lock.description");
        this.setPermissionGroup(GameMode.Creative);
        this.playerArg = this.withRequiredArg("player", "server.commands.wardrobe.lock.args.player.description", ArgTypes.PLAYER_REF);
        this.cosmeticArg = this.withRequiredArg("cosmetic", "server.commands.wardrobe.lock.args.cosmetic.description", WardrobeCommand.COSMETIC_ARGUMENT_TYPE);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        PlayerRef targetPlayerRef = this.playerArg.get(context);
        if (targetPlayerRef == null) {
            context.sendMessage(Message.raw("Target player not found."));
            return;
        }

        Ref<EntityStore> ref = targetPlayerRef.getReference();
        if (ref == null || !ref.isValid()) {
            context.sendMessage(Message.raw("Target player is not in a world."));
            return;
        }

        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();
        CosmeticAsset cosmetic = this.cosmeticArg.get(context);

        world.execute(() -> {
            PlayerWardrobe wardrobe = store.ensureAndGetComponent(ref, PlayerWardrobeComponent.getComponentType());
            boolean locked = wardrobe.lockCosmetic(cosmetic.getId());
            if (locked) {
                context.sendMessage(Message.join(
                        Message.raw("Locked cosmetic '"),
                        cosmetic.getProperties().getTranslationProperties().getName(),
                        Message.raw("' for " + targetPlayerRef.getUsername() + ".")
                ));
                targetPlayerRef.sendMessage(Message.join(
                        Message.raw("Cosmetic locked: "),
                        cosmetic.getProperties().getTranslationProperties().getName()
                ));
            } else {
                context.sendMessage(Message.join(
                        Message.raw("Player " + targetPlayerRef.getUsername() + " does not have cosmetic '"),
                        cosmetic.getProperties().getTranslationProperties().getName(),
                        Message.raw("' unlocked.")
                ));
            }
        });
    }
}
