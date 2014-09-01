package model;

import org.openntf.domino.Database;

import config.AppConfig;

import frostillicus.xsp.bean.ApplicationScoped;
import frostillicus.xsp.bean.ManagedBean;
import frostillicus.xsp.model.domino.AbstractDominoManager;
import frostillicus.xsp.model.domino.AbstractDominoModel;
import frostillicus.xsp.util.FrameworkUtils;

public class Comment extends AbstractDominoModel {
	private static final long serialVersionUID = 1L;

	@ManagedBean(name="Comments")
	@ApplicationScoped
	public static class Manager extends AbstractDominoManager<Comment> {
		private static final long serialVersionUID = 1L;

		public static Manager get() {
			Manager existing = (Manager)FrameworkUtils.resolveVariable(Manager.class.getAnnotation(ManagedBean.class).name());
			return existing == null ? new Manager() : existing;
		}

		@Override
		protected String getViewPrefix() {
			return "Comments\\";
		}
		@Override
		protected Database getDatabase() {
			AppConfig appConfig = AppConfig.get();
			String server = (String)appConfig.getValue("dataDatabaseServer");
			String filePath = (String)appConfig.getValue("dataDatabaseFilePath");
			return FrameworkUtils.getDatabase(server, filePath);
		}
	}
}
