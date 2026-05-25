package net.lpcamors.optical;

import com.simibubi.create.AllShapes;

import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

public class COShapes {

    public static final VoxelShape OPTICAL_RECEPTOR = shape(3, 0, 0, 13, 2, 16)
            .add(3.5, 3.5, 2, 12.5, 12.5, 14)
            .build(),
            OPTICAL_RECEPTOR_VERTICAL = shape(4, 1, 4, 12, 2, 12)
                    .add(4, 14, 4, 12, 15, 12)
                    .add(3.5, 2, 3.5, 12.5, 14, 12.5)
                    .build();
    public static final VoxelShaper OPTICAL_SOURCE = shape(0, 0, 0, 16, 11, 16)
            .forHorizontal(Direction.NORTH),

            THERMAL_OPTICAL_SOURCE = shape(0, 0, 0, 16, 13, 16)
                    .forHorizontal(Direction.NORTH),

            ENCASED_MIRROR = shape(0, 0, 0, 16, 2, 16)
                    .add(0, 14, 0, 16, 16, 16)
                    .add(1, 2, 1, 15, 14, 15)
                    .forDirectional(Direction.UP),
            ENCASED_MIRROR_SHAFT = shape(5, 0, 5, 11, 16, 11)
                    .forDirectional(Direction.UP),
            POLARIZING_BEAM_SPLITTER_CUBE = shape(2, 0, 2, 14, 16, 14)
                    .forHorizontal(Direction.NORTH),
            ABSORPTION_POLARIZING_FILTER = shape(6, 0, 1, 10, 2, 15)
                    .add(6, 14, 1, 10, 16, 15)
                    .add(7, 2, 2, 9, 14, 14)
                    .forHorizontal(Direction.NORTH),
            BEAM_CONDENSER = shape(2, 0, 2, 14, 2, 14)
                    .add(2, 14, 2, 14, 16, 14)
                    .add(3, 2, 3, 13, 14, 13)
                    .forHorizontal(Direction.NORTH),
            FOCUSER = shape(3, 3, 1, 13, 13, 15).add(2, 2, 2, 14, 4, 14).forHorizontal(Direction.NORTH),
            HOLOGRAM_SOURCE = shape(0, 0, 2, 16, 3, 14)
                    .add(1.5, 3.5, 3.5, 14.5, 7.5, 12.5).forHorizontalAxis(),
            BEAM_READER = shape(3, 0, 1, 13, 5.5, 15)
                    .add(5, 5, 5, 11, 11, 11)
                    .add(5.5, 5.5, 0, 10.5, 10.5, 16)
                    .forHorizontal(Direction.NORTH),
            BEAM_MODULATOR = shape(3, 0, 2, 13, 2, 14)
                    .add(5, 2, 4, 11, 4, 8)
                    .forHorizontal(Direction.NORTH);

    ;

    public static VoxelShape SENSOR_UP = shape(5, 0, 5, 11, 11, 11).build(),
            SENSOR_DOWN = shape(5, 5, 5, 11, 16, 11).build();

    private static AllShapes.Builder shape(VoxelShape shape) {
        return new AllShapes.Builder(shape);
    }

    private static AllShapes.Builder shape(double x1, double y1, double z1, double x2, double y2, double z2) {
        return shape(cuboid(x1, y1, z1, x2, y2, z2));
    }

    private static VoxelShape cuboid(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Block.box(x1, y1, z1, x2, y2, z2);
    }

}
