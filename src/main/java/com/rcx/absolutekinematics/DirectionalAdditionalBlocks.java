package com.rcx.absolutekinematics;

import java.util.List;
import java.util.Set;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;

import dev.simulated_team.simulated.index.SimBlockMovementChecks.AdditionalBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class DirectionalAdditionalBlocks implements AdditionalBlocks {

	public Block block;

	public DirectionalAdditionalBlocks(Block block) {
		this.block = block;
	}

	@Override
	public Iterable<BlockPos> addAdditionalBlocks(BlockState state, Level world, BlockPos pos, Set<BlockPos> visited) {
		if (state.is(block)) {
			return List.of(pos.relative(state.getValue(DirectionalKineticBlock.FACING)));
		}
		return List.of();
	}
}
