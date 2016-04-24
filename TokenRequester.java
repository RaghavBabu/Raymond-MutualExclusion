import java.util.Random;

/**
 * Class Token Requester which try to invoke the token 
 * if the process currently in released state.
 * @author Raghav Babu
 * Date : 04/15/2016
 */
public class TokenRequester extends Thread {

	Process process;
	public TokenRequester(Process process) {
		this.process = process;
	}
	
	@Override
	public void run(){
		
		Random rand = new Random();
		
		while(true){
			
			int low = 10;
			int high = 15;
			int randomTime = rand.nextInt(high - low) + low;
			//wait for random time before requesting token.
			try {
				Thread.sleep(rand.nextInt(randomTime)*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//System.out.println(process.requestQueue);
			if(Process.status == CriticalSectionStatus.RELEASED ){
				System.out.println("Process state : "+Process.status+", so making the process acquire CS");
				Process.status = CriticalSectionStatus.WANTED;
			}
			
		}
		
	}
}
