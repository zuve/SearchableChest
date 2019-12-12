package zuve.searchablechests;

import java.util.ArrayList;
import java.util.Locale;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
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
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;

@EventBusSubscriber(modid = "searchablechests", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ChestEventHandler {

	private boolean skip;

	private Minecraft mc;
	private TextFieldWidget searchField;
	private ArrayList<Slot> nonMatchingSlots;
	private String searchString;
	private ResourceLocation searchBar = new ResourceLocation("searchablechests", "textures/gui/search_bar.png");

	public ChestEventHandler() {
		mc = Minecraft.getInstance();
		nonMatchingSlots = new ArrayList<Slot>();
		searchString = "";
	}

	@SubscribeEvent
	public static void onModConfigEvent(final ModConfig.ModConfigEvent event) {
		final ModConfig config = event.getConfig();
		if (config.getSpec() == SearchableChests.CONFIG_SPEC) {
			SearchableChestsConfig.autoFocus = SearchableChests.CONFIG.autoFocus.get();
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
				&& !SearchableChestsConfig.blacklist.contains(gui.getTitle().getString())) {
			mc.keyboardListener.enableRepeatEvents(true);
			FontRenderer fontRenderer = mc.fontRenderer;
			searchField = new TextFieldWidget(fontRenderer, 81, 6, 80, fontRenderer.FONT_HEIGHT, "");
			searchField.setText("");
			searchField.setMaxStringLength(50);
			searchField.setEnableBackgroundDrawing(false);
			searchField.setTextColor(16777215);
			searchField.setCanLoseFocus(true);
			searchField.setVisible(true);
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
						if (Screen.hasShiftDown()) {
							if (searchField.getSelectedText().isEmpty()) {
								searchField.setSelectionPos(searchField.getCursorPosition());
							}
							if (Screen.hasControlDown()) {
								searchField.setCursorPosition(searchField.getNthWordFromCursor(1));
							} else {
								searchField.moveCursorBy(1);
							}
						} else if (Screen.hasControlDown()) {
							searchField.setCursorPosition(searchField.getNthWordFromCursor(1));
							searchField.setSelectionPos(searchField.getCursorPosition());
						} else if (!searchField.getSelectedText().isEmpty()) {
							String text = searchField.getText();
							String selectedText = searchField.getSelectedText();
							int selectedTextLength = selectedText.length();
							int cursorPosition = searchField.getCursorPosition();
							int rightSelection;
							if (cursorPosition + selectedTextLength < text.length()
									&& text.substring(cursorPosition, cursorPosition + selectedTextLength)
											.equals(selectedText)) {
								rightSelection = cursorPosition + selectedTextLength;
								searchField.setCursorPosition(rightSelection);
							} else {
								rightSelection = cursorPosition;
								searchField.setSelectionPos(rightSelection);
							}
						} else {
							searchField.moveCursorBy(1);
							searchField.setSelectionPos(searchField.getCursorPosition());
						}
						break;
					case 263:
						if (Screen.hasShiftDown()) {
							if (searchField.getSelectedText().isEmpty()) {
								searchField.setSelectionPos(searchField.getCursorPosition());
							}
							if (Screen.hasControlDown()) {
								searchField.setCursorPosition(searchField.getNthWordFromCursor(-1));
							} else {
								searchField.moveCursorBy(-1);
							}
						} else if (Screen.hasControlDown()) {
							searchField.setCursorPosition(searchField.getNthWordFromCursor(-1));
							searchField.setSelectionPos(searchField.getCursorPosition());
						} else if (!searchField.getSelectedText().isEmpty()) {
							String text = searchField.getText();
							String selectedText = searchField.getSelectedText();
							int selectedTextLength = selectedText.length();
							int cursorPosition = searchField.getCursorPosition();
							int leftSelection;
							if (cursorPosition + selectedTextLength < text.length()
									&& text.substring(cursorPosition, cursorPosition + selectedTextLength)
											.equals(selectedText)) {
								leftSelection = cursorPosition;
								searchField.setSelectionPos(leftSelection);
							} else {
								leftSelection = cursorPosition - selectedTextLength;
								searchField.setCursorPosition(leftSelection);
							}
						} else {
							searchField.moveCursorBy(-1);
							searchField.setSelectionPos(searchField.getCursorPosition());
						}
						break;
					case 264:
						if (Screen.hasShiftDown()) {
							searchField.setSelectionPos(searchField.getText().length());
						} else {
							searchField.setCursorPositionEnd();
							searchField.setSelectionPos(searchField.getCursorPosition());
						}
						break;
					case 265:
						if (Screen.hasShiftDown()) {
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
					searchField.changeFocus(true);
					event.setCanceled(true);
					skip = true;
				}
			}
		}
	}

	@SubscribeEvent
	public void onMouseClicked(GuiScreenEvent.MouseClickedEvent.Pre event) {
		if (searchField != null) {
			double x = event.getMouseX() - ((ContainerScreen<?>) event.getGui()).getGuiLeft();
			double y = event.getMouseY() - ((ContainerScreen<?>) event.getGui()).getGuiTop();

			searchField.mouseClicked(x, y, event.getButton());
			if (!Screen.hasShiftDown()) {
				searchField.setSelectionPos(searchField.getCursorPosition());
			}
		}
	}

	@SubscribeEvent
	public void onForeground(GuiContainerEvent.DrawForeground event) {
		if (searchField != null) {
			GlStateManager.disableLighting();
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(searchBar);
			AbstractGui.blit(79, 4, 0.0F, 0.0F, 90, 12, 90, 12);
			searchField.render(event.getMouseX(), event.getMouseY(), mc.getRenderPartialTicks());
			if (!searchString.equals(searchField.getText())) {
				searchString = searchField.getText();
				nonMatchingSlots.clear();
				for (Slot s : event.getGuiContainer().getContainer().inventorySlots) {
					if (!(s.inventory instanceof PlayerInventory)) {
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
				AbstractGui.fill(x, y, x + 16, y + 16, 0x80FF0000);
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