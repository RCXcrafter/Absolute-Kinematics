package com.rcx.absolutekinematics.client;

import com.rcx.absolutekinematics.AbsoluteKinematics;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;

public class KinematicsPartialModels {

	public static final PartialModel HINGE_AXLE = block("hinge_axle");
	public static final PartialModel CARDAN_BOTTOM = block("cardan_bottom");
	public static final PartialModel CARDAN_JOINT_BOTTOM = block("cardan_joint_bottom");
	public static final PartialModel CARDAN_MIDDLE = block("cardan_middle");
	public static final PartialModel CARDAN_TOP = block("cardan_top");
	public static final PartialModel CARDAN_JOINT_TOP = block("cardan_joint_top");

	public static PartialModel block(String path) {
		return PartialModel.of(ResourceLocation.fromNamespaceAndPath(AbsoluteKinematics.MODID, "block/" + path));
	}

	public static void init() {
		//init static fields
	}
}
