package net.zapp.quantized.blocks.machine_block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.zapp.quantized.api.energy.CustomEnergyStorage;
import net.zapp.quantized.blocks.machine_block.recipe.MachineBlockRecipe;
import net.zapp.quantized.blocks.machine_block.recipe.MachineBlockRecipeInput;
import net.zapp.quantized.init.ModBlockEntities;
import net.zapp.quantized.init.ModRecipes;
import net.zapp.quantized.api.energy.ICustomEnergyStorage;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Optional;

public class MachineBlockTile extends BlockEntity implements MenuProvider {
    public final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final CustomEnergyStorage energy = createEnergyStorage();
    private final Lazy<ICustomEnergyStorage> energyHandler = Lazy.of(() -> new CustomEnergyStorage(energy));

    private final FluidTank tank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };
    private final Lazy<FluidTank> tankHandler = Lazy.of(() -> tank);

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    public static final int CONSUMPTION = 16;
    public static final int MAX_FE_TRANSFER = 1000;
    public static final int FE_CAPACITY = 100000;

    public static final Holder<Fluid> FLUID_TYPE = Holder.direct(Fluids.WATER);
    public static final int TANK_CAPACITY = 8000;

    protected final ContainerData data;
    private int progress = 0;
    private int maxProgress = 72;

    public MachineBlockTile(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.MACHINE_BLOCK_TILE.get(), pos, blockState);
        data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> MachineBlockTile.this.progress;
                    case 1 -> MachineBlockTile.this.maxProgress;
                    case 2 -> MachineBlockTile.this.energyHandler.get().getEnergy();
                    case 3 -> MachineBlockTile.this.energyHandler.get().getMaxEnergyStored();
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int value) {
                switch (i) {
                    case 0: MachineBlockTile.this.progress = value;
                    case 1: MachineBlockTile.this.maxProgress = value;
                }
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.quantized.tile.name");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new MachineBlockMenu(i, inventory, this, this.data);
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        drops();
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        itemHandler.serialize(output);

        output.putInt("machine_block.energy", energyHandler.get().getEnergyStored());
        output.putInt("machine_block.max_energy", energyHandler.get().getMaxEnergyStored());
        output.store("machine_block.tank_fluid", FluidStack.CODEC, tank.getFluid());
        output.putInt("machine_block.tank_capacity", tank.getCapacity());
        output.putBoolean("machine_block.can_receive", energyHandler.get().canReceive());
        output.putBoolean("machine_block.can_extract", energyHandler.get().canExtract());

        output.putInt("machine_block.progress", progress);
        output.putInt("machine_block.max_progress", maxProgress);

        super.saveAdditional(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        energyHandler.get().setEnergy(input.getIntOr("machine_block.energy", 0));
        energyHandler.get().setCapacity(input.getIntOr("machine_block.max_energy", FE_CAPACITY));

        tank.setFluid(input.read("machine_block.tank_fluid", FluidStack.CODEC).orElse(new FluidStack(FLUID_TYPE, 0)));
        tank.setCapacity(input.getIntOr("machine_block.tank_capacity", TANK_CAPACITY));

        itemHandler.deserialize(input);
        progress = input.getIntOr("machine_block.progress", 0);
        maxProgress = input.getIntOr("machine_block.max_progress", 0);
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if(hasRecipe() && (energyHandler.get().getEnergyStored() > CONSUMPTION)) {
            increaseCraftingProgress();
            energyHandler.get().extractEnergy(CONSUMPTION, false);
            setChanged(level, blockPos, blockState);

            if(hasCraftingFinished()) {
                craftItem();
                resetProgress();
            }
        } else {
            resetProgress();
        }
    }

    private void craftItem() {
        Optional<RecipeHolder<MachineBlockRecipe>> recipe = getCurrentRecipe();
        ItemStack output = recipe.get().value().output();

        itemHandler.extractItem(INPUT_SLOT, 1, false);
        itemHandler.setStackInSlot(OUTPUT_SLOT, new ItemStack(output.getItem(),
                itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() + output.getCount()));
    }

    private void resetProgress() {
        progress = 0;
        maxProgress = 72;
    }

    private boolean hasCraftingFinished() {
        return this.progress >= this.maxProgress;
    }

    private void increaseCraftingProgress() {
        progress++;
    }

    private boolean hasRecipe() {
        Optional<RecipeHolder<MachineBlockRecipe>> recipe = getCurrentRecipe();
        if(recipe.isEmpty()) {
            return false;
        }

        ItemStack output = recipe.get().value().output();
        return canInsertAmountIntoOutputSlot(output.getCount()) && canInsertItemIntoOutputSlot(output);
    }

    private Optional<RecipeHolder<MachineBlockRecipe>> getCurrentRecipe() {
        return ((ServerLevel) this.level).recipeAccess()
                .getRecipeFor(ModRecipes.MACHINE_BLOCK_TYPE.get(), new MachineBlockRecipeInput(itemHandler.getStackInSlot(INPUT_SLOT)), level);
    }

    private boolean canInsertItemIntoOutputSlot(ItemStack output) {
        return itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() ||
                itemHandler.getStackInSlot(OUTPUT_SLOT).getItem() == output.getItem();
    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        int maxCount = itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() ? 64 : itemHandler.getStackInSlot(OUTPUT_SLOT).getMaxStackSize();
        int currentCount = itemHandler.getStackInSlot(OUTPUT_SLOT).getCount();

        return maxCount >= currentCount + count;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        return saveWithoutMetadata(pRegistries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Nonnull
    private CustomEnergyStorage createEnergyStorage() {
        return new CustomEnergyStorage(FE_CAPACITY, MAX_FE_TRANSFER, MAX_FE_TRANSFER, true, true);
    }

    public ICustomEnergyStorage getEnergyStorage() {
        return this.energyHandler.get();
    }

    public FluidTank getTank(){
        return this.tankHandler.get();
    }
}
