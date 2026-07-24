package com.rcx.absolutekinematics.blockentity;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import com.rcx.absolutekinematics.block.BaseJointBlock;
import com.rcx.absolutekinematics.block.BaseJointPlateBlock;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.IDisplayAssemblyExceptions;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.physics.PhysicsPipeline;
import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import dev.ryanhcode.sable.api.physics.constraint.GenericConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.GenericConstraintHandle;
import dev.ryanhcode.sable.api.schematic.SubLevelSchematicSerializationContext;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.plot.LevelPlot;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlockEntity.LockingSetting;
import dev.simulated_team.simulated.index.SimSoundEvents;
import dev.simulated_team.simulated.util.SimAssemblyHelper;
import dev.simulated_team.simulated.util.SimLevelUtil;
import dev.simulated_team.simulated.util.assembly.SimAssemblyException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredBlock;

public abstract class BaseJointBlockEntity extends KineticBlockEntity implements IDisplayAssemblyExceptions, BlockEntitySubLevelActor {

	public static final MutableComponent SCROLL_OPTION_TITLE = Component.translatable(Simulated.MOD_ID + ".scroll_option.swivel_default_locked");

	public final Set<ConstraintJointAxis> lockedAxes;
	public DeferredBlock<? extends Block> plateBlock;

	/**
	 * If the joint should assemble next tick
	 */
	public boolean assembleNextTick;
	protected AssemblyException lastException;
	/**
	 * The ID of the attached sub-level
	 */
	@Nullable
	public UUID subLevelID;
	/**
	 * The block position of the attached {@link BaseJointBlock}
	 */
	@Nullable
	public BlockPos jointPlatePos;
	/**
	 * The current constraint handle between this joint and the attached sub-level
	 */
	@Nullable
	public GenericConstraintHandle handle;
	/**
	 * If this BE is being destroyed as a part of assembly
	 */
	public boolean assembling;
	/**
	 * The locked default scroll option
	 */
	public ScrollOptionBehaviour<LockingSetting> lockedDefaultOption;

