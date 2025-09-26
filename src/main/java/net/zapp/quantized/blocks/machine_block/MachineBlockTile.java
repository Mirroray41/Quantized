package net.zapp.quantized.blocks.machine_block;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.zapp.quantized.api.module.EnergyModule;
import net.zapp.quantized.api.module.ItemModule;
import net.zapp.quantized.api.module.TankModule;
import net.zapp.quantized.api.module.identifiers.HasEnergyModule;
import net.zapp.quantized.api.module.identifiers.HasItemModule;
import net.zapp.quantized.api.module.identifiers.HasTankModule;
import net.zapp.quantized.blocks.machine_block.recipe.MachineBlockRecipe;
import net.zapp.quantized.blocks.machine_block.recipe.MachineBlockRecipeInput;
import net.zapp.quantized.init.ModBlockEntities;
import net.zapp.quantized.init.ModFluids;
import net.zapp.quantized.init.ModRecipes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MachineBlockTile extends BlockEntity implements MenuProvider, HasEnergyModule, HasTankModule, HasItemModule {
    // ---- Slots ----
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    // ---- Energy/Fluids constants ----
    public static final int CONSUMPTION = 16;
    public static final int MAX_FE_TRANSFER = 1000;
    public static final int FE_CAPACITY = 100_000;
    public static final int TANK_CAPACITY = 8_000;



    // ---- Modules (storage-only) ----
    private final String ownerName = "MachineBlockTile";
    private final ItemModule itemM = new ItemModule(ownerName, 2, slot -> markDirtyAndUpdate());
    private final EnergyModule energyM = new EnergyModule(ownerName, FE_CAPACITY, MAX_FE_TRANSFER, true, true);
    private final TankModule tankM = new TankModule(ownerName, TANK_CAPACITY, fs -> true, s -> markDirtyAndUpdate()
    );

    // ---- Menu sync data ----
    private int progress = 0;
    private int maxProgress = 72;

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int i) {
            return switch (i) {
                case 0 -> progress;
                case 1 -> maxProgress;
                case 2 -> energyM.getHandler().getEnergy();
                case 3 -> energyM.getHandler().getMaxEnergyStored();
                case 4 -> tankM.getHandler().getCapacity();
                default -> 0;
            };
        }

        @Override
        public void set(int i, int value) {
            switch (i) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    public MachineBlockTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_BLOCK_TILE.get(), pos, state);
    }

    // ---- UI / Menu ----
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.quantized.tile.name");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new MachineBlockMenu(id, inv, this, this.data);
    }

    // ---- Ticking / Crafting ----
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        if (hasRecipe() && energyM.getHandler().getEnergyStored() >= CONSUMPTION) {
            progress++;
            energyM.getHandler().extractEnergy(CONSUMPTION, false);
            tankM.getHandler().fill(new FluidStack(ModFluids.QUANTUM_FLUX, 50), IFluidHandler.FluidAction.EXECUTE);
            setChanged(level, pos, state);

            if (progress >= maxProgress) {
                craftItem();
                resetProgress();
            }
        } else {
            resetProgress();
        }
    }

    private void craftItem() {
        Optional<RecipeHolder<MachineBlockRecipe>> recipe = getCurrentRecipe();
        if (recipe.isEmpty()) return;

        ItemStack output = recipe.get().value().output();
        // consume input
        itemM.getHandler().extractItem(INPUT_SLOT, 1, false);
        // place output
        ItemStack curOut = itemM.getHandler().getStackInSlot(OUTPUT_SLOT);
        itemM.getHandler().setStackInSlot(OUTPUT_SLOT,
                new ItemStack(output.getItem(), curOut.getCount() + output.getCount()));
    }

    private Optional<RecipeHolder<MachineBlockRecipe>> getCurrentRecipe() {
        if (!(this.level instanceof ServerLevel server)) return Optional.empty();
        return server.recipeAccess()
                .getRecipeFor(ModRecipes.MACHINE_BLOCK_TYPE.get(),
                        new MachineBlockRecipeInput(itemM.getHandler().getStackInSlot(INPUT_SLOT)), level);
    }

    private boolean hasRecipe() {
        Optional<RecipeHolder<MachineBlockRecipe>> r = getCurrentRecipe();
        if (r.isEmpty()) return false;

        ItemStack out = r.get().value().output();
        ItemStack slot = itemM.getHandler().getStackInSlot(OUTPUT_SLOT);
        boolean itemOk = slot.isEmpty() || slot.is(out.getItem());
        int max = slot.isEmpty() ? 64 : slot.getMaxStackSize();
        return itemOk && (slot.getCount() + out.getCount() <= max);
    }

    private void resetProgress() {
        progress = 0;
        maxProgress = 72;
    }

    // ---- Drop items when broken ----
    public void drops() {
        if (level == null) return;
        SimpleContainer inv = new SimpleContainer(itemM.getHandler().getSlots());
        for (int i = 0; i < itemM.getHandler().getSlots(); i++) {
            inv.setItem(i, itemM.getHandler().getStackInSlot(i));
        }
        Containers.dropContents(level, worldPosition, inv);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        drops();
        super.preRemoveSideEffects(pos, state);
    }

    // ---- Save / Load ----
    @Override
    protected void saveAdditional(ValueOutput out) {
        HolderLookup.Provider regs = level != null ? level.registryAccess() : null;

        // modules
        itemM.save(out, regs);
        energyM.save(out, regs);
        tankM.save(out, regs);

        // local fields
        out.putInt("progress", progress);
        out.putInt("maxProgress", maxProgress);

        super.saveAdditional(out);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        HolderLookup.Provider regs = level != null ? level.registryAccess() : null;

        // modules
        itemM.load(in, regs);
        energyM.load(in, regs);
        tankM.load(in, regs);

        // local fields
        progress = in.getIntOr("progress", 0);
        maxProgress = in.getIntOr("maxProgress", 72);
    }

    // ---- Network sync ----
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // ---- Dirty+update helper ----
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

    @Override
    public @NotNull TankModule getTankModule() {
        return tankM;
    }

    public int getEnergyConsumption() {
        return this.CONSUMPTION;
    }

    public int getCurrentEnergyConsumption() {
        if (hasRecipe() && energyM.getHandler().getEnergyStored() >= CONSUMPTION) {
            return this.CONSUMPTION;
        }
        return 0;
    }
}
