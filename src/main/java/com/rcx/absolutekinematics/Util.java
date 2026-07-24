package com.rcx.absolutekinematics;

import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Util {

	public static VoxelShape[] get6directionShapes(VoxelShape shape) {
		VoxelShape[] shapes = new VoxelShape[6];
		for (Direction direction : Direction.values()) {
			shapes[direction.get3DDataValue()] = rotateVoxelShape(direction, shape);
		}
		return shapes;
	}

	public static VoxelShape rotateVoxelShape(Direction to, VoxelShape shape) {
		return rotateVoxelShape(Direction.UP, to, shape);
	}

	private static VoxelShape rotated = Shapes.empty();
	public static VoxelShape rotateVoxelShape(Direction from, Direction to, VoxelShape shape) {
		if (from == to)
			return shape;
		rotated = Shapes.empty();

		Vec3i vecF = from.getNormal();
		Vec3i vecT = to.getNormal();

		shape = shape.move(-0.5, -0.5, -0.5);

		int[][] map = new int[3][3];
		int[] skip = new int[] { -1, -1, -1 };
		boolean opposites = from.getOpposite() == to;

		for (int i = 0; i < 3; ++i) {
			int f = vecF.get(Axis.VALUES[i]);
			for (int j = 0; j < 3; ++j) {
				int k = j;
				if (!opposites)
					k = 2 - k;
				if (k == skip[0] || k == skip[1] || k == skip[2])
					continue;
				int t = vecT.get(Axis.VALUES[k]);
				if (t == f) {
					map[k][i] = 1;
					skip[i] = k;
					break;
				} else if (t == -f) {
					map[k][i] = -1;
					skip[i] = k;
					break;
				}
			}
		}

		shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> rotated = Shapes.joinUnoptimized(rotated,
				Shapes.create(
						Math.min(minX * map[0][0], maxX * map[0][0]) + Math.min(minY * map[0][1], maxY * map[0][1]) + Math.min(minZ * map[0][2], maxZ * map[0][2]),
						Math.min(minX * map[1][0], maxX * map[1][0]) + Math.min(minY * map[1][1], maxY * map[1][1]) + Math.min(minZ * map[1][2], maxZ * map[1][2]),
						Math.min(minX * map[2][0], maxX * map[2][0]) + Math.min(minY * map[2][1], maxY * map[2][1]) + Math.min(minZ * map[2][2], maxZ * map[2][2]),

						Math.max(minX * map[0][0], maxX * map[0][0]) + Math.max(minY * map[0][1], maxY * map[0][1]) + Math.max(minZ * map[0][2], maxZ * map[0][2]),
						Math.max(minX * map[1][0], maxX * map[1][0]) + Math.max(minY * map[1][1], maxY * map[1][1]) + Math.max(minZ * map[1][2], maxZ * map[1][2]),
						Math.max(minX * map[2][0], maxX * map[2][0]) + Math.max(minY * map[2][1], maxY * map[2][1]) + Math.max(minZ * map[2][2], maxZ * map[2][2])),
				BooleanOp.OR));

		return rotated.move(0.5, 0.5, 0.5).optimize();
	}

	public static Direction.Axis getOtherAxis(Direction.Axis axis1, Direction.Axis axis2) {
		switch (axis1) {
		default:
			return axis2;
		case X:
			switch (axis2) {
			case Y: return Direction.Axis.Z;
			case Z: return Direction.Axis.Y;
			default: return axis2;
			}
		case Y:
			switch (axis2) {
			case X: return Direction.Axis.Z;
			case Z: return Direction.Axis.X;
			default: return axis2;
			}
		case Z:
			switch (axis2) {
			case X: return Direction.Axis.Y;
			case Y: return Direction.Axis.X;
			default: return axis2;
			}
		}
	}

	public static LangBuilder langBuilder() {
		return Lang.builder(AbsoluteKinematics.MODID);
	}

	public static LangBuilder translate(final String key, final Object... args) {
		return langBuilder().translate(key, args);
	}
}
