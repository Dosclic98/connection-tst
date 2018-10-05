package dos.connes;
import jbook.util.*;

public class Tester {
	public static void main(String[] args) {
		System.out.println("Inserire la valuta iniziale:\n1) EURO\n"+
				"2) DOLLARO\n3) YEN\n4) DRACMA\n >>");
		int choice1 = Input.readInt();
		System.out.println("Inserire la valuta finale:\n1) EURO\n"+
				"2) DOLLARO\n3) YEN\n4) DRACMA\n >>");
		int choice2 = Input.readInt();

		System.out.print("Inserire il valore da convertire:\n>>");
		float amt = Input.readFloat();

		float[] chlist = new float[] {1, 1.5f, 2, 3};
		/* ---- CODICE VECCHIO ----
		Value[] vallist = new Value[4];
		for(int i=0;i<4;i++)
		{
			vallist[i].val = chlist[i];
		}
		-------------------------- */
		// ---- CODICE FIXATO ----
		Value[] vallist = new Value[] {
				new Value(),
				new Value(),
				new Value(),
				new Value()
		};
		for(int i=0; i<vallist.length; i+=1)
			vallist[i].val = chlist[i];
		// -----------------------

		float res = vallist[choice1-1].conversion(amt, vallist[choice2-1].val);
		System.out.print(amt + "-->" + res);
	}
}

class Value {
	float val;

	public float conversion(float amount, float valext) {
		float conv = (valext/val)*amount;
		return conv;
	}
}