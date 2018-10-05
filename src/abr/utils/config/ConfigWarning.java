package abr.utils.config;

import java.util.ArrayList;

public final class ConfigWarning {
	public final String message;
	public final int line;
	public final ArrayList<String> details = new ArrayList<>();

	ConfigWarning(String msg, int line) {
		this.message = msg;
		this.line = line;
	}

	ConfigWarning(String msg, int line, String... details) {
		this.message = msg;
		this.line = line;
		for(String detail: details)
			this.details.add(detail);
	}

	ConfigWarning(String msg, int line, Iterable<String> details) {
		this.message = msg;
		this.line = line;
		for(String detail: details)
			this.details.add(detail);
	}

	public Iterable<String> getMessageLines() {
		ArrayList<String> retn = new ArrayList<>();

		if(line >= 0)
			retn.add("Warning (at line " + line + "): " + message);
		else
			retn.add("Warning: " + message);

		for(String detail: details)
			retn.add('\t' + detail);

		return retn;
	}

	@Override
	public String toString() {
		if(line >= 0)
			return "[Line " + line + "] " + message;
		else
			return message;
	}
}