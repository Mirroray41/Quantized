package net.zapp.quantized.content.blocks.quantum_analyzer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.zapp.quantized.content.blocks.quantum_destabilizer.ProcessingCurves;
import net.zapp.quantized.content.blocks.quantum_destabilizer.QuantumDestabilizer;
import net.zapp.quantized.content.blocks.quantum_destabilizer.QuantumDestabilizerMenu;
import net.zapp.quantized.content.item.custom.drive_item.DriveItem;
import net.zapp.quantized.content.item.custom.drive_item.DriveRecord;
import net.zapp.quantized.core.datafixing.FluxDataFixerUpper;
import net.zapp.quantized.core.init.ModBlockEntities;
import net.zapp.quantized.core.init.ModDataComponents;
import net.zapp.quantized.core.init.ModFluids;
import net.zapp.quantized.core.init.ModSounds;
import net.zapp.quantized.core.utils.DataFluxPair;
import net.zapp.quantized.core.utils.module.EnergyModule;
import net.zapp.quantized.core.utils.module.ItemModule;
import net.zapp.quantized.core.utils.module.TankModule;
import net.zapp.quantized.core.utils.module.identifiers.HasEnergyModule;
import net.zapp.quantized.core.utils.module.identifiers.HasItemModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuantumAnalyzerTile extends BlockEntity implements MenuProvider, HasEnergyModule, HasItemModule {
    // ---- Rendering init ----
    private static final float ROTATION = 5f;

    // ---- Slots ----
    private static final int INPUT_SLOT = 0;
    private static final int DISK_SLOT = 1;


    // ---- Energy/Fluids constants ----
    public static final int FE_CAPACITY = 100_000;

    // ---- Modules (storage-only) ----
    private final String ownerName = "QuantumAnalyzerTile";
    private final ItemModule itemM = new ItemModule(ownerName, 17, slot -> markDirtyAndUpdate());
    private final EnergyModule energyM = new EnergyModule(ownerName, FE_CAPACITY, Integer.MAX_VALUE, Integer.MAX_VALUE, true, true);

    // ---- Menu sync data ----
    private int progress = 0;
    private int maxProgress = 72;
    public int powerConsumption = 16;

    private boolean wasWorking = false;
    private FluidStack cachedOut = FluidStack.EMPTY;

    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int i) {
            return switch (i) {
                case 0 -> progress;
                case 1 -> maxProgress;
                case 2 -> powerConsumption;
                case 3 -> energyM.getHandler().getEnergy();
                case 4 -> energyM.getHandler().getMaxEnergyStored();
                default -> 0;
            };
        }

        @Override
        public void set(int i, int value) {
            switch (i) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
                case 2 -> powerConsumption = value;
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    public QuantumAnalyzerTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.QUANTUM_ANALYZER_TILE.get(), pos, state);
    }

    // ---- UI / Menu ----
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.quantized.quantum_analyzer.name");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new QuantumAnalyzerMenu(id, inv, this, this.data);
    }

    // --- Tick ---
    public void tick(Level level, BlockPos pos, BlockState state) {
        //if (level.isClientSide) return;

        ItemStack in = itemM.getHandler().getStackInSlot(INPUT_SLOT);
        ItemStack disk = itemM.getHandler().getStackInSlot(DISK_SLOT);

        if (!disk.has(ModDataComponents.DRIVE_DATA)) {
            System.out.println("No Drive Data");
            if (!(disk.getItem() instanceof DriveItem)) {
                System.out.println("No Drive");
                for (int i = 0 ; i < 15 ; i++) {
                    itemM.getHandler().setStackInSlot(i + 2, ItemStack.EMPTY);
                }
                return;
            }
            disk.set(ModDataComponents.DRIVE_DATA, new DriveRecord(8, 2, 0, new String[0], 0));
        }

        DriveRecord diskData = disk.get(ModDataComponents.DRIVE_DATA);


        List<String> items = new ArrayList<>(Arrays.stream(diskData.items()).toList());

        for (int i = 0 ; i < 15 ; i++) {
            if (i < items.size()) {
                ResourceLocation location = ResourceLocation.parse(items.get(i));
                Item item = BuiltInRegistries.ITEM.get(location).get().value();
                itemM.getHandler().setStackInSlot(i + 2, new ItemStack(item));
            } else {
                itemM.getHandler().setStackInSlot(i + 2, ItemStack.EMPTY);
            }
        }

        DataFluxPair df = FluxDataFixerUpper.getDataFluxFromStack(in);
        if (df == null || df.isZero()) {
            resetCraft();
            setWorking(level, pos, state, false);
            return;
        }

        maxProgress = ProcessingCurves.timeTicks(df.data());
        powerConsumption = ProcessingCurves.powerPerTick(df.flux());

        boolean canPay = energyM.getHandler().extractEnergy(powerConsumption, true) == powerConsumption;
        boolean canOut = diskData.dataUsed() + df.data() <= diskData.capacity() && df.data() <= diskData.maxSizePerItem() && !(items.contains(in.getItem().toString()));
        boolean hasInput = !in.isEmpty();
        boolean working = canPay && canOut && hasInput;

        System.out.println(canOut + ", " + working);

        setWorking(level, pos, state, working);
        if (!working) {
            if (progress > 0) progress = Math.max(0, progress - 1);

            return;
        }

        progress++;
        energyM.getHandler().extractEnergy(powerConsumption, false);

        level.playSound(null, pos, ModSounds.QUANTUM_DESTABILIZER_WORK.value(),
                SoundSource.BLOCKS, 1f, 1f + (float) progress / (float) maxProgress);


        if (progress >= maxProgress) {
            items.add(in.getItem().toString());
            DriveRecord newData = new DriveRecord(diskData.capacity(), diskData.maxSizePerItem(), diskData.dataUsed() + df.data(), items.toArray(new String[diskData.count() + 1]), diskData.count() + 1);
            disk.set(ModDataComponents.DRIVE_DATA, newData);
            progress = 0;
        }
    }

    // ---- helpers ----
    private void setWorking(Level level, BlockPos pos, BlockState state, boolean working) {
        if (wasWorking != working) {
            wasWorking = working;
            BlockState ns = state.setValue(QuantumAnalyzer.ON, working);
            setChanged(level, pos, ns);
            level.setBlock(pos, ns, 3);
        }
    }

    private void resetCraft() {
        progress = 0;
        powerConsumption = 0;
        maxProgress = 72;
        cachedOut = FluidStack.EMPTY;
    }


    // ---- Drop items when broken ----
    public void drops() {
        if (level == null) return;
        SimpleContainer inv = new SimpleContainer(itemM.getHandler().getSlots());
        for (int i = 0; i < itemM.getHandler().getSlots() - 15; i++) {
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

    // ---- Rendering ----
    public float getRotationSpeed() {
        return ROTATION;
    }
}
