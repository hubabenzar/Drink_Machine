package ac.liv.csc.comp201;
import ac.liv.csc.comp201.model.ICoinHandler;
import ac.liv.csc.comp201.model.IMachine;


public class CoinManager {
    public static final String coinCodes[]={"ab","ac","ba","bc","bd","ef"};
    public static final int coinValues[]={1,5,10,20,50,100};
    
	public CoinManager() {}
	
    public void coinDeposited(String coinCode, IMachine machine){
        int i = 0;
        while(!coinCodes[i].equals(coinCode)) {
        	i++;
        }
        
        int currentBalance = machine.getBalance();
        machine.setBalance(currentBalance + coinValues[i]);
        
        System.out.println(coinValues[i]);
    }

	public String getNewCoinDisplay(int balance){
    	int pounds = balance / 100;
    	int pence = balance % 100;
    	
    	String poundsString = Integer.toString(pounds);
    	String penceString = Integer.toString(pence);
    	String offset = "";
    	
    	if(pence < 1) {offset = "0";}
    	
    	return '£' + poundsString + '.' + penceString + offset;
	}
    
	public void returnCoins(IMachine machine, ICoinHandler handler) {
		while(machine.getBalance() > 0) {
			System.out.println("Current balance is: " + Integer.toString(machine.getBalance()));
			
			boolean endLoop = false;
			int i = coinCodes.length-1;
			while(!endLoop) {
				if(i < 0) {
					endLoop = true;
				}else {
					if(!handler.coinAvailable(coinCodes[i])){
						System.out.println(Integer.toString(coinValues[i]) + " Not available");
						i -= 1;
					}else{
						if(coinValues[i] > machine.getBalance()){
							System.out.println(coinValues[i] + " is bigger than " + Integer.toString(machine.getBalance()));
							i -= 1;
						}else {
							System.out.println(coinValues[i] + " is less than " + Integer.toString(machine.getBalance()));
							endLoop = true;
						}
					}
				}
			}
			
			if(i < 0) {
				System.out.println("Sorry, no more change available");
			}else {
				handler.dispenseCoin(coinCodes[i]);
				System.out.println("Dispensed a " + Integer.toString(coinValues[i]));
				machine.setBalance(machine.getBalance() - coinValues[i]);
			}
		}
		
		
		
		
		System.out.println("Passed balance " + Integer.toString(machine.getBalance()));
	}
}