package com.rcx.absolutekinematics;

import com.rcx.absolutekinematics.client.KinematicsPartialModels;
import com.rcx.absolutekinematics.ponder.KinematicsPonders;
import com.rcx.absolutekinematics.visuals.BallJointVisual;
import com.rcx.absolutekinematics.visuals.JointPlateBlockRenderer;
import com.rcx.absolutekinematics.visuals.JointPlateVisual;
import com.rcx.absolutekinematics.visuals.HingeVisual;

import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.createmod.ponder.foundation.PonderIndex;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = AbsoluteKinematics.MODID, dist = Dist.CLIENT)
public class AbsoluteKinematicsClient {

	public AbsoluteKinematicsClient(IEventBus modEventBus, ModContainer container) {
		modEventBus.addListener(this::onClientSetup);
		modEventBus.addListener(this::registerRenderers);
		container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
	}

	void onClientSetup(FMLClientSetupEvent event) {
		KinematicsPartialModels.init();

		PonderIndex.addPlugin(new KinematicsPonders());

		SimpleBlockEntityVisualizer.builder(KinematicsRegistry.HINGE_ENTITY.get()).factory(HingeVisual::new).apply();
		SimpleBlockEntityVisualizer.builder(KinematicsRegistry.HINGE_LEAF_ENTITY.get()).factory(JointPlateVisual::new).apply();

		SimpleBlockEntityVisualizer.builder(KinematicsRegistry.BALL_JOINT_ENTITY.get()).factory(BallJointVisual::new).apply();
		SimpleBlockEntityVisualizer.builder(KinematicsRegistry.BALL_JOINT_PLATE_ENTITY.get()).factory(JointPlateVisual::new).apply();
	}

	void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(KinematicsRegistry.HINGE_LEAF_ENTITY.get(), JointPlateBlockRenderer::new);
		event.registerBlockEntityRenderer(KinematicsRegistry.BALL_JOINT_PLATE_ENTITY.get(), JointPlateBlockRenderer::new);
	}
}
