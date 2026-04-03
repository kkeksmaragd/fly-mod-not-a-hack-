package mods.example;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class flymod implements ClientModInitializer {


	private static int removeGlassFromInventory(Inventory inventory, int amount) {
		int remaining = amount;
		for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
			ItemStack stack = inventory.getItem(i);
			if (stack.getItem() == Items.GLASS) {
				int toRemove = Math.min(remaining, stack.getCount());
				stack.shrink(toRemove);
				remaining -= toRemove;
			}
		}
		return amount - remaining;
	}

	public static boolean hasEnoughGlass() {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) return false;

		Inventory inventory = client.player.getInventory();
		int count = 0;
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (stack.getItem() == Items.GLASS) {
				count += stack.getCount();
				if (count >= 64) return true;
			}
		}
		return false;
	}

	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
			dispatcher.register(
				ClientCommandManager.literal("fly")
					.executes(this::placePlatformCommand)
					.then(ClientCommandManager.literal("check")
						.then(ClientCommandManager.literal("inv")
							.executes(this::checkInventoryCommand)
						)
					)
			)
		);
	}

	private int checkInventoryCommand(CommandContext<FabricClientCommandSource> context) {
		boolean result = hasEnoughGlass();
		context.getSource().sendFeedback(
			Component.literal("Has at least 64 glass: " + result)
		);
		return 1;
	}

	private int placePlatformCommand(CommandContext<FabricClientCommandSource> context) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null) {
			context.getSource().sendFeedback(Component.literal("Cannot place platform: no player or world."));
			return 0;
		}

		if (client.player.onGround()) {
			context.getSource().sendFeedback(Component.literal("You are not in the air!"));
			return 0;
		}

		if (!hasEnoughGlass()) {
			context.getSource().sendFeedback(Component.literal("You need at least 64 glass blocks in your inventory!"));
			return 0;
		}

		if (!client.hasSingleplayerServer()) {
			context.getSource().sendFeedback(Component.literal("Platform placement only works in singleplayer."));
			return 0;
		}

		MinecraftServer server = client.getSingleplayerServer();
		ServerLevel serverLevel = server.getLevel(client.player.level().dimension());
		if (serverLevel == null) {
			context.getSource().sendFeedback(Component.literal("Could not access server world."));
			return 0;
		}

		BlockPos below = client.player.blockPosition().below();
		int placed = 0;
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				BlockPos pos = below.offset(x, 0, z);
				if (serverLevel.getBlockState(pos).isAir()) {
					serverLevel.setBlock(pos, Blocks.GLASS.defaultBlockState(), 3);
					removeGlassFromInventory(client.player.getInventory(), 1);
					placed++;
				}
			}
		}

		context.getSource().sendFeedback(Component.literal("Placed " + placed + " glass block(s) as a 3x3 platform."));
		return 1;
	}
}

