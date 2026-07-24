package com.rcx.absolutekinematics.datagen;

import java.util.concurrent.CompletableFuture;

import com.rcx.absolutekinematics.KinematicsRegistry;
import com.simibubi.create.AllBlocks;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

public class KinematicsRecipes extends RecipeProvider implements IConditionBuilder {

	public KinematicsRecipes(PackOutput gen, CompletableFuture<HolderLookup.Provider> registries) {
		super(gen, registries);
	}

	@Override
	public void buildRecipes(RecipeOutput recipeOutput) {
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, KinematicsRegistry.HINGE.get(), 1)
		.pattern(" A ")
		.pattern(" B ")
		.pattern(" C ")
		.define('A', ItemTags.WOODEN_SLABS)
		.define('B', AllBlocks.GEARBOX.get())
		.define('C', AllBlocks.INDUSTRIAL_IRON_BLOCK.get())
		.unlockedBy("has_ingredient", has(AllBlocks.ANDESITE_CASING.get()))
		.save(recipeOutput);

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, KinematicsRegistry.BALL_JOINT.get(), 1)
		.pattern(" A ")
		.pattern(" B ")
		.pattern(" C ")
		.define('A', ItemTags.WOODEN_SLABS)
		.define('B', KinematicsItemTags.BRASS_INGOT)
		.define('C', AllBlocks.INDUSTRIAL_IRON_BLOCK.get())
		.unlockedBy("has_ingredient", has(AllBlocks.ANDESITE_CASING.get()))
		.save(recipeOutput);
	}
}
