package com.haasapenas.trashcan.jei;

import com.haasapenas.trashcan.screen.FilterScreen;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.neoforge.NeoForgeTypes;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;

public class JeiFilterDropHandler implements IGhostIngredientHandler<FilterScreen> {

    @Override
    public <I> List<Target<I>> getTargetsTyped(FilterScreen screen, ITypedIngredient<I> ingredient, boolean doStart){
        if(ingredient.getType() == NeoForgeTypes.FLUID_STACK){
            FluidStack fluid = ingredient.getIngredient(NeoForgeTypes.FLUID_STACK).orElse(FluidStack.EMPTY);
            if(fluid.isEmpty())
                return List.of();

            Rect2i targetArea = screen.getJeiFilterTargetArea();
            return List.of(new Target<>() {
                @Override
                public Rect2i getArea(){
                    return targetArea;
                }

                @Override
                public void accept(I ingredient){
                    screen.addFluidFilterFromJei(fluid.copy());
                }
            });
        }

        if(ingredient.getType() == VanillaTypes.ITEM_STACK){
            ItemStack stack = ingredient.getIngredient(VanillaTypes.ITEM_STACK).orElse(ItemStack.EMPTY);
            if(stack.isEmpty())
                return List.of();

            Rect2i targetArea = screen.getJeiFilterTargetArea();
            return List.of(new Target<>() {
                @Override
                public Rect2i getArea(){
                    return targetArea;
                }

                @Override
                public void accept(I ingredient){
                    screen.addFilterFromJei(stack.copy());
                }
            });
        }

        return List.of();
    }

    @Override
    public void onComplete(){
    }
}
