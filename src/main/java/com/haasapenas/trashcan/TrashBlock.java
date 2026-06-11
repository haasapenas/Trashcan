package com.haasapenas.trashcan;

import com.haasapenas.trashcan.screen.BaseMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

public class TrashBlock extends Block implements EntityBlock {

    private static final VoxelShape SHAPE = Shapes.or(
        Shapes.box(3 / 16d, 0, 3 / 16d, 13 / 16d, 12 / 16d, 13 / 16d),
        Shapes.box(2 / 16d, 12 / 16d, 2 / 16d, 14 / 16d, 13 / 16d, 14 / 16d),
        Shapes.box(3 / 16d, 12.5 / 16d, 3 / 16d, 13 / 16d, 13.5 / 16d, 13 / 16d)
    );

    private final Supplier<? extends BlockEntityType<? extends TrashBlockEntity>> blockEntityType;
    private final MenuFactory menuProvider;

    public TrashBlock(BlockBehaviour.Properties properties, Supplier<? extends BlockEntityType<? extends TrashBlockEntity>> blockEntityType, MenuFactory menuProvider){
        super(properties);
        this.blockEntityType = blockEntityType;
        this.menuProvider = menuProvider;
    }

    public static BlockBehaviour.Properties blockProperties(){
        return BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_GRAY)
            .sound(SoundType.METAL)
            .strength(1.5f, 6)
            .requiresCorrectToolForDrops();
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult){
        return this.openMenu(level, pos, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult){
        return this.openMenu(level, pos, player);
    }

    private InteractionResult openMenu(Level level, BlockPos pos, Player player){
        if(!level.isClientSide()){
            SimpleMenuProvider provider = new SimpleMenuProvider(
                (containerId, inventory, viewer) -> this.menuProvider.create(containerId, viewer, pos),
                Component.translatable("trashcan.gui.trash_can.title")
            );
            player.openMenu(provider, pos);
        }
        return InteractionResult.SUCCESS;
    }


    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player){
        this.dropStoredFilter(level, pos);
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool){
        this.dropStoredFilter(level, pos, blockEntity);
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    private void dropStoredFilter(Level level, BlockPos pos){
        this.dropStoredFilter(level, pos, level.getBlockEntity(pos));
    }

    private void dropStoredFilter(Level level, BlockPos pos, BlockEntity blockEntity){
        if(!level.isClientSide() && blockEntity instanceof TrashBlockEntity trashCan && !trashCan.itemFilterStack.isEmpty()){
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, trashCan.itemFilterStack.copy());
            trashCan.itemFilterStack = ItemStack.EMPTY;
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context){
        return SHAPE;
    }


    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston){
        this.dropStoredFilter(level, pos);
        Containers.updateNeighboursAfterDestroy(state, level, pos);
    }
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
        return this.blockEntityType.get().create(pos, state);
    }

    @FunctionalInterface
    public interface MenuFactory {
        BaseMenu create(int containerId, Player player, BlockPos pos);
    }
}