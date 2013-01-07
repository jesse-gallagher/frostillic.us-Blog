package frostillicus;

import java.io.*;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.ibm.domino.xsp.module.nsf.ThreadSessionExecutor;
import frostillicus.event.*;
import lombok.*;
import lotus.domino.*;

@ToString
public class Messenger implements XPagesEventListener, Serializable {
	private static final long serialVersionUID = -9103610361157404676L;

	@SneakyThrows
	public void receiveEvent(XPagesEvent event) {
		if(event.getEventName().equals("comment posted")) {
			new MessengerThread(event, JSFUtil.getDatabase().getFilePath()).start();
		}
	}

	private class MessengerThread extends Thread {
		private final XPagesEvent event;
		private final String databaseFilePath;
		private ThreadSessionExecutor<IStatus> executor;

		public MessengerThread(XPagesEvent eventParam, String filePathParam) {
			this.event = eventParam;
			this.databaseFilePath = filePathParam;

			this.executor = new ThreadSessionExecutor<IStatus>() {

				@Override
				protected IStatus run(Session session) throws Exception {
					try {
						Database database = session.getDatabase("", databaseFilePath);

						Document comment = database.getDocumentByUNID(String.valueOf(event.getEventPayload()[0]));

						View configView = database.getView("Configuration");
						Document config = configView.getDocumentByKey("Communication", true);
						if(config != null) {
							Document mailDoc = database.createDocument();
							mailDoc.replaceItemValue("Form", "Memo");
							mailDoc.replaceItemValue("SendTo", config.getItemValueString("NotificationAddress"));
							mailDoc.replaceItemValue("Subject", "New comment on " + database.getTitle());
							mailDoc.replaceItemValue("Body", "http://frostillic.us/f.nsf/posts/" + comment.getItemValueString("PostID"));
							mailDoc.send();

							config.recycle();
						}
						configView.recycle();
						database.recycle();
					} catch(NotesException ne) {
						ne.printStackTrace();
					}

					return Status.OK_STATUS;
				}

			};
		}

		@Override
		public void run() {
			try {
				executor.run();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
