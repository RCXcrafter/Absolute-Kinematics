package com.rcx.absolutekinematics.block;

import com.rcx.absolutekinematics.KinematicsRegistry;
import com.rcx.absolutekinematics.blockentity.HingeLeafBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HingeLeafBlock extends BaseJointPlateBlock<HingeLeafBlockEntity> {

	public HingeLeafBlock(Properties properties) {
		super(properties, true);
		this.registerDefaultState(this.defaultBlockState().setValue(BlockStateProperties.AXIS, Axis.X));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(BlockStateProperties.AXIS));
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
	public Class<HingeLeafBlockEntity> getBlockEntityClass() {
		return HingeLeafBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends HingeLeafBlockEntity> getBlockEntityType() {
		return KinematicsRegistry.HINGE_LEAF_ENTITY.get();
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		Direction face = blockState.getValue(FACING);
		Axis axis = blockState.getValue(BlockStateProperties.AXIS);
		int z = ((axis == Axis.X && face.getAxis() == Axis.Y) || (axis != Axis.Y && face.getAxis() != Axis.Y)) ^ face.getAxis() == Axis.Z ? 0 : 1;
		return HingeBlock.LEAF_COLLISION[z][face.get3DDataValue()];
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		Direction face = blockState.getValue(FACING);
		Axis axis = blockState.getValue(BlockStateProperties.AXIS);
		int z = ((axis == Axis.X && face.getAxis() == Axis.Y) || (axis != Axis.Y && face.getAxis() != Axis.Y)) ^ face.getAxis() == Axis.Z ? 0 : 1;
		return HingeBlock.LEAF_SHAPES[z][face.get3DDataValue()];
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
		return new ItemStack(KinematicsRegistry.HINGE_ITEM.get());
	}
}
