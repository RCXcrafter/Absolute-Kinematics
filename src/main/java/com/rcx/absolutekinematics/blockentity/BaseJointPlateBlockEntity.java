package com.rcx.absolutekinematics.blockentity;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredBlock;

public class BaseJointPlateBlockEntity extends KineticBlockEntity implements BlockEntitySubLevelActor {

	public BlockPos parent;
	public UUID parentSubLevelId;
	public boolean assembling;
	public DeferredBlock<? extends Block> baseBlock;

	public BaseJointPlateBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state, DeferredBlock<? extends Block> baseBlock) {
		super(typeIn, pos, state);
		this.baseBlock = baseBlock;
	}

	/**
	 * Called before we disasemble/assemble the joint plate
	 */
	public void beforeAssembly() {
		this.assembling = true;
	}

	@Override
	public void remove() {
		// if the block was broken / destroyed, destroy our parent
		if (!this.level.isClientSide && !this.assembling) {
			this.destroyBase();
		}

		super.remove();
	}

	public void destroyBase() {
		if (this.parent != null && this.getLevel().getBlockState(this.parent).is(this.baseBlock)) {
			this.getLevel().destroyBlock(this.parent, false);
		}
	}

	public void setParent(BaseJointBlockEntity be) {
		final SubLevel subLevel = Sable.HELPER.getContaining(be);

		this.parent = be.getBlockPos();
		this.parentSubLevelId = subLevel != null ? subLevel.getUniqueId() : null;
	}

	@Override
	public void tick() {
		super.tick();
	}

	@Override
	public float propagateRotationTo(KineticBlockEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff, boolean connectedViaAxes, boolean connectedViaCogs) {
		return this.parent != null && target.equals(this.level.getBlockEntity(this.parent)) ? 1 : super.propagateRotationTo(target, stateFrom, stateTo, diff, connectedViaAxes, connectedViaCogs);
	}

	@Override
	public boolean isCustomConnection(KineticBlockEntity other, BlockState state, BlockState otherState) {
		return this.parent != null && other.equals(this.level.getBlockEntity(this.parent));
	}

	@Override
	public List<BlockPos> addPropagationLocations(IRotate block, BlockState state, List<BlockPos> neighbours) {
		if (this.parent != null) {
			neighbours.add(this.parent);
		}

		return super.addPropagationLocations(block, state, neighbours);
	}

	@Override
	protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(compound, registries, clientPacket);

		if (this.parent != null) {
			compound.put("ParentPos", NbtUtils.writeBlockPos(this.parent));
		}

		if (this.parentSubLevelId != null) {
			compound.putUUID("ParentSubLevelId", this.parentSubLevelId);
		}
	}

	@Override
	protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(compound, registries, clientPacket);

		if (compound.contains("parent")) {
			this.parent = NbtUtils.readBlockPos(compound, "parent").get();
		}


		if (compound.contains("ParentPos")) {
			this.parent = NbtUtils.readBlockPos(compound, "ParentPos").get();
		}

		if (compound.contains("ParentSubLevelId")) {
			this.parentSubLevelId = compound.getUUID("ParentSubLevelId");
		}
	}

	@Override
	public void sable$physicsTick(ServerSubLevel subLevel, RigidBodyHandle handle, double timeStep) {
		if (this.parent != null) {
			final BlockEntity parentBE = this.level.getBlockEntity(this.parent);

			if (parentBE instanceof final BaseJointBlockEntity baseBlockEntity) {
				baseBlockEntity.updateServoCoefficients();
			}
		}
	}

	@Override
	public @Nullable Iterable<@NotNull SubLevel> sable$getConnectionDependencies() {
		if (this.parent == null) {
			return null;
		}

		final SubLevelContainer container = SubLevelContainer.getContainer(this.level);

		if (this.parentSubLevelId != null) {
			final SubLevel subLevel = container.getSubLevel(this.parentSubLevelId);

			if (subLevel != null) {
				return List.of(subLevel);
			}
		}

		return null;
	}


	public void setParentAssembleNextTick() {
		final BlockEntity be = this.level.getBlockEntity(this.parent);
		if (be instanceof final BaseJointBlockEntity sbe) {
			sbe.assembleNextTick = true;
		}
	}

	public void fixParentLinkingWhenMoved() {
		if (this.level.isClientSide() || this.parent == null) {
			return;
		}

		final BlockEntity be = this.level.getBlockEntity(this.parent);

		if (be instanceof final BaseJointBlockEntity sbe) {
			sbe.setPlatePos(this.getBlockPos());

			final ServerSubLevel newSublevel = (ServerSubLevel)Sable.HELPER.getContaining(this);
			if (newSublevel != null) {
				final UUID subLevelID = sbe.getSubLevelID();
				final UUID newID = newSublevel.getUniqueId();

				if (newID != subLevelID) {
					sbe.setSubLevelID(newSublevel.getUniqueId());
					sbe.reattachConstraint(newSublevel, true);
				}
			} else {
				sbe.setSubLevelID(null);
				sbe.reattachConstraint(null, true);
			}
		}
	}
}
