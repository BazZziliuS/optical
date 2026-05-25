package net.lpcamors.optical;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.createmod.catnip.theme.Color;
import net.lpcamors.optical.items.COItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Vec3i;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class COUtils {

    public static Map<ItemEntry<?>, ItemEntry<?>> EQ_SHEETS = Map.of(
            COItems.COPPER_COIL, AllItems.COPPER_SHEET,
            COItems.GOLDEN_COIL, AllItems.GOLDEN_SHEET,
            COItems.ZINC_COIL, AllItems.ZINC_INGOT,
            COItems.ROSE_QUARTZ_CATALYST_COIL, AllItems.POLISHED_ROSE_QUARTZ);

    public static Map<ItemEntry<?>, ItemEntry<?>> EQ_INCOMPLETE = Map.of(
            COItems.COPPER_COIL, COItems.INCOMPLETE_COPPER_COIL,
            COItems.GOLDEN_COIL, COItems.INCOMPLETE_GOLDEN_COIL,
            COItems.ZINC_COIL, COItems.INCOMPLETE_ZINC_COIL,
            COItems.ROSE_QUARTZ_CATALYST_COIL, COItems.INCOMPLETE_QUARTZ_CATALYST_COIL);

    public static Vec3i getColor(DyeColor dyeColor) {
        int rgb = dyeColor.getTextureDiffuseColor();
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return new Vec3i(r, g, b);
    }

    public static Color color(Vec3i v) {
        return new Color(v.getX(), v.getY(), v.getZ(), 255);
    }

    public static int colorInt(Vec3i v) {
        return FastColor.ARGB32.color(255, v.getX(), v.getY(), v.getZ());
    }

    public static Vec3i colorVec(int i) {
        return new Vec3i(FastColor.ARGB32.red(i), FastColor.ARGB32.green(i), FastColor.ARGB32.blue(i));
    }

    public static Vec3i meanPoint(Vec3i v1, Vec3i v2) {
        return new Vec3i(
                (int) ((v1.getX() + v2.getX()) * 0.5f),
                (int) ((v1.getY() + v2.getY()) * 0.5f),
                (int) ((v1.getZ() + v2.getZ()) * 0.5f));
    }

    public static Vec3i multiplyVec3i(Vec3i v1, Vec3i v2) {
        return new Vec3i(v1.getX() * v2.getX(), v1.getY() * v2.getY(), v1.getZ() * v2.getZ());
    }

    public static AABB radius(Vec3 vec3, double radius) {
        return new AABB(vec3.x + radius, vec3.y + radius, vec3.z + radius, vec3.x - radius, vec3.y - radius,
                vec3.z - radius);
    }

    public static Vec3i getVec3iFromArray(List<Integer> ints) {
        if (ints.size() < 3)
            return Vec3i.ZERO;
        return new Vec3i(ints.get(0), ints.get(1), ints.get(2));
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> T getBlockEntity(BlockGetter worldIn, BlockPos pos, Class<T> c) {
        BlockEntity blockEntity = worldIn.getBlockEntity(pos);

        if (blockEntity == null)
            return null;
        if (!c.isInstance(blockEntity))
            return null;

        return (T) blockEntity;
    }

    public static Vec3 getAbsVec(Vec3 vec3) {
        return new Vec3(Math.abs(vec3.x), Math.abs(vec3.y), Math.abs(vec3.z));
    }

    public static Vec3 vecOf(double d) {
        return new Vec3(d, d, d);
    }

    public static double getPseudoLengthVec(Vec3 vec3) {
        return vec3.x + vec3.y + vec3.z;
    }

    public static void translatePose(PoseStack ms, Vec3 vec3) {
        ms.translate(vec3.x, vec3.y, vec3.z);
    }

    public static void scalePose(PoseStack ms, double d) {
        ms.translate(d, d, d);
    }

    public static Direction getDirectionByNormal(Vec3i n) {
        Axis axis = n.getX() != 0 ? Axis.X : n.getY() != 0 ? Axis.Y : Axis.Z;
        AxisDirection axisDirection = n.getX() + n.getY() + n.getZ() > 0 ? AxisDirection.POSITIVE
                : AxisDirection.NEGATIVE;
        return Direction.fromAxisAndDirection(axis, axisDirection);
    }

    public static Vec3 getNormalUnitary(Direction dir) {
        return Vec3.atLowerCornerOf(dir.getNormal().multiply(dir.getAxisDirection().getStep()));
    }

    public static Vec3 getPlaneNormal(Direction dir) {
        return COUtils.vecOf(1).subtract(getNormalUnitary(dir));
    }
}
