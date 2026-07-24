package com.rcx.absolutekinematics.datagen;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.rcx.absolutekinematics.KinematicsRegistry;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.registries.DeferredHolder;

public class KinematicsLootTables extends BlockLootSubProvider {

	public KinematicsLootTables(HolderLookup.Provider registries) {
		super(Set.of(), FeatureFlags.VANILLA_SET, registries);
	}

	@Override
	protected Iterable<Block> getKnownBlocks() {
		ArrayList<Block> blocks = new ArrayList<Block>();
		for (DeferredHolder<Block, ? extends Block> block : KinematicsRegistry.BLOCKS.getEntries()) {
			blocks.add(block.get());
		}
		return blocks;
	}

	@Override
	protected void generate() {
		dropSelf(KinematicsRegistry.HINGE.get());
		this.add(KinematicsRegistry.HINGE_LEAF.get(), noDrop());
		dropSelf(KinematicsRegistry.BALL_JOINT.get());
		this.add(KinematicsRegistry.BALL_JOINT_PLATE.get(), noDrop());
	}

	public static class KinematicsLootTableProvider extends LootTableProvider {
		public KinematicsLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
			super(output, Set.of(), List.of(
					new SubProviderEntry(KinematicsLootTables::new, LootContextParamSets.BLOCK)
					), lookupProvider);
		}
	}
}
