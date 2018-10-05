package dos.connes;
import jbook.util.*;

public class Tester {
	public static void main(String[] args) {
		float[] list = new float[] {1, 1.15f, 2, 3};
		System.out.print("Inserire la valuta iniziale:\n1) EURO\n"+
				"2) DOLLARO\n3) YEN\n4) DRACMA\n >>");
		int choice1 = Input.readInt();
		if(choice1<1 || choice1>4)
		{
			System.out.println("Valore non valido");
			System.exit(1);
		}
		System.out.println("Il valore inserito è: " + choice1);
		System.out.print("Inserire la valuta finale:\n1) EURO\n"+
				"2) DOLLARO\n3) YEN\n4) DRACMA\n >>");
		int choice2 = Input.readInt();
		if(choice2<1 || choice2>4)
		{
			System.out.println("Valore non valido");
			System.exit(1);
		}
		System.out.println("Il valore inserito è: " + choice2);
		
		System.out.print("Inserire la quota da convertire\n>>");
		float amount = Input.readFloat();
		
		float converted = (list[choice2-1] / list[choice1-1])*amount;
		
		System.out.println("Il valore convertito è: " + converted);
	}
}
