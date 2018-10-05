package dos.connes;
import jbook.util.*;

public class Tester {
	public static void main(String[] args) {
		System.out.println("Inserire la valuta iniziale:\n 1) EURO\n"+
				"2) DOLLARO\n 3) YEN\n 4) DRACMA\n >>");
		float choice1 = Input.readFloat();
		
	}
}

class Value {
	float val;
	
	public float conversion(float amount, float valext) {
		float conv = (valext/val)*amount;
		return conv;
	}
}