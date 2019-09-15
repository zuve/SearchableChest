package zuve.searchablechests;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.KeyboardInputEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber(modid = "searchablechests")
public class ChestEventHandler {

	private boolean skip;

	private Minecraft mc;
	private GuiTextField searchField;
	private ResourceLocation searchBar = new ResourceLocation("searchablechests", "textures/gui/search_bar.png");

	public ChestEventHandler() {
		mc = Minecraft.getMinecraft();
	}

	@SubscribeEvent
	public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
		GuiScreen gui = event.getGui();
		if (gui instanceof GuiContainer && !(gui instanceof InventoryEffectRenderer)
				&& ((GuiContainer) gui).inventorySlots.getInventory().size() >= 36 + SearchableChestsConfig.minimumContainerSize) {
			Keyboard.enableRepeatEvents(true);
			FontRenderer fontRenderer = mc.fontRenderer;
			searchField = new GuiTextField(0, fontRenderer, 81, 6, 80, fontRenderer.FONT_HEIGHT);
			searchField.setText("");
			searchField.setMaxStringLength(50);
			searchField.setEnableBackgroundDrawing(false);
			searchField.setTextColor(16777215);
			searchField.setCanLoseFocus(true);
			searchField.setVisible(true);
			System.out.println(SearchableChestsConfig.autoFocus);
			searchField.setFocused(SearchableChestsConfig.autoFocus);
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
				if (searchField.isFocused() && searchField.charTyped(event.get, ((Object) event).getCodePoint())) {
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public void onKeyPressed(GuiScreenEvent.KeyboardInputEvent.Pre event) {
		if (searchField != null) {
			int keyCode = event.getGui();
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
						} else {
							searchField.moveCursorBy(-1);
							searchField.setSelectionPos(searchField.getCursorPosition());
						}
						break;
					case 264:
						searchField.setCursorPositionEnd();
						break;
					case 265:
						searchField.setCursorPositionZero();
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
			if (!searchField.getText().isEmpty()) {
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
			}
			GlStateManager.enableLighting();
		}
	}

	private boolean stackMatches(String text, ItemStack stack) {
		if (stack.getItem().equals(Items.AIR)) {
			return true;
		}
		ArrayList<String> keys = new ArrayList<String>();
		keys.add(stack.getDisplayName().getString());
		keys.add(stack.getItem().getItem().getName().getString());
		for (Map.Entry<Enchantment, Integer> e : EnchantmentHelper.getEnchantments(stack).entrySet()) {
			keys.add(e.getKey().getDisplayName(e.getValue()).getString());
		}
		for (String key : keys) {
			if (key.toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))) {
				return true;
			}
		}
		return false;
	}

}