package net.lpcamors.optical.visual;

import java.util.function.Consumer;

import com.simibubi.create.content.kinetics.base.ShaftVisual;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.FlatLit;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.OrientedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.lpcamors.optical.COPartialModels;
import net.lpcamors.optical.blocks.beam_focuser.BeamFocuserBlockEntity;

public class FocuserVisual extends ShaftVisual<BeamFocuserBlockEntity> implements SimpleDynamicVisual {

    private static final int CYCLE_TICK = 5;
    private static final float HEIGHT = 21 / 16F;
    private final OrientedInstance ray;
    private final OrientedInstance ray2;

    public FocuserVisual(VisualizationContext context, BeamFocuserBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);
        this.ray = (OrientedInstance) this.instancerProvider()
                .instancer(InstanceTypes.ORIENTED, Models.partial(COPartialModels.FOCUS_BEAM)).createInstance();
        this.ray2 = (OrientedInstance) this.instancerProvider()
                .instancer(InstanceTypes.ORIENTED, Models.partial(COPartialModels.FOCUS_BEAM)).createInstance();

        this.transformModels(partialTick);
    }

    @Override
    public void beginFrame(dev.engine_room.flywheel.api.visual.DynamicVisual.Context arg0) {
        this.ray.position(this.getVisualPosition()).setChanged();
        this.ray2.position(this.getVisualPosition()).setChanged();
        if (this.blockEntity.processingTicks >= 0) {
            this.ray.setVisible(true);
            this.ray2.setVisible(true);
            this.transformModels(arg0.partialTick());
        } else {
            this.ray.setVisible(false);
            this.ray2.setVisible(false);
        }
    }

    private void transformModels(float pt) {
        this.ray.position(this.getVisualPosition()).translatePosition(0.0F, (2 / 16f) - this.getYOffset(pt, 0), 0.0F)
                .setChanged();
        this.ray2.position(this.getVisualPosition()).translatePosition(0.0F, (2 / 16f) - this.getYOffset(pt, 1), 0.0F)
                .setChanged();
    }

    private float getYOffset(float pt, int index) {
        int ticks = ((index * CYCLE_TICK / 2) + this.blockEntity.getProcessDuration() + 5
                - this.blockEntity.processingTicks) % CYCLE_TICK;
        return (ticks + pt) * (HEIGHT / (float) CYCLE_TICK);
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);
        this.relight(new FlatLit[] { this.ray, this.ray2 });
    }

    @Override
    protected void _delete() {
        super._delete();
        this.ray.delete();
        this.ray2.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<dev.engine_room.flywheel.api.instance.Instance> consumer) {
        super.collectCrumblingInstances(consumer);
        consumer.accept(this.ray);
        consumer.accept(this.ray2);
    }
}
