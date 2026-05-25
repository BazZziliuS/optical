package net.lpcamors.optical.data;

import net.lpcamors.optical.CreateOptical;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class COBlockTagsProvider extends BlockTagsProvider {

    public COBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
            @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, CreateOptical.ID, existingFileHelper);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void addTags(HolderLookup.Provider p_256380_) {
        tag(COTags.Blocks.PENETRABLE).add(Blocks.AIR, Blocks.LIGHT, Blocks.TRIPWIRE, Blocks.REDSTONE_WIRE, Blocks.WATER,
                Blocks.REPEATER, Blocks.COMPARATOR, Blocks.DAYLIGHT_DETECTOR, Blocks.LEVER, Blocks.FLOWER_POT,
                Blocks.COBWEB);
        tag(COTags.Blocks.PENETRABLE).addTags(
                BlockTags.ALL_HANGING_SIGNS,
                BlockTags.ALL_SIGNS,
                BlockTags.CORAL_PLANTS,
                BlockTags.CAMPFIRES,
                BlockTags.WALL_POST_OVERRIDE,
                BlockTags.BANNERS,
                BlockTags.SLABS,
                BlockTags.WOOL_CARPETS,
                BlockTags.BUTTONS, BlockTags.PRESSURE_PLATES,
                BlockTags.WOODEN_TRAPDOORS,
                BlockTags.CANDLES,
                BlockTags.CLIMBABLE,
                BlockTags.FIRE,
                BlockTags.LEAVES,
                BlockTags.FLOWERS,
                BlockTags.CORAL_PLANTS,
                BlockTags.SAPLINGS,
                BlockTags.CROPS,
                BlockTags.RAILS,
                BlockTags.REPLACEABLE_BY_TREES,
                Tags.Blocks.GLASS_BLOCKS,
                Tags.Blocks.GLASS_PANES);
        tag(COTags.Blocks.IMPENETRABLE).addTags(
                Tags.Blocks.GLASS_BLOCKS_TINTED);
    }
}
