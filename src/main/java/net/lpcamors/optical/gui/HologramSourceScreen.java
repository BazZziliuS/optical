package net.lpcamors.optical.gui;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;

import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.platform.CatnipServices;
import net.lpcamors.optical.blocks.hologram_source.HologramSourceBlockEntity;
import net.lpcamors.optical.blocks.hologram_source.HologramSourceBlockEntity.HologramSourceProfile;
import net.lpcamors.optical.data.COLang;
import net.lpcamors.optical.network.ConfigureHologramSourcePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public class HologramSourceScreen extends AbstractSimiScreen {

    private final COGuiTextures background = COGuiTextures.HOLOGRAM;
    private final Component modeTitle = COLang.Prefixes.OPTICAL.translate("gui.hologram_source.mode");

    private ScrollInput modeArea;
    private ScrollInput angleArea;
    private ScrollInput angleVelocityArea;
    private boolean shouldRenderAngle = false;
    private IconButton confirmButton;
    private Label modeLabel;
    private final CompoundTag tag;
    private int modeIndex;
    private int angle;
    private int angleVelocity;
    final HologramSourceBlockEntity be;

    public HologramSourceScreen(HologramSourceBlockEntity be) {
        super(COLang.Prefixes.OPTICAL.translate(("gui.hologram_source.title")));
        this.be = be;
        HologramSourceProfile profile = be.getController().getProfile();
        this.modeIndex = profile.displayMode.ordinal();
        this.angle = profile.fixedAngle;
        this.angleVelocity = profile.angleVelocity;
        this.tag = new CompoundTag();
        profile.write(this.tag, be.getLevel().registryAccess());
    }

    @Override
    protected void init() {
        setWindowSize(background.width, background.height);
        setWindowOffset(0, 0);
        super.init();

        int x = guiLeft;
        int y = guiTop;

        confirmButton = new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(this::onClose);
        addRenderableWidget(confirmButton);

        modeLabel = new Label(x + 29, y + 28, Component.empty()).withShadow();
        modeLabel.text = Component.empty();

        modeArea = new SelectionScrollInput(x + 22, y + 23, 109, 18)
                .forOptions(HologramSourceBlockEntity.Mode.getComponents())
                .titled(modeTitle.plainCopy())
                .calling(integer -> modeIndex = integer)
                .setState(this.modeIndex)
                .writingTo(modeLabel);

        shouldRenderAngle = HologramSourceBlockEntity.Mode.values()[this.modeIndex].isShouldRenderAngle();

        angleArea = new ScrollInput(x + 133, y + 23, 28, 18).withRange(0, 360)
                .calling(integer -> this.angle = integer)
                .setState(this.angle);

        angleVelocityArea = new ScrollInput(x + 133, y + 23, 28, 18).withRange(1, 361)
                .calling(integer -> this.angleVelocity = integer)
                .setState(this.angleVelocity);

        addRenderableWidget(modeArea);
        addRenderableWidget(angleArea);
        addRenderableWidget(angleVelocityArea);
    }

    public void sendPacket() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("ModeIndex", this.modeIndex);
        tag.putInt("Angle", this.angle);
        tag.putInt("AngleVelocity", this.angleVelocity);
        if (tag.equals(this.tag))
            return;
        CatnipServices.NETWORK
                .sendToServer(new ConfigureHologramSourcePacket(be.getBlockPos(), this.modeIndex, this.angle,
                        this.angleVelocity));
    }

    @Override
    public void removed() {
        sendPacket();
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;

        background.render(graphics, x, y);

        graphics.drawString(font, title, x + (background.width - 8) / 2 - font.width(title) / 2, y + 4, 0x592424,
                false);

        label(graphics, 30, 28, COLang.Prefixes.OPTICAL
                .translate(HologramSourceBlockEntity.Mode.values()[this.modeArea.getState()].getTranslationKey()));
        shouldRenderAngle = HologramSourceBlockEntity.Mode.values()[this.modeArea.getState()].isShouldRenderAngle();

        this.angleArea.visible = this.shouldRenderAngle;
        COGuiTextures toDraw = COGuiTextures.HOLOGRAM_PLUS_SLOT;
        toDraw.render(graphics, x + 130, y + 23);
        if (shouldRenderAngle) {
            Component c = Component.literal(this.angleArea.getState() + "°");

            label(graphics, 148 - font.width(c) / 2, 28, c);

        } else {
            Component c = Component.literal(this.angleVelocityArea.getState() + "°/s");

            label(graphics, 148 - font.width(c) / 2, 28, c);

        }
    }

    private void label(GuiGraphics graphics, int x, int y, Component text) {
        graphics.drawString(font, text, guiLeft + x, guiTop + y, 0xFFFFEE);
    }
}
