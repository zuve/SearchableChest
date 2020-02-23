package zuve.searchablechests;

import java.util.ArrayList;
import java.util.Locale;

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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;

@EventBusSubscriber(modid = "searchablechests", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ChestEventHandler {

	private boolean skip;

	private Minecraft mc;
	private GuiTextField searchField;
	private ResourceLocation searchBar = new ResourceLocation("searchablechests", "textures/gui/search_bar.png");
	private long lastClickTime;
	private int clickCount;

	public ChestEventHandler() {
		mc = Minecraft.getInstance();
	}

	@SubscribeEvent
	public static void onModConfigEvent(final ModConfig.ModConfigEvent event) {
		final ModConfig config = event.getConfig();
		if (config.getSpec() == SearchableChests.CONFIG_SPEC) {
			SearchableChestsConfig.autoFocus = SearchableChests.CONFIG.autoFocus.get();
			SearchableChestsConfig.autoSelect = SearchableChests.CONFIG.autoSelect.get();
			SearchableChestsConfig.minimumContainerSize = SearchableChests.CONFIG.minimumContainerSize.get();
		}
	}

	@SubscribeEvent
	public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
		GuiScreen gui = event.getGui();
		if (gui instanceof GuiContainer && !(gui instanceof InventoryEffectRenderer)
				&& ((GuiContainer) gui).inventorySlots.getInventory().size() >= 36
						+ SearchableChestsConfig.minimumContainerSize) {
			mc.keyboardListener.enableRepeatEvents(true);
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
			if (!skip) {
				searchField.charTyped(event.getCodePoint(), event.getCodePoint());
			} else {
				skip = false;
			}
		}
	}

	@SubscribeEvent
	public void onKeyPressed(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
		if (searchField != null) {
			int keyCode = event.getKeyCode();
			int scanCode = event.getScanCode();

			if (mc.gameSettings.keyBindChat.matchesKey(keyCode, scanCode)) {
				if (!searchField.keyPressed(keyCode, scanCode, event.getModifiers())) {
					searchField.setFocused(true);
					skip = true;
				}
			} else {
				if (keyCode >= 262 && keyCode <= 265) {
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
				} else if (searchField.keyPressed(keyCode, scanCode, event.getModifiers())) {
					for (int i = 0; i < 9; ++i) {
						if (mc.gameSettings.keyBindsHotbar[i]
								.isActiveAndMatches(InputMappings.getInputByCode(keyCode, scanCode))) {
							event.setCanceled(true);
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onMouseClicked(GuiScreenEvent.MouseClickedEvent.Pre event) {
		if (searchField != null) {
			long clickTime = System.currentTimeMillis();
			if (clickTime - lastClickTime <= 475) {
				clickCount++;
				lastClickTime = System.currentTimeMillis();
			} else {
				clickCount = 1;
				lastClickTime = System.currentTimeMillis();
			}
			double x = event.getMouseX() - ((GuiContainer) event.getGui()).getGuiLeft();
			double y = event.getMouseY() - ((GuiContainer) event.getGui()).getGuiTop();

			boolean alreadyFocused = searchField.isFocused();

			int lastCursorPos = searchField.getCursorPosition();
			boolean overSearchField = searchField.mouseClicked(x, y, event.getButton());
			int cursorPos = searchField.getCursorPosition();
			
			if (alreadyFocused && overSearchField) {
				if (GuiScreen.isShiftKeyDown()) {
					searchField.setCursorPosition(lastCursorPos);
					searchField.setSelectionPos(cursorPos);
				}
				if (cursorPos == lastCursorPos || clickCount == 3) {
					switch (clickCount) {
					case 2:
						searchField.setCursorPosition(searchField.getNthWordFromCursor(1)
								- ((searchField.getNthWordFromCursor(1) == searchField.getText().length()) ? 0 : 1));
						searchField.setSelectionPos(searchField.getNthWordFromCursor(-1));
						break;
					case 3:
						searchField.setCursorPositionEnd();
						searchField.setSelectionPos(0);
						break;
					}
				} else {
					clickCount = 1;
				}
			} else if (overSearchField && SearchableChestsConfig.autoSelect) {
				searchField.setCursorPositionZero();
				searchField.setSelectionPos(searchField.getText().length());
			} else {
				searchField.setCursorPositionZero();
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
			for (Slot s : event.getGuiContainer().inventorySlots.inventorySlots) {
				if (!(s.inventory instanceof InventoryPlayer)) {
					ItemStack stack = s.getStack();
					if (!stackMatches(searchField.getText(), stack)) {
						int x = s.xPos;
						int y = s.yPos;
						GlStateManager.disableDepthTest();
						Gui.drawRect(x, y, x + 16, y + 16, 0x80FF0000);
						GlStateManager.enableDepthTest();
					}
				}
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