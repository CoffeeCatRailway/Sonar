/*******************************************************************************
 * Copyright 2019 Brandon
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.ocelot.api.init;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.Validate;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * 
 * Registers the basics required for most mods into the game.
 * 
 * @author Ocelot5836
 * @since Jun 6, 2019
 *
 */
public class Registry
{
	/** The collection of items to be registered */
	private Set<Item> items;
	/** The collection of blocks to be registered */
	private Set<Block> blocks;

	public Registry()
	{
		this.items = new HashSet<Item>();
		this.blocks = new HashSet<Block>();
		MinecraftForge.EVENT_BUS.register(this);
	}

	/**
	 * Adds the specified item to the game.
	 * 
	 * @param item The item to add
	 */
	public void registerItem(Item item)
	{
		Validate.notNull(item, "Item cannot be null");
		Validate.notNull(item.getRegistryName(), "Registry name cannot be null");
		this.items.add(item);
	}

	/**
	 * Adds the specified block to the game under the specified item.
	 * 
	 * @param block The block to add
	 * @param item  The item to use as the block
	 */
	public void registerBlock(Block block, Item item)
	{
		this.registerBlockOnly(block);
		this.registerItem(item.setRegistryName(block.getRegistryName()));
	}

	/**
	 * Adds the specified block to the game under a generated {@link ItemBlock}.
	 * 
	 * @param block The block to add
	 */
	public void registerBlock(Block block)
	{
		this.registerBlockOnly(block);
		this.registerItem(new ItemBlock(block).setRegistryName(block.getRegistryName()));
	}

	/**
	 * Adds the specified block to the game.
	 * 
	 * @param block The block to add
	 */
	public void registerBlockOnly(Block block)
	{
		Validate.notNull(block, "Block cannot be null");
		Validate.notNull(block.getRegistryName(), "Registry name cannot be null");
		this.blocks.add(block);
	}

	@SubscribeEvent
	public void onRegisterItemEvent(RegistryEvent.Register<Item> event)
	{
		event.getRegistry().registerAll(this.items.toArray(new Item[0]));
		this.items.clear();
	}

	@SubscribeEvent
	public void onRegisterBlockEvent(RegistryEvent.Register<Block> event)
	{
		event.getRegistry().registerAll(this.blocks.toArray(new Block[0]));
		this.blocks.clear();
	}
}
