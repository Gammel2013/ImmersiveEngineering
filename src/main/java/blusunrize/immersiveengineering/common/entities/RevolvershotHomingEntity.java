/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.common.register.IEEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class RevolvershotHomingEntity extends RevolvershotEntity
{
	public int trackCountdown = 5;
	public double redirectionSpeed = .25;
	public LivingEntity targetOverride;

	public RevolvershotHomingEntity(EntityType<? extends RevolvershotHomingEntity> type, Level world)
	{
		super(type, world);
	}

	public RevolvershotHomingEntity(EntityType<? extends RevolvershotHomingEntity> eType, Level world, double x, double y, double z, double ax, double ay, double az, IBullet type)
	{
		super(eType, world, null, x, y, z, ax, ay, az, type);
	}

	public RevolvershotHomingEntity(Level world, double x, double y, double z, double ax, double ay, double az, IBullet type)
	{
		this(IEEntityTypes.HOMING_REVOLVERSHOT.get(), world, x, y, z, ax, ay, az, type);
	}

	public RevolvershotHomingEntity(Level world, LivingEntity living, double ax, double ay, double az, IBullet type)
	{
		super(IEEntityTypes.HOMING_REVOLVERSHOT.get(), world, living, ax, ay, az, type);
	}

	public RevolvershotHomingEntity(EntityType<? extends RevolvershotHomingEntity> type, Level world, LivingEntity living, double ax, double ay, double az, IBullet type1)
	{
		super(type, world, living, ax, ay, az, type1);
	}

	@Override
	public void tick()
	{
		super.tick();

		if(!level().isClientSide&&this.tickCount > trackCountdown)
		{
			LivingEntity target = getTarget();
			if(target!=null)
			{
				Vec3 oldMotion = getDeltaMovement();
				Vec3 newMotion = new Vec3(
						oldMotion.x*(1-redirectionSpeed)+(target.getX()-this.getX())*redirectionSpeed,
						oldMotion.y*(1-redirectionSpeed)+((target.getY()+target.getBbHeight()/2)-this.getY())*redirectionSpeed,
						oldMotion.z*(1-redirectionSpeed)+(target.getZ()-this.getZ())*redirectionSpeed).normalize();

				setDeltaMovement(newMotion);
			}
		}
	}

	public LivingEntity getTarget()
	{
		if(targetOverride!=null&&targetOverride.isAlive())
			return targetOverride;
		double r = 20D;
		AABB aabb = new AABB(getX()-r, getY()-r, getZ()-r, getX()+r, getY()+r, getZ()+r);
		LivingEntity target = null;
		for(Object o : level().getEntitiesOfClass(LivingEntity.class, aabb))
			if(o instanceof LivingEntity&&!((LivingEntity)o).getUUID().equals(this.shooterUUID))
				if(target==null||((LivingEntity)o).distanceToSqr(this) < target.distanceToSqr(this))
					target = (LivingEntity)o;
		return target;
	}
}