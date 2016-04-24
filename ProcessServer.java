import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Class ProcessServer
 * Each Process receives event from other process and updates its vector clock.
 * @author Raghav Babu
 * Date : 03/22/2016
 */
public class ProcessServer extends Thread {

	private InetSocketAddress boundPort = null;
	private static int port;
	private ServerSocket serverSocket;
	Process process;

	public ProcessServer(Process process) {
		port = ProcessIPPortXmlParser.processIDToPortMap.get(Process.processId);
		this.process = process;
	}

	@Override
	public void run(){

		try {

			initServerSocket();

			while(true) {

				Socket connectionSocket;
				ObjectInputStream ois;
				InputStream inputStream;

				connectionSocket = serverSocket.accept();
				inputStream = connectionSocket.getInputStream();
				ois = new ObjectInputStream(inputStream);

				Event event = (Event) ois.readObject();	

				if(event.eventType == EventType.TRANSFER_TOKEN){

					//if the sending process wants the token back, adding it to the queue.
					if(event.returnToken == true){
						process.requestQueue.add(event.processId);
					}

					System.out.println("Received token from process : "+event.processId );

					//if queue not empty.
					if(!process.requestQueue.isEmpty()){

						int id = process.requestQueue.poll();
						System.out.println("Process in the head of the queue : "+id);

						//forward the token to the required process.
						if(id != Process.processId){

							System.out.println("Process doesn't need critical section,so transferring it to process : "+id);

							Event e;
							//whether to return the token after use.
							if(process.requestQueue.isEmpty())
								e =  new Event(EventType.TRANSFER_TOKEN, Process.processId, false);
							else
								e =  new Event(EventType.TRANSFER_TOKEN, Process.processId, true);

							ProcessClient client = new ProcessClient(e, id);
							client.send();
						}

						//making the current process enter critical section.
						else{
							System.out.println("Releasing the lock, since it acquired the token from : "+event.processId);

							synchronized (process.lock) {
								process.lock.notify();
								Process.currentHolder = Process.processId;
							}

						}

					}
				}
				//if request for token  from other process.
				else if(event.eventType == EventType.REQUEST_TOKEN){

					System.out.println("Request received from process : "+event.processId);

					if(process.requestQueue.isEmpty()) {

						//pass the request to holder.
						if(Process.currentHolder != Process.processId){

							System.out.println("No token in this process, so requesting token from its holder : "+Process.currentHolder);

							//add to the request queue.
							process.requestQueue.add(event.processId);
							System.out.println("Request added to Queue : "+process.requestQueue);

							event = new Event(EventType.REQUEST_TOKEN, Process.processId, false);

							//send to current holder.
							ProcessClient client = new ProcessClient(event, Process.currentHolder);
							client.send();
						}
						//it itself is its holder, so it can grant the token.
						else{

							System.out.println("Current process has the token, so it can transfer token if CS not required.");

							System.out.println("Current process in "+Process.status+ " state");

							//if process not currently using the critical section.
							if(Process.status == CriticalSectionStatus.RELEASED){

								//whether to return the token after use.
								event = new Event(EventType.TRANSFER_TOKEN, Process.processId, false);

								System.out.println("Transfering the token to process : "+event.processId);

								//send to request process.
								ProcessClient client = new ProcessClient(event, event.processId);
								client.send();
							}

							else{	
								//poll out of request queue.
								process.requestQueue.add(event.processId);
							}

						}
					}

					else{
						//add to the request queue.
						process.requestQueue.add(event.processId);
						System.out.println("Request added to Queue : "+process.requestQueue);
					}

				}

				// ping to other processes.
				else if(event.eventType == EventType.PING){
					//System.out.println("Process : "+event.processId+" able to ping");			
				}

			}
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	/**
	 * method which initialized and bounds a server socket to a port.
	 * @return void.
	 */
	private void initServerSocket()
	{
		boundPort = new InetSocketAddress(port);
		try
		{
			serverSocket = new ServerSocket(port);

			if (serverSocket.isBound())
			{
				System.out.println("Server bound to data port " + serverSocket.getLocalPort() + " and is ready...");
			}
		}
		catch (Exception e)
		{
			System.out.println("Unable to initiate socket.");
		}

	}

}
