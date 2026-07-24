package com.rcx.absolutekinematics.blockentity;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiPredicate;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import com.rcx.absolutekinematics.KinematicsRegistry;
import com.rcx.absolutekinematics.block.HingeBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.PhysicsPipeline;
import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import dev.ryanhcode.sable.api.physics.constraint.FixedConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.FixedConstraintHandle;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import dev.simulated_team.simulated.config.server.physics.SimPhysics;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlockEntity.LockingSetting;
import dev.simulated_team.simulated.service.SimConfigService;
import dev.simulated_team.simulated.util.SimLevelUtil;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class BallJointBlockEntity extends BaseJointBlockEntity {

	/**
	 * The current constraint handle between this joint and the attached sub-level
	 */
	@Nullable
	public FixedConstraintHandle lockHandle;
	/**
	 * The target angle degrees from the last tick
	 */
	public double lastTargetAngleX = 0;
	/**
	 * The current target angle in degrees
	 */
	public double targetAngleX = 0;
	/**
	 * The target angle degrees from the last tick
	 */
	public double lastTargetAngleY = 0;
	/**
	 * The current target angle in degrees
	 */
	public double targetAngleY = 0;
	/**
	 * The target angle degrees from the last tick
	 */
	public double lastTargetAngleZ = 0;
	/**
	 * The current target angle in degrees
	 */
	public double targetAngleZ = 0;

	public BallJointBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state, KinematicsRegistry.BALL_JOINT_PLATE, Set.of(ConstraintJointAxis.LINEAR_X, ConstraintJointAxis.LINEAR_Y, ConstraintJointAxis.LINEAR_Z));
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);

		this.lockedDefaultOption = new ScrollOptionBehaviour<>(LockingSetting.class, SCROLL_OPTION_TITLE, this, new SelectionModeValueBox(this::isValidForOptionPanel));
		this.lockedDefaultOption.value = 2;
		behaviours.add(this.lockedDefaultOption);
	}

	/**
	 * @return if a direction is valid for a selector to be placed on
	 */
	public boolean isValidForOptionPanel(BlockState state, Direction direction) {
		return direction.getAxis() != state.getValue(HingeBlock.FACING).getAxis();
	}

	@Override
	public void tick() {
		super.tick();
		final Level level = this.getLevel();

		if (level.isClientSide) {
			/*if (this.isTooFast()) {
				this.playGrindingEffect();
			}*/
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

			/*if (attached != null && this.getPlatePos() != null) {
				this.setTargetAngleFromCurrentOrientation();
			}*/

			if (this.isAssembled()) {
				this.lockHandle = addLockConstraint((ServerSubLevel) Sable.HELPER.getContaining(this), attached);
			}
		} else if (!shouldLock && this.isLocking()) {
			this.level.setBlockAndUpdate(this.getBlockPos(), this.getBlockState().setValue(BlockStateProperties.POWERED, false));
			removeLock();

			if (this.handle != null) {
				//update our constraint
				this.reattachConstraint(attached, false);
			}
		}

		// update our target angles
		this.lastTargetAngleX = this.targetAngleX;
		this.lastTargetAngleY = this.targetAngleY;
		this.lastTargetAngleZ = this.targetAngleZ;
		//float angularSpeed = convertToAngular(this.limitAxleSpeed(this.sideAxle.getSpeed()));

		boolean shouldUpdateAngle = this.isAssembled();

		/*if (this.sequencedAngleLimit >= 0) {
			angularSpeed = (float) Mth.clamp(angularSpeed, -this.sequencedAngleLimit, this.sequencedAngleLimit);
			this.sequencedAngleLimit = Math.max(0, this.sequencedAngleLimit - Math.abs(angularSpeed));
		} else {*/
		final SubLevelPhysicsSystem physicsSystem = SubLevelPhysicsSystem.get(this.level);
		// if rotation is not sequenced (go to a set angle) and physics is paused, do not update target angle
		if (physicsSystem == null || physicsSystem.getPaused()) {
			shouldUpdateAngle = false;
		}
		//}

		if (shouldUpdateAngle) {
			//this.targetAngleDegrees += angularSpeed;
			this.targetAngleX %= 360;
			this.targetAngleY %= 360;
			this.targetAngleZ %= 360;

			/*if (attached != null && this.isAssembled() && this.handle != null) {
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
			}*/
		}
	}

	public FixedConstraintHandle addLockConstraint(ServerSubLevel container, ServerSubLevel subLevel) {
		final PhysicsPipeline pipeline = SubLevelContainer.getContainer((ServerLevel) this.getLevel()).physicsSystem().getPipeline();

		final FixedConstraintHandle handle = pipeline.addConstraint(container, subLevel, new FixedConstraintConfiguration(
				JOMLConversion.toJOML(this.getBlockPos().getCenter()),
				JOMLConversion.toJOML(this.getPlatePos().getCenter()),
				subLevel == null ? container.logicalPose().orientation() : subLevel.logicalPose().orientation()
				));
		return handle;
	}

	public void removeLock() {
		if (this.lockHandle != null) {
			this.lockHandle.remove();
			this.lockHandle = null;
		}
	}

	public double getCurrentAngle(Axis axis) {
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

		Direction dir = Direction.get(AxisDirection.POSITIVE, axis);

		double d = new Vec3(dir.step()).dot(new Vec3(localB.x(), localB.y(), localB.z()));
		double currentAngle = -2.0 * (float) Math.toDegrees(Math.atan2(-d, localB.w()));
		return currentAngle;
	}


	public void validateConstraintHandle() {
		super.validateConstraintHandle();
		if (this.lockHandle != null && !this.lockHandle.isValid()) {
			this.lockHandle = null;
		}
	}

	@Override
	public void checkPersistence(UUID id) {
		if (this.getPlatePos() != null && SimLevelUtil.isAreaActuallyLoaded(this.getLevel(), this.getPlatePos(), 1)) {
			if (!this.getLevel().getBlockState(this.getPlatePos()).is(plateBlock)) {
				return;
			}
		}

		final SubLevel subLevel = SubLevelContainer.getContainer(this.getLevel()).getSubLevel(id);
		this.validateConstraintHandle();

		if (this.handle == null) {
			this.reattachConstraint((ServerSubLevel) subLevel, true);
		}
		if (this.lockHandle == null && this.jointPlatePos != null && this.isAssembled() && this.isLocking()) {
			this.lockHandle = addLockConstraint((ServerSubLevel) Sable.HELPER.getContaining(this), (ServerSubLevel) subLevel);
		}
	}

	@Override
	public void assemble() {
		super.assemble();
		if (this.isLocking()) {
			this.lockHandle = addLockConstraint((ServerSubLevel) Sable.HELPER.getContaining(this), (ServerSubLevel) this.getAttachedSubLevel());
		}
	}

	/**
	 * Updates the target angle to reflect the current orientation of the containing and attached sub-levels.
	 * Called when the hinge starts locking, as to keep the angle it is currently at.
	 *
	 * @param attached the attached sublevel
	 */
	public void setTargetAngleFromCurrentOrientation() {
		this.targetAngleX = getCurrentAngle(Axis.X);
		this.lastTargetAngleX = this.targetAngleX;
		this.targetAngleY = getCurrentAngle(Axis.Y);
		this.lastTargetAngleY = this.targetAngleY;
		this.targetAngleZ = getCurrentAngle(Axis.Z);
		this.lastTargetAngleZ = this.targetAngleZ;
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
			this.handle.setMotor(ConstraintJointAxis.ANGULAR_X, 0.0, 0.0, config.swivelBearingFriction.get(), false, 0.0);
			this.handle.setMotor(ConstraintJointAxis.ANGULAR_Y, 0.0, 0.0, config.swivelBearingFriction.get(), false, 0.0);
			this.handle.setMotor(ConstraintJointAxis.ANGULAR_Z, 0.0, 0.0, config.swivelBearingFriction.get(), false, 0.0);
			return;
		}

		/*final SubLevel subLevelA = this.getContainingSubLevel();
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
		final float goalX = AngleHelper.rad(AngleHelper.angleLerp(physicsSystem.getPartialPhysicsTick(), this.lastTargetAngleX, this.targetAngleX));
		final float goalY = AngleHelper.rad(AngleHelper.angleLerp(physicsSystem.getPartialPhysicsTick(), this.lastTargetAngleY, this.targetAngleY));
		final float goalZ = AngleHelper.rad(AngleHelper.angleLerp(physicsSystem.getPartialPhysicsTick(), this.lastTargetAngleZ, this.targetAngleZ));

		this.handle.setMotor(ConstraintJointAxis.ANGULAR_X, goalX, kP, kD, false, 0.0);
		this.handle.setMotor(ConstraintJointAxis.ANGULAR_Y, goalY, kP, kD, false, 0.0);
		this.handle.setMotor(ConstraintJointAxis.ANGULAR_Z, goalZ, kP, kD, false, 0.0);
		this.handle.setContactsEnabled(false);*/
	}

	@Override
	public void disassemble() {
		this.removeLock();
		this.targetAngleX = 0;
		this.targetAngleY = 0;
		this.targetAngleZ = 0;
		super.disassemble();
	}

	public static class SelectionModeValueBox extends CenteredSideValueBoxTransform {
		public SelectionModeValueBox(BiPredicate<BlockState, Direction> allowedDirections) {
			super(allowedDirections);
		}

		@Override
		public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
			return super.getLocalOffset(level, pos, state)
					.subtract(Vec3.atLowerCornerOf(state.getValue(BlockStateProperties.FACING).getNormal())
							.scale(2 / 16f));
		}

		@Override
		protected Vec3 getSouthLocation() {
			return VecHelper.voxelSpace(8, 8, 14.5);
		}
	}

	@Override
	BlockState copyBlockStatePropertiesToPlate(BlockState plate) {
		return plate.setValue(BlockStateProperties.FACING, this.getBlockState().getValue(BlockStateProperties.FACING));
	}

	@Override
	Quaterniond getBaseRotationAxis(BlockState state) {
		Vec3i normal = state.getValue(BlockStateProperties.FACING).getNormal();
		return new Quaterniond(Direction.getNearest(Math.abs(normal.getX()), Math.abs(normal.getY()), Math.abs(normal.getZ())).getRotation());
	}

	/*Quaterniond getBaseRotationAxis(BlockState state, Axis axis) {
		Vec3i normal = state.getValue(BlockStateProperties.FACING).getNormal();
		switch (axis) {
		case X:
			return new Quaterniond(Direction.getNearest(normal.getY(), normal.getX(), normal.getZ()).getRotation());
		case Z:
			return new Quaterniond(Direction.getNearest(normal.getX(), normal.getZ(), normal.getY()).getRotation());
		case Y:
		default:
			return new Quaterniond(Direction.getNearest(normal.getX(), normal.getY(), normal.getZ()).getRotation());
		}
	}*/

	@Override
	public void attachConstraints(@Nullable ServerSubLevel plateSubLevel, Vector3d attachPos) {
		super.attachConstraints(plateSubLevel, attachPos);
		if (this.handle != null) {
			double limit = Math.PI / 2.0;
			this.handle.setLimit(ConstraintJointAxis.ANGULAR_X, -limit, limit);
			this.handle.setLimit(ConstraintJointAxis.ANGULAR_Z, -limit, limit);
		}
	}
}	
