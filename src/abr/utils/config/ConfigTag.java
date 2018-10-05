package abr.utils.config;

import java.io.PrintWriter;

import abr.utils.Utils;

public class ConfigTag extends ConfigEntry {

	public ConfigTag(ConfigSection sec, String name, byte[] overhead) {
		super(sec, name, overhead);
	}

	@Override
	void printOnFile(PrintWriter pw) {
		pw.println(
				Utils.indentation(overhead[0], '\t') +
				location.cfg.tagPrefix +
				Utils.indentation(overhead[1], ' ') +
				name);
	}

	@Override
	public String toString() { return "[" + location.fullname + ']'; }

}
