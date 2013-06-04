package org.appdev.entity;


public abstract class Entity extends Base {
	protected String cacheKey;
	
	public enum entityType{
		TYPE_LESSON, TYPE_QUIZ
	};

	protected int type;
	
	public String getCacheKey() {
		return cacheKey;
	}

	public void setCacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
	}
	
	public void setEntityType(int type){
		this.type = type;
		
	}
	
	public int getEntityType(){
		return type;
	}
	
}
