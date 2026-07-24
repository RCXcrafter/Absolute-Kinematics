package com.rcx.absolutekinematics.visuals;

import com.rcx.absolutekinematics.blockentity.BaseJointPlateBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import dev.simulated_team.simulated.index.SimPartialModels;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class JointPlateBlockRenderer<T extends BaseJointPlateBlockEntity> extends KineticBlockEntityRenderer<T> {

	public JointPlateBlockRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(T be, BlockState state) {
		return CachedBuffers.partialFacing(SimPartialModels.SHAFT_SIXTEENTH, state, state.getValue(BlockStateProperties.FACING));
	}
}
