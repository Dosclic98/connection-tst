package dos.connes;

import java.util.Scanner;
import dos.connes.val.Currency;

public class Tester {
	public static void main(String[] args) {
		String nomeValutaIn, nomeValutaOut;
		float quantita;
		Scanner input = new Scanner(System.in);
		Currency worldCurrency = new Currency();
		Currency[] currencies = new Currency[] {
				new Currency("Ä", worldCurrency, 1.0f),
				new Currency("$", worldCurrency, 2.0f),
				new Currency("£", worldCurrency, 3.0f)
		};

		// chiedi tutti gli input per la conversione
		System.out.print("Inserire nome prima valuta: ");
		nomeValutaIn = input.next();
		System.out.print("Inserire nome seconda valuta: ");
		nomeValutaOut = input.next();
		System.out.print("Inserire quantit√† di soldi: ");
		quantita = input.nextFloat();

		// cerca valuta corrispondente
		Currency valutaIn = null;
		Currency valutaOut = null;
		for(int i=0; i < currencies.length; i += 1) {
			if(currencies[i].name .equals (nomeValutaIn)) {
				valutaIn = currencies[i];
				continue;
			}
			if(currencies[i].name .equals (nomeValutaOut)) {
				valutaOut = currencies[i];
				continue;
			}
			 // controlla se sono state trovate tutte e due, per
			 // non ripetere il ciclo pi√π del necessario
			if((valutaIn != null) && (valutaOut != null))
				break;
		}

		// controlla se sono stati trovati tutti i valori:
		// se non ne √® stato trovato uno, lancia un'eccezione
		if((valutaIn == null) || (valutaOut == null)) {
			input.close();
			throw new RuntimeException(
					"valuta '" +
					(valutaIn == null? nomeValutaIn : nomeValutaOut) +
					"' non trovata");
		}

		System.out.println(
				valutaIn.valueToString(quantita) +
				" = " +
				valutaOut.valueToString(quantita, valutaIn));

		input.close();
	}
}
