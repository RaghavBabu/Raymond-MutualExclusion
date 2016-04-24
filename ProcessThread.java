import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


/**
 * Class ProcessThread
 * Each ProcessThread running initiates a withdraw,deposit event and try to acquire critical section to transfer amount
 * to another  random process.
 * @author Raghav Babu
 * Date : 04/14/2016
 */
public class ProcessThread extends Thread {

	Process process;
	File file;
	FileWriter fw;

	public ProcessThread(Process process) {
		this.process = process;
		this.file = new File(process.PROCESS_DEST);

		try {
			this.fw = new FileWriter(file);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {

		Random rand = new Random();

		while(true) {

			Event event = null;
			ProcessClient client;

			//if event is transfer, choose another process to send to.
			if(Process.status == CriticalSectionStatus.WANTED) {

				System.out.println("Process "+Process.processId+" wants critical section");	

				if(Process.currentHolder != Process.processId){

					System.out.println("No token in this process, so requesting token from its holder : "+Process.currentHolder);

					System.out.println("Adding the process : " +Process.processId+" to the request queue.");
					process.requestQueue.add(Process.processId);
					event = new Event(EventType.REQUEST_TOKEN, Process.processId, false);

					//send to current holder. parent in this case.
					client = new ProcessClient(event, Process.currentHolder);
					client.send();

					//synchronized lock to acquire the critical section.
					synchronized (process.lock) {

						System.out.println("Process "+Process.processId+" waiting for critical section");
						try {
							process.lock.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						System.out.println("Process out of wait, so acquired critical section ");
						Process.status = CriticalSectionStatus.HELD;

					}
				}
				else{
					System.out.println("Token already in this process, so acquired critical section ");
					Process.status = CriticalSectionStatus.HELD;
				}
			}
			else if(Process.status == CriticalSectionStatus.HELD){	

				System.out.println("Current status of process "+Process.processId+ " is "+Process.status);
				//note the time when it acquired critical section.
				Date date = new Date();
				DateFormat df = new SimpleDateFormat("HH:mm:ss");
				System.out.println("*************Process "+Process.processId+" acquired the critical section at : "+df.format(date)+" ****************");

				//writing to file CS acquired time.
				writeFile(df.format(date));

				System.out.println("Current Process status of process : "+Process.processId+ " is : "+Process.status);

				int low = 8;
				int high = 15;
				int randomTime = rand.nextInt(high - low) + low;

				//sleeping for some time inside critical section.
				System.out.println("Holding critical section for random time : "+randomTime);

				try {
					Thread.sleep(randomTime * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				date = new Date();
				df = new SimpleDateFormat("HH:mm:ss");
				System.out.println("**********Process released critical section at : "+df.format(date)+" *************");

				writeFile(df.format(date));
				writeFile("------------------------------------");

				//releasing CS after random time.
				Process.status = CriticalSectionStatus.RELEASED;
				process.processRequestsinQueue();
			}
		}
	}

	/**
	 * write file to directory.
	 */
	public void writeFile(String str){

		try {
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(str+"\n");
			bw.flush();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
