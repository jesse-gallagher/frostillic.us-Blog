package model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.validation.constraints.NotNull;

import org.openntf.domino.*;

import config.AppConfig;

import frostillicus.xsp.bean.ApplicationScoped;
import frostillicus.xsp.bean.ManagedBean;
import frostillicus.xsp.model.domino.AbstractDominoManager;
import frostillicus.xsp.model.domino.AbstractDominoModel;
import frostillicus.xsp.util.FrameworkUtils;

public class Post extends AbstractDominoModel {
	private static final long serialVersionUID = 1L;

	@NotNull Date posted;
	@NotNull PostStatus status;
	String thread;
	List<String> tags;


	@Override
	public void initFromDatabase(final Database database) {
		super.initFromDatabase(database);

		setValue("Form", "Post");
		setValue("Posted", new Date());
		setValue("$$Creator", FrameworkUtils.getUserName());
		setValue("Status", PostStatus.Draft);
	}

	public List<Comment> getComments() {
		Comment.Manager comments = Comment.Manager.get();
		return comments.getNamedCollection("By PostID", String.valueOf(getValue("PostID")));
	}

	public int getCommentCount() {
		return getComments().size();
	}

	@ManagedBean(name="Posts")
	@ApplicationScoped
	public static class Manager extends AbstractDominoManager<Post> {
		private static final long serialVersionUID = 1L;

		public static final ThreadLocal<DateFormat> MONTH_LABEL_FORMAT = new ThreadLocal<DateFormat>() {
			@Override
			protected DateFormat initialValue() {
				return new SimpleDateFormat("MMMM yyyy");
			}
		};
		public static final ThreadLocal<DateFormat> MONTH_CONVERT_FORMAT = new ThreadLocal<DateFormat>() {
			@Override
			protected DateFormat initialValue() {
				return new SimpleDateFormat("yyyy-MM");
			}
		};

		public static Manager get() {
			Manager existing = (Manager)FrameworkUtils.resolveVariable(Manager.class.getAnnotation(ManagedBean.class).name());
			return existing == null ? new Manager() : existing;
		}

		@SuppressWarnings({"deprecation", "unchecked"})
		@Override
		public Object getValue(final Object keyObject) {
			if("archiveMonths".equals(keyObject)) {
				View view = getDatabase().getView("Posts\\By Month");
				List<Object> months = view.getColumnValues(0);
				List<Map<String, Object>> result = new ArrayList<Map<String, Object>>(months.size());
				for(Object month : months) {
					try {
						Date monthDate = MONTH_CONVERT_FORMAT.get().parse(String.valueOf(month));
						Map<String, Object> node = new HashMap<String, Object>();
						node.put("label", MONTH_LABEL_FORMAT.get().format(monthDate));
						node.put("queryString", month);
						result.add(node);
					} catch (ParseException e) {
						throw new RuntimeException(e);
					}
				}
				return result;
			} else if("knownTags".equals(keyObject)) {
				View view = getDatabase().getView("Tags");
				Set<String> uniques = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);


				uniques.addAll((List<String>)(List<?>)view.getColumnValues(0));


				return new ArrayList<String>(uniques);
			}

			return super.getValue(keyObject);
		}

		@Override
		protected String getViewPrefix() {
			return "Posts\\";
		}

		@Override
		protected Database getDatabase() {
			AppConfig appConfig = AppConfig.get();
			String server = (String)appConfig.getValue("dataDatabaseServer");
			String filePath = (String)appConfig.getValue("dataDatabaseFilePath");
			return FrameworkUtils.getDatabase(server, filePath);
		}
	}

	public static enum PostStatus {
		Draft, Posted;
	}
}
