package com.rcx.absolutekinematics.datagen;

import com.rcx.absolutekinematics.AbsoluteKinematics;
import com.rcx.absolutekinematics.KinematicsRegistry;
import com.rcx.absolutekinematics.block.BaseJointBlock;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.ModelFile.ExistingModelFile;
import net.neoforged.neoforge.client.model.generators.loaders.CompositeModelBuilder;
import net.neoforged.neoforge.client.model.generators.loaders.ObjModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class KinematicsBlockStates extends BlockStateProvider {

	public KinematicsBlockStates(PackOutput gen, ExistingFileHelper exFileHelper) {
		super(gen, AbsoluteKinematics.MODID, exFileHelper);
	}

	@Override
	protected void registerStatesAndModels() {
		ExistingModelFile throughShaft = models().getExistingFile(ResourceLocation.fromNamespaceAndPath(AbsoluteKinematics.MODID, "through_shaft"));
		ExistingModelFile baseModelX = models().getExistingFile(ResourceLocation.fromNamespaceAndPath(AbsoluteKinematics.MODID, "hinge_base_x"));
		ExistingModelFile baseModelZ = models().getExistingFile(ResourceLocation.fromNamespaceAndPath(AbsoluteKinematics.MODID, "hinge_base_z"));
		ExistingModelFile leafModelX = models().getExistingFile(ResourceLocation.fromNamespaceAndPath(AbsoluteKinematics.MODID, "hinge_leaf_x"));
		ExistingModelFile leafModelZ = models().getExistingFile(ResourceLocation.fromNamespaceAndPath(AbsoluteKinematics.MODID, "hinge_leaf_z"));

		ModelFile hingeModelX = models().withExistingParent("hinge_x", "block").customLoader(CompositeModelBuilder::begin)
				.child("base", models().nested().parent(baseModelX))
				.child("leaf", models().nested().parent(leafModelX))
				.end().texture("particle", ResourceLocation.fromNamespaceAndPath(AbsoluteKinematics.MODID, "block/hinge_side"));
		ModelFile hingeModelZ = models().withExistingParent("hinge_z", "block").customLoader(CompositeModelBuilder::begin)
				.child("base", models().nested().parent(baseModelZ))
				.child("leaf", models().nested().parent(leafModelZ))
				.end().texture("particle", ResourceLocation.fromNamespaceAndPath(AbsoluteKinematics.MODID, "block/hinge_side"));
		ModelFile hingeItemModel = models().withExistingParent("hinge_item", "block").customLoader(CompositeModelBuilder::begin)
				.child("base", models().nested().parent(baseModelX))
				.child("leaf", models().nested().parent(leafModelX))
				.child("through_shaft", models().nested().parent(throughShaft))
				.child("side_shaft", models().nested().parent(models().getExistingFile(ResourceLocation.fromNamespaceAndPath(AbsoluteKinematics.MODID, "hinge_axle"))))
				.child("cardan_bottom", models().nested().parent(models().getExistingFile(ResourceLocation.fromNamespaceAndPath(AbsoluteKinematics.MODID, "cardan_bottom"))))
				.child("cardan_middle", models().nested().parent(models().getExistingFile(ResourceLocation.fromNamespaceAndPath(AbsoluteKinematics.MODID, "cardan_middle"))))
				.child("cardan_top", models().nested().parent(models().getExistingFile(ResourceLocation.fromNamespaceAndPath(AbsoluteKinematics.MODID, "cardan_top"))))
				.end().texture("particle", ResourceLocation.fromNamespaceAndPath(AbsoluteKinematics.MODID, "block/hinge_side"));

		simpleBlockItem(KinematicsRegistry.HINGE.get(), hingeItemModel);
		getVariantBuilder(KinematicsRegistry.HINGE.get()).forAllStates(state -> {
			Direction face = state.getValue(BlockStateProperties.FACING);
			Axis axis = state.getValue(BlockStateProperties.AXIS);
			Boolean assembled = state.getValue(BaseJointBlock.ASSEMBLED);
			ModelFile modelX = assembled ? baseModelX : hingeModelX;
			ModelFile modelZ = assembled ? baseModelZ : hingeModelZ;
			return ConfiguredModel.builder()
					.modelFile((axis == Axis.X && face.getAxis() == Axis.Y) || (axis != Axis.Y && face.getAxis() != Axis.Y) ? modelX : modelZ)
					.rotationX(face == Direction.DOWN ? 180 : face == Direction.UP ? 0 : 90)
					.rotationY(face == Direction.SOUTH ? 180 : face == Direction.WEST ? 270 : face == Direction.EAST ? 90 : 0)
					.uvLock(false)
					.build();
		});

		getVariantBuilder(KinematicsRegistry.HINGE_LEAF.get()).forAllStates(state -> {
			Direction face = state.getValue(BlockStateProperties.FACING);
			Axis axis = state.getValue(BlockStateProperties.AXIS);
			return ConfiguredModel.builder()
					.modelFile((axis == Axis.X && face.getAxis() == Axis.Y) || (axis != Axis.Y && face.getAxis() != Axis.Y) ? leafModelX : leafModelZ)
					.rotationX(face == Direction.DOWN ? 180 : face == Direction.UP ? 0 : 90)
					.rotationY(face == Direction.SOUTH ? 180 : face == Direction.WEST ? 270 : face == Direction.EAST ? 90 : 0)
					.uvLock(false)
					.build();
		});

		ModelFile ballJointBaseModel = models().withExistingParent("ball_joint_socket", "block")
				.customLoader(ObjModelBuilder::begin).modelLocation(ResourceLocation.fromNamespaceAndPath(AbsoluteKinematics.MODID, "models/block/ball_joint_socket.obj"))
				.flipV(true).end().ao(false)
				.texture("socket_side", ResourceLocation.fromNamespaceAndPath(AbsoluteKinematics.MODID, "block/ball_joint_socket_side"))
				.texture("socket_top", ResourceLocation.fromNamespaceAndPath(AbsoluteKinematics.MODID, "block/ball_joint_socket_top"))
				.texture("side", ResourceLocation.fromNamespaceAndPath(AbsoluteKinematics.MODID, "block/ball_joint_side"))
				.texture("particle", ResourceLocation.fromNamespaceAndPath(AbsoluteKinematics.MODID, "block/ball_joint_side"));
		ModelFile ballJointPlateModel = models().withExistingParent("ball_joint_ball", "block")
				.customLoader(ObjModelBuilder::begin).modelLocation(ResourceLocation.fromNamespaceAndPath(AbsoluteKinematics.MODID, "models/block/ball_joint_ball.obj"))
				.flipV(true).end().ao(false)
				.texture("ball", ResourceLocation.fromNamespaceAndPath(AbsoluteKinematics.MODID, "block/ball_joint_ball"))
				.texture("side", ResourceLocation.fromNamespaceAndPath(AbsoluteKinematics.MODID, "block/ball_joint_side"))
				.texture("top", ResourceLocation.fromNamespaceAndPath(AbsoluteKinematics.MODID, "block/ball_joint_top"))
				.texture("bottom", ResourceLocation.fromNamespaceAndPath(AbsoluteKinematics.MODID, "block/ball_joint_bottom"))
				.texture("particle", ResourceLocation.fromNamespaceAndPath(AbsoluteKinematics.MODID, "block/ball_joint_side"));

		ModelFile ballJointModel = models().withExistingParent("ball_joint", "block").customLoader(CompositeModelBuilder::begin)
				.child("socket", models().nested().parent(ballJointBaseModel))
				.child("ball", models().nested().parent(ballJointPlateModel))
				.end().texture("particle", ResourceLocation.fromNamespaceAndPath(AbsoluteKinematics.MODID, "block/ball_joint_side"));
		ModelFile ballJointItemModel = models().withExistingParent("ball_joint_item", "block").customLoader(CompositeModelBuilder::begin)
				.child("socket", models().nested().parent(ballJointBaseModel))
				.child("ball", models().nested().parent(ballJointPlateModel))
				.child("shaft", models().nested().parent(throughShaft))
				.end().texture("particle", ResourceLocation.fromNamespaceAndPath(AbsoluteKinematics.MODID, "block/ball_joint_side"));

		simpleBlockItem(KinematicsRegistry.BALL_JOINT.get(), ballJointItemModel);
		getVariantBuilder(KinematicsRegistry.BALL_JOINT.get()).forAllStates(state -> {
			Direction face = state.getValue(BlockStateProperties.FACING);
			Boolean assembled = state.getValue(BaseJointBlock.ASSEMBLED);
			return ConfiguredModel.builder()
					.modelFile(assembled ? ballJointBaseModel : ballJointModel)
					.rotationX(face == Direction.DOWN ? 180 : face == Direction.UP ? 0 : 90)
					.rotationY(face == Direction.SOUTH ? 180 : face == Direction.WEST ? 270 : face == Direction.EAST ? 90 : 0)
					.uvLock(false)
					.build();
		});
		getVariantBuilder(KinematicsRegistry.BALL_JOINT_PLATE.get()).forAllStates(state -> {
			Direction face = state.getValue(BlockStateProperties.FACING);
			return ConfiguredModel.builder()
					.modelFile(ballJointPlateModel)
					.rotationX(face == Direction.DOWN ? 180 : face == Direction.UP ? 0 : 90)
					.rotationY(face == Direction.SOUTH ? 180 : face == Direction.WEST ? 270 : face == Direction.EAST ? 90 : 0)
					.uvLock(false)
					.build();
		});
	}
}
