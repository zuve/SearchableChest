package zuve.searchablechests;

import java.util.ArrayList;
import java.util.Locale;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChestEventHandler {

	private boolean skip;

	private Minecraft mc;
	private GuiTextField searchField;
	private GuiContainer screen;
	private ArrayList<Slot> nonMatchingSlots;
	private String searchString;
	private ResourceLocation searchBar = new ResourceLocation("searchablechests", "textures/gui/search_bar.png");
	private long lastClickTime;
	private int clickCount;

	public ChestEventHandler() {
		mc = Minecraft.getMinecraft();
		nonMatchingSlots = new ArrayList<Slot>();
		searchString = "";
	}

	@SubscribeEvent
	public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
		GuiScreen gui = event.getGui();
		if (gui instanceof GuiContainer && !(gui instanceof InventoryEffectRenderer)
				&& ((GuiContainer) gui).inventorySlots.getInventory().size() >= 36 + Config.minimumContainerSize) {
			screen = (GuiContainer) gui;
			Keyboard.enableRepeatEvents(true);
			FontRenderer fontRenderer = mc.fontRenderer;
			searchField = new GuiTextField(0, fontRenderer, 81, 6, 80, fontRenderer.FONT_HEIGHT);
			searchField.setText("");
			searchField.setMaxStringLength(50);
			searchField.setEnableBackgroundDrawing(true);
			searchField.setTextColor(16777215);
			searchField.setCanLoseFocus(true);
			searchField.setVisible(true);
			searchField.setFocused(Config.autoFocus);
		} else {
			searchField = null;
		}
	}

	@SubscribeEvent
	public void onCharTyped(GuiScreenEvent.KeyboardInputEvent.Pre event) {
		if (searchField != null) {
			if (skip) {
				skip = false;
			} else {
				if (searchField.isFocused()
						&& searchField.textboxKeyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey())) {
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public void onKeyPressed(GuiScreenEvent.KeyboardInputEvent event) {
		if (searchField != null) {
			int keyCode = Keyboard.getEventKey();
			char keyChar = Keyboard.getEventCharacter();
			if (searchField.isFocused()) {
				if (keyCode == mc.gameSettings.keyBindInventory.getKeyCode() || (keyCode >= 262 && keyCode <= 265)) {
					event.setCanceled(true);
					switch (keyCode) {
					case 262:
						if (GuiScreen.isShiftKeyDown()) {
							if (searchField.getSelectedText().isEmpty()) {
								searchField.setSelectionPos(searchField.getCursorPosition());
							}
							if (GuiScreen.isCtrlKeyDown()) {
								searchField.setCursorPosition(searchField.getNthWordFromCursor(1));
							} else {
								searchField.setCursorPosition(searchField.getCursorPosition() + 1);
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
								searchField.setCursorPosition(searchField.getNthWordFromCursor(-1));
							} else {
								searchField.setCursorPosition(searchField.getCursorPosition() - 1);
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
				if (searchField.textboxKeyTyped(keyChar, keyCode)) {
					for (int i = 0; i < 9; ++i) {
						if (mc.gameSettings.keyBindsHotbar[i].isActiveAndMatches(keyCode)) {
							event.setCanceled(true);
						}
					}
				}
			} else {
				if (mc.gameSettings.keyBindChat.isActiveAndMatches(keyCode)) {
					searchField.setFocused(true);
					event.setCanceled(true);
					skip = true;
				}
			}
		}
	}

	@SubscribeEvent
	public void onMouseEvent(GuiScreenEvent.MouseInputEvent.Pre event) {
		if (searchField != null && Mouse.getEventButtonState()) {
			long clickTime = System.currentTimeMillis();
			if (clickTime - lastClickTime <= 475) {
				clickCount++;
				lastClickTime = System.currentTimeMillis();
			} else {
				clickCount = 1;
				lastClickTime = System.currentTimeMillis();
			}

			int scaleFactor = new ScaledResolution(mc).getScaleFactor();

			// Correct mouse location, for whatever reason textbox click method doesn't work
			// with raw mouse data.
			int x = (Mouse.getEventX() - screen.getGuiLeft() * scaleFactor) / scaleFactor;
			int y = ((screen.height * scaleFactor - Mouse.getEventY()) - screen.getGuiTop() * scaleFactor)
					/ scaleFactor;

			boolean alreadyFocused = searchField.isFocused();
			
			boolean overSearchField = searchField.mouseClicked(x, y, Mouse.getEventButton());
			
			if (alreadyFocused) {

				int lastCursorPos = searchField.getCursorPosition();
				int cursorPos = searchField.getCursorPosition();

				if (!GuiScreen.isShiftKeyDown()) {
					searchField.setSelectionPos(searchField.getCursorPosition());
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
			} else if (overSearchField) {
				searchField.setCursorPositionEnd();
				searchField.setSelectionPos(0);
			}
		}
	}

	@SubscribeEvent
	public void onForeground(GuiContainerEvent.DrawForeground event) {
		if (searchField != null) {
			GlStateManager.disableLighting();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(searchBar);
			Gui.drawModalRectWithCustomSizedTexture(79, 4, 0.0F, 0.0F, 90, 12, 90, 12);
			searchField.setEnabled(true);
			searchField.drawTextBox();
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
				GlStateManager.disableDepth();
				Gui.drawRect(x, y, x + 16, y + 16, 0x80FF0000);
				GlStateManager.enableDepth();
			}
			GlStateManager.enableLighting();
		}
	}

	private boolean stackMatches(String text, ItemStack stack) {
		if (stack.getItem().equals(Items.AIR)) {
			return true;
		}
		ArrayList<String> keys = new ArrayList<String>();
		for (String line : stack.getTooltip(mc.player,
				mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL)) {
			keys.add(line);
		}
		for (String key : keys) {
			if (key.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))) {
				return true;
			}
		}
		return false;
	}

}