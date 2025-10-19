package net.zapp.quantized.content.blocks.sterling_engine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.zapp.quantized.core.init.ModBlockEntities;
import net.zapp.quantized.core.utils.module.EnergyModule;
import net.zapp.quantized.core.utils.module.ItemModule;
import net.zapp.quantized.core.utils.module.identifiers.HasEnergyModule;
import net.zapp.quantized.core.utils.module.identifiers.HasItemModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SterlingEngineTile extends BlockEntity implements MenuProvider, HasEnergyModule, HasItemModule {
    private static final int FE_CAPACITY = 100_000;
    private static final int DEFAULT_FE_PRODUCTION = 100;

    private final String ownerName = "SterlingEngineTile";
    private EnergyModule energyM = new EnergyModule(ownerName, FE_CAPACITY, Integer.MAX_VALUE, true, true);
    private ItemModule itemM = new ItemModule(ownerName, new ItemStackHandler(1) {
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.getBurnTime(RecipeType.SMELTING, level.fuelValues()) > 0)
                return super.insertItem(slot, stack, simulate);
            return stack;
        }
    });


    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> burnTime;
                case 1 -> maxBurnTime;
                case 2 -> isWorking ? 1 : 0;
                case 3 -> feProduction;
                case 4 -> energyM.getHandler().getEnergy();
                case 5 -> energyM.getHandler().getMaxEnergyStored();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 6;
        }
    };

    private boolean isWorking;
    private int burnTime;
    private int maxBurnTime;
    private int feProduction = DEFAULT_FE_PRODUCTION;

    public SterlingEngineTile(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.STERLING_ENGINE_TILE.get(), pos, blockState);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        generateEnergy(level);
        energyM.pushEnergy(level, pos);
    }

    private void generateEnergy(Level level) {
        if (energyM.canInsert(DEFAULT_FE_PRODUCTION)) {
            if (burnTime <= 0) {
                feProduction = 0;
                ItemStack fuel = itemM.getHandler().getStackInSlot(0);
                if (fuel.isEmpty()) return;
                setBurnTime(fuel.getBurnTime(RecipeType.SMELTING, level.fuelValues()));
                maxBurnTime = burnTime;
                if (burnTime <= 0) return;
                itemM.getHandler().extractItem(0, 1, false);
            } else {
                feProduction = DEFAULT_FE_PRODUCTION;
                setBurnTime(burnTime - 1);
                energyM.getHandler().receiveEnergy(feProduction, false);
            }
        } else  {
            if (burnTime > 0) setBurnTime(burnTime - 1);
            feProduction = 0;
        }
    }


    @Override
    protected void saveAdditional(ValueOutput output) {
        HolderLookup.Provider regs = level != null ? level.registryAccess() : null;

        energyM.save(output, regs);
        itemM.save(output, regs);

        output.putInt("burnTime", burnTime);
        output.putInt("maxBurnTime", maxBurnTime);

        super.saveAdditional(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        HolderLookup.Provider regs = level != null ? level.registryAccess() : null;

        energyM.load(input, regs);
        itemM.load(input, regs);

        burnTime = input.getIntOr("burnTime", 0);
        maxBurnTime = input.getIntOr("maxBurnTime", 1);
    }

    private void setBurnTime(int burnTime) {
        if (this.burnTime == burnTime) return;
        this.burnTime = burnTime;
        if (getBlockState().getValue(SterlingEngine.LIT) != burnTime > 0) {
            level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(SterlingEngine.LIT, burnTime > 0));
        }
        markDirtyAndUpdate();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.quantized.tile.coal_generator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new SterlingEngineMenu(id, inv, this, data);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void markDirtyAndUpdate() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public @NotNull EnergyModule getEnergyModule() {
        return energyM;
    }

    @Override
    public @NotNull ItemModule getItemModule() {
        return itemM;
    }
}
