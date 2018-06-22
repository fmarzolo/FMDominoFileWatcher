package fmWatchCompanion;



import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Set;

import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.nio.file.Path;

/**
 * This Runnable is used to constantly attempt to take from the watch
 * queue, and will receive all events that are registered with the
 * fileWatcher it is associated. 
 */
public class FMDirChangeEventReceiver implements Runnable {

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>)event;
	}

	FMPathList pathList;
	FMBase base=null;

	private WatchService myWatcher;
	//	FMWatcher fmWatcher=null;

	//	public FMDirChangeEventReceiver(FMWatcher fmWatcher, WatchService myWatcher, FMPathList p_pathList) {
	public FMDirChangeEventReceiver(FMBase base, WatchService myWatcher, FMPathList p_pathList) {
		this.myWatcher = myWatcher;
		this.base=base;
		pathList=p_pathList;
		//		this.fmWatcher=fmWatcher;
	}

	private HashMap<String,Thread> threadMap=new HashMap<String,Thread>();

	/**
	 * In order to implement a file watcher, we loop forever
	 * ensuring requesting to take the next item from the file
	 * watchers queue.
	 */
	FMTimerAMGRun timerAMGRun;
	Thread thread4TimerAMGRun ;
	@Override
	public void run() {
		try {
			// wait the first event before looping
			WatchKey key = myWatcher.take();
			while(key != null) {
				// we have a polled event, now we traverse it and receive all the states from it
				for (WatchEvent<?> event : key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();
					// TBD - provide example of how OVERFLOW event is handled
					if (kind == OVERFLOW) {
						continue;
					} 

					String folderWatched=key.watchable().toString();
					System.out.println("folderWatched:" + folderWatched);

//					String command=pathList.getNotesiniCommand(folderWatched);
					Set<String> commands=pathList.getNotesiniCommandSet(folderWatched);

					WatchEvent<Path> ev = cast(event);
					String filename = ev.context().toAbsolutePath().toString();

					base.log("Received  "+ event.kind().name()+" event for file: " + filename);
//					timerAMGRun= new FMTimerAMGRun(5L, command);
					timerAMGRun= new FMTimerAMGRun(5L, commands);
					//need to know if a thread is running, we need to stop it to feed with new files

					// keep a HashMap listing all threads started. Every cycle destroy the previous one if it is still alive 
//					base.log("New thread managing: thread knowed:" + threadMap.containsKey(folderWatched));
					if (threadMap.containsKey(folderWatched)) {
//						base.log("New thread managing: thread isAlive:" + threadMap.get(folderWatched).isAlive());
						if (threadMap.get(folderWatched).isAlive()) {
//							base.log("New thread managing: INTERRUPTING");
							threadMap.get(folderWatched).interrupt();
						}
					}

					// Start thread for run command with name equal to folder path to watch
					// this will help to destroy it if the same folder receive other files 
					thread4TimerAMGRun = new Thread(timerAMGRun);
					thread4TimerAMGRun.start();
					threadMap.put(folderWatched,thread4TimerAMGRun);

				}
				key.reset();
				key = myWatcher.take();			//halt thread waiting for an event for next loop

			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			System.out.println("Stopping thread");
		}
	}
}
