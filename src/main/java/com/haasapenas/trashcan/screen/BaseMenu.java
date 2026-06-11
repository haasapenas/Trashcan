package com.haasapenas.trashcan.screen;

import com.haasapenas.trashcan.TrashBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;

public abstract class BaseMenu extends AbstractContainerMenu {

    protected final Player player;
    protected final BlockPos blockEntityPos;
    protected final ContainerLevelAccess access;
    protected TrashBlockEntity object;
    public final int width, height;
    private final int inventoryLeft;
    private final int inventoryTop;

    public BaseMenu(MenuType<?> type, int containerId, Player player, BlockPos pos, int width, int height){
        this(type, containerId, player, pos, width, height, 8, height - 82);
    }

    public BaseMenu(MenuType<?> type, int containerId, Player player, BlockPos pos, int width, int height, int inventoryLeft, int inventoryTop){
        super(type, containerId);
        this.player = player;
        this.blockEntityPos = pos;
        this.access = ContainerLevelAccess.create(player.level(), pos);
        this.width = width;
        this.height = height;
        this.inventoryLeft = inventoryLeft;
        this.inventoryTop = inventoryTop;

        if(player.level().getBlockEntity(pos) instanceof TrashBlockEntity entity){
            this.object = entity;
            this.addSlots(player, entity);
        }
        this.addStandardInventorySlots(player.getInventory(), this.inventoryLeft, this.inventoryTop);
    }

    protected abstract void addSlots(Player player, TrashBlockEntity entity);

    protected boolean validateObjectOrClose(){
        return this.object != null && this.object.hasLevel() && !this.object.isRemoved();
    }

    @Override
    public boolean stillValid(Player player){
        return this.validateObjectOrClose() && stillValid(this.access, player, this.object.getBlockState().getBlock());
    }

    public BlockPos getBlockEntityPos(){
        return this.blockEntityPos;
    }

    public TrashBlockEntity getBlockEntity(){
        return this.object;
    }
}