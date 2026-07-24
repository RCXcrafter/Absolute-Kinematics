package com.rcx.absolutekinematics.visuals;

import java.util.function.Consumer;

import org.joml.Vector3f;

import com.rcx.absolutekinematics.blockentity.HingeBlockEntity;
import com.rcx.absolutekinematics.client.KinematicsPartialModels;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visual.TickableVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.lib.visual.SimpleTickableVisual;
import dev.simulated_team.simulated.index.SimPartialModels;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class HingeVisual extends AbstractBlockEntityVisual<HingeBlockEntity> implements SimpleDynamicVisual, SimpleTickableVisual {

	private final RotatingInstance bottomAxle;
	private final RotatingInstance topAxle;
	private final RotatingInstance sideAxle;

	private final RotatingInstance cardanBottom;
	private final RotatingInstance cardanMiddle;
	private final RotatingInstance cardanTop;
	private final RotatingInstance[] cardan;

	Direction facing;
	Direction axisDirection;
	private double lastAngle = 0;
	private double angle = 0;
	private Vector3f[] startRotations = new Vector3f[2];
	private Vector3f[] lastRotations = new Vector3f[2];

	private boolean wasAssembled = false;

	public HingeVisual(VisualizationContext context, HingeBlockEntity blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);
		facing = blockEntity.getBlockState().getValue(BlockStateProperties.FACING);
		axisDirection = Direction.fromAxisAndDirection(blockEntity.getBlockState().getValue(BlockStateProperties.AXIS), AxisDirection.POSITIVE);

		for (int i = 0; i < startRotations.length; i ++) {
			startRotations[i] = new Vector3f(facing.getStepX(), facing.getStepY(), facing.getStepZ());
			lastRotations[i] = new Vector3f(facing.getStepX(), facing.getStepY(), facing.getStepZ());
		}

		this.bottomAxle = this.instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(SimPartialModels.SHAFT_SIXTEENTH))
				.createInstance()
				.rotateToFace(Direction.SOUTH, facing.getOpposite())
				.setup(blockEntity)
				.setPosition(this.getVisualPosition());
		this.topAxle = this.instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(SimPartialModels.SHAFT_SIXTEENTH))
				.createInstance()
				.rotateToFace(Direction.SOUTH, facing)
				.setup(blockEntity)
				.setPosition(this.getVisualPosition());
		this.sideAxle = this.instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(KinematicsPartialModels.HINGE_AXLE))
				.createInstance()
				.rotateToFace(Direction.EAST, axisDirection)
				.setup(blockEntity.getExtraKinetics(), blockEntity.getBlockState().getValue(BlockStateProperties.AXIS))
				.setPosition(this.getVisualPosition());

		float speedMult = facing.getAxisDirection().getStep();
		this.cardanBottom = this.instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(KinematicsPartialModels.CARDAN_BOTTOM))
				.createInstance()
				.rotateToFace(facing)
				.setup(this.blockEntity, this.blockEntity.getSpeed() * speedMult)
				.setRotationAxis(facing.getStepX(), facing.getStepY(), facing.getStepZ())
				.setPosition(this.getVisualPosition());
		this.cardanMiddle = this.instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(KinematicsPartialModels.CARDAN_MIDDLE))
				.createInstance()
				.rotateToFace(facing)
				.setup(this.blockEntity, this.blockEntity.getSpeed() * speedMult)
				.setRotationAxis(facing.getStepX(), facing.getStepY(), facing.getStepZ())
				.setPosition(this.getVisualPosition());
		this.cardanTop = this.instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(KinematicsPartialModels.CARDAN_TOP))
				.createInstance()
				.rotateToFace(facing)
				.setup(this.blockEntity, this.blockEntity.getSpeed() * speedMult)
				.setRotationAxis(facing.getStepX(), facing.getStepY(), facing.getStepZ())
				.setPosition(this.getVisualPosition());
		this.cardan = new RotatingInstance[] {
				cardanBottom,
				cardanMiddle,
				cardanTop
		};

		this.bottomAxle.setChanged();
		this.topAxle.setVisible(!this.blockEntity.isAssembled());
		this.topAxle.setChanged();
		this.sideAxle.setChanged();

		this.cardanBottom.setChanged();
		this.cardanMiddle.setChanged();
		this.cardanTop.setChanged();
	}

	@Override
	public void update(float pt) {
		super.update(pt);
		facing = this.blockEntity.getBlockState().getValue(BlockStateProperties.FACING);
		this.bottomAxle.setup(this.blockEntity).setChanged();
		this.topAxle.setVisible(!this.blockEntity.isAssembled());
		this.topAxle.setup(this.blockEntity).setChanged();
		this.sideAxle.setup(this.blockEntity.getExtraKinetics(), this.blockEntity.getBlockState().getValue(BlockStateProperties.AXIS)).setChanged();

		float speedMult = facing.getAxisDirection().getStep();
		cardanBottom.setup(this.blockEntity, this.blockEntity.getSpeed() * speedMult).setRotationAxis(facing.getStepX(), facing.getStepY(), facing.getStepZ()).setChanged();
		cardanMiddle.setup(this.blockEntity, this.blockEntity.getSpeed() * speedMult);
		cardanTop.setup(this.blockEntity, this.blockEntity.getSpeed() * speedMult);
		if (!this.blockEntity.isAssembled()) {
			cardanMiddle.setRotationAxis(facing.getStepX(), facing.getStepY(), facing.getStepZ());
			cardanTop.setRotationAxis(facing.getStepX(), facing.getStepY(), facing.getStepZ());
		}
		cardanMiddle.setChanged();
		cardanTop.setChanged();
	}

	@Override
	public void updateLight(final float partialTick) {
		this.relight(this.bottomAxle);
		this.relight(this.topAxle);
		this.relight(this.sideAxle);

		this.relight(this.cardanBottom);
		this.relight(this.cardanMiddle);
		this.relight(this.cardanTop);
	}

	@Override
	protected void _delete() {
		this.bottomAxle.delete();
		this.topAxle.delete();
		this.sideAxle.delete();

		this.cardanBottom.delete();
		this.cardanMiddle.delete();
		this.cardanTop.delete();
	}

	@Override
	public void collectCrumblingInstances(final Consumer<Instance> consumer) {
		consumer.accept(this.bottomAxle);
		consumer.accept(this.topAxle);
		consumer.accept(this.sideAxle);

		consumer.accept(this.cardanBottom);
		consumer.accept(this.cardanMiddle);
		consumer.accept(this.cardanTop);
	}

	@Override
	public void beginFrame(DynamicVisual.Context ctx) {
		if (this.blockEntity.isAssembled()) {
			wasAssembled = true;
			float currentAngle = (float) (lastAngle * (1 - ctx.partialTick()) + angle * ctx.partialTick());
			Vector3f[] newRotations = new Vector3f[startRotations.length];
			for (int i = 0; i < newRotations.length; i ++) {
				newRotations[i] = new Vector3f(startRotations[i]);
			}

			float radianAngle = (float) (Math.PI * currentAngle / 180.0f);

			//un-gimbal lock the quaternion before rotating it >:(
			if (axisDirection == Direction.UP || facing == Direction.DOWN && axisDirection == Direction.SOUTH) {
				cardan[1].rotateToFace(facing, Direction.UP);
				cardan[2].rotateToFace(facing, Direction.UP);
			}
			newRotations[0].rotateAxis(radianAngle / 2.0f, axisDirection.getStepX(), axisDirection.getStepY(), axisDirection.getStepZ());
			newRotations[1].rotateAxis(radianAngle, axisDirection.getStepX(), axisDirection.getStepY(), axisDirection.getStepZ());

			float offset = (float) ((Math.cos(radianAngle * 2f) - 1f) / 32.0f);
			for (int i = 0; i < newRotations.length; i ++) {
				cardan[i + 1].setPosition(this.getVisualPosition())
				.rotateTo(lastRotations[i].x, lastRotations[i].y, lastRotations[i].z, newRotations[i].x, newRotations[i].y, newRotations[i].z)
				.setRotationAxis(newRotations[i]);
			}

			if (axisDirection == Direction.UP || facing == Direction.DOWN && axisDirection == Direction.SOUTH) {
				cardan[1].rotateToFace(facing);
				cardan[2].rotateToFace(facing);
			}

			float mul = 1.73f;
			cardan[0].setPosition(this.getVisualPosition());
			cardan[0].nudge(facing.getStepX() * offset, facing.getStepY() * offset, facing.getStepZ() * offset);
			cardan[1].nudge(facing.getStepX() * offset * mul, facing.getStepY() * offset * mul, facing.getStepZ() * offset * mul);
			cardan[1].nudge(-newRotations[1].x * offset * mul, -newRotations[1].y * offset * mul, -newRotations[1].z * offset * mul);
			cardan[2].nudge(-newRotations[1].x * offset, -newRotations[1].y * offset, -newRotations[1].z * offset);

			for (int i = 0; i < cardan.length; i ++) {
				cardan[i].setChanged();
			}
			lastRotations = newRotations;
		} else if (wasAssembled) {
			for (RotatingInstance part : cardan) {
				part.setPosition(this.getVisualPosition()).setChanged();
			}
			for (int i = 0; i < lastRotations.length; i ++) {
				lastRotations[i] = new Vector3f(startRotations[i]);
			}
			wasAssembled = false;
		}
	}

	@Override
	public void tick(TickableVisual.Context context) {
		if (this.blockEntity.isAssembled()) {
			lastAngle = angle;
			angle = this.blockEntity.getCurrentAngle() % 360;
		}
	}
}
