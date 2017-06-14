package com.astraeus.entity;

import com.astraeus.cache.def.ItemDefinition;
import com.astraeus.entity.model.Model;

public final class Item extends Renderable {

	public final Model getRotatedModel() {
		ItemDefinition itemDef = ItemDefinition.lookup(ID);
		return itemDef.getModel(itemCount);
	}

	public int ID;
	public int x;
	public int y;
	public int itemCount;
}
