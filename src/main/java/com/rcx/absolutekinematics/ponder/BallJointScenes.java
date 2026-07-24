package com.rcx.absolutekinematics.ponder;

import com.rcx.absolutekinematics.block.BaseJointBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import dev.simulated_team.simulated.index.SimItems;
import dev.simulated_team.simulated.ponder.SmoothMovementUtils;
import dev.simulated_team.simulated.ponder.instructions.CustomAnimateWorldSectionInstruction;
import dev.simulated_team.simulated.ponder.scenes.SwivelBearingScenes;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.EffectInstructions;
import net.createmod.ponder.api.scene.OverlayInstructions;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.api.scene.SelectionUtil;
import net.createmod.ponder.api.scene.VectorUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BallJointScenes {

	public static void ballJointIntro(final SceneBuilder builder, final SceneBuildingUtil util) {
		final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		final CreateSceneBuilder.WorldInstructions world = scene.world();
		final OverlayInstructions overlay = scene.overlay();
		final SelectionUtil select = util.select();
		final VectorUtil vector = util.vector();

		scene.title("ball_joint_intro", "Assembling Structures using the Ball Joint");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.setSceneOffsetY(-0.5f);

		final BlockPos ballJoint = new BlockPos(2, 1, 2);

		final Selection ballJointThingy = select.fromTo(2, 2, 1, 3, 4, 2);

		scene.idle(10);
		world.showSection(select.position(ballJoint), Direction.DOWN);

		scene.idle(20);

		overlay.showText(80)
		.text("Ball Joints attach to the block in front of them")
		.pointAt(vector.topOf(ballJoint))
		.colored(PonderPalette.GREEN)
		.placeNearTarget();

		final AABB bb1 = AABB.unitCubeFromLowerCorner(new Vec3(2, 2, 2));
		overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, bb1, bb1, 90);

		scene.idle(70);

		scene.world().modifyBlock(ballJoint, s -> s.setValue(BaseJointBlock.ASSEMBLED, true), false);

		final ElementLink<WorldSectionElement> contraption =
				scene.world().showIndependentSectionImmediately(select.position(2, 2, 2));

		world.moveSection(contraption, new Vec3(0, -1, 0), 0);

		scene.world().showSectionAndMerge(select.position(2, 3, 2), Direction.DOWN, contraption);

		scene.idle(10);
		scene.effects().superGlue(ballJoint.above(), Direction.DOWN, true);
		world.showSectionAndMerge(ballJointThingy.substract(select.fromTo(2, 2, 2, 2, 3, 2)), Direction.DOWN, contraption);
		scene.idle(10);

		scene.overlay().showControls(util.vector().centerOf(4, 2, 2), Pointing.RIGHT, 40)
		.withItem(SimItems.HONEY_GLUE.asStack())
		.rightClick();
		scene.idle(5);
		final AABB bb2 = new AABB(util.grid().at(3, 3, 1));
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb2, bb2, 1);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb2, bb2.expandTowards(-1, -1, 1), 80);

		scene.idle(10);

		overlay.showText(70)
		.text("Use Super Glue or Honey Glue to select a group of blocks")
		.pointAt(vector.centerOf(2, 3, 2))
		.colored(PonderPalette.OUTPUT)
		.attachKeyFrame()
		.placeNearTarget();

		scene.idle(90);

		overlay.showText(80)
		.text("When right clicked with an empty hand, it will assemble into a Simulated Contraption")
		.pointAt(vector.centerOf(ballJoint.above(2)))
		.attachKeyFrame()
		.placeNearTarget();

		scene.idle(50);

		scene.overlay().showControls(vector.centerOf(ballJoint), Pointing.LEFT, 80).rightClick();

		scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(contraption, new Vec3(-45, 0, -45), 20, SmoothMovementUtils.cubicRise()));
		scene.idle(20);
		scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(contraption, new Vec3(5, 0, 3), 2, SmoothMovementUtils.quadraticRiseOut()));
		scene.idle(2);
		scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(contraption, new Vec3(-3, 0, -3), 1, SmoothMovementUtils.quadraticRise()));
		scene.idle(1);
		scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(contraption, new Vec3(0, 0, -2), 1, SmoothMovementUtils.linear()));

		scene.idle(130);




		overlay.showText(100)
		.text("Ball Joints can not directly be moved with Rotational Force")
		.pointAt(vector.centerOf(ballJoint))
		.attachKeyFrame()
		.placeNearTarget();





		scene.idle(140);




		scene.markAsFinished();
	}

	public static void ballJointLocking(final SceneBuilder builder, final SceneBuildingUtil util) {
		final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		final CreateSceneBuilder.WorldInstructions world = scene.world();
		final OverlayInstructions overlay = scene.overlay();
		final SelectionUtil select = util.select();
		final VectorUtil vector = util.vector();
		final EffectInstructions effects = scene.effects();

		scene.title("ball_joint_locking", "Locking Ball Joints");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.setSceneOffsetY(-1);
		world.showSection(select.position(1, 0, 5), Direction.UP);

		final BlockPos ballJoint = new BlockPos(2, 4, 1);
		final BlockPos swivelBearing = new BlockPos(2, 1, 4);
		final BlockPos leverPos = new BlockPos(1, 5, 1);

		final Selection pendulum = select.fromTo(2, 1, 1, 2, 2, 1);

		final Selection rod = select.fromTo(2, 3, 4, 5, 3, 4);

		final Selection kinetics = select.fromTo(2, 0, 5, 2, 1, 5);


		world.showSection(kinetics, Direction.DOWN);
		scene.idle(5);
		world.showSection(select.position(swivelBearing), Direction.DOWN);

		scene.idle(5);

		final ElementLink<WorldSectionElement> rodLink = world.showIndependentSectionImmediately(select.position(2, 2, 4));
		world.moveSection(rodLink, new Vec3(0, -1, 0), 0);

		world.showSectionAndMerge(rod, Direction.DOWN, rodLink);

		scene.idle(20);

		world.showSection(select.position(leverPos).add(select.position(2, 5, 1)), Direction.DOWN);
		scene.idle(5);
		world.showSection(select.position(ballJoint), Direction.UP);

		scene.idle(10);

		final ElementLink<WorldSectionElement> contraption = world.showIndependentSectionImmediately(select.position(2, 3, 1));
		world.moveSection(contraption, new Vec3(0, 1, 0), 0);

		scene.world().modifyBlock(ballJoint, s -> s.setValue(BaseJointBlock.ASSEMBLED, true), false);

		world.configureCenterOfRotation(contraption, vector.centerOf(ballJoint.below()));

		world.showSectionAndMerge(pendulum, Direction.UP, contraption);

		scene.idle(20);

		overlay.showText(80)
		.text("When provided with Redstone Power...")
		.pointAt(vector.of(1.75, 5.5, 1.5))
		.attachKeyFrame()
		.colored(PonderPalette.INPUT)
		.placeNearTarget();

		scene.idle(40);

		world.setKineticSpeed(kinetics, -16);
		SwivelBearingScenes.setSwivelCogKineticSpeed(scene, util, swivelBearing, 16f);
		world.rotateSection(rodLink, 0, 180, 0, 50);

		scene.idle(20);
		scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(contraption, new Vec3(15, 10, -60), 10, SmoothMovementUtils.quadraticRiseOut()));
		scene.idle(10);
		scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(contraption, new Vec3(-25, 8, 100), 30, SmoothMovementUtils.cubicSmoothing()));
		scene.idle(20);

		world.setKineticSpeed(kinetics, 0);
		SwivelBearingScenes.setSwivelCogKineticSpeed(scene, util, swivelBearing, 0f);

		scene.idle(10);

		world.toggleRedstonePower(select.position(leverPos));
		effects.indicateRedstone(leverPos);

		overlay.showText(80)
		.text("...the Ball Joint locks, blocking all rotation")
		.pointAt(vector.centerOf(3, 2, 1))
		.colored(PonderPalette.OUTPUT)
		.placeNearTarget();

		scene.idle(100);

		final Vec3 valuePanelPos = new Vec3(2.5, 4.625, 1.125);

		overlay.showText(80)
		.text("This behavior can be configured using the value panel")
		.pointAt(valuePanelPos)
		.attachKeyFrame()
		.placeNearTarget();

		scene.overlay().showControls(valuePanelPos, Pointing.RIGHT, 80).rightClick();

		overlay.showFilterSlotInput(valuePanelPos, Direction.SOUTH, 80);

		scene.idle(40);

		scene.markAsFinished();
	}

	public static void ballJointPassthrough(final SceneBuilder builder, final SceneBuildingUtil util) {
		final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		final CreateSceneBuilder.WorldInstructions world = scene.world();
		final OverlayInstructions overlay = scene.overlay();
		final SelectionUtil select = util.select();
		final VectorUtil vector = util.vector();

		scene.title("ball_joint_passthrough", "Passing Rotation through a Ball Joint");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();

		final BlockPos ballJoint = new BlockPos(2, 2, 2);
		final BlockPos floorBreak = new BlockPos(0, 0, 2);
		final Selection thatOneShaftThatIHate = select.position(1, 2, 2);

		final Selection speedometer = select.position(1, 3, 2);
		final Selection drill = select.position(0, 3, 2);
		final Selection plate = select.position(2, 3, 2);

		final Selection kinetics = select.fromTo(5, 0, 2, 5, 2, 2).add(select.fromTo(3, 2, 2, 4, 2, 2));

		scene.idle(10);

		world.showSection(kinetics, Direction.DOWN);

		final ElementLink<WorldSectionElement> speedoLink = world.showIndependentSection(speedometer, Direction.DOWN);
		world.moveSection(speedoLink, vector.of(0, -1, 0), 0);
		world.configureCenterOfRotation(speedoLink, vector.centerOf(ballJoint.above()));

		final ElementLink<WorldSectionElement> thatOneShaftThatIHateLink = world.showIndependentSection(thatOneShaftThatIHate, Direction.DOWN);
		world.moveSection(thatOneShaftThatIHateLink, vector.of(1, 0, 0), 0);

		scene.idle(10);

		world.setKineticSpeed(kinetics, 32);
		world.setKineticSpeed(thatOneShaftThatIHate, 32);
		world.setKineticSpeed(speedometer, 32);
		world.setKineticSpeed(drill, 32);
		world.setKineticSpeed(plate, 32);

		scene.idle(20);

		world.hideIndependentSection(thatOneShaftThatIHateLink, Direction.SOUTH);

		scene.idle(15);

		world.showSection(select.position(ballJoint), Direction.SOUTH);

		scene.idle(20);

		final ElementLink<WorldSectionElement> contraption = world.showIndependentSectionImmediately(plate);
		world.moveSection(contraption, vector.of(0, -1, 0), 0);

		overlay.showText(80)
		.text("Rotational power via the Shaft passes directly through the Ball Joint")
		.pointAt(vector.centerOf(1, 2, 2))
		.attachKeyFrame()
		.placeNearTarget();

		scene.idle(87);
		scene.idle(13);

		scene.world().modifyBlock(ballJoint, s -> s.setValue(BaseJointBlock.ASSEMBLED, true), false);

		world.showSectionAndMerge(drill, Direction.EAST, contraption);

		scene.idle(5);

		scene.overlay().showControls(util.vector().centerOf(1, 2, 2), Pointing.RIGHT, 40)
		.withItem(SimItems.HONEY_GLUE.asStack())
		.rightClick();
		scene.idle(5);
		final AABB bb2 = new AABB(util.grid().at(1, 2, 2));
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb2, bb2, 1);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb2, bb2.expandTowards(-1, 0, 0), 40);
		scene.idle(40);

		scene.overlay().showControls(vector.centerOf(ballJoint), Pointing.DOWN, 80).rightClick();

		scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(speedoLink, new Vec3(0, 0, 30), 10, SmoothMovementUtils.cubicRise()));
		scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(contraption, new Vec3(0, 0, 30), 10, SmoothMovementUtils.cubicRise()));

		scene.idle(40);

		overlay.showText(160)
		.text("Rotation remains uninterrupted as the Ball Joint rotates")
		.pointAt(vector.centerOf(0, 1, 2))
		.attachKeyFrame()
		.placeNearTarget();

		world.incrementBlockBreakingProgress(floorBreak);

		scene.idle(30);
		scene.markAsFinished();
		scene.idle(10);

		for (int i = 0; i < 8; i++) {
			world.incrementBlockBreakingProgress(floorBreak);
			scene.idle(40);
		}
		world.incrementBlockBreakingProgress(floorBreak);

		scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(speedoLink, new Vec3(0, 0, 2), 5, SmoothMovementUtils.cubicRise()));
		scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(contraption, new Vec3(0, 0, 2), 5, SmoothMovementUtils.cubicRise()));

		scene.idle(60);
	}
}
