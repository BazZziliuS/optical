package net.lpcamors.optical.recipes;

import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import net.lpcamors.optical.CreateOptical;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class FocusingRecipeInfo implements IRecipeTypeInfo {

    private ResourceLocation id;
    private FocusingRecipe.Serializer<FocusingRecipe> serializer;
    private RecipeType<FocusingRecipe> type;


    public FocusingRecipeInfo(String name, FocusingRecipe.Serializer<FocusingRecipe> serializer, RecipeType<FocusingRecipe> type){
        this.id = ResourceLocation.fromNamespaceAndPath(CreateOptical.ID, "focusing_"+name);
        this.serializer = serializer;
        this.type = type;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends RecipeSerializer<?>> T getSerializer() {
        return (T) this.serializer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I extends RecipeInput, R extends Recipe<I>> RecipeType<R> getType() {
        return (RecipeType<R>)this.type;
    }
}

