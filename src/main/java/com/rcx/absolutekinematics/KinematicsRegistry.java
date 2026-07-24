package com.rcx.absolutekinematics;

import com.rcx.absolutekinematics.block.BallJointBlock;
import com.rcx.absolutekinematics.block.BallJointPlateBlock;
import com.rcx.absolutekinematics.block.HingeBlock;
import com.rcx.absolutekinematics.block.HingeLeafBlock;
import com.rcx.absolutekinematics.blockentity.BallJointBlockEntity;
import com.rcx.absolutekinematics.blockentity.BallJointPlateBlockEntity;
import com.rcx.absolutekinematics.blockentity.HingeBlockEntity;
import com.rcx.absolutekinematics.blockentity.HingeLeafBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class KinematicsRegistry {

	public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AbsoluteKinematics.MODID);
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AbsoluteKinematics.MODID);
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AbsoluteKinematics.MODID);

	public static final DeferredBlock<Block> HINGE = BLOCKS.registerBlock("hinge", HingeBlock::new, BlockBehaviour.Properties.of().sound(SoundType.NETHERITE_BLOCK).noOcclusion().destroyTime(5f).mapColor(MapColor.STONE));
	public static final DeferredBlock<HingeLeafBlock> HINGE_LEAF = BLOCKS.registerBlock("hinge_leaf", HingeLeafBlock::new, BlockBehaviour.Properties.of().sound(SoundType.NETHERITE_BLOCK).destroyTime(5f).mapColor(MapColor.STONE));
	public static final DeferredBlock<Block> BALL_JOINT = BLOCKS.registerBlock("ball_joint", BallJointBlock::new, BlockBehaviour.Properties.of().sound(SoundType.NETHERITE_BLOCK).noOcclusion().destroyTime(5f).mapColor(MapColor.STONE));
	public static final DeferredBlock<BallJointPlateBlock> BALL_JOINT_PLATE = BLOCKS.registerBlock("ball_joint_plate", BallJointPlateBlock::new, BlockBehaviour.Properties.of().sound(SoundType.NETHERITE_BLOCK).destroyTime(5f).mapColor(MapColor.STONE));

	public static final DeferredItem<BlockItem> HINGE_ITEM = ITEMS.registerSimpleBlockItem("hinge", HINGE);
	public static final DeferredItem<BlockItem> BALL_JOINT_ITEM = ITEMS.registerSimpleBlockItem("ball_joint", BALL_JOINT);

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HingeBlockEntity>> HINGE_ENTITY = BLOCK_ENTITIES.register("hinge", () -> BlockEntityType.Builder.of((pos, state) -> new HingeBlockEntity(KinematicsRegistry.HINGE_ENTITY.get(), pos, state), HINGE.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HingeLeafBlockEntity>> HINGE_LEAF_ENTITY = BLOCK_ENTITIES.register("hinge_leaf", () -> BlockEntityType.Builder.of((pos, state) -> new HingeLeafBlockEntity(KinematicsRegistry.HINGE_LEAF_ENTITY.get(), pos, state), HINGE_LEAF.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BallJointBlockEntity>> BALL_JOINT_ENTITY = BLOCK_ENTITIES.register("ball_joint", () -> BlockEntityType.Builder.of((pos, state) -> new BallJointBlockEntity(KinematicsRegistry.BALL_JOINT_ENTITY.get(), pos, state), BALL_JOINT.get()).build(null));
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BallJointPlateBlockEntity>> BALL_JOINT_PLATE_ENTITY = BLOCK_ENTITIES.register("ball_joint_plate", () -> BlockEntityType.Builder.of((pos, state) -> new BallJointPlateBlockEntity	(KinematicsRegistry.BALL_JOINT_PLATE_ENTITY.get(), pos, state), BALL_JOINT_PLATE.get()).build(null));
}
