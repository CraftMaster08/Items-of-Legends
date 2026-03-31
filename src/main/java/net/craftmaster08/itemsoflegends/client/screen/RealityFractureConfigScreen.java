package net.craftmaster08.itemsoflegends.client.screen;

import net.craftmaster08.itemsoflegends.network.ModNetwork;
import net.craftmaster08.itemsoflegends.network.packet.C2SSaveRealityFractureConfigPacket;
import net.craftmaster08.itemsoflegends.network.packet.S2COpenRealityFractureConfigPacket;
import net.craftmaster08.itemsoflegends.reality.RealityFractureMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class RealityFractureConfigScreen extends Screen {
    private final String startDimension;
    private final List<String> dimensionOptions;
    private String selectedDestination;
    private RealityFractureMode selectedMode;

    public RealityFractureConfigScreen(S2COpenRealityFractureConfigPacket packet) {
        super(Component.translatable("gui.itemsoflegends.reality_fracture.title"));
        this.startDimension = packet.startDimension();
        this.dimensionOptions = new ArrayList<>(packet.dimensions().stream()
                .filter(dimension -> !dimension.equals(startDimension))
                .toList());

        this.selectedDestination = packet.destinationDimension();
        if (!dimensionOptions.contains(selectedDestination)) {
            selectedDestination = dimensionOptions.isEmpty() ? startDimension : dimensionOptions.get(0);
        }
        this.selectedMode = RealityFractureMode.fromName(packet.mode());
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int top = this.height / 2 - 60;

        this.addRenderableWidget(CycleButton.<String>builder(this::dimensionLabel)
                .withValues(dimensionOptions.isEmpty() ? List.of(startDimension) : dimensionOptions)
                .withInitialValue(selectedDestination)
                .create(centerX - 110, top + 52, 220, 20,
                        Component.translatable("gui.itemsoflegends.reality_fracture.destination_dimension"),
                        (button, value) -> selectedDestination = value));

        this.addRenderableWidget(CycleButton.<RealityFractureMode>builder(mode -> Component.translatable(mode.getTranslationKey()))
                .withValues(RealityFractureMode.values())
                .withInitialValue(selectedMode)
                .create(centerX - 110, top + 82, 220, 20,
                        Component.translatable("gui.itemsoflegends.reality_fracture.mode"),
                        (button, value) -> selectedMode = value));

        this.addRenderableWidget(Button.builder(Component.translatable("gui.itemsoflegends.reality_fracture.save"), button -> {
                    ModNetwork.sendToServer(new C2SSaveRealityFractureConfigPacket(selectedDestination, selectedMode.name()));
                    this.onClose();
                })
                .pos(centerX - 110, top + 116)
                .size(105, 20)
                .build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.itemsoflegends.reality_fracture.cancel"), button -> this.onClose())
                .pos(centerX + 5, top + 116)
                .size(105, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int centerX = this.width / 2;
        int top = this.height / 2 - 60;

        graphics.drawCenteredString(this.font, this.title, centerX, top, 0xFFFFFF);
        graphics.drawString(this.font,
                Component.translatable("gui.itemsoflegends.reality_fracture.start_dimension"),
                centerX - 110,
                top + 24,
                0xA0A0A0);
        graphics.drawString(this.font, Component.literal(startDimension), centerX - 110, top + 36, 0xFFFFFF);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private Component dimensionLabel(String dimensionId) {
        return Component.literal(dimensionId);
    }
}
