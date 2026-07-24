package com.rcx.absolutekinematics.datagen;

import java.util.concurrent.CompletableFuture;

import com.rcx.absolutekinematics.AbsoluteKinematics;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class KinematicsItemTags extends ItemTagsProvider {

	public static final TagKey<Item> BRASS_INGOT = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/brass"));

	public KinematicsItemTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTagProvider, ExistingFileHelper existingFileHelper) {
		super(output, lookupProvider, blockTagProvider, AbsoluteKinematics.MODID, existingFileHelper);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {

	}
}
