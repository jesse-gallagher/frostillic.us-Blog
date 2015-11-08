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

		Map<String, Object> configData = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
		AppConfig appConfig = AppConfig.get();
		for(String key : appConfig.keySet()) {
			configData.put(key, appConfig.getValue(key));
		}

		if(!configData.containsKey("profileLinks") || "".equals(configData.get("profileLinks"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			configData.put("profileLinks", new ArrayList<Map<String, Object>>()); //$NON-NLS-1$
		}
		if(!configData.containsKey("aliases") || "".equals(configData.get("aliases"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			configData.put("aliases", new ArrayList<Map<String, Object>>()); //$NON-NLS-1$
		}


		FrameworkUtils.getViewScope().put("appConfigData", configData); //$NON-NLS-1$
	}

	@Override
	@SuppressWarnings("unchecked")
	public String save() throws IOException {
		Map<String, Object> viewScope = ExtLibUtil.getViewScope();
		Map<String, Object> appConfigData = (Map<String, Object>)viewScope.get("appConfigData"); //$NON-NLS-1$

		AppConfig appConfig = AppConfig.get();
		for(Map.Entry<String, Object> configEntry : appConfigData.entrySet()) {
			appConfig.setValue(configEntry.getKey(), configEntry.getValue());
		}
		appConfig.save();

		FrameworkUtils.flashMessage("confirmation", Translation.get().getValue("configChangeConfirmation")); //$NON-NLS-1$ //$NON-NLS-2$

		return "xsp-success"; //$NON-NLS-1$
	}

	/* ******************************************************************************
	 * Profile Links
	 ********************************************************************************/
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getProfileLinks() {
		Map<String, Object> viewScope = FrameworkUtils.getViewScope();
		Map<String, Object> appConfigData = (Map<String, Object>)viewScope.get("appConfigData"); //$NON-NLS-1$
		return (List<Map<String, Object>>)appConfigData.get("profileLinks"); //$NON-NLS-1$
	}

	public void addLink() {
		getProfileLinks().add(new HashMap<String, Object>());
	}

	public void removeLink() {
		int index = (Integer)FrameworkUtils.resolveVariable("linkIndex"); //$NON-NLS-1$
		getProfileLinks().remove(index);
	}

	public void moveLinkUp() {
		int index = (Integer)FrameworkUtils.resolveVariable("linkIndex"); //$NON-NLS-1$
		Collections.swap(getProfileLinks(), index, index-1);
	}

	public void moveLinkDown() {
		int index = (Integer)FrameworkUtils.resolveVariable("linkIndex"); //$NON-NLS-1$
		Collections.swap(getProfileLinks(), index, index+1);
	}

	/* ******************************************************************************
	 * Aliases
	 ********************************************************************************/

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getAliases() {
		Map<String, Object> viewScope = FrameworkUtils.getViewScope();
		Map<String, Object> appConfigData = (Map<String, Object>)viewScope.get("appConfigData"); //$NON-NLS-1$
		return (List<Map<String, Object>>)appConfigData.get("aliases"); //$NON-NLS-1$
	}

	public void addAlias() {
		getAliases().add(new HashMap<String, Object>());
	}

	public void removeAlias() {
		int index = (Integer)FrameworkUtils.resolveVariable("aliasIndex"); //$NON-NLS-1$
		getAliases().remove(index);
	}

	public void moveAliasUp() {
		int index = (Integer)FrameworkUtils.resolveVariable("aliasIndex"); //$NON-NLS-1$
		Collections.swap(getAliases(), index, index-1);
	}

	public void moveAliasDown() {
		int index = (Integer)FrameworkUtils.resolveVariable("aliasIndex"); //$NON-NLS-1$
		Collections.swap(getAliases(), index, index+1);
	}
}
