package com.rcx.absolutekinematics.block;

import com.rcx.absolutekinematics.KinematicsRegistry;
import com.rcx.absolutekinematics.Util;
import com.rcx.absolutekinematics.blockentity.HingeBlockEntity;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import dev.simulated_team.simulated.util.extra_kinetics.ExtraKinetics;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HingeBlock extends BaseJointBlock<HingeBlockEntity> implements ExtraKinetics.ExtraKineticsBlock {

	public static VoxelShape BASE_AABB = Shapes.or(Block.box(0,0,2,16,2,14), Block.box(1,5,4,3,12,12), Block.box(0,2,4,3,5,12), Block.box(13,5,4,15,12,12), Block.box(13,2,4,16,5,12), Block.box(0,5,5,1,11,11), Block.box(15,5,5,16,11,11));
	public static VoxelShape LEAF_AABB = Shapes.or(Block.box(0,14,0,16,16,16), Block.box(3,4,4,13,14,12));
	public static VoxelShape LEAF_COLLISION_AABB = Shapes.or(Block.box(0,14,0,16,16,16), Block.box(3.1,4,4,12.9,14,12));
	public static VoxelShape JOINT_AABB = Block.box(5,2,5,11,14,11);
	public static VoxelShape FULL_AABB = Shapes.or(BASE_AABB, JOINT_AABB, LEAF_AABB);

	public static VoxelShape[][] BASE_SHAPES = new VoxelShape[][] {
		Util.get6directionShapes(BASE_AABB),
		Util.get6directionShapes(Util.rotateVoxelShape(Direction.NORTH, Direction.EAST, BASE_AABB))
	};

	public static VoxelShape[][] LEAF_SHAPES = new VoxelShape[][] {
		Util.get6directionShapes(LEAF_AABB),
		Util.get6directionShapes(Util.rotateVoxelShape(Direction.NORTH, Direction.EAST, LEAF_AABB))
	};
	public static VoxelShape[][] LEAF_COLLISION = new VoxelShape[][] {
		Util.get6directionShapes(LEAF_COLLISION_AABB),
		Util.get6directionShapes(Util.rotateVoxelShape(Direction.NORTH, Direction.EAST, LEAF_COLLISION_AABB))
	};
	public static VoxelShape[][] FULL_SHAPES = new VoxelShape[][] {
		Util.get6directionShapes(FULL_AABB),
		Util.get6directionShapes(Util.rotateVoxelShape(Direction.NORTH, Direction.EAST, FULL_AABB))
	};

	public HingeBlock(Properties properties) {
		super(properties, true);
		this.registerDefaultState(this.defaultBlockState().setValue(BlockStateProperties.AXIS, Axis.X));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(BlockStateProperties.AXIS));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		Direction[] directions = pContext.getNearestLookingDirections();
		for (Direction direction : directions) {
			BlockState blockstate = this.defaultBlockState().setValue(FACING, direction.getOpposite());
			Axis axis = Axis.Z;
			for (Direction facing : directions) {
				if (facing.getAxis() != direction.getAxis()) {
					axis = Util.getOtherAxis(direction.getAxis(), facing.getAxis());
					break;
				}
			}
			return blockstate.setValue(BlockStateProperties.AXIS, axis);
		}
		return this.defaultBlockState();
	}

	//TODO: disallow rotating this wrong
	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState rotated = this.getRotatedBlockState(state, context.getClickedFace());
		if (!rotated.canSurvive(level, context.getClickedPos()))
			return InteractionResult.PASS;

		if (!level.isClientSide) {
			this.withBlockEntityDo(level, pos, HingeBlockEntity::disassemble);
		}

		// blockstate could have changed from disassembly
		rotated = this.getRotatedBlockState(level.getBlockState(pos), context.getClickedFace());
		KineticBlockEntity.switchToBlockState(level, pos, this.updateAfterWrenched(rotated, context));

		if (level.getBlockState(pos) != state)
			IWrenchable.playRotateSound(level, pos);

		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
		if (targetedFace.getAxis() == originalState.getValue(BlockStateProperties.FACING).getAxis())
			return originalState.setValue(BlockStateProperties.AXIS,
					VoxelShaper
					.axisAsFace(originalState.getValue(BlockStateProperties.AXIS))
					.getClockWise(targetedFace.getAxis())
					.getAxis());

		if (targetedFace.getAxis() == originalState.getValue(BlockStateProperties.AXIS))
			return originalState.setValue(DirectionalKineticBlock.FACING,
					originalState.getValue(DirectionalKineticBlock.FACING).getClockWise(targetedFace.getAxis()));

		return originalState.setValue(DirectionalKineticBlock.FACING,
				originalState.getValue(DirectionalKineticBlock.FACING).getClockWise(targetedFace.getAxis())).
				setValue(BlockStateProperties.AXIS,
						VoxelShaper
						.axisAsFace(originalState.getValue(BlockStateProperties.AXIS))
						.getClockWise(targetedFace.getAxis())
						.getAxis());
	}

	@Override
	public Class<HingeBlockEntity> getBlockEntityClass() {
		return HingeBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends HingeBlockEntity> getBlockEntityType() {
		return KinematicsRegistry.HINGE_ENTITY.get();
	}

	@Override
	public IRotate getExtraKineticsRotationConfiguration() {
		return HingeBlockEntity.HingeSideAxleBlockEntity.EXTRA_AXLE_CONFIG;
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		Direction face = blockState.getValue(FACING);
		Axis axis = blockState.getValue(BlockStateProperties.AXIS);
		int z = ((axis == Axis.X && face.getAxis() == Axis.Y) || (axis != Axis.Y && face.getAxis() != Axis.Y)) ^ face.getAxis() == Axis.Z ? 0 : 1;
		return blockState.getValue(ASSEMBLED) ? BASE_SHAPES[z][face.get3DDataValue()] : FULL_SHAPES[z][face.get3DDataValue()];
	}
}
