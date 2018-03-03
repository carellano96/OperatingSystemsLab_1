
import java.util.ArrayList;
import java.util.Map;

public class Module {
int base_address;
int Modulesize;
boolean multiple=false;
boolean inUseListButNotUsed=false;

ArrayList<Definition> definition= new ArrayList<Definition>();
ArrayList<Use> useList= new ArrayList<Use>();
ArrayList<Program> programText= new ArrayList<Program>();

}