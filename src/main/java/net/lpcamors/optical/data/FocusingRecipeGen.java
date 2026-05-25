package net.lpcamors.optical.data;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.api.data.recipe.ProcessingRecipeGen;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import net.lpcamors.optical.CreateOptical;
import net.lpcamors.optical.CORecipeTypes;
import net.lpcamors.optical.items.COItems;
import net.lpcamors.optical.recipes.FocusingRecipe;
import net.lpcamors.optical.recipes.FocusingRecipe.Builder;
import net.lpcamors.optical.recipes.FocusingRecipeParams;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;


public class FocusingRecipeGen extends ProcessingRecipeGen<FocusingRecipeParams, FocusingRecipe, FocusingRecipe.Builder<FocusingRecipe>> {

    GeneratedRecipe
        MIRROR = create(CreateOptical.loc("mirror"), 
                f -> f.require(Tags.Items.GLASS_PANES).output(COItems.MIRROR).duration(50).setBeamTypeCondition(FocusingRecipeParams.BeamTypeCondition.VISIBLE)),    
        FILTER = create(CreateOptical.loc("filter"), 
            f -> f.require(Items.TINTED_GLASS).output(COItems.POLARIZING_FILTER).duration(50)
                    .setBeamTypeCondition(FocusingRecipeParams.BeamTypeCondition.VISIBLE))
        
    ;


    public FocusingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, CreateOptical.ID);
	}
    @Override
    protected IRecipeTypeInfo getRecipeType() {
        return CORecipeTypes.FOCUSING;

    }
    @Override
    protected Builder<FocusingRecipe> getBuilder(ResourceLocation id) {
        return new FocusingRecipe.Builder<FocusingRecipe>(FocusingRecipe::new, id);
    }


}
