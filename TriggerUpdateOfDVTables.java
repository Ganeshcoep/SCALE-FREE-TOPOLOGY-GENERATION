/** 
 * Class used to Trigger the Update Of Distance Vector Tables
 */

public class TriggerUpdateOfDVTables extends Thread{ 
	private static int timer=3000;  

	public void run(){

		while(true){
			//System.out.println("Waiting for the timer to finish: timer set for "+ timer);
			//Sleep for the specified time	

			try {
				Thread.sleep(timer);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//System.out.println("Triggering the Update Of Distance Vector Tables" );

			// Set the TCPSenderFlag to trigger the Update Of Distance Vector Tables

			synchronized (TCPSender.startDV) {

				TCPSender.startDV=1;

				//System.out.println("Changed value to 1...");
			}

			//System.out.println("Triggering the Update Of Distance Vector Tables.." );
		}

	}

}
