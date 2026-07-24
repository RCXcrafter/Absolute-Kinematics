package com.rcx.absolutekinematics.block;

import com.rcx.absolutekinematics.KinematicsRegistry;
import com.rcx.absolutekinematics.Util;
import com.rcx.absolutekinematics.blockentity.BallJointBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BallJointBlock extends BaseJointBlock<BallJointBlockEntity> {

	public static VoxelShape BASE_AABB = Shapes.or(Block.box(4,0,4,12,4,12), Block.box(6,4,1,10,8,3), Block.box(13,4,6,15,8,10), Block.box(6,4,13,10,8,15), Block.box(1,4,6,3,8,10), Block.box(3,4,3,13,8,13), Block.box(5,3,3,11,5,13), Block.box(3,3,5,13,5,11));
	public static VoxelShape BASE_COLLISION_AABB = Shapes.or(Block.box(5,0,6,11,4,10), Block.box(6,0,5,10,4,11), Block.box(4,6,4,12,10,12), Block.box(5,10,5,11,12,11), Block.box(5,4,5,11,6,11), Block.box(6,12,6,10,13,10));
	public static VoxelShape PLATE_AABB = Shapes.or(Block.box(2,12,14,14,16,16), Block.box(2,14,2,14,16,14), Block.box(6,12,6,10,14,10), Block.box(2,12,0,14,16,2), Block.box(14,12,0,16,16,16), Block.box(0,12,0,2,16,16), Block.box(4,6,4,12,10,12), Block.box(5,10,5,11,12,11), Block.box(5,4,5,11,6,11), Block.box(6,3,6,10,4,10));
	public static VoxelShape PLATE_COLLISION_AABB = Shapes.or(Block.box(2,12,14,14,16,16), Block.box(2,14,2,14,16,14), Block.box(2,12,0,14,16,2), Block.box(14,12,0,16,16,16), Block.box(0,12,0,2,16,16));

	public static VoxelShape FULL_AABB = Shapes.or(BASE_AABB, PLATE_AABB);

	public static VoxelShape[] BASE_SHAPES = Util.get6directionShapes(BASE_AABB);
	public static VoxelShape[] BASE_COLLISION = Util.get6directionShapes(BASE_COLLISION_AABB);
	public static VoxelShape[] PLATE_SHAPES = Util.get6directionShapes(PLATE_AABB);
	public static VoxelShape[] PLATE_COLLISION = Util.get6directionShapes(PLATE_COLLISION_AABB);
	public static VoxelShape[] FULL_SHAPES = Util.get6directionShapes(FULL_AABB);

	public BallJointBlock(Properties properties) {
		super(properties, true);
	}

	@Override
	public Class<BallJointBlockEntity> getBlockEntityClass() {
		return BallJointBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends BallJointBlockEntity> getBlockEntityType() {
		return KinematicsRegistry.BALL_JOINT_ENTITY.get();
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		Direction face = blockState.getValue(FACING);
		return blockState.getValue(ASSEMBLED) ? BASE_COLLISION[face.get3DDataValue()] : FULL_SHAPES[face.get3DDataValue()];
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		Direction face = blockState.getValue(FACING);
		return blockState.getValue(ASSEMBLED) ? BASE_SHAPES[face.get3DDataValue()] : FULL_SHAPES[face.get3DDataValue()];
	}
}
