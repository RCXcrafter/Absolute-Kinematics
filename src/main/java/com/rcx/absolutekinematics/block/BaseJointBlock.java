package com.rcx.absolutekinematics.block;

import com.rcx.absolutekinematics.blockentity.BaseJointBlockEntity;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public abstract class BaseJointBlock<T extends BaseJointBlockEntity> extends DirectionalKineticBlock implements IBE<T>,IRotate,BlockSubLevelAssemblyListener {

	boolean hasThroughShaft;

	public static final BooleanProperty ASSEMBLED = BooleanProperty.create("assembled");

	public BaseJointBlock(Properties properties, boolean hasThroughShaft) {
		super(properties);
		this.hasThroughShaft = hasThroughShaft;
		this.registerDefaultState(this.defaultBlockState().setValue(ASSEMBLED, false).setValue(BlockStateProperties.POWERED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(ASSEMBLED).add(BlockStateProperties.POWERED));
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
		if (!player.mayBuild()) {
			return ItemInteractionResult.FAIL;
		}

		if (player.isShiftKeyDown()) {
			return ItemInteractionResult.FAIL;
		}

		if (player.getItemInHand(interactionHand).isEmpty()) {
			if (level.isClientSide) {
				return ItemInteractionResult.SUCCESS;
			}

			this.withBlockEntityDo(level, blockPos, be -> be.assembleNextTick = true);
			return ItemInteractionResult.SUCCESS;
		}

		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public Direction.Axis getRotationAxis(BlockState blockState) {
		return blockState.getValue(FACING).getAxis();
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		Direction facing = state.getValue(FACING);
		return hasThroughShaft && ((state.getValue(ASSEMBLED) ? face == facing.getOpposite() : face.getAxis() == facing.getAxis()));
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState rotated = this.getRotatedBlockState(state, context.getClickedFace());
		if (!rotated.canSurvive(level, context.getClickedPos()))
			return InteractionResult.PASS;

		if (!level.isClientSide) {
			this.withBlockEntityDo(level, pos, BaseJointBlockEntity::disassemble);
		}

		// blockstate could have changed from disassembly
		rotated = this.getRotatedBlockState(level.getBlockState(pos), context.getClickedFace());
		KineticBlockEntity.switchToBlockState(level, pos, this.updateAfterWrenched(rotated, context));

		if (level.getBlockState(pos) != state)
			IWrenchable.playRotateSound(level, pos);

		return InteractionResult.SUCCESS;
	}

	@Override
	public void beforeMove(ServerLevel originLevel, ServerLevel resultingLevel, BlockState newState, BlockPos oldPos, BlockPos newPos) {
		this.withBlockEntityDo(originLevel, oldPos, BaseJointBlockEntity::beforeAssembly);
	}

	@Override
	public void afterMove(ServerLevel originLevel, ServerLevel resultingLevel, BlockState newState, BlockPos oldPos, BlockPos newPos) {
		this.withBlockEntityDo(resultingLevel, newPos, BaseJointBlockEntity::associatePlateWithParent);
	}
}
