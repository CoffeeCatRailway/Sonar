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
package com.example.examplemod;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.Logger;

import com.example.examplemod.init.ExampleBlocks;
import com.example.examplemod.init.ExampleItems;
import com.ocelot.api.init.Registry;
import com.ocelot.api.utils.OnlineRequest;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = ExampleMod.MODID, name = ExampleMod.NAME, version = ExampleMod.VERSION)
public class ExampleMod
{
	public static final String MODID = "examplemod";
	public static final String NAME = "Example Mod";
	public static final String VERSION = "1.0";
	public static final Registry REGISTRY = new Registry();

	static
	{
		ExampleItems.init(REGISTRY);
		ExampleBlocks.init(REGISTRY);
	}

	private static Logger logger;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		logger = event.getModLog();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		// some example code

		OnlineRequest.make("https://thestayathomechef.com/wp-content/uploads/2013/01/Roasted-Chicken-1-e1544064817373.jpg", stream ->
		{
			try
			{
				System.out.println("Saving PNG to file!");
				ImageIO.write(ImageIO.read(stream), "PNG", new File("test.png"));
			}
			catch (IOException e)
			{
				logger.error("Could not download image!", e);
			}
		});
	}
}
