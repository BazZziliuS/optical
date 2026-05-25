package net.lpcamors.optical.blocks.hologram_source;

import java.util.List;
import java.util.Optional;

import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;

import net.lpcamors.optical.blocks.COBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class HologramSourceDisplayTarget extends DisplayTarget {

    @Override
    public void acceptText(int line, List<MutableComponent> text, DisplayLinkContext context) {
        HologramSourceBlockEntity controller = this.getController(context);
        if (controller == null || controller.getProfile() == null)
            return;
        boolean changed = false;
        for (int i = 0; i < text.size() && i + line < controller.getProfile().getRowsCount(); i++) {
            if (i == 0)
                reserve(i + line, controller, context);
            if (i > 0 && isReserved(i + line, controller, context))
                break;

            String content = text.get(i).getString(maxCharSize(controller.getProfile().getConnectionLength()));
            controller.getProfile().addSection(i + line, content);
            changed = true;
        }

        if (changed) {
            controller.update();
            context.level()
                    .sendBlockUpdated(controller.getBlockPos(), controller.getBlockState(), controller.getBlockState(),
                            2);
        }
    }

    @Override
    public DisplayTargetStats provideStats(DisplayLinkContext context) {

        HologramSourceBlockEntity controller = getController(context);
        if (controller == null || controller.getProfile() == null)
            return new DisplayTargetStats(1, 1, this);
        return new DisplayTargetStats(controller.getProfile().getRowsCount(),
                maxCharSize(controller.getProfile().getConnectionLength()), this);
    }

    private static int maxCharSize(int xSize) {
        return 8 + xSize * 2;
    }

    private HologramSourceBlockEntity getController(DisplayLinkContext context) {
        BlockEntity teIn = context.getTargetBlockEntity();
        if (!(teIn instanceof HologramSourceBlockEntity be))
            return null;
        return be.getController();
    }

    @Override
    public AABB getMultiblockBounds(LevelAccessor level, BlockPos pos) {
        AABB aabb = new AABB(0, 0, 0, 1, 10 / 16f, 1);
        Optional<HologramSourceBlockEntity> opt = level.getBlockEntity(pos, COBlockEntities.HOLOGRAM_SOURCE.get());
        if (opt.isPresent()) {
            HologramSourceBlockEntity controller = opt.get().getController();
            if (controller != null && controller.getProfile() != null) {
                Direction dir = HologramSourceBlock.getConnectionAxis(controller.getBlockState()).equals(Axis.X)
                        ? Direction.EAST
                        : Direction.SOUTH;
                aabb = aabb.expandTowards(
                        Vec3.atLowerCornerOf(dir.getNormal()).scale(controller.getProfile().getConnectionLength() - 1));
                aabb = aabb.move(controller.getBlockPos());
            }
        }
        return aabb;
    }
}
