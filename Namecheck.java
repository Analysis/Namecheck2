
package scripts.Namecheck;

import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import org.tribot.api.General;
import org.tribot.api.input.Keyboard;
import org.tribot.api.input.Mouse;
import org.tribot.api2007.Game;
import org.tribot.api2007.GameTab;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Arguments;
import org.tribot.script.interfaces.Painting;
import org.tribot.util.Util;
import org.tribot.api2007.Interfaces;

@ScriptManifest(authors = "SKENGRAT", category = "Tools", name = "Namechecker", version = 1.0, description = "Checks name availability from text file. Text file must have each name on a seperate line, and be located in .tribot folder. Text file name, with extension must be supplied in script arguments.")



public class Namecheck extends Script implements Arguments, Painting{

	public static List<String> lines;
	public static BufferedReader reader;
	public static OutputStream outputStream;
	public static OutputStreamWriter outputWriter;
	private int amountOfPlayersOnIgnoreList, amountOfPlayersOnIgnoreListOld, lineCount = 0, removedPlayers = 0, numberOfLine = 0, paintingCount = 0;
	private String currentName, separator = System.getProperty("line.separator"), arguments, paintingText;

	@Override
	public void run() {
		paintingText = "Loading...";
		Mouse.setSpeed(90);
		while (Game.getGameState() == 10){
			General.sleep(3000);
		}
		openIgnoreList();
		openAddToIgnoreListDialogue();
		addModMark();
		General.sleep(1500, 2500);
		removeFromIgnoreList();
		countLines();
		while (numberOfLine < lineCount){
			openAddToIgnoreListDialogue();
			getCurrentName();
			enterName();
			checkAvailability();
			numberOfLine++;
		}
	}

	@Override
	public void onPaint(Graphics g) {
		g.setColor(Color.CYAN);
		g.drawString("Ratz Name Checker v1.0 : " + paintingText, 340, 330);
	}

	public void passArguments(HashMap<String, String> argumentsRaw) {
		arguments = argumentsRaw.get("custom_input");
		if (argumentsRaw.get("custom_input") == null) {
			arguments = argumentsRaw.get("client_starter");
			if (argumentsRaw.get("client_starter") == null) {
				println("No arguments supplied, script ending.");
			}
		}
	}

	private void openIgnoreList(){
		while ((GameTab.getOpen() != GameTab.TABS.IGNORE)){
			GameTab.open(GameTab.TABS.IGNORE);
		}
	}

	private void countPlayersOnIgnoreList(){
		if (Interfaces.isInterfaceValid(432)){
			if (!(Interfaces.get(432, 3).isHidden())){
				amountOfPlayersOnIgnoreList = Interfaces.get(432,3).getChildren().length;
			}
		}
	}

	private void openAddToIgnoreListDialogue(){
		if ((GameTab.getOpen() != GameTab.TABS.IGNORE)){
			openIgnoreList();
		}
		toggleRoof();
		if (Interfaces.isInterfaceValid(432)){
			if ((Interfaces.get(432, 7).isClickable())){
				Interfaces.get(432,7).click();
			}
		}
	}

	private void countLines(){
		//BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(Util.getWorkingDirectory()+"\\"+arguments));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			while (reader.readLine() != null) lineCount++;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		General.println("Ratz Name Checker: Loaded " + lineCount + " Usernames to check.");
	}

	private void getCurrentName(){
		if ((GameTab.getOpen() != GameTab.TABS.IGNORE)){
			openIgnoreList();
		}
		if (Interfaces.get(162,32).isHidden()){
			openAddToIgnoreListDialogue();
		}
		try {
			currentName = Files.readAllLines(Paths.get(Util.getWorkingDirectory()+"\\"+arguments)).get(numberOfLine);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		paintingCount = numberOfLine + 1;
		paintingText = "Checking " + currentName + " ("+ paintingCount + "/" + lineCount + ")";
	}

	private void enterName(){
		if ((GameTab.getOpen() != GameTab.TABS.IGNORE)){
			openIgnoreList();
		}
		if (Interfaces.get(162,32).isHidden()){
			openAddToIgnoreListDialogue();
		}
		countPlayersOnIgnoreList();
		amountOfPlayersOnIgnoreListOld = amountOfPlayersOnIgnoreList;
		if (!Interfaces.get(162,32).isHidden()){
			Mouse.clickBox(Interfaces.get(162, 32).getAbsoluteBounds(), 1);
			Keyboard.typeSend(currentName);
		}
	}

	private void removeFromIgnoreList(){
		if ((GameTab.getOpen() != GameTab.TABS.IGNORE)){
			openIgnoreList();
		}
		countPlayersOnIgnoreList();
		removedPlayers = amountOfPlayersOnIgnoreList - 1;
		while (removedPlayers > 0){
			if (Interfaces.get(432,3).getChild(removedPlayers).getText() == ""){
				removedPlayers--;
			}else{
				Interfaces.get(432,3).getChild(removedPlayers).click("Delete");
				removedPlayers--;
			}
		}
	}

	private void checkAvailability(){
		while((Interfaces.get(162, 43).getChild(0).getText().contains("Roof")) && (amountOfPlayersOnIgnoreListOld == amountOfPlayersOnIgnoreList)){
			General.sleep(1000);
			countPlayersOnIgnoreList();
		}
		if (amountOfPlayersOnIgnoreListOld == amountOfPlayersOnIgnoreList){
			try {
				outputStream = new FileOutputStream(Util.getWorkingDirectory()+"\\Available Names.txt", true);
				outputWriter = new OutputStreamWriter (outputStream, "UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				outputWriter.append(currentName);
				General.println("Ratz Name Checker: " + currentName + " is avaliable!");
				outputWriter.append(separator);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				outputWriter.flush();
				outputWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			try {
				outputStream = new FileOutputStream(Util.getWorkingDirectory()+"\\Unavailable Names.txt", true);
				outputWriter = new OutputStreamWriter (outputStream, "UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
			} 
			try {
				outputWriter.append(currentName);
				General.println("Ratz Name Checker: " + currentName + " is not avaliable.");
				outputWriter.append(separator);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				outputWriter.flush();
				outputWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			removeFromIgnoreList();
		}
	}	

	private void addModMark(){
		paintingText = "Adding Mod Mark";
		if (!Interfaces.get(162,32).isHidden()){
			Mouse.clickBox(Interfaces.get(162, 32).getAbsoluteBounds(), 1);
			Keyboard.typeSend("Mod Mark");
		}
	}

	private void toggleRoof(){
		Keyboard.typeSend("::toggleroof");
		while (!(Interfaces.get(162, 43).getChild(0).getText().contains("Roof"))){
			General.sleep(1000);
		}
	}
}
