/*
 * Minecraft Forge
 * Copyright (c) 2016-2020.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.common.util;

import net.minecraft.block.PortalInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.TeleportationRepositioner;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.DimensionType;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.server.ServerWorld;

import java.util.Optional;
import java.util.function.Function;

/**
 * Interface for handling the placement of entities during dimension change.
 *
 * An implementation of this interface can be used to place the entity
 * in a safe location, or generate a return portal, for instance.
 *
 * See the {@link net.minecraft.world.Teleporter} class, which has
 * been patched to implement this interface, for a vanilla example.
 */
public interface ITeleporter {

    /**
     * Called to handle placing the entity in the new world.
     *
     * The initial position of the entity will be its
     * position in the origin world, multiplied horizontally
     * by the computed cross-dimensional movement factor.
     *
     * Note that the supplied entity has not yet been spawned
     * in the destination world at the time.
     *
     * @param entity           the entity to be placed
     * @param currentWorld     the entity's origin
     * @param destWorld        the entity's destination
     * @param yaw              the suggested yaw value to apply
     * @param repositionEntity a function to reposition the entity, which returns the new entity in the new dimension. This is the vanilla implementation of the dimension travel logic. If the supplied boolean is true, it is attempted to spawn a new portal.
     * @return the entity in the new World. Vanilla creates for most {@link Entity}s a new instance and copy the data. But <b>you are not allowed</b> to create a new instance for {@link PlayerEntity}s! Move the player and update its state, see {@link ServerPlayerEntity#changeDimension(net.minecraft.world.server.ServerWorld, ITeleporter)}
     */
    default Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
       return repositionEntity.apply(true);
    }

    // States if this teleporter is the vanilla instance
    default boolean isVanilla()
    {
        return getClass() == Teleporter.class;
    }

    // Replicated from the vanilla code for interface implementation. Return an empty optional if no portal was found.
    default Optional<TeleportationRepositioner.Result> findPortal(ServerWorld fromWorld, ServerWorld toWorld,
            Entity entity)
    {
        return new Teleporter(toWorld).func_242957_a(new BlockPos(entity.getPositionVec()),
                DimensionType.func_242715_a(fromWorld.func_230315_m_(), toWorld.func_230315_m_()) < 1,
                this.getPortalPOI());
    }

    // Creates a portal if one doesn't exist and returns the result. Default does nothing so you'll need to implement it for your portal.
    default Optional<TeleportationRepositioner.Result> createAndGetPortal(ServerWorld fromWorld, ServerWorld toWorld, Entity entity)
    {
        return Optional.empty();
    }

    // Returns the portal info for the result. Default returns the tpResult position and zero values for everything else.
    default PortalInfo getPortalInfo(TeleportationRepositioner.Result tpResult)
    {
        return new PortalInfo(new Vector3d(tpResult.field_243679_a.getX(), tpResult.field_243679_a.getY(), tpResult.field_243679_a.getZ()), Vector3d.ZERO, 0, 0);
    }
    
    // Scales the given blockpos based on the worlds passed in.
    default BlockPos getScaledPos(World fromWorld, World toWorld, BlockPos originalPos)
    {
    	WorldBorder worldborder = toWorld.getWorldBorder();
        double minX = Math.max(-2.9999872E7D, worldborder.minX() + 16.0D);
        double minZ = Math.max(-2.9999872E7D, worldborder.minZ() + 16.0D);
        double maxX = Math.min(2.9999872E7D, worldborder.maxX() - 16.0D);
        double maxZ = Math.min(2.9999872E7D, worldborder.maxZ() - 16.0D);
        double dimensionScaling = DimensionType.func_242715_a(fromWorld.func_230315_m_(), toWorld.func_230315_m_());
        return new BlockPos(MathHelper.clamp(originalPos.getX() * dimensionScaling, minX, maxX), originalPos.getY(), MathHelper.clamp(originalPos.getZ() * dimensionScaling, minZ, maxZ));
    }

    // Returns the point of interest type for teleporter to look for.
    default PointOfInterestType getPortalPOI()
    {
    	return PointOfInterestType.NETHER_PORTAL;
    }
}
