package net.zapp.quantized.content.blocks.quantum_analyzer;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.zapp.quantized.core.init.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

public class QuantumAnalyzer extends BaseEntityBlock {
    public static final MapCodec<QuantumAnalyzer> CODEC = simpleCodec(QuantumAnalyzer::new);

    public static final BooleanProperty ON = BooleanProperty.create("on");

    private static final VoxelShape SHAPE =
            Shapes.or(Block.box(0, 0, 1, 16, 1, 15),
                    Block.box(1, 0, 0, 15, 1, 1),
                    Block.box(1, 0, 15, 15, 1, 16),
                    Block.box(0, 15, 1, 16, 16, 15),
                    Block.box(1, 15, 0, 15, 16, 1),
                    Block.box(1, 15, 15, 15, 16, 16),
                    Block.box(1, 1, 2, 2, 15, 14),
                    Block.box(2, 1, 1, 14, 15, 2),
                    Block.box(14, 1, 2, 15, 15, 14),
                    Block.box(2, 1, 14, 14, 15, 15));

    public QuantumAnalyzer(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ON, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        stateBuilder.add(ON);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new QuantumAnalyzerTile(blockPos, blockState);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }


    @Override
    protected InteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos,
                                          Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult) {
        if (!pLevel.isClientSide()) {
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if(entity instanceof QuantumAnalyzerTile quantumAnalyzerTile) {
                pPlayer.openMenu(new SimpleMenuProvider(quantumAnalyzerTile, Component.translatable("block.quantized.quantum_analyzer")), pPos);
            } else {
                throw new IllegalStateException("Our Container provider is missing!");
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if(level.isClientSide()) {
            return null;
        }

        return createTickerHelper(blockEntityType, ModBlockEntities.QUANTUM_ANALYZER_TILE.get(),
                (level1, blockPos, blockState, blockEntity) -> blockEntity.tick(level1, blockPos, blockState));
    }
}
