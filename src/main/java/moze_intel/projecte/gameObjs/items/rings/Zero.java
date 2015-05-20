package moze_intel.projecte.gameObjs.items.rings;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import com.google.common.collect.Lists;
import moze_intel.projecte.api.IModeChanger;
import moze_intel.projecte.api.IPedestalItem;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.items.ItemCharge;
import moze_intel.projecte.gameObjs.tiles.DMPedestalTile;
import moze_intel.projecte.utils.MathUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

import java.util.List;

@Optional.Interface(iface = "baubles.api.IBauble", modid = "Baubles")
public class Zero extends ItemCharge implements IModeChanger, IBauble, IPedestalItem
{
	private int coolCooldown;

	public Zero() 
	{
		super("zero_ring", (byte)4);
		this.setContainerItem(this);
		this.setNoRepair();
	}
	
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) 
	{
		super.onUpdate(stack, world, entity, par4, par5);
		
		if (world.isRemote || par4 > 8 || stack.getItemDamage() == 0)
		{
			return;
		}

		AxisAlignedBB box = new AxisAlignedBB(entity.posX - 3, entity.posY - 3, entity.posZ - 3, entity.posX + 3, entity.posY + 3, entity.posZ + 3);
		freezeInBoundingBox(world, box);

	}

	public void freezeInBoundingBox(World world, AxisAlignedBB box)
	{
		for (int x = (int) box.minX; x <= box.maxX; x++)
		{
			for (int y = (int) box.minY; y <= box.maxY; y++)
			{
				for (int z = (int) box.minZ; z <= box.maxZ; z++)
				{
					Block b = world.getBlock(x, y, z);

					if (b == Blocks.water || b == Blocks.flowing_water)
					{
						world.setBlock(x, y, z, Blocks.ice);
					}
					else if (b.isSideSolid(world, x, y, z, ForgeDirection.UP))
					{
						Block b2 = world.getBlock(x, y + 1, z);

						if (b2 == Blocks.air)
						{
							world.setBlock(x, y + 1, z, Blocks.snow_layer);
						}
					}
				}
			}
		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		if (!world.isRemote)
		{
			int offset = 3 + this.getCharge(stack);
			AxisAlignedBB box = player.boundingBox.expand(offset, offset, offset);
			world.playSoundAtEntity(player, "projecte:item.pepower", 1.0F, 1.0F);
			freezeInBoundingBox(world, box);
		}
		
		return stack;
	}
	
	@Override
	public boolean doesContainerItemLeaveCraftingGrid(ItemStack stack)
	{
		return false;
	}

	@Override
	public byte getMode(ItemStack stack)
	{
		return (byte) stack.getItemDamage();
	}

	@Override
	public void changeMode(EntityPlayer player, ItemStack stack) 
	{
		stack.setItemDamage(stack.getItemDamage() == 0 ? 1 : 0);
	}
	
	@Override
	@Optional.Method(modid = "Baubles")
	public baubles.api.BaubleType getBaubleType(ItemStack itemstack)
	{
		return BaubleType.RING;
	}

	@Override
	@Optional.Method(modid = "Baubles")
	public void onWornTick(ItemStack stack, EntityLivingBase player) 
	{
		this.onUpdate(stack, player.worldObj, player, 0, false);
	}

	@Override
	@Optional.Method(modid = "Baubles")
	public void onEquipped(ItemStack itemstack, EntityLivingBase player) {}

	@Override
	@Optional.Method(modid = "Baubles")
	public void onUnequipped(ItemStack itemstack, EntityLivingBase player) {}

	@Override
	@Optional.Method(modid = "Baubles")
	public boolean canEquip(ItemStack itemstack, EntityLivingBase player) 
	{
		return true;
	}

	@Override
	@Optional.Method(modid = "Baubles")
	public boolean canUnequip(ItemStack itemstack, EntityLivingBase player) 
	{
		return true;
	}

	@Override
	public void updateInPedestal(World world, BlockPos pos)
	{
		if (!world.isRemote && ProjectEConfig.zeroPedCooldown != -1)
		{
			if (coolCooldown == 0) {
				TileEntity tile = world.getTileEntity(pos);
				AxisAlignedBB aabb = ((DMPedestalTile) tile).getEffectBounds();
				freezeInBoundingBox(world, aabb);
				List<Entity> list = world.getEntitiesWithinAABB(Entity.class, aabb);
				for (Entity ent : list)
				{
					if (ent.isBurning())
					{
						ent.extinguish();
					}
				}
				coolCooldown = ProjectEConfig.zeroPedCooldown;
			}
			else
			{
				coolCooldown--;
			}
		}
	}

	@Override
	public List<String> getPedestalDescription()
	{
		List<String> list = Lists.newArrayList();
		if (ProjectEConfig.zeroPedCooldown != -1) {
			list.add(EnumChatFormatting.BLUE + StatCollector.translateToLocal("pe.zero.pedestal1"));
			list.add(EnumChatFormatting.BLUE + StatCollector.translateToLocal("pe.zero.pedestal2"));
			list.add(EnumChatFormatting.BLUE + String.format(
					StatCollector.translateToLocal("pe.zero.pedestal3"), MathUtils.tickToSecFormatted(ProjectEConfig.zeroPedCooldown)));
		}
		return list;
	}
}
