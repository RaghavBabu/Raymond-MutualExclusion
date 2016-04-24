import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Class Process
 * Each Process running initiates an event.
 * Initiates the XMLParser, Server Thread, Process Thread and TokenRequestor thread.
 * @author Raghav Babu
 * Date : 04/15/2016
 */
public class Process {

	static int processId;
	static int totalProcess;
	static int childsInTree;
	static volatile CriticalSectionStatus status;
	static volatile int currentHolder;
	static String token;
	static String processName;
	Object lock;

	int initialTokenHolder;
	String PROCESS_DEST;	
	Queue<Integer> requestQueue;

	public Process(){

		this.lock = new Object();
		processName = "Process_"+processId;
		status = CriticalSectionStatus.RELEASED;
		try {
			PROCESS_DEST = "process_"+InetAddress.getLocalHost().getHostName()+".txt";
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}	
		this.requestQueue =  new ConcurrentLinkedQueue<Integer>();	
	}


	/*
	 * Main method.
	 */
	public static void main(String[] args) {

		//parse XML files to updates process running IP and ports map.
		ProcessIPPortXmlParser parser = new ProcessIPPortXmlParser();
		parser.parseXML();

		processId = Integer.parseInt(args[0]);
		totalProcess = Integer.parseInt(args[1]);
		childsInTree = Integer.parseInt(args[2]);
		
		Process process = new Process();

		boolean entryFlag = true;
		boolean markerProceed = false;

		//start process server to receive events from other processes.
		ProcessServer server = new ProcessServer(process);
		server.start();


		while(entryFlag){

			//ping all running processing so that it can try to acquire critical section.
			if(entryFlag){

				Event eve = new Event(EventType.PING, Process.processId, false);

				for(Entry<Integer, String> e : ProcessIPPortXmlParser.processIDToIpMap.entrySet() ){

					if(!e.getKey().equals(Process.processId)) {
						ProcessClient dummySend = new ProcessClient(eve, e.getKey());

						if(dummySend.send()){
							markerProceed = true;
						}else{
							markerProceed = false;
						}
					}
				}
			}

			if(!markerProceed){
				//System.out.println("Waiting for all process to start running before initiation");
				continue;
			}
			else{

				if(entryFlag){
					System.out.println("All the processes are up and running, so Ricart-Agarwala algorithm can be initiated.");

					//setting entry flag as false, so that ping process need not be done again.
					entryFlag = false;
				}

			}
		}


		//construct the K-ary tree using the list of processes available.
		Utils.constructNaryTree();

		process.initialTokenHolder = Utils.getRootProcessId();

		//if its root process id, create a token and make itself its own holder initially.
		if(processId == process.initialTokenHolder){
			token = new String("Hi, I am the token");
			currentHolder = processId;
			Process.status = CriticalSectionStatus.WANTED;
			System.out.println("Current token holder of process "+processId+" is : "+currentHolder);
		}
		//if its not root process, set its parent as current token holder.
		else{
			currentHolder = Utils.getParentProcessId(processId);
			System.out.println("Current token holder of process "+processId+" is : "+currentHolder);
		}

		//process thread
		ProcessThread processThread = new ProcessThread(process);
		processThread.start();

		//start Request token thread
		TokenRequester requester = new TokenRequester(process);
		requester.start();

	}

	/*
	 * process requests in the queue whenever this process release the critical section.
	 */
	public void processRequestsinQueue() {

		Event event;
		ProcessClient client;

		if(!requestQueue.isEmpty()){

			System.out.println("Since current process released token, dispatching it to the first process in the queue");

			//CS released, it can pass the token to the next requesting process.
			int requestID = requestQueue.poll();

			if(!requestQueue.isEmpty())
				event = new Event(EventType.TRANSFER_TOKEN, Process.processId, true);
			else
				event = new Event(EventType.TRANSFER_TOKEN, Process.processId, false);

			System.out.println("Transferring token to process : "+ requestID);
			//send to first request process id in the queue.
			client = new ProcessClient(event, requestID);
			client.send();

		}
	}
}
