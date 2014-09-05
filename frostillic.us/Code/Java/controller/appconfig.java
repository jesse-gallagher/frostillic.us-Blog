package controller;

import config.AppConfig;
import config.Translation;
import java.util.*;
import java.io.*;

import com.ibm.xsp.extlib.util.ExtLibUtil;

import frostillicus.xsp.controller.BasicXPageController;
import frostillicus.xsp.util.FrameworkUtils;

public class appconfig extends BasicXPageController {
	private static final long serialVersionUID = 1L;

	@Override
	public void beforePageLoad() throws Exception {
		super.beforePageLoad();

		Map<String, Object> configData = new HashMap<String, Object>();
		AppConfig appConfig = AppConfig.get();
		for(String key : appConfig.keySet()) {
			configData.put(key, appConfig.getValue(key));
		}

		if(!configData.containsKey("profileLinks")) {
			configData.put("profileLinks", new ArrayList<Map<String, Object>>());
		}
		if(!configData.containsKey("aliases")) {
			configData.put("aliases", new ArrayList<Map<String, Object>>());
		}


		FrameworkUtils.getViewScope().put("appConfigData", configData);
	}

	@Override
	@SuppressWarnings("unchecked")
	public String save() throws IOException {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		Map<String, Object> appConfigData = (Map<String, Object>)viewScope.get("appConfigData");

		AppConfig appConfig = AppConfig.get();
		for(Map.Entry<String, Object> configEntry : appConfigData.entrySet()) {
			appConfig.setValue(configEntry.getKey(), configEntry.getValue());
		}
		appConfig.save();

		FrameworkUtils.flashMessage("confirmation", Translation.get().getValue("configChangeConfirmation"));

		return "xsp-success";
	}

	/* ******************************************************************************
	 * Profile Links
	 ********************************************************************************/
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getProfileLinks() {
		Map<String, Object> viewScope = FrameworkUtils.getViewScope();
		Map<String, Object> appConfigData = (Map<String, Object>)viewScope.get("appConfigData");
		return (List<Map<String, Object>>)appConfigData.get("profileLinks");
	}

	public void addLink() {
		getProfileLinks().add(new HashMap<String, Object>());
	}

	public void removeLink() {
		int index = (Integer)FrameworkUtils.resolveVariable("linkIndex");
		getProfileLinks().remove(index);
	}

	public void moveLinkUp() {
		int index = (Integer)FrameworkUtils.resolveVariable("linkIndex");
		Collections.swap(getProfileLinks(), index, index-1);
	}

	public void moveLinkDown() {
		int index = (Integer)FrameworkUtils.resolveVariable("linkIndex");
		Collections.swap(getProfileLinks(), index, index+1);
	}

	/* ******************************************************************************
	 * Aliases
	 ********************************************************************************/

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getAliases() {
		Map<String, Object> viewScope = FrameworkUtils.getViewScope();
		Map<String, Object> appConfigData = (Map<String, Object>)viewScope.get("appConfigData");
		return (List<Map<String, Object>>)appConfigData.get("aliases");
	}

	public void addAlias() {
		getAliases().add(new HashMap<String, Object>());
	}

	public void removeAlias() {
		int index = (Integer)FrameworkUtils.resolveVariable("aliasIndex");
		getAliases().remove(index);
	}

	public void moveAliasUp() {
		int index = (Integer)FrameworkUtils.resolveVariable("aliasIndex");
		Collections.swap(getAliases(), index, index-1);
	}

	public void moveAliasDown() {
		int index = (Integer)FrameworkUtils.resolveVariable("aliasIndex");
		Collections.swap(getAliases(), index, index+1);
	}
}
