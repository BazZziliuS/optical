package net.lpcamors.optical.recipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

import net.lpcamors.optical.CreateOptical;
import net.lpcamors.optical.blocks.optical_source.BeamHelper;
import net.lpcamors.optical.config.COConfigs;
import net.lpcamors.optical.data.IndexedEnum;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

public class FocusingRecipeParams extends ProcessingRecipeParams {

    public static MapCodec<FocusingRecipeParams> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            codec(FocusingRecipeParams::new).forGetter(Function.identity()),
            Codec.INT.optionalFieldOf("mode", 0).forGetter(FocusingRecipeParams::mode))
            .apply(instance, (params, mode) -> {
                params.condition = BeamTypeCondition.values()[mode];
                return params;
            }));
    public static StreamCodec<RegistryFriendlyByteBuf, FocusingRecipeParams> STREAM_CODEC = streamCodec(
            FocusingRecipeParams::new);

    public FocusingRecipeParams output(ProcessingOutput output) {
        return this.output(List.of(output));
    }

    public FocusingRecipeParams output(List<ProcessingOutput> outputs) {
        this.results.addAll(outputs);
        return this;
    }

    public FocusingRecipeParams ingredient(Ingredient ing) {
        return this.ingredient(List.of(ing));
    }

    public FocusingRecipeParams ingredient(List<Ingredient> ingredients) {
        this.ingredients.addAll(ingredients);
        return this;
    }

    public FocusingRecipeParams condition(BeamTypeCondition condition) {
        this.condition = condition;
        return this;
    }

    public BeamTypeCondition condition = BeamTypeCondition.NONE;

    public FocusingRecipeParams(BeamTypeCondition condition) {
        this.condition = condition;
    }

    public FocusingRecipeParams() {
    }

    int mode() {
        return this.condition.id;
    }

    public NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    public NonNullList<ProcessingOutput> getOutputs() {
        return this.results;
    }
    /*
     * private static List<ProcessingOutput> append(ProcessingOutput
     * processingOutput, List<ProcessingOutput> p){
     * List<ProcessingOutput> p0 = new ArrayList<>();
     * p0.add(processingOutput);
     * p0.addAll(p);
     * return p0;
     * }
     */

    public static Boolean smokingPredicate(Level level, ItemStack itemStack) {
        Optional<RecipeHolder<SmokingRecipe>> recipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.SMOKING, new SingleRecipeInput(itemStack), level);
        if (recipe.isPresent())
            return true;
        return false;
    }

    public static ItemStack smokingMap(Level level, ItemStack itemStack) {
        Optional<RecipeHolder<SmokingRecipe>> recipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.SMOKING, new SingleRecipeInput(itemStack), level);
        return recipe.map(holder -> holder.value().getResultItem(level.registryAccess())).orElse(ItemStack.EMPTY);

    }

    public enum BeamTypeCondition {
        RADIO(0, 6192150),
        MICROWAVE(1, 8991416),
        VISIBLE(2, 0xE88300),
        GAMMA(3, 0x5C93E8),
        NONE(4, 0xffffff);

        public static final IndexedEnum<BeamTypeCondition> INDEXED = new IndexedEnum<>(true,
                BeamTypeCondition.values());

        private final int id;
        private final int color;

        BeamTypeCondition(int id, int color) {
            this.id = id;
            this.color = color;
        }

        public int getId() {
            return this.ordinal();
        }

        public boolean test(BeamHelper.BeamType beamType) {
            return this.id == 4 || (beamType.ordinal() == this.id);
        }

        public static BeamTypeCondition getFromType(BeamHelper.BeamType b) {
            return Arrays.stream(BeamTypeCondition.values())
                    .filter(beamTypeCondition1 -> beamTypeCondition1.test(b) && beamTypeCondition1.id != 4).toList()
                    .get(0);
        }

        public int getColor() {
            return this.color;
        }

        public String getTranslationKey() {
            return "required_beam_type." + this.name().toLowerCase();
        }
    }

    private static Function<RecipeHolder<?>, RecipeHolder<FocusingRecipe>> smoking(RegistryAccess level) {
        return holder -> {

            return new RecipeHolder<FocusingRecipe>(holder.id(), FocusingRecipe.focusing(
                    getFromConfig(holder.value().getIngredients(),
                            holder.value().getResultItem(level),
                            Items.CHARCOAL.getDefaultInstance(),
                            COConfigs.server().recipes.focusingSmokingFailedOutputProbability.getF()),
                    BeamTypeCondition.MICROWAVE));
        };
    }

    private static Function<RecipeHolder<?>, RecipeHolder<FocusingRecipe>> sandpaper(RegistryAccess level) {
        return holder -> {
            return new RecipeHolder<FocusingRecipe>(holder.id(), FocusingRecipe.focusing(
                    getFromConfig(List.of(holder.value().getIngredients().get(0), Ingredient.of(Items.SAND)),
                            holder.value().getResultItem(level),
                            Items.CHARCOAL.getDefaultInstance(),
                            COConfigs.server().recipes.focusingSmokingFailedOutputProbability.getF()),
                    BeamTypeCondition.MICROWAVE));
        };
    }

    private static Function<RecipeHolder<?>, RecipeHolder<FocusingRecipe>> coloring(RegistryAccess level) {
        return holder -> {
            Ingredient dye;
            Ingredient complement;
            if (holder.value().getIngredients().get(0).getItems()[0].getItem() instanceof DyeItem) {
                dye = holder.value().getIngredients().get(0);
                complement = holder.value().getIngredients().get(1);
            } else {
                dye = holder.value().getIngredients().get(1);
                complement = holder.value().getIngredients().get(0);
            }

            return new RecipeHolder<FocusingRecipe>(holder.id(),
                    FocusingRecipe.focusing(
                            getFromConfig(List.of(complement, dye), holder.value().getResultItem(level),
                                    Items.CHARCOAL.getDefaultInstance(),
                                    COConfigs.server().recipes.focusingColoringFailedOutputProbability.getF()),
                            BeamTypeCondition.VISIBLE));
        };
    }

    public static Collection<RecipeHolder<FocusingRecipe>> copy(Ingredient ing, Predicate<Item> predicate) {
        ArrayList<RecipeHolder<FocusingRecipe>> list = new ArrayList<>();
        BuiltInRegistries.ITEM.forEach(item -> {
            if (predicate.test(item)) {
                FocusingRecipeParams params = new FocusingRecipeParams()
                        .ingredient(List.of(ing, Ingredient.of(item)))
                        .output(new ProcessingOutput(new ItemStack(item), 1));
                FocusingRecipe recipe = new FocusingRecipe(params);
                list.add(new RecipeHolder<>(CreateOptical.loc("focusing_copying_" + item.getDescriptionId()), recipe));
            }
        });
        return list;
    }

    private static FocusingRecipeParams getFromConfig(List<Ingredient> ingredients, ItemStack output,
            ItemStack failOutput, float failChance) {
        List<ProcessingOutput> outputs = new ArrayList<>();
        outputs.add(new ProcessingOutput(output, 1.0F - failChance));
        if (failChance > 0.0)
            outputs.add(new ProcessingOutput(failOutput, failChance));
        return new FocusingRecipeParams().ingredient(ingredients).output(outputs);
    }

    public enum BeamTypeConditionProfile {
        SMOKING(RecipeType.SMOKING, l -> smoking(l)),
        ADD_COLOR(BeamTypeConditionProfile::colorItems, l -> coloring(l)),
        SANDPAPER((RecipeType<?>) AllRecipeTypes.SANDPAPER_POLISHING.getType(), l -> sandpaper(l)),
        SHERDS(copy(Ingredient.of(Items.BRICK), item -> new ItemStack(item).is(ItemTags.DECORATED_POT_SHERDS))),
        PATTERN(copy(Ingredient.of(Items.PAPER), item -> item instanceof BannerPatternItem)),
        DISC(copy(Ingredient.of(Tags.Items.MUSIC_DISCS), item -> new ItemStack(item).is(Tags.Items.MUSIC_DISCS))),
        ;

        private final Predicate<RecipeHolder<?>> recipePredicate;
        private final Function<RegistryAccess, Function<RecipeHolder<?>, RecipeHolder<FocusingRecipe>>> converter;
        private final Function<MinecraftServer, Collection<RecipeHolder<FocusingRecipe>>> recipesGenerator;

        private static final Map<BeamTypeConditionProfile, Collection<RecipeHolder<FocusingRecipe>>> CACHE = new EnumMap<>(
                BeamTypeConditionProfile.class);

        public static void rebuild(MinecraftServer server) {
            CreateOptical.LOGGER.info("Reloading focusing recipes");
            CACHE.clear();

            for (BeamTypeConditionProfile profile : BeamTypeConditionProfile.values()) {
                CACHE.put(profile, profile.recipesGenerator.apply(server));
            }
        }

        public Collection<? extends RecipeHolder<FocusingRecipe>> getRecipes() {
            return CACHE.getOrDefault(this, List.of());
        }

        BeamTypeConditionProfile(RecipeType<?> recipeType,
                Function<RegistryAccess, Function<RecipeHolder<?>, RecipeHolder<FocusingRecipe>>> converter) {
            this(r -> r.value().getType().equals(recipeType), converter);
        }

        BeamTypeConditionProfile(Collection<RecipeHolder<FocusingRecipe>> recipes) {
            this.recipePredicate = null;
            this.converter = null;
            this.recipesGenerator = (a) -> recipes;
        }

        BeamTypeConditionProfile(Predicate<RecipeHolder<?>> recipePredicate,
                Function<RegistryAccess, Function<RecipeHolder<?>, RecipeHolder<FocusingRecipe>>> converter) {
            this.recipePredicate = recipePredicate;
            this.converter = converter;
            this.recipesGenerator = server -> {
                ArrayList<RecipeHolder<FocusingRecipe>> recipes = new ArrayList<>();
                server.getRecipeManager().getRecipes().forEach(recipe -> {
                    if (this.recipePredicate.test(recipe)) {
                        recipes.add(this.getConverter(server.registryAccess()).apply(recipe));
                    }
                });
                return recipes;
            };
        }

        public Function<RecipeHolder<?>, RecipeHolder<FocusingRecipe>> getConverter(RegistryAccess registryAccess) {
            if (this.converter == null)
                CreateOptical.LOGGER.error("Trying to access null focusing profile converter");
            return converter.apply(registryAccess);
        }

        public Predicate<RecipeHolder<?>> getRecipePredicate() {
            if (this.recipePredicate == null)
                CreateOptical.LOGGER.error("Trying to access null focusing profile predicate");
            return recipePredicate;
        }

        private static boolean colorItems(RecipeHolder<?> r) {
            NonNullList<Ingredient> ing = r.value().getIngredients();
            boolean f = ing.size() == 2 && ing.get(0).getItems().length > 0 && ing.get(1).getItems().length > 0;

            return f && (ing.get(0).getItems()[0].getItem() instanceof DyeItem
                    ^ r.value().getIngredients().get(1).getItems()[0].getItem() instanceof DyeItem);
        }

        public static boolean canBeProcessed(Level level, RecipeWrapper recipeWrapper, BeamHelper.BeamType b) {
            return getRecipeFor(level, recipeWrapper, b).isPresent();
        }

        public static Optional<RecipeHolder<FocusingRecipe>> getRecipeFor(Level level, RecipeWrapper recipeWrapper,
                BeamHelper.BeamType beamType) {
            List<RecipeHolder<FocusingRecipe>> list = new ArrayList<>();
            for (BeamTypeConditionProfile profile : BeamTypeConditionProfile.values()) {
                list.addAll(profile.getRecipes().stream()
                        .filter(r -> r.value().matches(recipeWrapper, level)
                                && r.value().beamTypeCondition.test(beamType))
                        .toList());
            }

            return Optional.ofNullable(list.isEmpty() ? null : list.get(0));
        }
    }

}
