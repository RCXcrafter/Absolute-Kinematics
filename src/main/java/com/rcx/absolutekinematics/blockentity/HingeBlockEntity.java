package com.rcx.absolutekinematics.blockentity;

import static net.minecraft.ChatFormatting.GOLD;

import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import com.rcx.absolutekinematics.KinematicsRegistry;
import com.rcx.absolutekinematics.Util;
import com.rcx.absolutekinematics.block.HingeBlock;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.AbstractShaftBlock;
import com.simibubi.create.content.kinetics.transmission.sequencer.SequencerInstructions;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.item.TooltipHelper;

import dev.ryanhcode.sable.api.physics.PhysicsPipeline;
import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import dev.simulated_team.simulated.config.server.physics.SimPhysics;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlockEntity.LockingSetting;
import dev.simulated_team.simulated.service.SimConfigService;
import dev.simulated_team.simulated.util.extra_kinetics.ExtraBlockPos;
import dev.simulated_team.simulated.util.extra_kinetics.ExtraKinetics;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class HingeBlockEntity extends BaseJointBlockEntity implements ExtraKinetics {

	@NotNull
	public final HingeSideAxleBlockEntity sideAxle;
	/**
	 * The target angle degrees from the last tick
	 */
	public double lastTargetAngleDegrees = 0;
	/**
	 * The current target angle in degrees
	 */
	public double targetAngleDegrees = 0;
	/**
	 * The angle limit from sequenced contexts
	 */
	public double sequencedAngleLimit = -1;

	public HingeBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state, KinematicsRegistry.HINGE_LEAF, Set.of(ConstraintJointAxis.LINEAR_X, ConstraintJointAxis.LINEAR_Y, ConstraintJointAxis.LINEAR_Z, ConstraintJointAxis.ANGULAR_X, ConstraintJointAxis.ANGULAR_Z));

		this.sideAxle = new HingeSideAxleBlockEntity(typeIn, pos, state, this);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);

		this.lockedDefaultOption = new ScrollOptionBehaviour<>(LockingSetting.class, SCROLL_OPTION_TITLE, this, new SelectionModeValueBox(this::isValidForOptionPanel));
		this.lockedDefaultOption.value = 1;
		behaviours.add(this.lockedDefaultOption);
	}

	/**
	 * @return if a direction is valid for a selector to be placed on
	 */
	public boolean isValidForOptionPanel(BlockState state, Direction direction) {
		return direction.getAxis() != state.getValue(BlockStateProperties.FACING).getAxis() && direction.getAxis() == state.getValue(BlockStateProperties.AXIS);
	}

	@Override
	public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		if (super.addToTooltip(tooltip, isPlayerSneaking))
			return true;

		if (isPlayerSneaking)
			return false;

		if (this.sideAxle.getSpeed() == 0)
			return false;

		if (this.isAssembled()) {
			if (this.isTooFast()) {
				Util.translate("hinge.too_fast")
				.style(GOLD)
				.forGoggles(tooltip);

				final MutableComponent component = Util.translate("hinge.too_fast_error")
						.component();

				final List<Component> cutString = TooltipHelper.cutTextComponent(component, FontHelper.Palette.GRAY_AND_WHITE);
				tooltip.addAll(cutString);

				return true;
			}

			return false;
		}
		final BlockState state = this.getBlockState();
		if (!(state.getBlock() instanceof HingeBlock))
			return false;

		final BlockState attachedState = this.level.getBlockState(this.worldPosition.relative(state.getValue(BlockStateProperties.FACING)));
		if (attachedState.canBeReplaced())
			return false;
		TooltipHelper.addHint(tooltip, "hint.empty_bearing");
		return true;
	}

	public boolean isTooFast() {
		final float maxSwivelRPM = SimConfigService.INSTANCE.server().blocks.maxSwivelBearingSpeed.getF();
		return Math.abs(this.sideAxle.getSpeed()) > maxSwivelRPM;
	}

	public float limitAxleSpeed(float speed) {
		final float maxSwivelRPM = SimConfigService.INSTANCE.server().blocks.maxSwivelBearingSpeed.getF();
		return Mth.clamp(speed, -maxSwivelRPM, maxSwivelRPM);
	}

	@Override
	public void tick() {
		super.tick();
		final Level level = this.getLevel();

		this.sideAxle.tick();

		if (level.isClientSide) {
			if (this.isTooFast()) {
				this.playGrindingEffect();
			}

			return;
		}

		final ServerSubLevel attached = (ServerSubLevel) this.getAttachedSubLevel();

		// update our powered state and reattach constraints
		final int bestSignal = this.level.getBestNeighborSignal(this.getBlockPos());
		final boolean shouldLock = this.lockedDefaultOption.get().shouldLock(bestSignal);

		if (shouldLock && !this.isLocking()) {
			this.level.setBlockAndUpdate(this.getBlockPos(), this.getBlockState().setValue(BlockStateProperties.POWERED, true));

			if (this.handle != null) {
				//update our constraint
				this.reattachConstraint(attached, false);
			}

			if (attached != null && this.getPlatePos() != null) {
				this.setTargetAngleFromCurrentOrientation();
			}
		} else if (!shouldLock && this.isLocking()) {
			this.level.setBlockAndUpdate(this.getBlockPos(), this.getBlockState().setValue(BlockStateProperties.POWERED, false));

			if (this.handle != null) {
				//update our constraint
				this.reattachConstraint(attached, false);
			}
		}

		// update our target angles
		this.lastTargetAngleDegrees = this.targetAngleDegrees;
		float angularSpeed = convertToAngular(this.limitAxleSpeed(this.sideAxle.getSpeed()));

		boolean shouldUpdateAngle = this.isAssembled();

		if (this.sequencedAngleLimit >= 0) {
			angularSpeed = (float) Mth.clamp(angularSpeed, -this.sequencedAngleLimit, this.sequencedAngleLimit);
			this.sequencedAngleLimit = Math.max(0, this.sequencedAngleLimit - Math.abs(angularSpeed));
		} else {
			final SubLevelPhysicsSystem physicsSystem = SubLevelPhysicsSystem.get(this.level);
			// if rotation is not sequenced (go to a set angle) and physics is paused, do not update target angle
			if (physicsSystem == null || physicsSystem.getPaused()) {
				shouldUpdateAngle = false;
			}
		}

		if (shouldUpdateAngle) {
			this.targetAngleDegrees += angularSpeed;
			this.targetAngleDegrees = Math.clamp(this.targetAngleDegrees % 360, -90, 90);

			if (attached != null && this.isAssembled() && this.handle != null) {
				final SubLevel containing = this.getContainingSubLevel();

				if (angularSpeed != 0.0) {
					final PhysicsPipeline pipeline = ((ServerSubLevelContainer) SubLevelContainer.getContainer(this.level)).physicsSystem().getPipeline();

					if (containing instanceof final ServerSubLevel serverSubLevel) {
						pipeline.wakeUp(serverSubLevel);
					}

					if (attached instanceof final ServerSubLevel serverSubLevel) {
						pipeline.wakeUp(serverSubLevel);
					}
				}
			}
		}
	}

	public double getCurrentAngle() {
		SubLevel attached = this.getAttachedSubLevel();
		assert attached != null : "Attached sub-level is null!";
		BlockState attachedState = this.level.getBlockState(this.getPlatePos());

		if (!attachedState.is(this.plateBlock))
			return 0;

		Quaterniond orientationA = new Quaterniond();
		final Quaterniond blockOrientationA = getBaseRotationAxis(this.getBlockState());
		final Quaterniond blockOrientationB = getBaseRotationAxis(attachedState);
		Quaterniond orientationB = new Quaterniond(attached.logicalPose().orientation());
		SubLevel containing = this.getContainingSubLevel();
		if (containing != null) {
			orientationA.set(containing.logicalPose().orientation());
		}

		Quaterniond localB = new Quaterniond(orientationA).mul(blockOrientationA).conjugate().mul(new Quaterniond(orientationB).mul(blockOrientationB));

		double d = new Vec3(0.0, 1.0, 0.0).dot(new Vec3(localB.x(), localB.y(), localB.z()));
		double currentAngle = -2.0 * (float) Math.toDegrees(Math.atan2(-d, localB.w()));
		return currentAngle;
	}

	/**
	 * Updates the target angle to reflect the current orientation of the containing and attached sub-levels.
	 * Called when the hinge starts locking, as to keep the angle it is currently at.
	 *
	 * @param attached the attached sublevel
	 */
	public void setTargetAngleFromCurrentOrientation() {
		this.targetAngleDegrees = getCurrentAngle();
		this.lastTargetAngleDegrees = this.targetAngleDegrees;
	}

	@Override
	public void updateServoCoefficients() {
		this.validateConstraintHandle();
		if (!this.isAssembled() || this.handle == null) {
			return;
		}

		final SimPhysics config = SimConfigService.INSTANCE.server().physics;

		if (!this.isLocking()) {
			// Passive un-locked damping
			this.handle.setMotor(ConstraintJointAxis.ANGULAR_Y, 0.0, 0.0, config.swivelBearingFriction.get(), false, 0.0);
			return;
		}

		final SubLevel subLevelA = this.getContainingSubLevel();
		final SubLevel subLevelB = this.getAttachedSubLevel();

		final Vec3i facingVec3I = this.getBlockState().getValue(BlockStateProperties.FACING).getNormal();
		final Vector3dc facingVec = new Vector3d(facingVec3I.getX(), facingVec3I.getY(), facingVec3I.getZ());

		double inertiaA = Double.MAX_VALUE;
		double inertiaB = Double.MAX_VALUE;
		final Vector3d temp = new Vector3d();
		if (subLevelA instanceof final ServerSubLevel serverSubLevel) {
			inertiaA = serverSubLevel.getMassTracker().getInertiaTensor().transform(facingVec, temp).dot(facingVec);
		}

		if (subLevelB instanceof final ServerSubLevel serverSubLevel) {
			inertiaB = serverSubLevel.getMassTracker().getInertiaTensor().transform(facingVec, temp).dot(facingVec);
		}

		final double totalInertia = Math.max(10.0,
				subLevelA != null && subLevelB != null ?
						Math.max(inertiaA, inertiaB) : Math.min(inertiaA, inertiaB)
				);

		final SubLevelPhysicsSystem physicsSystem = ((ServerSubLevelContainer) SubLevelContainer.getContainer(this.level)).physicsSystem();

		final double kP = config.swivelBearingStiffness.get() * totalInertia;
		final double kD = config.swivelBearingDamping.get() * totalInertia;
		final float goal = AngleHelper.rad(AngleHelper.angleLerp(physicsSystem.getPartialPhysicsTick(), this.lastTargetAngleDegrees, this.targetAngleDegrees));

		this.handle.setMotor(ConstraintJointAxis.ANGULAR_Y, goal, kP, kD, false, 0.0);
		this.handle.setContactsEnabled(false);
	}

	@Override
	public void disassemble() {
		this.targetAngleDegrees = 0;
		super.disassemble();
	}

	@Override
	public @NotNull KineticBlockEntity getExtraKinetics() {
		return this.sideAxle;
	}

	@Override
	public boolean shouldConnectExtraKinetics() {
		return false;
	}

	@Override
	public String getExtraKineticsSaveName() {
		return "HingeAxle";
	}

	public static class SelectionModeValueBox extends CenteredSideValueBoxTransform {
		public SelectionModeValueBox(BiPredicate<BlockState, Direction> allowedDirections) {
			super(allowedDirections);
		}

		@Override
		public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
			return super.getLocalOffset(level, pos, state)
					.subtract(Vec3.atLowerCornerOf(state.getValue(BlockStateProperties.FACING).getNormal())
							.scale(5 / 16f));
		}

		@Override
		protected Vec3 getSouthLocation() {
			return VecHelper.voxelSpace(8, 8, 15.5);
		}
	}

	public static class HingeSideAxleBlockEntity extends KineticBlockEntity implements ExtraKineticsBlockEntity {
		public static final AbstractShaftBlock EXTRA_AXLE_CONFIG = AllBlocks.SHAFT.get();

		public final HingeBlockEntity parent;

		public HingeSideAxleBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state, HingeBlockEntity parent) {
			super(typeIn, new ExtraBlockPos(pos), state);
			this.parent = parent;
		}

		@Override
		public void onSpeedChanged(float previousSpeed) {
			super.onSpeedChanged(previousSpeed);

			if (this.speed != 0.0 && !this.parent.isAssembled()) {
				this.parent.assembleNextTick = true;
			}

			this.parent.sequencedAngleLimit = -1;

			if (this.sequenceContext != null && this.sequenceContext.instruction() == SequencerInstructions.TURN_ANGLE) {
				this.parent.sequencedAngleLimit = this.sequenceContext.getEffectiveValue(this.getTheoreticalSpeed());
			}
		}

		@Override
		public KineticBlockEntity getParentBlockEntity() {
			return this.parent;
		}

		@Override
		protected void addStressImpactStats(List<Component> tooltip, float stressAtBase) {
			super.addStressImpactStats(tooltip, stressAtBase);
		}

		@Override
		protected boolean canPropagateDiagonally(IRotate block, BlockState state) {
			return false;
		}

		@Override
		public Component getKey() {
			return Util.translate("extra_kinetics.extra_axle").component();
		}
	}

	@Override
	BlockState copyBlockStatePropertiesToPlate(BlockState plate) {
		return plate.setValue(BlockStateProperties.FACING, this.getBlockState().getValue(BlockStateProperties.FACING))
				.setValue(BlockStateProperties.AXIS, this.getBlockState().getValue(BlockStateProperties.AXIS));
	}

	@Override
	Quaterniond getBaseRotationAxis(BlockState state) {
		return new Quaterniond(Direction.fromAxisAndDirection(state.getValue(BlockStateProperties.AXIS), AxisDirection.POSITIVE).getRotation());
	}

	@Override
	public void attachConstraints(@Nullable ServerSubLevel plateSubLevel, Vector3d attachPos) {
		super.attachConstraints(plateSubLevel, attachPos);
		if (this.handle != null) {
			this.handle.setLimit(ConstraintJointAxis.ANGULAR_Y, -Math.PI / 2.0, Math.PI / 2.0);
		}
	}

	@Override
	protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(compound, registries, clientPacket);
		compound.putDouble("TargetAngle", this.targetAngleDegrees);

		if (this.sequencedAngleLimit >= 0)
			compound.putDouble("SequencedAngleLimit", this.sequencedAngleLimit);
	}

	@Override
	protected void read(final CompoundTag compound, final HolderLookup.Provider registries, final boolean clientPacket) {
		super.read(compound, registries, clientPacket);
		this.targetAngleDegrees = compound.getDouble("TargetAngle");
		this.sequencedAngleLimit = compound.contains("SequencedAngleLimit") ? compound.getDouble("SequencedAngleLimit") : -1;
	}
}	
