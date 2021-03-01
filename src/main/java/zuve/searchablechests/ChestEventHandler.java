package zuve.searchablechests;

import java.util.ArrayList;
import java.util.Locale;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;

@EventBusSubscriber(modid = "searchablechests", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ChestEventHandler {

	private boolean skip;

	private Minecraft mc;
	private RichTextFieldWidget searchField;
	private boolean newGui;
	private ResourceLocation searchBar = new ResourceLocation("searchablechests", "textures/gui/search_bar.png");

	public ChestEventHandler() {
		mc = Minecraft.getInstance();
	}

	@SubscribeEvent
	public static void onModConfigEvent(final ModConfig.ModConfigEvent event) {
		final ModConfig config = event.getConfig();
		if (config.getSpec() == SearchableChests.CONFIG_SPEC) {
			SearchableChestsConfig.autoFocus = SearchableChests.CONFIG.autoFocus.get();
			SearchableChestsConfig.autoSelect = SearchableChests.CONFIG.autoSelect.get();
			SearchableChestsConfig.preserveSearch = SearchableChests.CONFIG.preserveSearch.get();
			SearchableChestsConfig.minimumContainerSize = SearchableChests.CONFIG.minimumContainerSize.get();
			SearchableChestsConfig.blacklist = SearchableChests.CONFIG.blacklist.get();
		}
	}

	@SubscribeEvent
	public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
		Screen gui = event.getGui();
		if (gui instanceof ContainerScreen && !(gui instanceof InventoryScreen)
				&& ((ContainerScreen<?>) gui).getContainer().getInventory().size() >= 36
						+ SearchableChestsConfig.minimumContainerSize
				&& !SearchableChestsConfig.blacklist.contains(gui.getTitle().getString())
				&& !SearchableChestsConfig.blacklistCode.contains(gui.getClass().getName())) {
			ContainerScreen<?> containerGui = (ContainerScreen<?>) gui;
			mc.keyboardListener.enableRepeatEvents(true);
			FontRenderer fontRenderer = mc.fontRenderer;
			searchField = new RichTextFieldWidget(fontRenderer, containerGui.getGuiLeft() + 81,
					containerGui.getGuiTop() + 6, 80, fontRenderer.FONT_HEIGHT, newGui ? null : searchField, ITextComponent.getTextComponentOrEmpty(null));
			newGui = false;
			event.addWidget(searchField);
			searchField.setMaxStringLength(50);
			searchField.setEnableBackgroundDrawing(false);
			searchField.setTextColor(16777215);
			searchField.setCanLoseFocus(true);
			searchField.setSelectOnFocus(SearchableChestsConfig.autoSelect);
			searchField.setVisible(true);
			gui.changeFocus(true);
			searchField.setFocused2(SearchableChestsConfig.autoFocus);
		} else {
			searchField = null;
		}
	}

	@SubscribeEvent
	public void onCharTyped(GuiScreenEvent.KeyboardCharTypedEvent.Pre event) {
		if (searchField != null) {
			if (skip) {
				skip = false;
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void onKeyPressed(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
		if (searchField != null) {
			if (!searchField.isFocused() && mc.gameSettings.keyBindChat.getKey().getKeyCode() == event.getKeyCode()) {
				searchField.setFocused2(true);
				skip = true;
			} else if (searchField.isFocused()){
				for (KeyBinding k : mc.gameSettings.keyBindings) {
					if (k.isActiveAndMatches(InputMappings.getInputByCode(event.getKeyCode(), event.getScanCode()))) {
						event.setCanceled(true);
						break;
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onOpenGui(GuiOpenEvent event) {
		newGui = !SearchableChestsConfig.preserveSearch;
	}

	@SubscribeEvent
	public void onBackground(GuiContainerEvent.DrawBackground event) {
		if (searchField != null) {
			mc.getTextureManager().bindTexture(searchBar);
			AbstractGui.blit(event.getMatrixStack(),
					event.getGuiContainer().getGuiLeft() + 79, event.getGuiContainer().getGuiTop() + 4,
					0.0F, 0.0F, 90, 12, 90, 12);
		}
	}

	@SubscribeEvent
	public void onForeground(GuiContainerEvent.DrawForeground event) {
		if (searchField != null) {
			for (Slot s : event.getGuiContainer().getContainer().inventorySlots) {
				if (!(s.inventory instanceof PlayerInventory)) {
					ItemStack stack = s.getStack();
					if (!stackMatches(searchField.getText(), stack)) {
						int x = s.xPos;
						int y = s.yPos;
						RenderSystem.disableDepthTest();
						AbstractGui.fill(event.getMatrixStack(), x, y, x + 16, y + 16, 0x80FF0000);
						RenderSystem.enableDepthTest();
					}
				}
			}
		}
	}

	private boolean stackMatches(String text, ItemStack stack) {
		if (stack.getItem().equals(Items.AIR)) {
			return true;
		}
		ArrayList<String> keys = new ArrayList<String>();
		for (ITextComponent line : stack.getTooltip(mc.player,
				mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL)) {
			keys.add(line.getString());
		}
		for (String key : keys) {
			if (key.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))) {
				return true;
			}
		}
		return false;
	}

}
