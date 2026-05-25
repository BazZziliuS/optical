package net.lpcamors.optical;

import java.util.List;
import java.util.function.Predicate;

import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;

import org.jetbrains.annotations.ApiStatus.Internal;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.lpcamors.optical.data.COLang;
import net.lpcamors.optical.items.COItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class COCreativeModeTabs {

    @Internal
    public static void initiate(IEventBus bus) {
        CREATIVE_MODE_TABS.register(bus);
    }

    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, CreateOptical.ID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CO_BASE_CREATIVE_TAB = CREATIVE_MODE_TABS
            .register("co_base",
                    () -> CreativeModeTab.builder()
                            .title(COLang.Prefixes.CREATIVE_TAB.translate("co_base"))
                            .withTabsBefore(AllCreativeModeTabs.PALETTES_CREATIVE_TAB.getKey())
                            .icon(() -> COItems.OPTICAL_DEVICE.asStack())
                            .displayItems(DisplayItems::displayItemsGenerator)
                            .build());

    private static class DisplayItems {

        private static final List<Item> FILTER = List.of(
                COItems.INCOMPLETE_MIRROR.get(),
                COItems.INCOMPLETE_ZINC_COIL.get(),
                COItems.INCOMPLETE_COPPER_COIL.get(),
                COItems.INCOMPLETE_GOLDEN_COIL.get(),
                COItems.INCOMPLETE_OPTICAL_DEVICE.get(),
                COItems.INCOMPLETE_POLARIZING_FILTER.get(),
                COItems.INCOMPLETE_QUARTZ_CATALYST_COIL.get());

        public static void displayItemsGenerator(CreativeModeTab.ItemDisplayParameters itemDisplayParameters,
                CreativeModeTab.Output output) {
            collectBlocks(FILTER::contains).forEach(output::accept);
            collectItems(FILTER::contains).forEach(output::accept);
        }

        private static List<Item> collectBlocks(Predicate<Item> exclusionPredicate) {
            List<Item> items = new ReferenceArrayList<>();
            for (RegistryEntry<Block, Block> entry : CreateOptical.REGISTRATE.getAll(Registries.BLOCK)) {
                if (!CreateRegistrate.isInCreativeTab(entry, COCreativeModeTabs.CO_BASE_CREATIVE_TAB))
                    continue;
                Item item = entry.get()
                        .asItem();
                if (item == Items.AIR)
                    continue;
                if (!exclusionPredicate.test(item))
                    items.add(item);
            }
            items = new ReferenceArrayList<>(new ReferenceLinkedOpenHashSet<>(items));
            return items;
        }

        private static List<Item> collectItems(Predicate<Item> exclusionPredicate) {
            List<Item> items = new ReferenceArrayList<>();
            for (RegistryEntry<Item, Item> entry : CreateOptical.REGISTRATE.getAll(Registries.ITEM)) {
                if (!CreateRegistrate.isInCreativeTab(entry, COCreativeModeTabs.CO_BASE_CREATIVE_TAB))
                    continue;
                Item item = entry.get();
                if (item instanceof BlockItem)
                    continue;
                if (!exclusionPredicate.test(item))
                    items.add(item);
            }
            return items;
        }

    }

}
