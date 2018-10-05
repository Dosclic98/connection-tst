package dos.connes.val;

public class Currency {
	float factor = 1.0f;  // non accessibile al main()
	public final String name;

	 /** inizializza una valuta generale */
	public Currency() {
		this.name = "<no_currency>";
	}

	 /** inizializza una valuta che è (relativeFactor) volte
	  * più grande di 'other' */
	public Currency(String name, Currency other, float relativeFactor) {
		this.name = name;
		factor = other.factor * relativeFactor;
	}

	public float convert(float quantity, Currency targetCur) {
		return (quantity * targetCur.factor) / factor;
	}

	public String valueToString(float value) {
		return valueToString(value, this);
	}
	public String valueToString(float value, Currency currency) {
		return name+convert(value, currency);
	}

}
