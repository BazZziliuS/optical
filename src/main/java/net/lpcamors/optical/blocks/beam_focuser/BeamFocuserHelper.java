package net.lpcamors.optical.blocks.beam_focuser;

import java.util.Optional;

import javax.annotation.Nullable;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;

import net.lpcamors.optical.CORecipeTypes;
import net.lpcamors.optical.blocks.optical_source.BeamHelper;
import net.lpcamors.optical.recipes.FocusingRecipe;
import net.lpcamors.optical.recipes.FocusingRecipeParams;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

public class BeamFocuserHelper {

    public static boolean canBeProcessed(Level world, RecipeWrapper w, BeamHelper.BeamType beamType) {
        return getFocusingRecipe(world, w, beamType).isPresent();
    }

    public static Optional<RecipeHolder<FocusingRecipe>> getFocusingRecipe(Level world, RecipeWrapper w,
            @Nullable BeamHelper.BeamType beamType) {

        Optional<RecipeHolder<FocusingRecipe>> focusingRecipe = SequencedAssemblyRecipe
                .getRecipe(world, w.getItem(0), CORecipeTypes.FOCUSING.getType(), FocusingRecipe.class);
        if (focusingRecipe.isEmpty()) {
            focusingRecipe = world.getRecipeManager()
                    .getRecipeFor(CORecipeTypes.FOCUSING.getType(), w, world);
        }
        if (focusingRecipe.isEmpty()) {
            focusingRecipe = FocusingRecipeParams.BeamTypeConditionProfile.getRecipeFor(world, w, beamType);
        }
        if (focusingRecipe.isPresent()) {
            if (focusingRecipe.get().value().beamTypeCondition.test(beamType)) {
                return focusingRecipe;
            }
        }
        return Optional.empty();
    }

}
