package mods.example;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class flymod implements ClientModInitializer {

	// Whether the auto-platform mode is currently active (toggled by /fly)
	private static boolean flyModeActive = false;
	private static boolean isGlass(ItemStack stack) {
		// Only accept plain, non-stained glass (minecraft:glass)
		return stack.getItem() == Items.GLASS;
	}

	/** Remove glass from direct inventory slots (not containers). */
	private static int removeGlassFromInventory(Inventory inventory, int amount) {
		int remaining = amount;
		// First check direct slots
		for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
			ItemStack stack = inventory.getItem(i);
			if (isGlass(stack)) {
				int toRemove = Math.min(remaining, stack.getCount());
				stack.shrink(toRemove);
				remaining -= toRemove;
			}
		}
		// Then check inside containers (shulker boxes etc.)
		for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
			ItemStack containerStack = inventory.getItem(i);
			ItemContainerContents contents = containerStack.get(DataComponents.CONTAINER);
			if (contents != null) {
				for (ItemStack nested : contents.nonEmptyItems()) {
					if (isGlass(nested) && remaining > 0) {
						int toRemove = Math.min(remaining, nested.getCount());
						nested.shrink(toRemove);
						remaining -= toRemove;
					}
				}
			}
		}
		return amount - remaining;
	}

	/** Find and take one glass block from inventory (including containers), return its BlockState. */
	private static BlockState takeOneGlass(Inventory inventory) {
		// Direct slots first
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (isGlass(stack)) {
				// Every placeable block item in Minecraft is an instance of BlockItem.
				// We cast here so we can call .getBlock() to get the actual Block object
				// and then retrieve its default visual/collision state.
				if (stack.getItem() instanceof BlockItem) {
					var blockItem = (BlockItem) stack.getItem();
					BlockState state = blockItem.getBlock().defaultBlockState();
					stack.shrink(1); // remove one block from the stack
					return state;
				}
			}
		}
		// Then containers
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemContainerContents contents = inventory.getItem(i).get(DataComponents.CONTAINER);
			if (contents != null) {
				for (ItemStack nested : contents.nonEmptyItems()) {
					if (isGlass(nested)) {
						if (nested.getItem() instanceof BlockItem) {
							var blockItem = (BlockItem) nested.getItem();
							BlockState state = blockItem.getBlock().defaultBlockState();
							nested.shrink(1);
							return state;
						}
					}
				}
			}
		}
		return Blocks.GLASS.defaultBlockState();
	}

	/**
	 * Counts glass in a single ItemStack, including recursing into containers (shulker boxes, etc.).
	 */
	private static int countGlassIn(ItemStack stack) {
		int count = 0;
		if (isGlass(stack)) {
			count += stack.getCount();
		}
		ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
		if (contents != null) {
			for (ItemStack nested : contents.nonEmptyItems()) {
				count += countGlassIn(nested);
			}
		}
		return count;
	}

	public static boolean hasEnoughGlass() {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) return false;

		Inventory inventory = client.player.getInventory();
		int count = 0;
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			count += countGlassIn(inventory.getItem(i));
			if (count >= 64) return true;
		}
		return false;
	}

	@Override
	public void onInitializeClient() {
		// Every game tick (~20 per second), check if fly mode is on and place glass if needed
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (!flyModeActive) return;
			if (client.player == null || client.level == null) return;
			if (client.player.onGround()) return;
			if (!client.hasSingleplayerServer()) return;

			MinecraftServer server = client.getSingleplayerServer();
			ServerLevel serverLevel = server.getLevel(client.player.level().dimension());
			if (serverLevel == null) return;

			// Place glass only directly below the player's feet (1 block, not full 3x3 every tick)
			BlockPos below = client.player.blockPosition().below();
			if (serverLevel.getBlockState(below).isAir() && hasEnoughGlass()) {
				BlockState glassState = takeOneGlass(client.player.getInventory());
				serverLevel.setBlock(below, glassState, 3);
			}
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
			dispatcher.register(
				ClientCommandManager.literal("fly")
					.executes(this::toggleFlyCommand)
					.then(ClientCommandManager.literal("check")
						.then(ClientCommandManager.literal("inv")
							.executes(this::checkInventoryCommand)
						)
					)
			)
		);
	}

	private int toggleFlyCommand(CommandContext<FabricClientCommandSource> context) {
		flyModeActive = !flyModeActive;
		String status = flyModeActive ? "ON — glass will be placed under you automatically" : "OFF";
		context.getSource().sendFeedback(Component.literal("Fly mode: " + status));
		return 1;
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
					BlockState glassState = takeOneGlass(client.player.getInventory());
					serverLevel.setBlock(pos, glassState, 3);
					placed++;
				}
			}
		}

		context.getSource().sendFeedback(Component.literal("Placed " + placed + " glass block(s) as a 3x3 platform."));
		return 1;
	}
}

