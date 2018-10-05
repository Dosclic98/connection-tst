package abr.utils.config;

import java.io.PrintWriter;
import java.util.Objects;

import abr.utils.Utils;
import abr.utils.config.ConfigEntry;

public class ConfigOption extends ConfigEntry {

	final String value;
	final String formatted;

	ConfigOption(ConfigSection loc, String name, byte[] overhead) {
		super(Objects.requireNonNull(loc), name, overhead);
		value = "null";
		formatted = initValueFormat();
		//Debug.out.println("new option " + location.fullname);
	}

	ConfigOption(ConfigSection loc, String name, String value, byte[] overhead) {
		super(Objects.requireNonNull(loc), name != null? name:"null", overhead);
		this.value = value;
		formatted = initValueFormat();
		//Debug.out.println("new option " + location.fullname);
	}

	/** Used when custom-parsing the 'formatted' field */
	protected ConfigOption(
			ConfigSection loc,
			String name,
			String value, String formatted,
			byte[] overhead
	) {
		super(Objects.requireNonNull(loc), name != null? name:"null", overhead);
		this.value = value;
		this.formatted = formatted;
	}

	private String initValueFormat() {
		try {
			return ConfigUtils.formatString(value, location.cfg, -1);
		} catch (InvalidConfigException ex) {
			throw new RuntimeException("this exception should never happen at this point", ex);
		}
	}

	public boolean isNull() { return value.equals("null"); }
	@Override
	public boolean isValid() { return super.isValid() && !isNull(); }

	public String    getValue()          { return value; }
	public String    getValueFormatted() { return formatted; }
	public Integer   toInteger()         { return Integer.valueOf(value); }
	public Long      toLong()            { return Long.valueOf(value); }
	public Float     toFloat()           { return Float.valueOf(value); }
	public Double    toDouble()          { return Double.valueOf(value); }
	public Boolean   toBoolean()         { return Boolean.valueOf(value); }

	public String[] toStringArray() {
		return toStringArray(",");
	}
	public String[] toStringArray(String separator) {
		String[] split = value.split(",");
		for(int i = 0; i < split.length; i += 1) {
			split[i] = abr.utils.Utils.trimSpaces(split[i]);
		}
		return split;
	}

	@Override
	public String toString() {
		return "[" + location.fullname + "=\"" + value + "\"]";
	}

	@Override
	void printOnFile(PrintWriter pr) {
		pr.println(
				Utils.indentation(overhead[0], '\t') +
				name +
				Utils.indentation(overhead[1], ' ') +
				location.cfg.optionSeparator +
				Utils.indentation(overhead[2], ' ') +
				value);
	}

}
