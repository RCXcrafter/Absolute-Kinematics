package com.rcx.absolutekinematics.visuals;

import java.util.function.Consumer;

import com.rcx.absolutekinematics.blockentity.BaseJointPlateBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.simulated_team.simulated.index.SimPartialModels;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class JointPlateVisual<T extends BaseJointPlateBlockEntity> extends KineticBlockEntityVisual<T> {

	private final RotatingInstance topAxle;

	public JointPlateVisual(VisualizationContext context, T blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);
		this.topAxle = this.instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(SimPartialModels.SHAFT_SIXTEENTH))
				.createInstance()
				.rotateToFace(Direction.SOUTH, blockEntity.getBlockState().getValue(BlockStateProperties.FACING))
				.setup(blockEntity)
				.setPosition(this.getVisualPosition());
		this.topAxle.setChanged();
	}

	@Override
	public void update(float pt) {
		super.update(pt);
		this.topAxle.setup(this.blockEntity).setChanged();
	}

	@Override
	public void updateLight(float partialTick) {
		this.relight(this.topAxle);
	}

	@Override
	protected void _delete() {
		this.topAxle.delete();
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		consumer.accept(this.topAxle);
	}
}
