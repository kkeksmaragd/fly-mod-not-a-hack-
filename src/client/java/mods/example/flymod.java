package mods.example;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

public class flymod implements ClientModInitializer {

	/**
	 * Returns true if the local player has at least 64 glass blocks in their inventory.
	 */
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
}
