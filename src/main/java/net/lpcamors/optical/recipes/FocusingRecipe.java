package net.lpcamors.optical.recipes;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe;

import net.lpcamors.optical.CORecipeTypes;
import net.lpcamors.optical.blocks.COBlocks;
import net.lpcamors.optical.blocks.beam_focuser.BeamFocuserBlockEntity;
import net.lpcamors.optical.compat.jei.FocusingAssemblySubcategory;
import net.lpcamors.optical.data.COLang;
import net.lpcamors.optical.recipes.FocusingRecipeParams.BeamTypeCondition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

public class FocusingRecipe extends ProcessingRecipe<RecipeWrapper, FocusingRecipeParams> implements IAssemblyRecipe {

    private static final String REQUIRED_BEAM_TYPE_KEY = "required_beam_type";

    public FocusingRecipeParams.BeamTypeCondition beamTypeCondition = FocusingRecipeParams.BeamTypeCondition.NONE;

    public static FocusingRecipe focusing(FocusingRecipeParams params, FocusingRecipeParams.BeamTypeCondition type) {
        FocusingRecipe f = new FocusingRecipe(params);
        f.beamTypeCondition = type;
        return f;
    }

    public FocusingRecipe(FocusingRecipeParams params) {
        super(
                CORecipeTypes.FOCUSING,
                params);
    }

    public FocusingRecipe(ProcessingRecipeParams params) {
        super(
                CORecipeTypes.FOCUSING,
                (FocusingRecipeParams) params);
    }

    @Override
    protected int getMaxInputCount() {
        return 2;
    }

    @Override
    protected int getMaxOutputCount() {
        return 2;
    }

    @Override
    public Component getDescriptionForAssembly() {
        return COLang.Prefixes.JEI.translate("focusing.sequence." + this.beamTypeCondition.getId());
    }

    @Override
    public void addRequiredMachines(Set<ItemLike> list) {
        list.add(COBlocks.BEAM_FOCUSER.get());
    }

    @Override
    public void addAssemblyIngredients(List<Ingredient> list) {
        if (this.ingredients.size() > 1)
            list.add(this.getIngredients().get(1));
    }

    @Override
    public Supplier<Supplier<SequencedAssemblySubCategory>> getJEISubCategory() {
        return () -> FocusingAssemblySubcategory::new;
    }

    @Override
    public boolean matches(RecipeWrapper p_44002_, Level p_44003_) {
        if (p_44002_.isEmpty())
            return false;
        boolean f = this.getIngredient().test(p_44002_.getItem(0));
        if (p_44002_.size() > 1) {
            f &= this.getSecondIngredient().test(p_44002_.getItem(1));
        }
        return f;
    }

    public Ingredient getIngredient() {
        return this.getIngredients().get(0);
    }

    public Ingredient getSecondIngredient() {
        if (this.ingredients.size() > 1) {
            return this.getIngredients().get(1);
        }
        return Ingredient.EMPTY;
    }

    public ProcessingOutput getOutput() {
        return this.results.get(0);
    }

    @Override
    public int getProcessingDuration() {
        int i = super.getProcessingDuration();
        return i == 0 ? BeamFocuserBlockEntity.PROCESSING_TICK : i;
    }

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    }

    public FocusingRecipeParams.BeamTypeCondition getRequiredBeamType() {
        return this.beamTypeCondition;
    }

    protected FocusingRecipeParams.BeamTypeCondition readRequiredBeamType(JsonObject jsonObject) {
        if (jsonObject.has(REQUIRED_BEAM_TYPE_KEY)) {
            var got = jsonObject.get(REQUIRED_BEAM_TYPE_KEY);
            var parsed = FocusingRecipeParams.BeamTypeCondition.INDEXED.fromJson(got);
            if (parsed != null) {
                return parsed;
            }
        }
        return FocusingRecipeParams.BeamTypeCondition.NONE;
    }

    protected FocusingRecipeParams.BeamTypeCondition readRequiredBeamType(FriendlyByteBuf buffer) {
        int i = buffer.readInt();
        if (i >= 0 && i < FocusingRecipeParams.BeamTypeCondition.values().length) {
            return FocusingRecipeParams.BeamTypeCondition.values()[i];
        }
        return FocusingRecipeParams.BeamTypeCondition.NONE;
    }

    public static class Serializer<R extends FocusingRecipe> implements RecipeSerializer<R> {
        private final MapCodec<R> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, R> streamCodec;

        public Serializer(ProcessingRecipe.Factory<FocusingRecipeParams, R> factory) {
            this.codec = ProcessingRecipe.codec(factory, FocusingRecipeParams.CODEC);
            this.streamCodec = ProcessingRecipe.streamCodec(factory, FocusingRecipeParams.STREAM_CODEC);
        }

        @Override
        public MapCodec<R> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, R> streamCodec() {
            return streamCodec;
        }

    }

    @FunctionalInterface
    public interface Factory<R extends FocusingRecipe> extends ProcessingRecipe.Factory<FocusingRecipeParams, R> {
        R create(FocusingRecipeParams params);
    }

    public static class Builder<R extends FocusingRecipe>
            extends ProcessingRecipeBuilder<FocusingRecipeParams, R, Builder<R>> {
        public Builder(Factory<R> factory, ResourceLocation recipeId) {
            super(factory, recipeId);
        }

        @Override
        protected FocusingRecipeParams createParams() {
            return new FocusingRecipeParams();
        }

        public Builder<R> setBeamTypeCondition(BeamTypeCondition condition) {
            this.params.condition = condition;
            return this;
        }

        @Override
        public Builder<R> self() {
            return this;
        }

    }
}
