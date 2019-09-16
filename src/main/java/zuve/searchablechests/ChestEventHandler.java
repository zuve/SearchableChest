package zuve.searchablechests;

import java.util.ArrayList;
import java.util.Locale;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;

@EventBusSubscriber(modid = "searchablechests")
public class ChestEventHandler {

	private boolean skip;

	private Minecraft mc;
	private GuiTextField searchField;
	private ArrayList<Slot> nonMatchingSlots;
	private String searchString;
	private ResourceLocation searchBar = new ResourceLocation("searchablechests", "textures/gui/search_bar.png");

	public ChestEventHandler() {
		mc = Minecraft.getMinecraft();
		nonMatchingSlots = new ArrayList<Slot>();
		searchString = "";
	}

	@SubscribeEvent
	public static void onModConfigEvent(final ModConfig.ModConfigEvent event) {
		final ModConfig config = event.getConfig();
		if (config.getSpec() == SearchableChests.CONFIG_SPEC) {
			SearchableChestsConfig.autoFocus = SearchableChests.CONFIG.autoFocus.get();
			SearchableChestsConfig.minimumContainerSize = SearchableChests.CONFIG.minimumContainerSize.get();
		}
	}

	@SubscribeEvent
	public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
		GuiScreen gui = event.getGui();
		if (gui instanceof GuiContainer && !(gui instanceof InventoryEffectRenderer)
				&& ((GuiContainer) gui).inventorySlots.getInventory().size() >= 36
						+ SearchableChestsConfig.minimumContainerSize) {
			Keyboard.enableRepeatEvents(true);
			FontRenderer fontRenderer = mc.fontRenderer;
			searchField = new GuiTextField(0, fontRenderer, 81, 6, 80, fontRenderer.FONT_HEIGHT);
			searchField.setText("");
			searchField.setMaxStringLength(50);
			searchField.setEnableBackgroundDrawing(false);
			searchField.setTextColor(16777215);
			searchField.setCanLoseFocus(true);
			searchField.setVisible(true);
			searchField.setFocused(SearchableChestsConfig.autoFocus);
		} else {
			searchField = null;
		}
	}

	@SubscribeEvent
	public void onCharTyped(GuiScreenEvent.KeyboardCharTypedEvent.Pre event) {
		if (searchField != null) {
			if (skip) {
				skip = false;
			} else {
				if (searchField.isFocused() && searchField.charTyped(event.getCodePoint(), event.getCodePoint())) {
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public void onKeyPressed(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
		if (searchField != null) {
			int keyCode = event.getKeyCode();
			int scanCode = event.getScanCode();
			if (searchField.isFocused()) {
				if (keyCode == 69 || (keyCode >= 262 && keyCode <= 265)) {
					event.setCanceled(true);
					switch (keyCode) {
					case 262:
						if (GuiScreen.isShiftKeyDown()) {
							if (searchField.getSelectedText().isEmpty()) {
								searchField.setSelectionPos(searchField.getCursorPosition());
							}
							if (GuiScreen.isCtrlKeyDown()) {
								searchField.func_212422_f(searchField.getNthWordFromCursor(1));
							} else {
								searchField.func_212422_f(searchField.getCursorPosition() + 1);
							}
						} else if (GuiScreen.isCtrlKeyDown()) {
							searchField.setCursorPosition(searchField.getNthWordFromCursor(1));
						} else if (!searchField.getSelectedText().isEmpty()) {
							int rightSelection = searchField.getCursorPosition() > searchField.getSelectionEnd()
									? searchField.getCursorPosition()
									: searchField.getSelectionEnd();
							searchField.setCursorPosition(rightSelection);
						} else {
							searchField.moveCursorBy(1);
						}
						break;
					case 263:
						if (GuiScreen.isShiftKeyDown()) {
							if (searchField.getSelectedText().isEmpty()) {
								searchField.setSelectionPos(searchField.getCursorPosition());
							}
							if (GuiScreen.isCtrlKeyDown()) {
								searchField.func_212422_f(searchField.getNthWordFromCursor(-1));
							} else {
								searchField.func_212422_f(searchField.getCursorPosition() - 1);
							}
						} else if (GuiScreen.isCtrlKeyDown()) {
							searchField.setCursorPosition(searchField.getNthWordFromCursor(-1));
						} else if (!searchField.getSelectedText().isEmpty()) {
							int leftSelection = searchField.getCursorPosition() < searchField.getSelectionEnd()
									? searchField.getCursorPosition()
									: searchField.getSelectionEnd();
							searchField.setCursorPosition(leftSelection);
						} else {
							searchField.moveCursorBy(-1);
						}
						break;
					case 264:
						if (GuiScreen.isShiftKeyDown()) {
							searchField.setSelectionPos(searchField.getText().length());
						} else {
							searchField.setCursorPositionEnd();
							searchField.setSelectionPos(searchField.getCursorPosition());
						}
						break;
					case 265:
						if (GuiScreen.isShiftKeyDown()) {
							searchField.setSelectionPos(0);
						} else {
							searchField.setCursorPositionZero();
							searchField.setSelectionPos(searchField.getCursorPosition());
						}
						break;
					}
					return;
				}
				if (searchField.keyPressed(keyCode, scanCode, event.getModifiers())) {
					for (int i = 0; i < 9; ++i) {
						if (mc.gameSettings.keyBindsHotbar[i]
								.isActiveAndMatches(InputMappings.getInputByCode(keyCode, scanCode))) {
							event.setCanceled(true);
						}
					}
				}
			} else {
				if (mc.gameSettings.keyBindChat.matchesKey(keyCode, scanCode)) {
					searchField.setFocused(true);
					event.setCanceled(true);
					skip = true;
				}
			}
		}
	}

	@SubscribeEvent
	public void onMouseClicked(GuiScreenEvent.MouseClickedEvent.Pre event) {
		if (searchField != null) {
			double x = event.getMouseX() - ((GuiContainer) event.getGui()).getGuiLeft();
			double y = event.getMouseY() - ((GuiContainer) event.getGui()).getGuiTop();

			int initialCursorPos = searchField.getCursorPosition();
			searchField.mouseClicked(x, y, event.getButton());
			if (GuiScreen.isShiftKeyDown()) {
				searchField.setSelectionPos(initialCursorPos);
			}
		}
	}

	@SubscribeEvent
	public void onForeground(GuiContainerEvent.DrawForeground event) {
		if (searchField != null) {
			GlStateManager.disableLighting();
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(searchBar);
			Gui.drawModalRectWithCustomSizedTexture(79, 4, 0.0F, 0.0F, 90, 12, 90, 12);
			searchField.drawTextField(event.getMouseX(), event.getMouseY(), mc.getRenderPartialTicks());
			if (!searchString.equals(searchField.getText())) {
				searchString = searchField.getText();
				nonMatchingSlots.clear();
				for (Slot s : event.getGuiContainer().inventorySlots.inventorySlots) {
					if (!(s.inventory instanceof InventoryPlayer)) {
						ItemStack stack = s.getStack();
						if (!stackMatches(searchField.getText(), stack)) {
							nonMatchingSlots.add(s);
						}
					}
				}
			}
			for (Slot s : nonMatchingSlots) {
				int x = s.xPos;
				int y = s.yPos;
				GlStateManager.disableDepthTest();
				Gui.drawRect(x, y, x + 16, y + 16, 0x80FF0000);
				GlStateManager.enableDepthTest();
			}
			GlStateManager.enableLighting();
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