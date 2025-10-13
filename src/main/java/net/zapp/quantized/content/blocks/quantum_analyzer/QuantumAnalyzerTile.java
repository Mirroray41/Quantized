package net.zapp.quantized.content.blocks.quantum_analyzer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.zapp.quantized.content.blocks.ProcessingCurves;
import net.zapp.quantized.content.item.custom.drive_item.DriveItem;
import net.zapp.quantized.content.item.custom.drive_item.DriveRecord;
import net.zapp.quantized.core.fluxdata.FluxDataFixerUpper;
import net.zapp.quantized.core.init.ModBlockEntities;
import net.zapp.quantized.core.init.ModDataComponents;
import net.zapp.quantized.core.init.ModSounds;
import net.zapp.quantized.core.utils.DataFluxPair;
import net.zapp.quantized.core.utils.module.EnergyModule;
import net.zapp.quantized.core.utils.module.ItemModule;
import net.zapp.quantized.core.utils.module.identifiers.HasEnergyModule;
import net.zapp.quantized.core.utils.module.identifiers.HasItemModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class QuantumAnalyzerTile extends BlockEntity implements MenuProvider, HasEnergyModule, HasItemModule {
    // ---- Rendering init ----
    private static final float ROTATION = 5f;

    // ---- Slots ----
    private static final int INPUT_SLOT = 0;
    private static final int DISK_SLOT = 1;

    private static List<String> items = new ArrayList<>();
    private List<String> filtered = new ArrayList<>();
    private String filter = "";


    public int rowOffest = 0;
    int viewers = 0;

    // ---- Energy/Fluids constants ----
    public static final int FE_CAPACITY = 1_000_000;

    // ---- Modules (storage-only) ----
    private final String ownerName = "QuantumAnalyzerTile";
    private final ItemModule itemM = new ItemModule(ownerName, new ItemStackHandler(17) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirtyAndUpdate();
        }

        @Override
        @NotNull
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (slot == DISK_SLOT) {
                if (stack.getItem() instanceof DriveItem)
                    return super.insertItem(slot, stack, simulate);
            } else {
                if (DataFluxPair.isValid(FluxDataFixerUpper.getDataFluxFromStack(stack))) {
                    return super.insertItem(slot, stack, simulate);
                }
            }
            return stack;
        }
    });
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
                case 5 -> filtered.size();
                default -> 0;
            };
        }

        @Override
        public void set(int i, int value) {
            switch (i) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
                case 2 -> powerConsumption = value;
                case 6 -> rowOffest = value;
            }
        }

        @Override
        public int getCount() {
            return 7;
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
        if (level.isClientSide) return;

        ItemStack in = itemM.getHandler().getStackInSlot(INPUT_SLOT);
        ItemStack disk = itemM.getHandler().getStackInSlot(DISK_SLOT);

        if (!disk.has(ModDataComponents.DRIVE_DATA)) {
            if (!(disk.getItem() instanceof DriveItem)) {
                for (int i = 0 ; i < 15 ; i++) {
                    itemM.getHandler().setStackInSlot(i + 2, ItemStack.EMPTY);
                }
                items.clear();
                filtered.clear();
                return;
            }
            disk.set(ModDataComponents.DRIVE_DATA, new DriveRecord(8, 2, 0, new String[0], 0));
        }

        DriveRecord diskData = disk.get(ModDataComponents.DRIVE_DATA);
        items = new ArrayList<>(Arrays.asList(diskData.items()));
        rebuildFiltered();

        List<Item> driveItems = DriveItem.getStoredItems(disk);
        for (int i = 0; i < Math.min(15, driveItems.size()); i++) {
            itemM.getHandler().setStackInSlot(i + 2, new ItemStack(driveItems.get(i)));
        }


        for (int i = 0 ; i < 15 ; i++) {
            int idx = i + (rowOffest * 5);
            if (idx < filtered.size()) {
                ResourceLocation rl = ResourceLocation.parse(filtered.get(idx));
                Item it = BuiltInRegistries.ITEM.get(rl).get().value();
                itemM.getHandler().setStackInSlot(i + 2, new ItemStack(it));
            } else {
                itemM.getHandler().setStackInSlot(i + 2, ItemStack.EMPTY);
            }
        }

        DataFluxPair df = FluxDataFixerUpper.getDataFluxFromStack(in);
        if (!DataFluxPair.isValid(df)) {
            resetCraft();
            setWorking(level, pos, state, false);
            return;
        }

        maxProgress = ProcessingCurves.timeTicks(df.data());
        int toConsume = ProcessingCurves.powerPerTick(df.flux());

        boolean canPay = energyM.getHandler().extractEnergy(toConsume, true) == toConsume;
        boolean canOut = diskData != null && diskData.dataUsed() + df.data() <= diskData.capacity() && df.data() <= diskData.maxSizePerItem() && !(driveItems.contains(in.getItem()));
        boolean hasInput = !in.isEmpty();
        boolean working = canPay && canOut && hasInput;

        setWorking(level, pos, state, working);
        if (!working) {
            if (progress > 0) progress = Math.max(0, progress - 1);
            powerConsumption = 0; // Fixes people thinking the machine is using power even when it's not.
            return;
        }

        powerConsumption = toConsume;
        progress++;
        energyM.getHandler().extractEnergy(powerConsumption, false);

        level.playSound(null, pos, ModSounds.QUANTUM_DESTABILIZER_WORK.value(),
                SoundSource.BLOCKS, 1f, 1f + (float) progress / (float) maxProgress);


        if (progress >= maxProgress) {
            DriveItem.addItem(disk, in, df);
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

    public void setFilter(String newFilter) {
        String f = newFilter == null ? "" : newFilter.trim().toLowerCase(Locale.ROOT);
        if (!Objects.equals(filter, f)) {
            filter = f;
            rebuildFiltered();
            markDirtyAndUpdate();
        }
    }

    public void rebuildFiltered() {
        if (filter.isEmpty()) {
            filtered = new ArrayList<>(items);
        } else {
            filtered = new ArrayList<>();
            for (String s: items) {
                if (s == null) continue;
                String lc = s.toLowerCase(Locale.ROOT);
                if (lc.contains(filter)) filtered.add(s);
                else {
                    try {
                        ResourceLocation rl = ResourceLocation.parse(s);
                        Optional<Holder.Reference<Item>> opt = BuiltInRegistries.ITEM.get(rl);
                        if (opt.isPresent()) {
                            Item it = opt.get().value();
                            String name = it.getDescriptionId().toLowerCase(Locale.ROOT);
                            if (name.contains(filter)) filtered.add(s);
                        }
                    } catch (Exception ignored) {}
                }
            }

            int rows = (int) Math.ceil(filtered.size() / 5.0);
            if (rowOffest >= Math.max(0, rows - 3)) {
                rowOffest = Math.max(0, rows - 3);
            }
        }
    }

    public void onMenuOpened() {
        viewers++;
    }

    public void onMenuClosed() {
        viewers = Math.max(0, viewers - 1);
        if (viewers == 0) {
            resetFilterAndScroll();
        }
    }

    public void resetFilterAndScroll() {
        filter = "";
        rowOffest = 0;
        rebuildFiltered();
        for (int i = 0; i < 15; i++) {
            int idx = i + (rowOffest * 5);
            if (idx < filtered.size()) {
                var rl = ResourceLocation.parse(filtered.get(idx));
                var it = BuiltInRegistries.ITEM.get(rl).get().value();
                itemM.getHandler().setStackInSlot(i + 2, new ItemStack(it));
            } else {
                itemM.getHandler().setStackInSlot(i + 2, ItemStack.EMPTY);
            }
        }
        markDirtyAndUpdate();
    }
}
