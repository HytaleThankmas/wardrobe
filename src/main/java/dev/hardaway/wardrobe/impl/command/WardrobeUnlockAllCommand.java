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
import dev.hardaway.wardrobe.impl.player.PlayerWardrobeComponent;

import javax.annotation.Nonnull;

public class WardrobeUnlockAllCommand extends CommandBase {

    private final RequiredArg<PlayerRef> playerArg;

    public WardrobeUnlockAllCommand() {
        super("unlockall", "server.commands.wardrobe.unlockall.description");
        this.setPermissionGroup(GameMode.Creative);
        this.playerArg = this.withRequiredArg("player", "server.commands.wardrobe.unlockall.args.player.description", ArgTypes.PLAYER_REF);
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

        world.execute(() -> {
            PlayerWardrobe wardrobe = store.ensureAndGetComponent(ref, PlayerWardrobeComponent.getComponentType());
            wardrobe.unlockAllCosmetics();
            context.sendMessage(Message.raw("Unlocked all cosmetics for " + targetPlayerRef.getUsername() + "."));
            targetPlayerRef.sendMessage(Message.raw("All cosmetics have been unlocked for you!"));
        });
    }
}
