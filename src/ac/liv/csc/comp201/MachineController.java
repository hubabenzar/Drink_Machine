package ac.liv.csc.comp201;

import ac.liv.csc.comp201.model.IMachine;
import ac.liv.csc.comp201.model.IMachineController;
import ac.liv.csc.comp201.simulate.Cup;
import ac.liv.csc.comp201.simulate.Hoppers;
import java.util.*;
public class MachineController extends Thread implements IMachineController {
	
	private static final String version="1.22";
	
	private IMachine machine;
	private CoinManager coinManager;
	
	private boolean running=true;
	boolean tempOverride=false;
	boolean cupVended=false;
	String input = "";
	String[] codes = new String[] {"101", "102", "201", "202", "300"};
	int price = 0;
	static int cupType = 1;
	double multiplier = 1;
	boolean largeCup = false;
	boolean mediumCup = false;
	boolean smallCup = false;
	boolean extraCharge = false;
	static int stringlength2 = 3;
	final double lowerHeatIdle = 75;
	final double upperHeatIdle = 80;
	double lowerHeatBoundary = lowerHeatIdle;
	double upperHeatBoundary = upperHeatIdle;
	
	public void startController(IMachine machine) {
		this.machine=machine;				// Machine that is being controlled

		machine.getKeyPad().setCaption(9,"Reject");
		super.start();
	}
	
	public MachineController() {
		coinManager = new CoinManager();
	}
	
	public void reset(){
		System.out.println("RESET");
		price = 0;
		largeCup = false;
		mediumCup = false;
		smallCup = false;
		extraCharge = false;
		input = "";
	}
	
	public void charges(){
		if(extraCharge == true){
			if(largeCup == true){
				System.out.println("testing large price"+ price );
				price+=25;
			}
			if(mediumCup == true){
				System.out.println("testing medium price"+ price );
				price+=20;
			}
		}
	}

	private boolean checkDrinkFinished(double coffee, double sugar, double milk, double chocolate, double waterLevel, Cup cup) {

        boolean coffeeLevelsReached = (cup.getCoffeeGrams() >= coffee);
        boolean sugarLevelsReached = (cup.getSugarGrams() >= sugar);
        boolean milkLevelsReached = (cup.getMilkGrams() >= milk);
        boolean chocolateLevelsReached = (cup.getChocolateGrams() >= chocolate);
        boolean waterLevelsReached = (cup.getWaterLevelLitres() >= waterLevel);

        if (coffeeLevelsReached && sugarLevelsReached && milkLevelsReached && chocolateLevelsReached && waterLevelsReached) {
            return true;
        }
        return false;
    }

	public void makeDrink(double localMultiplier, double coffee, double sugar, double milk, double chocolate, double temp, int size){
	    new Thread(() -> {	
	    
	        tempOverride = true;
	        Cup cup;
	        machine.vendCup(size);
	        upperHeatBoundary = temp;
	        lowerHeatBoundary = temp - 1;
	        
	        
	        double waterLevel;
	        if (size == Cup.SMALL_CUP) {
	            waterLevel = Cup.SMALL;
	        } else if (size == Cup.MEDIUM_CUP) {
	            waterLevel = Cup.MEDIUM;
	        } else {
	            waterLevel = Cup.LARGE;
	        }
	        
	        try {
	        	Thread.sleep(4000);
	        } catch (InterruptedException e) {
	        	System.out.println("Error making drink: " + e);
	        } // waits 4 seconds
	        
	        
	        while (machine.getWaterHeater().getTemperatureDegreesC() < lowerHeatBoundary) {
	        	machine.getWaterHeater().setHeaterOn(); 
	        	System.out.println("Waiting for temp");
	        }
	        cup = machine.getCup();
	        
	        //Hoppers turn on if there are ingredients
	        if (coffee > 0) {
	        	machine.getHoppers().setHopperOn(Hoppers.COFFEE);
	        }
	        if (sugar > 0) {
	        	machine.getHoppers().setHopperOn(Hoppers.SUGAR);
	        }
	        if (milk > 0) {
	        	machine.getHoppers().setHopperOn(Hoppers.MILK);
	        }
	        if (chocolate > 0) {
	        	machine.getHoppers().setHopperOn(Hoppers.CHOCOLATE);
	        }

	        boolean drinkFinished = false;
	        lowerHeatBoundary = 75; //heater turns back on so the drink doesn't end up colder than 76.8
	        machine.getWaterHeater().setHotWaterTap(true); // turns on the hot water tap
	        System.out.println("Start drink making loop");
	        //System.out.println("preloop localMultiplier " +localMultiplier);
	        while (drinkFinished == false) { // while the drink hasn't yet been made
	        	input=""; //disallows other others to happen while one is being made
	        	if (machine.getWaterHeater().getTemperatureDegreesC() <= lowerHeatBoundary){
	                machine.getWaterHeater().setHeaterOn();
	            }
		        else{
		        	machine.getWaterHeater().setHeaterOff();
		        }
	        	//Turn hot water off
	        	if (cup.getWaterLevelLitres() >= waterLevel) {
	        		machine.getWaterHeater().setHotWaterTap(false);
	        		//System.out.println("WaterLevel "+waterLevel);
	        	}
	        	
                //Turn ingredients off
                if (cup.getCoffeeGrams() >= coffee*localMultiplier) {
                    machine.getHoppers().setHopperOff(Hoppers.COFFEE);
                    //System.out.println("COFFEE ");
                }
                if (cup.getSugarGrams() >= sugar*localMultiplier) {
                    machine.getHoppers().setHopperOff(Hoppers.SUGAR);
                    //System.out.println("SUGAR ");
                }
                if (cup.getMilkGrams() >= milk*localMultiplier) {
                    machine.getHoppers().setHopperOff(Hoppers.MILK);
                    //System.out.println("MILK ");
                }
                if (cup.getChocolateGrams() >= chocolate*localMultiplier) {
                    machine.getHoppers().setHopperOff(Hoppers.CHOCOLATE);
                    //System.out.println("CHOCOLATE ");
                }

	        	//checks if the drink is finished
	        	if (checkDrinkFinished(coffee*localMultiplier, sugar*localMultiplier, milk*localMultiplier, chocolate*localMultiplier, waterLevel, cup)) {
	        		drinkFinished = true;
	        		System.out.println("Drink finished");
	        	}
	        	//Synchronizer for 1ms
	        	try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	
	        }

	        // resets the resting temperature values
	        System.out.println("makeDrink thread finished");
	    	//Simulating the user taking their drink out and machine resetting.
	    	//try {
			//	Thread.sleep(1000);
			//} catch (InterruptedException e) {
			//	e.printStackTrace();
			//}
	    	System.out.println("Drink is removed");
	    	cup = null;
	    	lowerHeatBoundary = lowerHeatIdle;
	    	upperHeatBoundary = upperHeatIdle;
	    	tempOverride = false;
	    	multiplier = 1;
	    }).start();
	}
	

