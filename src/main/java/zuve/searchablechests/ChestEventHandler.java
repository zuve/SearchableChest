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
import net.minecraft.util.math.MathHelper;
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
	private int selectionEnd;
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
			if (!skip) {
				searchField.charTyped(event.getCodePoint(), event.getCodePoint());
			} else {
				skip = false;
			}
		}
	}

	private void updateCursorPosition(int position) {
		searchField.setCursorPosition(position);
		if (!Screen.hasShiftDown()) {
			updateSelectionEnd(position);
		}
	}

	private void updateSelectionEnd(int position) {
		int i = searchField.getText().length();
		searchField.setSelectionPos(position);
		selectionEnd = MathHelper.clamp(position, 0, i);
	}

	@SubscribeEvent
	public void onKeyPressed(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
		if (searchField != null) {
			int keyCode = event.getKeyCode();
			int scanCode = event.getScanCode();

			if (mc.gameSettings.keyBindChat.matchesKey(keyCode, scanCode)) {
				if (!searchField.keyPressed(keyCode, scanCode, event.getModifiers())) {
					searchField.setFocused2(true);
					skip = true;
				}
			} else {
				if (keyCode >= 262 && keyCode <= 265) {
					switch (keyCode) {
					case 262:
						if (Screen.hasShiftDown()) {
							if (searchField.getSelectedText().isEmpty()) {
								updateSelectionEnd(searchField.getCursorPosition());
							}
							if (Screen.hasControlDown()) {
								updateCursorPosition(searchField.getNthWordFromCursor(1));
							} else {
								updateCursorPosition(searchField.getCursorPosition() + 1);
							}
						} else if (Screen.hasControlDown()) {
							updateCursorPosition(searchField.getNthWordFromCursor(1));
						} else if (!searchField.getSelectedText().isEmpty()) {
							int rightSelection = searchField.getCursorPosition() > selectionEnd
									? searchField.getCursorPosition()
									: selectionEnd;
							updateCursorPosition(rightSelection);
						} else {
							updateCursorPosition(searchField.getCursorPosition() + 1);
						}
						break;
					case 263:
						if (Screen.hasShiftDown()) {
							if (searchField.getSelectedText().isEmpty()) {
								updateSelectionEnd(searchField.getCursorPosition());
							}
							if (Screen.hasControlDown()) {
								updateCursorPosition(searchField.getNthWordFromCursor(-1));
							} else {
								updateCursorPosition(searchField.getCursorPosition() - 1);
							}
						} else if (Screen.hasControlDown()) {
							updateCursorPosition(searchField.getNthWordFromCursor(-1));
						} else if (!searchField.getSelectedText().isEmpty()) {
							int leftSelection = searchField.getCursorPosition() < selectionEnd
									? searchField.getCursorPosition()
									: selectionEnd;
							updateCursorPosition(leftSelection);
						} else {
							updateCursorPosition(searchField.getCursorPosition() - 1);
							updateCursorPosition(searchField.getCursorPosition());
						}
						break;
					case 264:
						if (Screen.hasShiftDown()) {
							updateSelectionEnd(searchField.getText().length());
						} else {
							updateCursorPosition(searchField.getText().length());
						}
						break;
					case 265:
						if (Screen.hasShiftDown()) {
							updateSelectionEnd(0);
						} else {
							updateCursorPosition(0);
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
			double x = event.getMouseX() - ((ContainerScreen<?>) event.getGui()).getGuiLeft();
			double y = event.getMouseY() - ((ContainerScreen<?>) event.getGui()).getGuiTop();

			boolean alreadyFocused = searchField.isFocused();

			int lastCursorPos = searchField.getCursorPosition();
			boolean overSearchField = searchField.mouseClicked(x, y, event.getButton());
			int cursorPos = searchField.getCursorPosition();

			if (alreadyFocused && overSearchField) {

				updateCursorPosition(searchField.getCursorPosition());

				if (cursorPos == lastCursorPos || clickCount == 3) {
					switch (clickCount) {
					case 2:
						updateCursorPosition(searchField.getNthWordFromCursor(1)
								- ((searchField.getNthWordFromCursor(1) == searchField.getText().length()) ? 0 : 1));
						updateSelectionEnd(searchField.getNthWordFromCursor(-1));
						break;
					case 3:
						updateCursorPosition(searchField.getText().length());
						updateSelectionEnd(0);
						break;
					}
				} else {
					clickCount = 1;
				}
			} else if (overSearchField && SearchableChestsConfig.autoSelect) {
				updateCursorPosition(searchField.getText().length());
				updateSelectionEnd(0);
			} else {
				updateCursorPosition(0);
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
			for (Slot s : event.getGuiContainer().getContainer().inventorySlots) {
				if (!(s.inventory instanceof PlayerInventory)) {
					ItemStack stack = s.getStack();
					if (!stackMatches(searchField.getText(), stack)) {
						int x = s.xPos;
						int y = s.yPos;
						GlStateManager.disableDepthTest();
						AbstractGui.fill(x, y, x + 16, y + 16, 0x80FF0000);
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