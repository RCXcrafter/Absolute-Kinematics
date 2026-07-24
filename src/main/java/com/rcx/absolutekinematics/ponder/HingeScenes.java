package com.rcx.absolutekinematics.ponder;

import com.rcx.absolutekinematics.block.BaseJointBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlockEntity;
import dev.simulated_team.simulated.index.SimItems;
import dev.simulated_team.simulated.ponder.SmoothMovementUtils;
import dev.simulated_team.simulated.ponder.instructions.CustomAnimateWorldSectionInstruction;
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

public class HingeScenes {

	public static void setSwivelCogKineticSpeed(final CreateSceneBuilder scene, final SceneBuildingUtil util, final BlockPos swivelPos, final Float rpm) {
		scene.world().modifyBlock(swivelPos,
				s -> s.setValue(BaseJointBlock.ASSEMBLED, true), false);

		scene.world().modifyBlockEntityNBT(util.select().position(swivelPos), SwivelBearingBlockEntity.class, nbt -> {
			nbt.getCompound("HingeAxle").putFloat("Speed", rpm);
		});
	}

	public static void hingeIntro(final SceneBuilder builder, final SceneBuildingUtil util) {
		final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		final CreateSceneBuilder.WorldInstructions world = scene.world();
		final OverlayInstructions overlay = scene.overlay();
		final SelectionUtil select = util.select();
		final VectorUtil vector = util.vector();
		final EffectInstructions effects = scene.effects();

		scene.title("hinge_intro", "Moving Structures using the Hinge");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.setSceneOffsetY(-0.5f);
		world.showSection(select.position(5, 0, 3), Direction.UP);

		final BlockPos hinge = new BlockPos(2, 1, 2);

		final Selection hingePlatform = select.fromTo(2, 2, 2, 2, 4, 2);
		//final Selection bearingRedstoneDecor = select.fromTo(0, 4, 2, 4, 4, 2);

		final Selection lampAndLever = select.position(1, 4, 2).add(select.position(3, 4, 2));

		final Selection axle = select.fromTo(3, 1, 2, 5, 1, 2);

		final Selection kinetics = select.position(5, 0, 3);
		final Selection inverseKinetics = axle.add(select.position(hinge));

		scene.idle(10);
		world.showSection(axle, Direction.DOWN);
		scene.idle(10);
		world.showSection(select.position(hinge), Direction.DOWN);

		scene.idle(20);

		overlay.showText(80)
		.text("Hinges attach to the block in front of them")
		.pointAt(vector.topOf(hinge))
		.colored(PonderPalette.GREEN)
		.placeNearTarget();

		final AABB bb1 = AABB.unitCubeFromLowerCorner(new Vec3(2, 2, 2));
		overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, bb1, bb1, 90);

		scene.idle(70);

		final ElementLink<WorldSectionElement> contraption =
				scene.world().showIndependentSectionImmediately(select.position(2, 2, 2));

		world.moveSection(contraption, new Vec3(0, -1, 0), 0);

		scene.world().showSectionAndMerge(select.position(2, 3, 2), Direction.DOWN, contraption);

		scene.idle(10);
		scene.effects().superGlue(hinge.above(), Direction.DOWN, true);
		world.showSectionAndMerge(hingePlatform.substract(select.fromTo(2, 2, 2, 2, 3, 2)), Direction.DOWN, contraption);
		scene.idle(10);
		world.showSectionAndMerge(lampAndLever, Direction.DOWN, contraption);
		scene.idle(10);

