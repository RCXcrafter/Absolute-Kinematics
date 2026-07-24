package com.rcx.absolutekinematics.datagen;

import com.rcx.absolutekinematics.AbsoluteKinematics;
import com.rcx.absolutekinematics.KinematicsRegistry;
import com.rcx.absolutekinematics.ponder.KinematicsPonders;

import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class KinematicsLang extends LanguageProvider {

	public KinematicsLang(PackOutput gen) {
		super(gen, AbsoluteKinematics.MODID, "en_us");
	}

	@Override
	protected void addTranslations() {
		add("simulated.simulated_section." + AbsoluteKinematics.MODID, "Absolute Kinematics");

		addBlock(KinematicsRegistry.HINGE, "Hinge");
		addBlock(KinematicsRegistry.HINGE_LEAF, "Hinge Link Block");
		add(AbsoluteKinematics.MODID + ".hinge.too_fast", "Too Fast");
		add(AbsoluteKinematics.MODID + ".hinge.too_fast_error", "It appears that this _Hinge_ is rotating _too fast_.");
		add(AbsoluteKinematics.MODID + ".extra_kinetics.extra_axle", "Hinge Shaft");

		addBlock(KinematicsRegistry.BALL_JOINT, "Ball Joint");
		addBlock(KinematicsRegistry.BALL_JOINT_PLATE, "Ball Joint Link Block");

		PonderIndex.addPlugin(new KinematicsPonders());
		PonderIndex.getLangAccess().provideLang(AbsoluteKinematics.MODID, this::add);
	}
}
