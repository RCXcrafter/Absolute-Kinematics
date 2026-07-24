package com.rcx.absolutekinematics.block;

import com.rcx.absolutekinematics.KinematicsRegistry;
import com.rcx.absolutekinematics.blockentity.BallJointPlateBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BallJointPlateBlock extends BaseJointPlateBlock<BallJointPlateBlockEntity> {

	public BallJointPlateBlock(Properties properties) {
		super(properties, true);
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return face == state.getValue(FACING);
	}

	@Override
	public Direction.Axis getRotationAxis(BlockState state) {
		return state.getValue(FACING).getAxis();
	}

	@Override
	public Class<BallJointPlateBlockEntity> getBlockEntityClass() {
		return BallJointPlateBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends BallJointPlateBlockEntity> getBlockEntityType() {
		return KinematicsRegistry.BALL_JOINT_PLATE_ENTITY.get();
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return BallJointBlock.PLATE_COLLISION[blockState.getValue(FACING).get3DDataValue()];
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return BallJointBlock.PLATE_SHAPES[blockState.getValue(FACING).get3DDataValue()];
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
		return new ItemStack(KinematicsRegistry.BALL_JOINT_ITEM.get());
	}
}
