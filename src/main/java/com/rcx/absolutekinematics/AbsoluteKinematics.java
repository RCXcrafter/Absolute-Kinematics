package com.rcx.absolutekinematics;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.DoubleSupplier;

import com.rcx.absolutekinematics.datagen.KinematicsBlockStates;
import com.rcx.absolutekinematics.datagen.KinematicsBlockTags;
import com.rcx.absolutekinematics.datagen.KinematicsItemTags;
import com.rcx.absolutekinematics.datagen.KinematicsLang;
import com.rcx.absolutekinematics.datagen.KinematicsLootTables.KinematicsLootTableProvider;
import com.rcx.absolutekinematics.datagen.KinematicsRecipes;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;

import dev.simulated_team.simulated.index.SimBlockMovementChecks;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredItem;

@Mod(AbsoluteKinematics.MODID)
public class AbsoluteKinematics {

	public static final String MODID = "absolute_kinematics";

	public AbsoluteKinematics(IEventBus modEventBus, ModContainer modContainer) {
		modEventBus.addListener(this::commonSetup);
		modEventBus.addListener(this::gatherData);

		KinematicsRegistry.BLOCKS.register(modEventBus);
		KinematicsRegistry.ITEMS.register(modEventBus);
		KinematicsRegistry.BLOCK_ENTITIES.register(modEventBus);	

		//modContainer.registerConfig(ModConfig.Type.COMMON, KinematicsConfig.SPEC);

		BlockStressValues.IMPACTS.registerProvider(impacts::get);
	}

	public HashMap<Block, DoubleSupplier> impacts = new HashMap<Block, DoubleSupplier>();

	public void commonSetup(FMLCommonSetupEvent event) {
		addToSection(KINEMATICS_SECTION, KinematicsRegistry.HINGE_ITEM);
		addToSection(KINEMATICS_SECTION, KinematicsRegistry.BALL_JOINT_ITEM);

		addStressImpact(KinematicsRegistry.HINGE.get(), 4.0);

		SimBlockMovementChecks.registerAdditionalBlocks(new DirectionalAdditionalBlocks(KinematicsRegistry.HINGE.get()));
		SimBlockMovementChecks.registerAdditionalBlocks(new DirectionalAdditionalBlocks(KinematicsRegistry.BALL_JOINT.get()));
	}

	public void addStressImpact(Block block, double impact) {
		impacts.put(block, () -> impact);
		TooltipModifier.REGISTRY.register(block.asItem(), KineticStats.create(block.asItem()));
	}

	public static final ResourceLocation KINEMATICS_SECTION = ResourceLocation.fromNamespaceAndPath(MODID, MODID);
	public static void addToSection(ResourceLocation sectionId, DeferredItem<? extends Item> item) {
		SimulatedRegistrate.TAB_ITEMS.add(() -> item.get());
		SimulatedRegistrate.ITEM_TO_SECTION.put(item.getId(), sectionId);
	}

	public void gatherData(GatherDataEvent event) {
		DataGenerator gen = event.getGenerator();
		PackOutput output = gen.getPackOutput();
		ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
		CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

		if (event.includeClient()) {
			gen.addProvider(true, new KinematicsLang(output));
			gen.addProvider(true, new KinematicsBlockStates(output, existingFileHelper));
		} if (event.includeServer()) {
			gen.addProvider(true, new KinematicsLootTableProvider(output, lookupProvider));
			gen.addProvider(true, new KinematicsRecipes(output, lookupProvider));
			BlockTagsProvider blockTags = new KinematicsBlockTags(output, lookupProvider, existingFileHelper);
			gen.addProvider(true, blockTags);
			gen.addProvider(true, new KinematicsItemTags(output, lookupProvider, blockTags.contentsGetter(), existingFileHelper));
		}
	}
}
