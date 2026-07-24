package com.rcx.absolutekinematics.blockentity;

import com.rcx.absolutekinematics.KinematicsRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BallJointPlateBlockEntity extends BaseJointPlateBlockEntity {

	public BallJointPlateBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state, KinematicsRegistry.BALL_JOINT);
	}
}
