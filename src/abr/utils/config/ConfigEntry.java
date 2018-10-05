package abr.utils.config;

import java.io.PrintWriter;

public abstract class ConfigEntry {

	public final String name;
	public final ConfigSection section;
	public final ConfigLocation location;
	 /** Represents the amounts of whitespace characters in the line,
	  * before and after any token */
	final byte[] overhead;
	boolean valid = true;

	ConfigEntry(ConfigSection sec, String name, byte[] overhead) {
		this.name = name;
		this.overhead = overhead;
		if(sec.location == null)
			location = new ConfigLocation(null, name);
		else
			location = sec.location.subLocation(name);
		section = sec;
	}

	 // should only be used for root
	ConfigEntry(String name, ConfigFile file) {
		this.name = name;
		this.overhead = new byte[] {-1};
		location = new ConfigLocation(file);
		section = null;
	}

	boolean isValid() {
		return valid;
	}
	void invalidate() {
		valid = false;
	}

	abstract void printOnFile(PrintWriter out);

}

class EmptyLine extends ConfigEntry {

	final String comment;

	EmptyLine(ConfigSection sec, int line, byte indent) {
		super(sec, ".empty"+line, new byte[] {indent});
		comment = "";
	}

	EmptyLine(ConfigSection sec, int line, String comment) {
		super(sec, ".comment"+line, new byte[] {0});
		this.comment = comment;
	}

	EmptyLine(ConfigSection sec, EmptyLine cpy) {
		super(sec, cpy.name, cpy.overhead);
		comment = cpy.comment;
	}

	@Override
	public String toString() {
		return "["+name+']';
	}

	@Override
	void invalidate() {
		throw new RuntimeException("tried to invalidate an empty line");
	}

	@Override
	void printOnFile(PrintWriter out) {
		if(comment == null)
			out.println(abr.utils.Utils.indentation(overhead[0], '\t'));
		else
			out.println(comment);
	}

}