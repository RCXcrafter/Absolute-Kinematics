package com.rcx.absolutekinematics.ponder;

import com.rcx.absolutekinematics.AbsoluteKinematics;
import com.rcx.absolutekinematics.KinematicsRegistry;
import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;

import dev.simulated_team.simulated.index.SimPonderTags;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class KinematicsPonders implements PonderPlugin {

	@Override
	public String getModId() {
		return AbsoluteKinematics.MODID;
	}

	@Override
	public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
		helper.forComponents(KinematicsRegistry.HINGE.getId())
		.addStoryBoard("hinge/intro", HingeScenes::hingeIntro)
		.addStoryBoard("hinge/unlocking", HingeScenes::hingeUnlocking)
		.addStoryBoard("hinge/passthrough", HingeScenes::hingePassthrough);

		helper.forComponents(KinematicsRegistry.BALL_JOINT.getId())
		.addStoryBoard("ball_joint/intro", BallJointScenes::ballJointIntro)
		.addStoryBoard("ball_joint/locking", BallJointScenes::ballJointLocking)
		.addStoryBoard("ball_joint/passthrough", BallJointScenes::ballJointPassthrough);
	}

	@Override
	public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
		helper.addToTag(AllCreatePonderTags.KINETIC_APPLIANCES)
		.add(KinematicsRegistry.HINGE.getId())
		.add(KinematicsRegistry.BALL_JOINT.getId());
		helper.addToTag(AllCreatePonderTags.MOVEMENT_ANCHOR)
		.add(KinematicsRegistry.HINGE.getId())
		.add(KinematicsRegistry.BALL_JOINT.getId());
		helper.addToTag(SimPonderTags.PHYSICS_BEHAVIOR)
		.add(KinematicsRegistry.HINGE.getId())
		.add(KinematicsRegistry.BALL_JOINT.getId());
	}
}