	private synchronized void runStateMachine() {
		//System.out.println("Running state machine");
		if(tempOverride == false){
            if (machine.getWaterHeater().getTemperatureDegreesC() <= lowerHeatBoundary){
            	machine.getWaterHeater().setHeaterOn();
            }
            if (machine.getWaterHeater().getTemperatureDegreesC() >= upperHeatBoundary){
                machine.getWaterHeater().setHeaterOff();
            }
        }

        if (machine.getWaterHeater().getTemperatureDegreesC() > 99){
            machine.shutMachineDown();
            System.out.println("ERROR: Water Temperature too Hot!");
        }
		
		int keyCode=machine.getKeyPad().getNextKeyCode();
		
		String coinCode=machine.getCoinHandler().getCoinKeyCode();
		if (coinCode!=null) {
			machine.getDisplay().setTextString("Got coin code .."+coinCode);
			
			coinManager.coinDeposited(coinCode, machine);
			System.out.println(machine.getBalance());

			String newDisplay = coinManager.getNewCoinDisplay(machine.getBalance());

			machine.getDisplay().setTextString(newDisplay);
		}

		switch (keyCode) {
			case 0:
				input = input + "0";
				//System.out.println("Vending cup");
				//machine.vendCup(Cup.SMALL_CUP);
				break;
			case 1:
				input = input + "1";
				//machine.getWaterHeater().setHeaterOn();								
				break;
			case 2:
				input = input + "2";
				//machine.getWaterHeater().setHeaterOff();
				break;
			case 3:
				input = input + "3";
				//machine.getWaterHeater().setHotWaterTap(true);
				break;
			case 4:
				input = input + "4";
				//machine.getWaterHeater().setHotWaterTap(false);
				break;
			case 5:
				input = input + "5";
				//machine.getHoppers().setHopperOn(Hoppers.COFFEE);
				break;
			case 6:
				input = input + "6";
				//machine.getHoppers().setHopperOn(Hoppers.MILK);
				break;			
			case 7:
				input = input + "7";
				//machine.getWaterHeater().setColdWaterTap(true);
				break;
			case 8:
				input = input + "8";
				//machine.getWaterHeater().setColdWaterTap(false);
				break;
			case 9:
				machine.getCoinHandler().clearCoinTry();
				coinManager.returnCoins(machine, machine.getCoinHandler());
				machine.getDisplay().setTextString("£0.00");
				reset();
				break;
			default:
				break;
		}
		int stringLength = String.valueOf(input).length();
		if(input.startsWith("6")){
			System.out.println("LARGE CUP");
			cupType = 3;
			largeCup = true;
        	multiplier = Cup.LARGE/Cup.SMALL;
        	stringlength2 = 4;
		}
		else if(input.startsWith("5")){
        	System.out.println("MEDIUM CUP");
        	cupType = 2;
        	mediumCup = true;
        	multiplier = Cup.MEDIUM/Cup.SMALL;
        	stringlength2 = 4;
		}
		else if(stringLength == 3 && (!input.startsWith("5") || !input.startsWith("6"))){
        	System.out.println("SMALL CUP");
        	cupType = 1;
        	smallCup = true;
        	multiplier = Cup.SMALL/Cup.SMALL;
        	stringlength2 = 3;
		}
		
		if(stringLength == 4 && (input.startsWith("6") || input.startsWith("5"))){
			System.out.println("CHARGES ON " + price);
			extraCharge = true;
			charges();
		}
		
		//multiplier, coffee, sugar, milk, chocolate, temp, size
		//1
		if(input.contains("101")) {
			System.out.println("Black Coffee");
			price += 120;
			if(machine.getBalance() >= price && machine.getHoppers().getHopperLevelsGrams(Hoppers.COFFEE) >= 2) {
				makeDrink(multiplier, 2, 0, 0, 0, 95.9, cupType);
				machine.setBalance(machine.getBalance()-price);
				machine.getDisplay().setTextString("£" + (double) (machine.getBalance() / 100));
				System.out.println("Final Drink Price is: "+price);
				reset();
			}
			else{
				System.out.println("Not enough ingredients or money");
				reset();
			}
		}
		//2
		if(input.contains("102")) {
			System.out.println("Black coffee with sugar");
			price += 130;
			if(machine.getBalance() >= price && machine.getHoppers().getHopperLevelsGrams(Hoppers.COFFEE) >= 2 && machine.getHoppers().getHopperLevelsGrams(Hoppers.SUGAR) >= 5) {
				System.out.println("SMALL CUP");
				makeDrink(multiplier, 2, 0, 0, 0, 95.9, cupType);
				machine.setBalance(machine.getBalance()-price);
				machine.getDisplay().setTextString("£" + (double) (machine.getBalance() / 100));
				System.out.println("Final Drink Price is: "+price);
				reset();
			}
			else{
				System.out.println("Not enough ingredients or money");
				reset();
			}
		}
		//3
		if(input.contains("201")) {
			System.out.println("White Coffee");
			price = 120;
			if(machine.getBalance() >= price && machine.getHoppers().getHopperLevelsGrams(Hoppers.COFFEE) >= 2 && machine.getHoppers().getHopperLevelsGrams(Hoppers.MILK) >= 3) {
				makeDrink(multiplier, 2, 0, 0, 0, 95.9, cupType);
				machine.setBalance(machine.getBalance()-price);
				machine.getDisplay().setTextString("£" + (double) (machine.getBalance() / 100));
				System.out.println("Final Drink Price is: "+price);
				reset();
			}
			else{
				System.out.println("Not enough ingredients or money");
				reset();
			}
		}
		//4
		if(input.contains("202")) {
			System.out.println("White Coffee");
			price = 130;
			if(machine.getBalance() >= price && machine.getHoppers().getHopperLevelsGrams(Hoppers.COFFEE) >= 2 && machine.getHoppers().getHopperLevelsGrams(Hoppers.SUGAR) >= 5 && machine.getHoppers().getHopperLevelsGrams(Hoppers.MILK) >= 3) {
				makeDrink(multiplier, 2, 0, 0, 0, 95.9, cupType);
				machine.setBalance(machine.getBalance()-price);
				machine.getDisplay().setTextString("£" + (double) (machine.getBalance() / 100));
				System.out.println("Final Drink Price is: "+price);
				reset();
			}
			else{
				System.out.println("Not enough ingredients or money");
				reset();
			}
		}
		//5
		if(input.equals("300")) {
			System.out.println("White Coffee");
			price = 110;
			if(machine.getBalance() >= price && machine.getHoppers().getHopperLevelsGrams(Hoppers.CHOCOLATE) >= 28) {
				makeDrink(multiplier, 2, 0, 0, 0, 95.9, cupType);
				machine.setBalance(machine.getBalance()-price);
				machine.getDisplay().setTextString("£" + (double) (machine.getBalance() / 100));
				System.out.println("Final Drink Price is: "+price);
				reset();
			}
			else{
				System.out.println("Not enough ingredients or money");
				reset();
			}
		}
		if(input.length() == stringlength2){
			for (int i=0; i<(codes.length - 1); i++) {
			    if (!input.contains(codes[i])) {
			        machine.getDisplay().setTextString("Invalid code entered retry");
			        reset();
			    }
			}
		}
	}

	public void run() {
		// Controlling thread for coffee machine
		int counter=1;
		while (running) {
			//machine.getDisplay().setTextString("Running drink machine controller "+counter);
			counter++;
			try {
				Thread.sleep(10);		// Set this delay time to lower rate if you want to increase the rate
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			runStateMachine();
		}		
	}
	
	public void updateController() {
		//runStateMachine();
	}
	
	public void stopController() {
		running=false;
	}
}
