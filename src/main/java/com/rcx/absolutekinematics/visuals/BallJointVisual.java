package com.rcx.absolutekinematics.visuals;

import java.util.function.Consumer;

import com.rcx.absolutekinematics.blockentity.BaseJointBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.simulated_team.simulated.index.SimPartialModels;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class BallJointVisual<T extends BaseJointBlockEntity> extends AbstractBlockEntityVisual<T> {

	private final RotatingInstance bottomAxle;
	private final RotatingInstance topAxle;

	public BallJointVisual(VisualizationContext context, T blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);
		this.bottomAxle = this.instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(SimPartialModels.SHAFT_SIXTEENTH))
				.createInstance()
				.rotateToFace(Direction.SOUTH, blockEntity.getBlockState().getValue(BlockStateProperties.FACING).getOpposite())
				.setup(blockEntity)
				.setPosition(this.getVisualPosition());
		this.topAxle = this.instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(SimPartialModels.SHAFT_SIXTEENTH))
				.createInstance()
				.rotateToFace(Direction.SOUTH, blockEntity.getBlockState().getValue(BlockStateProperties.FACING))
				.setup(blockEntity)
				.setPosition(this.getVisualPosition());
		this.bottomAxle.setChanged();
		this.topAxle.setVisible(!this.blockEntity.isAssembled());
		this.topAxle.setChanged();
	}

	@Override
	public void update(float pt) {
		super.update(pt);
		this.bottomAxle.setup(this.blockEntity).setChanged();
		this.topAxle.setVisible(!this.blockEntity.isAssembled());
		this.topAxle.setup(this.blockEntity).setChanged();
	}

	@Override
	public void updateLight(final float partialTick) {
		this.relight(this.bottomAxle);
		this.relight(this.topAxle);
	}

	@Override
	protected void _delete() {
		this.bottomAxle.delete();
		this.topAxle.delete();
	}

	@Override
	public void collectCrumblingInstances(final Consumer<Instance> consumer) {
		consumer.accept(this.bottomAxle);
		consumer.accept(this.topAxle);
	}
}
