package com.haasapenas.trashcan.filter;

import com.haasapenas.trashcan.TrashCan;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public final class FilterData {

    public static final int SLOT_COUNT = 54;

    private static final String WHITELIST = "whitelist";
    private static final String FILTER_PREFIX = "filter";
    private static final String NBT_SUFFIX = "Nbt";

    private FilterData(){
    }

    public static boolean isFilter(ItemStack stack){
        return isItemFilter(stack) || isLiquidFilter(stack);
    }

    public static boolean isItemFilter(ItemStack stack){
        return !stack.isEmpty() && stack.is(TrashCan.FILTER_ITEM.get());
    }

    public static boolean isLiquidFilter(ItemStack stack){
        return !stack.isEmpty() && stack.is(TrashCan.LIQUID_FILTER_ITEM.get());
    }

    public static boolean isWhitelist(ItemStack filterStack){
        if(!isFilter(filterStack))
            return false;
        return filterStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBooleanOr(WHITELIST, true);
    }

    public static void setWhitelist(ItemStack filterStack, boolean whitelist){
        if(!isFilter(filterStack))
            return;
        CustomData.update(DataComponents.CUSTOM_DATA, filterStack, tag -> tag.putBoolean(WHITELIST, whitelist));
    }

    public static ItemStack getFilter(ItemStack filterStack, int slot){
        validateSlot(slot);
        if(!isItemFilter(filterStack))
            return ItemStack.EMPTY;
        CompoundTag tag = filterStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        String itemId = tag.getString(FILTER_PREFIX + slot).orElse("");
        if(itemId.isEmpty())
            return ItemStack.EMPTY;
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId));
        return item == null ? ItemStack.EMPTY : item.getDefaultInstance();
    }

    public static void setFilter(ItemStack filterStack, int slot, ItemStack stack){
        validateSlot(slot);
        if(!isItemFilter(filterStack))
            return;

        CustomData.update(DataComponents.CUSTOM_DATA, filterStack, tag -> {
            if(stack.isEmpty()){
                tag.remove(FILTER_PREFIX + slot);
                return;
            }

            Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if(itemId == null)
                tag.remove(FILTER_PREFIX + slot);
            else
                tag.putString(FILTER_PREFIX + slot, itemId.toString());
        });
    }

    public static void setFilter(ItemStack filterStack, int slot, String itemId){
        setFilter(filterStack, slot, itemId, "");
    }

    public static void setFilter(ItemStack filterStack, int slot, String itemId, String nbtData){
        validateSlot(slot);
        if(!isItemFilter(filterStack))
            return;

        CustomData.update(DataComponents.CUSTOM_DATA, filterStack, tag -> {
            if(itemId == null || itemId.isBlank()){
                tag.remove(FILTER_PREFIX + slot);
                tag.remove(FILTER_PREFIX + slot + NBT_SUFFIX);
                return;
            }

            Identifier id = Identifier.tryParse(itemId);
            if(id == null || !BuiltInRegistries.ITEM.containsKey(id))
                return;
            for(int otherSlot = 0; otherSlot < SLOT_COUNT; otherSlot++){
                if(otherSlot != slot && id.toString().equals(tag.getString(FILTER_PREFIX + otherSlot).orElse("")))
                    return;
            }
            tag.putString(FILTER_PREFIX + slot, id.toString());
            if(nbtData == null || nbtData.isBlank())
                tag.remove(FILTER_PREFIX + slot + NBT_SUFFIX);
            else
                tag.putString(FILTER_PREFIX + slot + NBT_SUFFIX, nbtData);
        });
    }

    public static String getFilterId(ItemStack filterStack, int slot){
        validateSlot(slot);
        if(!isItemFilter(filterStack))
            return "";
        return filterStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString(FILTER_PREFIX + slot).orElse("");
    }

    public static String getFilterNbt(ItemStack filterStack, int slot){
        validateSlot(slot);
        if(!isItemFilter(filterStack))
            return "";
        return filterStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString(FILTER_PREFIX + slot + NBT_SUFFIX).orElse("");
    }

    public static boolean matches(ItemStack filterStack, ItemStack candidate){
        if(candidate.isEmpty())
            return false;

        for(int i = 0; i < SLOT_COUNT; i++){
            ItemStack filter = getFilter(filterStack, i);
            if(filter.isEmpty() || !ItemStack.isSameItem(candidate, filter))
                continue;
            String nbt = getFilterNbt(filterStack, i);
            if(nbt.isEmpty() || candidate.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().toString().equals(nbt))
                return true;
        }
        return false;
    }

    public static boolean accepts(ItemStack filterStack, ItemStack candidate){
        if(!isItemFilter(filterStack))
            return true;
        boolean whitelist = isWhitelist(filterStack);
        return matches(filterStack, candidate) == whitelist;
    }

    public static FluidStack getFluidFilter(ItemStack filterStack, int slot){
        validateSlot(slot);
        if(!isLiquidFilter(filterStack))
            return FluidStack.EMPTY;

        String fluidId = getFluidFilterId(filterStack, slot);
        if(fluidId.isEmpty())
            return FluidStack.EMPTY;

        Identifier id = Identifier.tryParse(fluidId);
        if(id == null)
            return FluidStack.EMPTY;

        Fluid fluid = BuiltInRegistries.FLUID.getValue(id);
        return fluid == null || fluid == Fluids.EMPTY ? FluidStack.EMPTY : new FluidStack(fluid, FluidType.BUCKET_VOLUME);
    }

    public static void setFluidFilter(ItemStack filterStack, int slot, FluidStack stack){
        validateSlot(slot);
        if(stack.isEmpty()){
            setFluidFilter(filterStack, slot, "");
            return;
        }

        Identifier fluidId = BuiltInRegistries.FLUID.getKey(stack.getFluid());
        setFluidFilter(filterStack, slot, fluidId == null ? "" : fluidId.toString());
    }

    public static void setFluidFilter(ItemStack filterStack, int slot, FluidResource resource){
        validateSlot(slot);
        if(resource.isEmpty()){
            setFluidFilter(filterStack, slot, "");
            return;
        }

        Identifier fluidId = BuiltInRegistries.FLUID.getKey(resource.getFluid());
        setFluidFilter(filterStack, slot, fluidId == null ? "" : fluidId.toString());
    }

    public static void setFluidFilter(ItemStack filterStack, int slot, String fluidId){
        validateSlot(slot);
        if(!isLiquidFilter(filterStack))
            return;

        CustomData.update(DataComponents.CUSTOM_DATA, filterStack, tag -> {
            if(fluidId == null || fluidId.isBlank()){
                tag.remove(FILTER_PREFIX + slot);
                return;
            }

            Identifier id = Identifier.tryParse(fluidId);
            if(id == null || !BuiltInRegistries.FLUID.containsKey(id) || BuiltInRegistries.FLUID.getValue(id) == Fluids.EMPTY)
                return;

            for(int otherSlot = 0; otherSlot < SLOT_COUNT; otherSlot++){
                if(otherSlot != slot && id.toString().equals(tag.getString(FILTER_PREFIX + otherSlot).orElse("")))
                    return;
            }
            tag.putString(FILTER_PREFIX + slot, id.toString());
        });
    }

    public static String getFluidFilterId(ItemStack filterStack, int slot){
        validateSlot(slot);
        if(!isLiquidFilter(filterStack))
            return "";
        return filterStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString(FILTER_PREFIX + slot).orElse("");
    }

    public static boolean matchesFluid(ItemStack filterStack, FluidResource candidate){
        if(candidate.isEmpty())
            return false;

        Identifier candidateId = BuiltInRegistries.FLUID.getKey(candidate.getFluid());
        if(candidateId == null)
            return false;

        String normalized = candidateId.toString();
        for(int i = 0; i < SLOT_COUNT; i++){
            if(normalized.equals(getFluidFilterId(filterStack, i)))
                return true;
        }
        return false;
    }

    public static boolean acceptsFluid(ItemStack filterStack, FluidResource candidate){
        if(!isLiquidFilter(filterStack))
            return true;
        boolean whitelist = isWhitelist(filterStack);
        return matchesFluid(filterStack, candidate) == whitelist;
    }

    private static void validateSlot(int slot){
        if(slot < 0 || slot >= SLOT_COUNT)
            throw new IndexOutOfBoundsException("Filter slot must be between 0 and " + (SLOT_COUNT - 1) + ", got " + slot);
    }
}
