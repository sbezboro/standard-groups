package com.sbezboro.standardgroups.model;

import java.util.Map;

import com.sbezboro.standardplugin.persistence.persistables.Persistable;
import com.sbezboro.standardplugin.persistence.persistables.PersistableImpl;

public class Claim extends PersistableImpl implements Persistable {
	
	private int x;
	private int z;
	private String world;

	@Override
	public void loadFromPersistance(Map<String, Object> map) {
		
	}

}
