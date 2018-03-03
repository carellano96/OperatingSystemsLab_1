
import java.io.File;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Scanner;
 class mainProgram {
	 static int MachineSize=0;
	 static ArrayList<Module> Modules = new ArrayList<Module>();

		public static void main(String[] args) throws IOException,FileNotFoundException, ClassNotFoundException, InterruptedException {				
	String file1 = args[0];
	File file = new File(file1);
	Scanner input = new Scanner(file);
	input.next();

	while (input.hasNext()){	
		int TempSize=0;
		Module tempMod = new Module();
		String definitionNumber = input.next();
		int length=Integer.parseInt(definitionNumber);
		for (int i=1;i<=length;i++){
			String key = input.next();
			String value = input.next();
			Definition definitionObject= new Definition();
			definitionObject.key=key;
			definitionObject.value=Integer.parseInt(value);
			tempMod.definition.add(definitionObject);
			
		}
		
		String useListNumber = input.next();
		for (int j=1;j<=Integer.parseInt(useListNumber);j++){
			String useListValue = input.next();
			Use useListObject = new Use();
			useListObject.symbol=useListValue;
			tempMod.useList.add(useListObject);
		}
		String programTextNumber = input.next();
		for (int k=1;k<=Integer.parseInt(programTextNumber);k++){
			Program programObject = new Program();
			String key1 = input.next();
			int value1 = Integer.parseInt(input.next());
			if (key1.equals("E")&&getDigit(value1)>tempMod.useList.size()){
				programObject.ExternalAddressError=true;
				programObject.addresstype="I";
				programObject.address=value1;
				tempMod.programText.add(programObject);
				TempSize++;
			}
			else{
			programObject.addresstype=key1;
			programObject.address=value1;
			tempMod.programText.add(programObject);
			TempSize++;
			}
			
			
		}
		CheckAddressDefinition(tempMod.definition,TempSize);
		MachineSize+=TempSize;
		tempMod.Modulesize=TempSize;
		Modules.add(tempMod);
		
		//symbol table is found by adding the relative address to base address
		}
	
	
	
	SymbolTable SymbolTable = new SymbolTable();
	


	
SymbolTable = FirstPass(Modules);
System.out.println("\nSymbol Table");
for (Symbol symbol:SymbolTable.symbols){
		if (symbol.multiple==true){
			System.out.print(symbol.key+" = "+symbol.value);
			System.out.print(" This variable is multiply defined; first value used.\n");
			continue;
		}
		if (symbol.ErrorDefAddressSize==true){
			System.out.print(symbol.key+" = "+symbol.value);
			System.out.print("The variable definition is larger than module size. 0 used");
			continue;

		}
		else{
			System.out.println(symbol.key+" = "+symbol.value);

		}
	
}
System.out.println("\nMemory Map");
SecondPass(Modules,SymbolTable);
		}
		
		
	static SymbolTable FirstPass(ArrayList<Module> Modules){
		int base_address=-1;
		boolean multiple=false;
		SymbolTable SymbolTable = new SymbolTable();
		for (int i=0; i<Modules.size();i++){
			Module mod = Modules.get(i);
			mod.base_address=base_address+1;
			for (Program program:mod.programText){
				base_address++;
			}
			for (Definition defs:mod.definition){

					int tempvalue=0;
					tempvalue = defs.value+mod.base_address;
					Symbol symbol = new Symbol();
					if (defs.AddressSizeError==true){
						symbol.ErrorDefAddressSize=true;
					}
					symbol.value=tempvalue;
					symbol.key = defs.key;
					symbol.moduleDefined= i;
					boolean CheckM = CheckMultiples(SymbolTable,symbol);
					if (CheckM){
						
						continue;
					};//figure out how to mark flag in symbol as true
					SymbolTable.symbols.add(symbol);

				}
			}
		return SymbolTable;
		
		
 }

	static boolean CheckMultiples(SymbolTable SymbolTable,Symbol symbol){
		for (Symbol symbol1:SymbolTable.symbols){
			if (symbol.key.equals(symbol1.key)){
				symbol1.multiple=true;
				return true;
			}
		}
		return false;
	}

	static void CheckAddressDefinition(ArrayList<Definition> definitions, int Module_Size){
		for (Definition definition: definitions ){
			if ((definition.value+1)>Module_Size){
				definition.value=0;
				definition.AddressSizeError=true;
			}
		}
	}
	static void SecondPass(ArrayList<Module>Modules,SymbolTable SymbolTable1){
	int counter=-1;
	for (int i=0; i<Modules.size();i++){
		Module mod = Modules.get(i);
		int ReplacementNum = 0;
		for (Program program:mod.programText){
				counter++;
				System.out.print(counter+": ");
				if (program.addresstype.equals("R")){
					int oldvalue = program.address;
					if (getDigit(program.address)>mod.Modulesize){
						program.address=replaceLastThree(program.address,0);
						System.out.print(program.address+" Error: Relative address exceeds size of module. Zero value used.\n");
						continue;
					}
					program.address=oldvalue+mod.base_address;
					System.out.print(program.address+"\n");
					
				}
				
				else if (program.addresstype.equals("E")){
					int CurrentValue = program.address;
					int Address = getDigit(CurrentValue);
					String SymbolKey = mod.useList.get(Address).symbol;//xy
					boolean SymbolDefined=false;
					for (Symbol symbol: SymbolTable1.symbols){
						if (symbol.key.equals(SymbolKey)){
							ReplacementNum = symbol.value;
							symbol.ActuallyUsed=true;
							mod.useList.get(Address).moduleActuallyUsed=i;
							SymbolDefined=true;
						}


					}
					if (SymbolDefined==false){
						program.address=replaceLastThree(CurrentValue,ReplacementNum);
						System.out.print(program.address+" Error: "+SymbolKey+" is not defined. Zero used.\n");
					}
					else{
					program.address=replaceLastThree(CurrentValue,ReplacementNum);
					System.out.print(program.address+"\n");
					}
					
					}
				else if (program.addresstype.equals("A")&&getDigit(program.address)>MachineSize){
					program.address=replaceLastThree(program.address,0);
					System.out.print(program.address+" Error: Absolute Address exceeds machine size. Value zero used\n");
				}
				else{
					if (program.ExternalAddressError==true){
						System.out.print(program.address+" Error: External address exceeds length of use list; treated as immediate.\n");
						continue;
					}
					System.out.print(program.address+"\n");
				}
				
				
			}
		for (Use use: mod.useList){
			for (Symbol symbol: SymbolTable1.symbols){
				if (symbol.key.equals(use.symbol)){
					symbol.used=true;//used
				}
			}
		}
		for (Definition definition:mod.definition){
	
					for (Symbol symbol:SymbolTable1.symbols){
						if (symbol.key.equals(definition.key)){
							symbol.defined=true;//defined
						
				
			}
		}
		}

		for (Symbol symbol:SymbolTable1.symbols){
			if (symbol.used==true&&symbol.ActuallyUsed==false){
				symbol.InUseListButNotUsed=true;
			}
		}

			}
	System.out.println();
		for (Symbol symbol: SymbolTable1.symbols){
			if (symbol.defined==true&&symbol.used==false){
				System.out.println("Warning: "+symbol.key+" was defined in module "+symbol.moduleDefined+" but never used.");
			}
			if (symbol.used==true&&symbol.defined==false){
				System.out.println("Warning: "+symbol.key+" was used in module "+symbol.moduleDefined+" but never defined.");

			}
			else if (symbol.InUseListButNotUsed==true){
				System.out.println("Warning: In module "+modNotUsed(symbol,Modules)+" "+symbol.key+" appeared in the use list but was not actually used.");
			}
		}
	}
	static int getDigit(int num){
		int tmp=0;
		int result=0;
		tmp=num/1000;
		result = num-(tmp*1000);
		return result;
	}
	static int replaceLastThree(int num, int replace){//4563, 15
		int temp=0;
		int Lastthree = getDigit(num);//563
		temp = num/1000;//
		temp = temp*1000;//3000
		int result = temp+replace;//515
		return result;
	}
	static int modNotUsed(Symbol symbol,ArrayList<Module> Modules){
		for (int i=0;i<Modules.size();i++){
			for (Use use:Modules.get(i).useList){
			if (symbol.key.equals(use.symbol)){
				return i;
			}
			}
		}
		return -1;

	
	}
 }
	
	
	
 
 
 
