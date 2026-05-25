package net.lpcamors.optical.compat.jei;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.compat.jei.category.CreateRecipeCategory;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import net.lpcamors.optical.CORecipeTypes;
import net.lpcamors.optical.CreateOptical;
import net.lpcamors.optical.blocks.COBlocks;
import net.lpcamors.optical.recipes.FocusingRecipe;
import net.lpcamors.optical.recipes.FocusingRecipeCategory;
import net.lpcamors.optical.recipes.FocusingRecipeParams;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class CreateOpticalJei implements IModPlugin {

    private static ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CreateOptical.ID, "jei_plugin");

    private List<CreateRecipeCategory<?>> categories = new ArrayList<>();
    public IIngredientManager ingredientManager;

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        categories.clear();

        categories.add(new CreateRecipeCategory.Builder<>(FocusingRecipe.class)
                .addAllRecipesIf(FocusingRecipeParams.BeamTypeConditionProfile.SMOKING.getRecipePredicate(),
                        FocusingRecipeParams.BeamTypeConditionProfile.SMOKING
                                .getConverter(Minecraft.getInstance().level.registryAccess()))

                .addAllRecipesIf(FocusingRecipeParams.BeamTypeConditionProfile.ADD_COLOR.getRecipePredicate(),
                        FocusingRecipeParams.BeamTypeConditionProfile.ADD_COLOR
                                .getConverter(Minecraft.getInstance().level.registryAccess()))

                .addAllRecipesIf(FocusingRecipeParams.BeamTypeConditionProfile.SANDPAPER.getRecipePredicate(),
                        FocusingRecipeParams.BeamTypeConditionProfile.SANDPAPER
                                .getConverter(Minecraft.getInstance().level.registryAccess()))
                .addRecipes(() -> FocusingRecipeParams.BeamTypeConditionProfile.SHERDS.getRecipes())
                .addRecipes(() -> FocusingRecipeParams.BeamTypeConditionProfile.PATTERN.getRecipes())
                .addRecipes(() -> FocusingRecipeParams.BeamTypeConditionProfile.DISC.getRecipes())
                .addTypedRecipes(CORecipeTypes.FOCUSING::getType)
                .catalystStack(() -> new ItemStack(COBlocks.BEAM_FOCUSER.get().asItem(), 1))
                .itemIcon(COBlocks.BEAM_FOCUSER)
                .emptyBackground(177, 103)
                .build("focusing", FocusingRecipeCategory::new));

        categories.forEach(registration::addRecipeCategories);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        this.ingredientManager = registration.getIngredientManager();
        categories.forEach(createRecipeCategory -> createRecipeCategory.registerRecipes(registration));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        this.categories.forEach(createRecipeCategory -> {
            createRecipeCategory.registerCatalysts(registration);
        });
    }

}
