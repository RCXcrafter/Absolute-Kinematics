package com.rcx.absolutekinematics.block;

import com.rcx.absolutekinematics.blockentity.BaseJointPlateBlockEntity;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import dev.ryanhcode.sable.api.block.BlockSubLevelAssemblyListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public abstract class BaseJointPlateBlock<T extends BaseJointPlateBlockEntity> extends DirectionalKineticBlock implements IBE<T>, BlockSubLevelAssemblyListener {

	boolean hasThroughShaft;

	public BaseJointPlateBlock(Properties properties, boolean hasThroughShaft) {
		super(properties);
		this.hasThroughShaft = hasThroughShaft;
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return hasThroughShaft && face == state.getValue(FACING);
	}

	@Override
	public void beforeMove(ServerLevel originLevel, ServerLevel resultingLevel, BlockState newState, BlockPos oldPos, BlockPos newPos) {
		this.withBlockEntityDo(originLevel, oldPos, BaseJointPlateBlockEntity::beforeAssembly);
	}

	@Override
	public Direction.Axis getRotationAxis(BlockState state) {
		return state.getValue(FACING).getAxis();
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (!player.mayBuild()) {
			return ItemInteractionResult.FAIL;
		}

		if (player.isShiftKeyDown()) {
			return ItemInteractionResult.FAIL;
		}

		if (player.getItemInHand(hand).isEmpty()) {
			if (level.isClientSide) {
				return ItemInteractionResult.SUCCESS;
			}

			this.withBlockEntityDo(level, pos, BaseJointPlateBlockEntity::setParentAssembleNextTick);
		}

		return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		return InteractionResult.PASS;
	}

	@Override
	public void afterMove(ServerLevel originLevel, ServerLevel resultingLevel, BlockState newState, BlockPos oldPos, BlockPos newPos) {
		this.withBlockEntityDo(resultingLevel, newPos, BaseJointPlateBlockEntity::fixParentLinkingWhenMoved);
	}
}
