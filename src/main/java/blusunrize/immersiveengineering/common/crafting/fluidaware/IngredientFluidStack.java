/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.fluidaware;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author BluSunrize - 03.07.2017
 */
public class IngredientFluidStack extends Ingredient
{
	public static final Codec<IngredientFluidStack> CODEC = FluidTagInput.CODEC.xmap(
			IngredientFluidStack::new, IngredientFluidStack::getFluidTagInput
	);

	private final FluidTagInput fluidTagInput;

	public IngredientFluidStack(FluidTagInput fluidTagInput)
	{
		super(Stream.empty());
		this.fluidTagInput = fluidTagInput;
	}

	public IngredientFluidStack(TagKey<Fluid> tag, int amount)
	{
		this(new FluidTagInput(tag, amount, null));
	}

	public FluidTagInput getFluidTagInput()
	{
		return fluidTagInput;
	}

	ItemStack[] cachedStacks;

	@Nonnull
	@Override
	public ItemStack[] getItems()
	{
		if(cachedStacks==null)
			cachedStacks = this.fluidTagInput.getMatchingFluidStacks()
					.stream()
					.map(FluidUtil::getFilledBucket)
					.filter(s -> !s.isEmpty())
					.toArray(ItemStack[]::new);
		return this.cachedStacks;
	}

	@Override
	public boolean isEmpty()
	{
		return false;
		// todo? I don't think there is a way to do this now, because the tag isn't bound yet on world load
		// this.fluidTagInput.getMatchingFluidStacks().isEmpty();
	}

	@Override
	public boolean test(@Nullable ItemStack stack)
	{
		if(stack==null||stack.isEmpty())
			return false;
		Optional<IFluidHandlerItem> handler = FluidUtil.getFluidHandler(stack).resolve();
		return handler.isPresent()&&fluidTagInput.extractFrom(handler.get(), FluidAction.SIMULATE);
	}

	@Override
	public boolean isSimple()
	{
		return false;
	}

	public ItemStack getExtractedStack(ItemStack input)
	{
		Optional<IFluidHandlerItem> handlerOpt = FluidUtil.getFluidHandler(ItemHandlerHelper.copyStackWithSize(input, 1)).resolve();
		if(handlerOpt.isPresent())
		{
			IFluidHandlerItem handler = handlerOpt.get();
			fluidTagInput.extractFrom(handler, FluidAction.EXECUTE);
			return handler.getContainer();
		}
		return input.getCraftingRemainingItem();
	}

	@Override
	@NotNull
	public JsonElement toJson(boolean p_301239_)
	{
		// TODO why is this necessary?
		// TODO do I need to add the type somewhere?
		return CODEC.encodeStart(JsonOps.INSTANCE, this).result().orElseThrow();
	}
}
