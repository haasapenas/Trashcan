package com.haasapenas.trashcan;

import com.haasapenas.trashcan.filter.FilterData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class TrashBlockEntity extends BlockEntity {

    private final ResourceHandler<ItemResource> itemHandler = new ResourceHandler<ItemResource>() {
        @Override
        public int size(){
            return 1;
        }

        @Override
        public ItemResource getResource(int index){
            return ItemResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int index){
            return 0;
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource){
            return resource.isEmpty() || this.isValid(index, resource) ? Long.MAX_VALUE : 0;
        }

        @Override
        public boolean isValid(int index, ItemResource resource){
            return !resource.isEmpty() && TrashBlockEntity.this.isRegularItemValid(resource.toStack());
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction){
            if(resource.isEmpty() || amount <= 0)
                return 0;
            return this.isValid(index, resource) ? amount : 0;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction){
            return 0;
        }
    };

    private final ResourceHandler<FluidResource> fluidHandler = new ResourceHandler<FluidResource>() {
        @Override
        public int size(){
            return 1;
        }

        @Override
        public FluidResource getResource(int index){
            return FluidResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int index){
            return 0;
        }

        @Override
        public long getCapacityAsLong(int index, FluidResource resource){
            return !resource.isEmpty() && this.isValid(index, resource) ? Long.MAX_VALUE : 0;
        }

        @Override
        public boolean isValid(int index, FluidResource resource){
            return !resource.isEmpty() && TrashBlockEntity.this.isRegularFluidValid(resource);
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction){
            if(resource.isEmpty() || amount <= 0)
                return 0;
            return this.isValid(index, resource) ? amount : 0;
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction){
            return 0;
        }
    };

    public ItemStack itemFilterStack = ItemStack.EMPTY;

    public TrashBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state){
        super(blockEntityType, pos, state);
    }

    public ResourceHandler<ItemResource> itemHandler(){
        return this.itemHandler;
    }

    public ResourceHandler<FluidResource> fluidHandler(){
        return this.fluidHandler;
    }

    public boolean isRegularItemValid(ItemStack stack){
        return !FilterData.isItemFilter(this.itemFilterStack) || FilterData.accepts(this.itemFilterStack, stack);
    }

    public boolean isRegularFluidValid(FluidResource resource){
        return !FilterData.isLiquidFilter(this.itemFilterStack) || FilterData.acceptsFluid(this.itemFilterStack, resource);
    }

    @Override
    protected void saveAdditional(ValueOutput output){
        super.saveAdditional(output);
        if(this.level.isClientSide())
            return;
        if(!this.itemFilterStack.isEmpty())
            output.store("itemFilterStack", ItemStack.CODEC, this.itemFilterStack);
    }

    @Override
    protected void loadAdditional(ValueInput input){
        super.loadAdditional(input);
        this.itemFilterStack = input.read("itemFilterStack", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    public void dataChanged(){
        this.setChanged();
        if(this.level != null && !this.level.isClientSide())
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
    }
}