	public BaseJointBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state, DeferredBlock<? extends Block> plateBlock, Set<ConstraintJointAxis> lockedAxes) {
		super(typeIn, pos, state);
		this.assembleNextTick = false;
		this.plateBlock = plateBlock;
		this.lockedAxes = lockedAxes;
	}

	@Override
	public void tick() {
		super.tick();
		final Level level = this.getLevel();

		if (level.isClientSide) {
			return;
		}

		// assemble or disassemble
		if (this.assembleNextTick) {
			if (!this.isAssembled()) {
				this.assemble();
			} else {
				this.disassemble();
			}
		}

		// check persistence to make sure we keep our sublevel after reload
		if (this.getSubLevelID() != null) {
			this.checkPersistence(this.getSubLevelID());
		}

		this.assembleNextTick = false;
	}

	public void playGrindingEffect() {
		final Direction facing = this.getBlockState().getValue(BlockStateProperties.FACING);

		final RandomSource random = this.level.random;

		final int stepX = facing.getStepX();
		final int stepY = facing.getStepY();
		final int stepZ = facing.getStepZ();

		for (int i = 0; i < 2; i++) {
			final Vec3 particlePos = this.getBlockPos().getCenter()
					.add(stepX * 7.0 / 16.0, stepY * 7.0 / 16.0, stepZ * 7.0 / 16.0)
					.add((random.nextFloat() - 0.5f) * (stepX == 0 ? 1 : 0), (random.nextFloat() - 0.5f) * (stepY == 0 ? 1 : 0), (random.nextFloat() - 0.5f) * (stepZ == 0 ? 1 : 0));

			this.level.addParticle(ParticleTypes.CRIT, particlePos.x, particlePos.y, particlePos.z, 0.0f, 0.0f, 0.0f);
		}
	}

	public void updateServoCoefficients() {

	}

	public void validateConstraintHandle() {
		if (this.handle != null && !this.handle.isValid()) {
			this.handle = null;
		}
	}

	public void assemble() {
		final BlockPos pos = this.getBlockPos();
		final BlockPos toAssemble = pos.relative(this.getBlockState().getValue(BlockStateProperties.FACING));
		final SimAssemblyHelper.AssemblyResult result;

		try {
			result = SimAssemblyHelper.assembleFromSingleBlock(this.level, pos, toAssemble, false, false);
			this.lastException = null;
		} catch (final AssemblyException e) {
			this.lastException = e;
			this.sendData();
			return;
		}

		this.sendData();

		final ServerSubLevel assembledSubLevel;
		final BlockPos assembleOffset;
		final BlockState link = copyBlockStatePropertiesToPlate(plateBlock.get().defaultBlockState());

		if (result != null) {
			assembledSubLevel = (ServerSubLevel) result.subLevel();
			assembleOffset = result.offset();
		} else {
			final ServerSubLevelContainer container = (ServerSubLevelContainer) SubLevelContainer.getContainer(this.level);

			final Pose3d pose = new Pose3d();
			pose.position().set(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

			assembledSubLevel = (ServerSubLevel) container.allocateNewSubLevel(pose);
			final LevelPlot plot = assembledSubLevel.getPlot();

			final ChunkPos center = plot.getCenterChunk();
			plot.newEmptyChunk(center);
			plot.getEmbeddedLevelAccessor().setBlock(BlockPos.ZERO, link, 3);

			final BlockPos plotAnchor = plot.getCenterBlock();
			final Vector3dc centerOfMass = assembledSubLevel.getMassTracker().getCenterOfMass();
			final Vector3d subLevelCenter = JOMLConversion.atLowerCornerOf(pos);

			if (centerOfMass != null) {
				subLevelCenter.add(centerOfMass.x() - plotAnchor.getX(), centerOfMass.y() - plotAnchor.getY(), centerOfMass.z() - plotAnchor.getZ());
			} else {
				assembledSubLevel.logicalPose().rotationPoint()
				.set(plotAnchor.getX() + 0.5, plotAnchor.getY() + 0.5, plotAnchor.getZ() + 0.5);
			}

			assembledSubLevel.logicalPose().position().set(subLevelCenter.x, subLevelCenter.y, subLevelCenter.z);
			assembleOffset = plotAnchor.subtract(pos);

			final SubLevelPhysicsSystem physicsSystem = container.physicsSystem();
			final PhysicsPipeline pipeline = physicsSystem.getPipeline();

			final SubLevel containingSubLevel = this.getContainingSubLevel();
			if (containingSubLevel != null) {
				SubLevelAssemblyHelper.kickFromContainingSubLevel((ServerLevel) this.level, physicsSystem, pipeline, assembledSubLevel, containingSubLevel);
				assembledSubLevel.logicalPose().orientation().set(containingSubLevel.logicalPose().orientation());
			}

			pipeline.teleport(assembledSubLevel, assembledSubLevel.logicalPose().position(), assembledSubLevel.logicalPose().orientation());
			assembledSubLevel.updateLastPose();

			this.level.playSound(null, pos, SimSoundEvents.SIMULATED_CONTRAPTION_MOVES.event(), SoundSource.BLOCKS, 1.0f, 1.0f);
		}

		this.getLevel().setBlockAndUpdate(pos, this.getBlockState().setValue(BaseJointBlock.ASSEMBLED, true));

		this.attachConstraints(assembledSubLevel, JOMLConversion.toJOML(toAssemble.getCenter()));//this.getConstraintPos(toAssemble, assembleOffset));
		this.setSubLevelID(assembledSubLevel.getUniqueId());

		final BlockPos plotPos = pos.offset(assembleOffset);
		if (result != null) {
			this.getLevel().setBlockAndUpdate(plotPos, link);
		}
		final BlockEntity be = this.getLevel().getBlockEntity(plotPos);

		if (be instanceof final BaseJointPlateBlockEntity plateBE) {
			plateBE.setParent(this);
			this.setPlatePos(plotPos);
		}
	}

	abstract BlockState copyBlockStatePropertiesToPlate(BlockState plate);

	public void disassemble() {
		if (this.isRemoved()) {
			return;
		}

		this.removeHandle();
		final SubLevel subLevel = SubLevelContainer.getContainer(this.level).getSubLevel(this.getSubLevelID());
		final BlockPos platePos = this.getPlatePos();
		if (platePos != null) {
			this.destroyPlate();

			if (Objects.equals(subLevel, Sable.HELPER.getContaining(this.level, this.getBlockPos()))) {
				this.lastException = SimAssemblyException.sameSubLevel();
				this.level.playSound(null, platePos, SimSoundEvents.ASSEMBLER_FAIL.event(), SoundSource.BLOCKS, 1.0f, 1.0f);
			} else if (subLevel != null) {
				// if destroying the plate removed the sub-level, skip disassembling
				if (!subLevel.isRemoved()) {
					SimAssemblyHelper.disassembleSubLevel(this.level, subLevel, platePos, this.getBlockPos(), Rotation.NONE, true);
				} else {
					this.level.playSound(null, platePos, SimSoundEvents.SIMULATED_CONTRAPTION_STOPS.event(), SoundSource.BLOCKS, 1.0f, 1.0f);
				}
			}
		}

		this.getLevel().setBlockAndUpdate(this.getBlockPos(), this.getBlockState().setValue(BaseJointBlock.ASSEMBLED, false));

		this.setSubLevelID(null);
		this.setPlatePos(null);
		this.sendData();
	}

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
	}

	public void reattachConstraint(final @Nullable ServerSubLevel plateSubLevel, final boolean updatePlate) {
		// we also want to "reset" the plate BE here too, so it's correct
		final BlockPos platePos = this.getPlatePos();

		if (platePos != null) {
			if (this.handle != null) {
				this.handle.remove();
			}

			if (updatePlate) {
				this.associatePlateWithParent();
			}

			final BlockState plateState = this.level.getBlockState(platePos);
			if (!plateState.is(plateBlock)) return;

			this.attachConstraints(plateSubLevel, JOMLConversion.toJOML(platePos.getCenter()));
		}
	}

	public void associatePlateWithParent() {
		if (this.getPlatePos() != null) {
			if (this.getLevel().getBlockState(this.getPlatePos()).is(plateBlock)) {
				final BaseJointPlateBlockEntity plate = (BaseJointPlateBlockEntity) this.getLevel().getBlockEntity(this.getPlatePos());
				plate.setParent(this);
			}
		}
	}

	public void attachConstraints(@Nullable ServerSubLevel plateSubLevel, Vector3d attachPos) {
		final BlockPos platePos = this.getPlatePos();

		if (platePos == null) return;
		final BlockState plateState = this.level.getBlockState(platePos);

		if (!plateState.is(plateBlock)) return;

		final Vector3d anchorPos = JOMLConversion.toJOML(this.getBlockPos().getCenter());

		final GenericConstraintConfiguration constraint = new GenericConstraintConfiguration(
				anchorPos,
				attachPos,
				getBaseRotationAxis(this.getBlockState()),
				getBaseRotationAxis(plateState),
				lockedAxes
				);

		final ServerSubLevelContainer container = SubLevelContainer.getContainer((ServerLevel) this.getLevel());
		final ServerSubLevel containingSubLevel = (ServerSubLevel) Sable.HELPER.getContaining(this);
		final PhysicsPipeline pipeline = container.physicsSystem().getPipeline();

		if (containingSubLevel == plateSubLevel) return;
		this.handle = pipeline.addConstraint(containingSubLevel, plateSubLevel, constraint);
	}

	abstract Quaterniond getBaseRotationAxis(BlockState state);

	@Override
	protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(compound, registries, clientPacket);

		BlockPos platePos = this.getPlatePos();
		UUID id = this.getSubLevelID();

		// Handle serializing assembled joints to schematics
		final SubLevelSchematicSerializationContext schematicContext = SubLevelSchematicSerializationContext.getCurrentContext();

		if (id != null && schematicContext != null) {
			final SubLevelSchematicSerializationContext.SchematicMapping mapping = schematicContext.getMapping(id);

			if (mapping != null) {
				id = mapping.newUUID();
				platePos = mapping.transform().apply(platePos);
			} else {
				id = null;
				platePos = null;
			}
		}

		if (id != null) {
			compound.putUUID("SubLevelID", id);
		}

		if (platePos != null) {
			compound.put("JointPlate", NbtUtils.writeBlockPos(platePos));
		}

		AssemblyException.write(compound, registries, this.lastException);
	}

	@Override
	protected void read(final CompoundTag compound, final HolderLookup.Provider registries, final boolean clientPacket) {
		super.read(compound, registries, clientPacket);

		final SubLevelSchematicSerializationContext schematicContext = SubLevelSchematicSerializationContext.getCurrentContext();

		SubLevelSchematicSerializationContext.SchematicMapping mapping = null;

		if (compound.hasUUID("SubLevelID")) {
			UUID subLevelID = compound.getUUID("SubLevelID");

			if (schematicContext != null) {
				mapping = schematicContext.getMapping(subLevelID);
			}

			if (mapping != null) {
				subLevelID = mapping.newUUID();
			}

			this.setSubLevelID(subLevelID);
		}

		if (compound.contains("JointPlate")) {
			final BlockPos blockPos = NbtUtils.readBlockPos(compound, "JointPlate").orElseThrow();
			this.setPlatePos(blockPos);
		}

		this.lastException = AssemblyException.read(compound, registries);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		this.removeHandle();
	}

	/**
	 * Called before we assemble the joint base into a sub-level
	 */
	public void beforeAssembly() {
		this.assembling = true;
	}

	@Override
	public void remove() {
		if (!this.level.isClientSide && !this.assembling) {
			// If we're actually removed and not just unloaded, let's break the plate as well
			this.destroyPlate();
		}

		super.remove();
	}

	public boolean isAssembled() {
		return this.getBlockState().getValue(BaseJointBlock.ASSEMBLED);
	}

	public @Nullable SubLevel getAttachedSubLevel() {
		final SubLevelContainer container = SubLevelContainer.getContainer(this.level);
		return container.getSubLevel(this.subLevelID);
	}

	public @Nullable SubLevel getContainingSubLevel() {
		return Sable.HELPER.getContaining(this);
	}

	public boolean isLocking() {
		return this.getBlockState().getValue(BlockStateProperties.POWERED);
	}

	public @NotNull Vector3d getConstraintPos(final BlockPos relative, final BlockPos offset) {
		return JOMLConversion.toJOML(relative.offset(offset).getCenter());
	}

	@SuppressWarnings("unchecked")
	public void destroyPlate() {
		final BlockPos platePos = this.getPlatePos();
		if (platePos != null) {
			final SubLevelContainer container = SubLevelContainer.getContainer(this.level);
			if (container == null) return;

			final SubLevel subLevel = container.getSubLevel(this.subLevelID);
			if (this.subLevelID != null && subLevel == null) return;

			if (this.getLevel().getBlockState(platePos).is(plateBlock)) {
				((IBE<? extends BaseJointPlateBlockEntity>) plateBlock.get()).withBlockEntityDo(this.level, platePos, BaseJointPlateBlockEntity::beforeAssembly);
				this.getLevel().setBlock(platePos, Blocks.AIR.defaultBlockState(), 2);
			}
		}
	}

	public void removeHandle() {
		if (this.handle != null) {
			this.handle.remove();
			this.handle = null;
		}
	}

	@Override
	public float propagateRotationTo(KineticBlockEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff, boolean connectedViaAxes, boolean connectedViaCogs) {
		return this.getPlatePos() != null && stateTo.getBlock() instanceof BaseJointPlateBlock ? 1 : super.propagateRotationTo(target, stateFrom, stateTo, diff, connectedViaAxes, connectedViaCogs);
	}

	@Override
	public boolean isCustomConnection(KineticBlockEntity other, BlockState state, BlockState otherState) {
		return this.getPlatePos() != null && otherState.getBlock() instanceof BaseJointPlateBlock;
	}

	@Override
	public List<BlockPos> addPropagationLocations(IRotate block, BlockState state, List<BlockPos> neighbours) {
		if (this.getPlatePos() != null) {
			neighbours.add(this.getPlatePos());
		}

		return super.addPropagationLocations(block, state, neighbours);
	}

	public @Nullable BlockPos getPlatePos() {
		return this.jointPlatePos;
	}

	public void setPlatePos(@Nullable BlockPos jointPlatePos) {
		this.jointPlatePos = jointPlatePos;
	}

	public @Nullable UUID getSubLevelID() {
		return this.subLevelID;
	}

	public void setSubLevelID(@Nullable UUID subLevelID) {
		this.subLevelID = subLevelID;
	}

	// passthrough shaft should not cost stress
	@Override
	public float calculateStressApplied() {
		return 0;
	}

	@Override
	public AssemblyException getLastAssemblyException() {
		return this.lastException;
	}

	@Override
	public @Nullable Iterable<@NotNull SubLevel> sable$getConnectionDependencies() {
		final SubLevel attachedSubLevel = this.getAttachedSubLevel();

		if (attachedSubLevel == null) {
			return null;
		}

		return List.of(attachedSubLevel);
	}
}	
