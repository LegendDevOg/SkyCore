package com.skytonia.SkyCore.cosmetics.pets.pathfinders;

import com.skytonia.SkyCore.cosmetics.pets.entities.PetZombieSource;
import net.minecraft.server.v1_9_R2.EntityPlayer;
import net.minecraft.server.v1_9_R2.PathEntity;
import net.minecraft.server.v1_9_R2.PathfinderGoal;
import net.minecraft.server.v1_9_R2.RandomPositionGenerator;
import net.minecraft.server.v1_9_R2.Vec3D;
import org.bukkit.Location;

import java.util.Random;

/**
 * Created by Chris Brown (OhBlihv) on 4/10/2017.
 */
public class PathfinderPetActions extends PathfinderGoal
{

	protected static final double MIN_RANGE = 2D,
								  MAX_RANGE = 16;

	protected final Random random = new Random();

	protected final PetZombieSource entity;
	protected final EntityPlayer owningPlayer;
	protected final double speed;

	protected int pathingDelay = 0;
	protected int jumpDelay = 0;
	protected int actionDelay = 0;

	protected static final int
		DELAY_DEFAULT_PATHING = 3,
		DELAY_DEFAULT_JUMP = 25,
		DELAY_DEFAULT_ACTION = 50;

	protected PathEntity path;
	
	protected ActivePathfinderAction activePathfinder = null;
	
	public PathfinderPetActions(PetZombieSource var1, EntityPlayer owningPlayer, double speed)
	{
		this.entity = var1;
		this.owningPlayer = owningPlayer;
		this.speed = speed;
	}
	
	public boolean a()
	{
		if(this.owningPlayer == null || !this.owningPlayer.isAlive())
		{
			return path != null;
		}
		
		if(shouldPath())
		{
			pathingDelay = DELAY_DEFAULT_PATHING;
			
			c();
			
			if(path != null && activePathfinder != null && activePathfinder.getPathfinderAction() != PathfinderAction.FOLLOW_PLAYER)
			{
				//BUtil.log("Started movement pathing. Actions cancelled. Ticks Left: (" + activePathfinder.ticksLeft + ")");
				activePathfinder = new ActivePathfinderAction(PathfinderAction.FOLLOW_PLAYER, -1);
				
				actionDelay = 0; //Reset the action cooldown in order to start actions immediately once free
			}
		}
		
		if(activePathfinder == null || (path == null && activePathfinder.getPathfinderAction() == PathfinderAction.FOLLOW_PLAYER))
		{
			activePathfinder = new ActivePathfinderAction();
		}
		
		int rolledNumber = random.nextInt(100);
		if(activePathfinder.getPathfinderAction() != PathfinderAction.FOLLOW_PLAYER && --actionDelay < 0)
		{
			if(activePathfinder.getPathfinderAction() == PathfinderAction.NONE)
			{
				PathfinderAction selectedAction = null;
				int actionLength = 200;
				
				//Random Look Around
				/*if(rolledNumber < 15)
				{
					selectedAction = PathfinderAction.RANDOM_LOOKAROUND;
					actionLength = TICK_LENGTH_RANDOMLOOKAROUND;
				}*/
				//Random Walkaround (Idle)
				/*else */if(path == null && rolledNumber < 25)
				{
					selectedAction = PathfinderAction.RANDOM_WALKAROUND;
					actionLength = TICK_LENGTH_RANDOMWALKAROUND;
				}
				//Look at Player
				else if(path != null || rolledNumber < 25)
				{
					selectedAction = PathfinderAction.LOOK_AT_PLAYER;
					actionLength = TICK_LENGTH_LOOKATPLAYER;
				}
				
				if(selectedAction != null)
				{
					activePathfinder = new ActivePathfinderAction(selectedAction, actionLength);
					//BUtil.log("Selected '" + selectedAction + "'");
				}
			}
			
			switch(activePathfinder.getPathfinderAction())
			{
				case LOOK_AT_PLAYER:
				{
					doLookAtPlayer();
					break;
				}
				case RANDOM_LOOKAROUND:
				{
					doRandomLookAround();
					break;
				}
				case RANDOM_WALKAROUND:
				{
					doRandomWalkAround();
					break;
				}
			}
			
			if(activePathfinder.tick())
			{
				//BUtil.log("Pathfinder " + activePathfinder.pathfinderAction + " expired... (" + activePathfinder.ticksLeft + ")");
				activePathfinder = null;
			}
			
			actionDelay = DELAY_DEFAULT_ACTION;
		}
		
		if(--jumpDelay < 0)
		{
			boolean canJump;
			//Not Moving
			if(path == null)
			{
				canJump = rolledNumber < 10;
			}
			//Moving
			else
			{
				canJump = rolledNumber < 75;
			}
			
			if(canJump)
			{
				//Jump
				entity.jump();
			}
			
			jumpDelay = DELAY_DEFAULT_JUMP;
		}
		
		return path != null;
	}
	
	public boolean shouldPath()
	{
		if(--pathingDelay > 0)
		{
			return false;
		}
		
		double distance = Math.sqrt(this.entity.h(this.owningPlayer));
		if(distance > MAX_RANGE)
		{
			if(owningPlayer.getBukkitEntity().isOnGround())
			{
				entity.getBukkitEntity().teleport(owningPlayer.getBukkitEntity().getLocation());
				return true;
			}
			
			return false;
		}
		else
		{
			if(activePathfinder != null && activePathfinder.getPathfinderAction() == PathfinderAction.RANDOM_WALKAROUND)
			{
				return distance >= (3 * MIN_RANGE);
			}
			else
			{
				return distance >= MIN_RANGE;
			}
		}
	}
	
	public void c()
	{
		Location playerLocation = owningPlayer.getBukkitEntity().getLocation();
		
		path = entity.getNavigation().a(playerLocation.getX() + 1.0D, playerLocation.getY(), playerLocation.getZ() + 1.0D);
		
		if(path != null)
		{
			double speed = this.speed;
			if(owningPlayer.isSprinting())
			{
				speed *= 1.25;
			}
			
			this.entity.getNavigation().a(path, speed);
		}
	}
	
	/*
	 * Merged Pathfinder Methods
	 */
	
	static final int
		TICK_LENGTH_LOOKATPLAYER = 15,
		TICK_LENGTH_RANDOMLOOKAROUND = 5,
		TICK_LENGTH_RANDOMWALKAROUND = 10;
	
	private void doLookAtPlayer()
	{
		this.entity.getControllerLook().a(this.owningPlayer.locX,
		                                  this.owningPlayer.locY/* + (double) this.owningPlayer.getHeadHeight()*/,
		                                  this.owningPlayer.locZ,
		                                  (float) this.entity.cF(), (float) this.entity.N());
	}
	
	private void doRandomLookAround()
	{
		double var1 = 6.283185307179586D * random.nextDouble();
		
		this.entity.getControllerLook().a(this.entity.locX + Math.cos(var1),
		                                  this.entity.locY + (double)this.entity.getHeadHeight(),
		                                  this.entity.locZ + Math.sin(var1),
		                                  (float)this.entity.cF(), (float)this.entity.N());
	}
	
	private void doRandomWalkAround()
	{
		Vec3D var1 = RandomPositionGenerator.a(this.entity, 4, 1);
		if(var1 != null)
		{
			this.entity.getNavigation().a(entity.getNavigation().a(var1.x, var1.y, var1.z), speed / 2D);
		}
	}

}

