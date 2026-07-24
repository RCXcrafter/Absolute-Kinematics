package com.rcx.absolutekinematics.datagen;

import java.util.concurrent.CompletableFuture;

import com.rcx.absolutekinematics.AbsoluteKinematics;
import com.rcx.absolutekinematics.KinematicsRegistry;
import com.simibubi.create.AllTags;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class KinematicsBlockTags extends BlockTagsProvider {

	public KinematicsBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
		super(output, lookupProvider, AbsoluteKinematics.MODID, existingFileHelper);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
				KinematicsRegistry.HINGE.get(),
				KinematicsRegistry.HINGE_LEAF.get(),
				KinematicsRegistry.BALL_JOINT.get(),
				KinematicsRegistry.BALL_JOINT_PLATE.get());

		tag(AllTags.AllBlockTags.NON_MOVABLE.tag).add(
				KinematicsRegistry.HINGE.get(),
				KinematicsRegistry.HINGE_LEAF.get(),
				KinematicsRegistry.BALL_JOINT.get(),
				KinematicsRegistry.BALL_JOINT_PLATE.get());

		tag(AllTags.AllBlockTags.SAFE_NBT.tag).add(
				KinematicsRegistry.HINGE.get(),
				KinematicsRegistry.BALL_JOINT.get());
	}
}
