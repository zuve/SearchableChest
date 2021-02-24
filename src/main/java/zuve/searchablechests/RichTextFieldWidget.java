package zuve.searchablechests;

import javax.annotation.Nullable;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RichTextFieldWidget extends TextFieldWidget {
	private boolean selectOnFocus = false;

	public RichTextFieldWidget(FontRenderer renderer, int x, int y, int width, int height, ITextComponent title) {
		super(renderer, x, y, width, height, title);
	}

	public RichTextFieldWidget(FontRenderer renderer, int x, int y, int width, int height, @Nullable TextFieldWidget text, ITextComponent title) {
		super(renderer, x, y, width, height, text, title);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
	    boolean wasFocused = this.isFocused();
	    boolean isInside = super.mouseClicked(mouseX, mouseY, button);

		System.out.println("mouseClicked - isInside: " + isInside);
		if (isInside && !wasFocused) {
			this.updateSelection(false, true);
		}

		return isInside;
	}

	/**
	 * Sets focus to this gui element
	 */
	@Override
	public void setFocused2(boolean isFocusedIn) {
	    boolean wasFocused = this.isFocused();
		super.setFocused(isFocusedIn);
		this.updateSelection(wasFocused, isFocusedIn);
	}

	/**
	 * Update selection (select all or nothing) depending on focus state
	 * and selectOnFocus setting
	 */
	private void updateSelection(boolean wasFocused, boolean isFocused) {
		if (!wasFocused && isFocused && selectOnFocus) {
			// select all
			this.setCursorPositionEnd();
			this.setSelectionPos(0);
		} else if (!isFocused) {
			// clear selection
			this.setCursorPositionZero();
			this.setSelectionPos(0);
		}
	}

	/**
	 * Sets whether this text box will select its contents when it is first focused.
	 */
	public void setSelectOnFocus(boolean selectOnFocusIn) {
		this.selectOnFocus = selectOnFocusIn;
	}
}