		scene.overlay().showControls(util.vector().centerOf(4, 2, 2), Pointing.RIGHT, 40)
		.withItem(SimItems.HONEY_GLUE.asStack())
		.rightClick();
		scene.idle(5);
		final AABB bb2 = new AABB(util.grid().at(3, 3, 2));
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb2, bb2, 1);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb2, bb2.expandTowards(-2, -1, 0), 80);

		scene.idle(10);

		overlay.showText(70)
		.text("Use Super Glue or Honey Glue to select a group of blocks")
		.pointAt(vector.centerOf(0, 2, 2))
		.colored(PonderPalette.OUTPUT)
		.attachKeyFrame()
		.placeNearTarget();

		scene.idle(90);

		world.setKineticSpeed(kinetics, 8);
		world.setKineticSpeed(inverseKinetics, -8);
		setSwivelCogKineticSpeed(scene, util, hinge, 8f);

		world.rotateSection(contraption, -90, 0, 0, 100);

		overlay.showText(80)
		.text("When powered via the shaft, it will assemble into a Simulated Contraption")
		.pointAt(vector.topOf(hinge))
		.attachKeyFrame()
		.placeNearTarget();

		scene.idle(100);

		world.toggleRedstonePower(lampAndLever);

		effects.indicateRedstone(new BlockPos(1, 1, 0));

		/*for (int i = 0; i < 3; i++) {
			final int finalI = i;
			scene.world().modifyBlock(new BlockPos(1 + finalI, 4, 2),
					s -> s.setValue(RedStoneWireBlock.POWER, 15 - finalI), false);
		}*/

		overlay.showText(160)
		.text("The Hinge is locked to 90 degrees of rotation on each side, giving a total of 180 degrees of movement")
		.pointAt(vector.topOf(hinge))
		.attachKeyFrame()
		.placeNearTarget();

		scene.idle(80);

		world.setKineticSpeed(kinetics, -8);
		world.setKineticSpeed(inverseKinetics, 8);
		setSwivelCogKineticSpeed(scene, util, hinge, -8f);

		world.rotateSection(contraption, 180, 0, 0, 200);

		scene.markAsFinished();

		scene.idle(200);

		world.setKineticSpeed(kinetics, 8);
		world.setKineticSpeed(inverseKinetics, -8);
		setSwivelCogKineticSpeed(scene, util, hinge, 8f);

		world.rotateSection(contraption, -90, 0, 0, 100);
		scene.idle(100);

		world.setKineticSpeed(kinetics, 0);
		world.setKineticSpeed(inverseKinetics, 0);
		setSwivelCogKineticSpeed(scene, util, hinge, 0f);
	}

	public static void hingeUnlocking(final SceneBuilder builder, final SceneBuildingUtil util) {
		final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		final CreateSceneBuilder.WorldInstructions world = scene.world();
		final OverlayInstructions overlay = scene.overlay();
		final SelectionUtil select = util.select();
		final VectorUtil vector = util.vector();
		final EffectInstructions effects = scene.effects();

		scene.title("hinge_unlocking", "Unlocking Hinges");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.setSceneOffsetY(-1);
		world.showSection(select.position(1, 0, 5), Direction.UP);

		final BlockPos hinge = new BlockPos(2, 4, 1);
		final BlockPos leverPos = new BlockPos(1, 5, 1);

		final Selection pendulum = select.fromTo(2, 2, 0, 2, 3, 0);

		final Selection kinetics = select.position(1, 0, 5).add(select.position(2, 2, 3).add(select.fromTo(2, 4, 2, 2, 4, 3)));
		final Selection inverseKinetics = select.fromTo(2, 1, 3, 2, 1, 5).add(select.position(2, 3, 3));

		scene.idle(10);
		world.showSection(select.fromTo(2, 1, 3, 2, 1, 5), Direction.DOWN);
		scene.idle(5);
		world.showSection(select.position(2, 2, 3), Direction.DOWN);
		scene.idle(5);
		world.showSection(select.fromTo(2, 3, 3, 2, 3, 3), Direction.DOWN);
		scene.idle(5);
		world.showSection(select.fromTo(2, 4, 1, 2, 4, 3), Direction.DOWN);
		scene.idle(5);
		world.showSection(select.fromTo(1, 5, 1, 2, 5, 3), Direction.DOWN);

		scene.idle(20);

		final ElementLink<WorldSectionElement> contraption = world.showIndependentSectionImmediately(select.position(2, 4, 0));
		world.moveSection(contraption, new Vec3(-0.005, 0.01, 0.99), 0);
		setSwivelCogKineticSpeed(scene, util, hinge, 0f);

		world.configureCenterOfRotation(contraption, vector.centerOf(hinge));

		world.showSectionAndMerge(pendulum, Direction.UP, contraption);

		scene.idle(20);

		world.setKineticSpeed(kinetics, 8);
		world.setKineticSpeed(inverseKinetics, -8);
		setSwivelCogKineticSpeed(scene, util, hinge, 8f);
		world.rotateSection(contraption, 0, 0, 45, 20);

		scene.idle(20);

		world.setKineticSpeed(kinetics, 0);
		world.setKineticSpeed(inverseKinetics, 0);
		setSwivelCogKineticSpeed(scene, util, hinge, 0f);

		scene.idle(20);

		overlay.showText(80)
		.text("When provided with Redstone Power...")
		.pointAt(vector.of(1.75, 5.5, 1.5))
		.attachKeyFrame()
		.colored(PonderPalette.INPUT)
		.placeNearTarget();

		scene.idle(40);

		world.toggleRedstonePower(select.position(leverPos));
		effects.indicateRedstone(leverPos);

		for (int i = 1; i < 5; i++) {
			final int direction = 1 - 2 * (i % 2);
			scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(contraption, new Vec3(0, 0, direction * (90 - 18 * i)), 20, SmoothMovementUtils.quadraticRiseInOut()));

			if (i == 4) {
				overlay.showText(80)
				.text("...the Hinge unlocks, spinning freely")
				.pointAt(vector.centerOf(2, 2, 1))
				.colored(PonderPalette.OUTPUT)
				.placeNearTarget();
			}

			scene.idle(20);
		}

		for (int i = 1; i < 3; i++) {
			final int direction = 1 - 2 * (i % 2);
			scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(contraption, new Vec3(0, 0, direction * (18 - 6 * i)), 20, SmoothMovementUtils.cubicSmoothing()));
			scene.idle(20);
		}
		scene.addInstruction(CustomAnimateWorldSectionInstruction.rotate(contraption, new Vec3(0, 0, -3), 20, SmoothMovementUtils.cubicSmoothing()));

		scene.idle(40);

		final Vec3 valuePanelPos = new Vec3(2.5, 4.85, 1);

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

	public static void hingePassthrough(final SceneBuilder builder, final SceneBuildingUtil util) {
		final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		final CreateSceneBuilder.WorldInstructions world = scene.world();
		final OverlayInstructions overlay = scene.overlay();
		final SelectionUtil select = util.select();
		final VectorUtil vector = util.vector();

		scene.title("hinge_passthrough", "Passing Rotation through a Hinge");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();

		final BlockPos hinge = new BlockPos(2, 2, 2);
		final Selection thatOneShaftThatIHate = select.position(1, 2, 2);

		final Selection speedometerWithShafts = select.fromTo(1, 3, 2, 1, 4, 2);

		final Selection kineticsThrough = select.position(2, 1, 2).add(select.position(4, 1, 3));
		final Selection inverseKineticsThrough = select.position(3, 1, 2).add(select.position(5, 0, 3));

		final Selection kineticsSide = select.fromTo(5, 0, 2, 5, 2, 2).add(select.fromTo(4, 2, 2, 3, 2, 2));
		final Selection contraption = select.fromTo(2, 3, 2, 2, 5, 2);

		scene.idle(10);

		world.showSection(kineticsThrough, Direction.DOWN);
		world.showSection(inverseKineticsThrough, Direction.DOWN);

		scene.idle(10);

		final ElementLink<WorldSectionElement> speedoLink = world.showIndependentSection(speedometerWithShafts, Direction.DOWN);
		final ElementLink<WorldSectionElement> thatOneShaftThatIHateLink = world.showIndependentSection(thatOneShaftThatIHate, Direction.DOWN);
		world.moveSection(thatOneShaftThatIHateLink, vector.of(1, 0, 0), 0);
		world.moveSection(speedoLink, vector.of(1, 0, 0), 0);

		world.setKineticSpeed(kineticsThrough, 32);
		world.setKineticSpeed(thatOneShaftThatIHate, 32);
		world.setKineticSpeed(speedometerWithShafts, 32);
		world.setKineticSpeed(contraption, 32);
		world.setKineticSpeed(inverseKineticsThrough, -32);

		scene.idle(20);

		world.hideIndependentSection(thatOneShaftThatIHateLink, Direction.SOUTH);

		scene.idle(15);

		world.showSection(select.position(hinge), Direction.SOUTH);

		scene.idle(20);

		overlay.showText(80)
		.text("Rotational power via the Shaft passes directly through the Hinge")
		.pointAt(vector.centerOf(2, 4, 2))
		.attachKeyFrame()
		.placeNearTarget();

		scene.idle(87);
		scene.idle(13);

		world.hideIndependentSection(speedoLink, Direction.UP);

		scene.idle(5);

		world.showSection(kineticsSide, Direction.DOWN);

		scene.idle(10);

		scene.rotateCameraY(-90);

		scene.idle(5);


		final ElementLink<WorldSectionElement> contraptionLink = world.showIndependentSection(contraption, Direction.DOWN);
		world.configureCenterOfRotation(contraptionLink, vector.centerOf(2, 3, 2));
		world.moveSection(contraptionLink, vector.of(0, -1, 0), 0);

		scene.idle(5);

		scene.overlay().showControls(util.vector().centerOf(2, 3, 2), Pointing.RIGHT, 40)
		.withItem(SimItems.HONEY_GLUE.asStack())
		.rightClick();
		scene.idle(5);
		final AABB bb2 = new AABB(util.grid().at(2, 3, 2));
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb2, bb2, 1);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, bb2, bb2.expandTowards(0, 1, 0), 40);
		scene.idle(40);

		world.setKineticSpeed(kineticsSide, 8);
		setSwivelCogKineticSpeed(scene, util, hinge, 8f);

		world.rotateSection(contraptionLink, 45, 0, 0, 18);

		scene.idle(18);

		world.setKineticSpeed(kineticsSide, -8);
		setSwivelCogKineticSpeed(scene, util, hinge, -8f);

		world.rotateSection(contraptionLink, -90, 0, 0, 36);

		scene.idle(36);

		world.setKineticSpeed(kineticsSide, 0);
		setSwivelCogKineticSpeed(scene, util, hinge, 0f);

		overlay.showText(160)
		.text("Rotation remains uninterrupted as the Hinge turns")
		.pointAt(vector.of(1.5, 4, 3))
		.attachKeyFrame()
		.placeNearTarget();

		scene.idle(60);
	}
}
