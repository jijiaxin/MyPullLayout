package com.mi.mypulllayout;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BaseBoardEntity {

	@SerializedName("id")
	private int id;

	@SerializedName("name")
	private String name;

	@SerializedName("icon")
	private String icon;

	@SerializedName("uri")
	private String uri;

	private int imageResId = -1;

	public BaseBoardEntity(int id, String name, String uri) {
		super();
		this.id = id;
		this.name = name;
		this.uri = uri;
	}

	public BaseBoardEntity(int id, String name, int imageResId) {
		super();
		this.id = id;
		this.name = name;
		this.imageResId = imageResId;
	}

	public static BaseBoardEntity fromJSONObjectToEntity(JSONObject json) {
		BaseBoardEntity board = new Gson().fromJson(json.toString(), BaseBoardEntity.class);
		return board;
	}

	public static ArrayList<BaseBoardEntity> fromJSONObjectToList(JSONArray json) {
		return new Gson().fromJson(json.toString(), new TypeToken<List<BaseBoardEntity>>() {}.getType());
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getIcon() {
		return icon;
	}

	public String getUri() {
		return uri;
	}

	public int getImageResId() {
		return imageResId;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public void setImageResId(int imageResId) {
		this.imageResId = imageResId;
	}

	@Override
	public String toString() {
		return "BaseBoardEntity [id=" + id + ", name=" + name + ", icon="
				+ icon + ", uri=" + uri + "]";
	}

}
