package app;
import java.util.LinkedList;

/**
 * GcodeSender
 * This will send messages over serial and let the generator know when it has done so.
 * 
 */
public class GcodeSender implements Runnable{
    private Thread t;
    private String threadName;
    private LinkedList<String> gCodeMessages = new LinkedList<String>();
    private final int MESSAGE_DELAY = 100;

    GcodeSender(String name, LinkedList<String> sharedQueue){
        threadName = name;
        gCodeMessages = sharedQueue;
        System.out.println("Creating " + threadName );
    }
    public void run(){
        System.out.println("Running " + threadName);
        while(gCodeMessages.size() > 0){
            try {
                //do thread stuff
                sendGcode();
                System.out.println("Threading is happening [" + threadName + "]");
            } catch (Exception e) {
                System.out.println("Thread interrupted.");
                e.printStackTrace();
            }
        }
        
        System.out.println("Exiting thread.");
    }
    public void start(){
        System.out.println("Starting " + threadName); 
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }

    public void sendGcode() throws InterruptedException{
        synchronized(gCodeMessages){
            while(gCodeMessages.isEmpty()){
                System.out.println("Queue is empty " + 
                    Thread.currentThread().getName() + 
                    " is waiting , size: " + gCodeMessages.size());
                gCodeMessages.wait();
            }
            Thread.sleep(MESSAGE_DELAY);
            String gstr = gCodeMessages.getFirst();
            gCodeMessages.removeFirst();
            System.out.println("Sending GCode: " + gstr);
            gCodeMessages.notifyAll();
        }
        
    }
}