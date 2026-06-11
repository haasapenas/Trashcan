package com.haasapenas.trashcan.screen;

import com.haasapenas.trashcan.TrashBlockEntity;
import com.haasapenas.trashcan.TrashCan;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TrashMenu extends BaseMenu {

    public static final int TRASH_SLOT = 0;

    public TrashMenu(int containerId, Player player, BlockPos pos){
        super(TrashCan.TRASH_MENU.get(), containerId, player, pos, 196, 132, 8, 51);
    }

    @Override
    protected void addSlots(Player player, TrashBlockEntity entity){
        this.addSlot(new DiscardSlot(TRASH_SLOT, 81, 16, stack ->
            TrashMenu.this.validateObjectOrClose() && TrashMenu.this.object.isRegularItemValid(stack)
        ));
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index){
        if(!this.validateObjectOrClose())
            return ItemStack.EMPTY;
        if(index >= 1){
            ItemStack stack = this.getSlot(index).getItem();
            if(this.getSlot(TRASH_SLOT).mayPlace(stack))
                this.getSlot(index).set(ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }
}