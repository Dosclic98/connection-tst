package abr.utils.config;

import java.io.PrintWriter;
import java.util.ArrayList;

import abr.utils.Utils;

public class ConfigOptionMultiLine extends ConfigOption {

	private final ArrayList<Line> lines = new ArrayList<>();
	/** Stores the value of getValue() */
	protected final String mlcache;

	public ConfigOptionMultiLine(
			ConfigSection loc,
			String name, Iterable<String> values,
			byte[] headOverhead
	) {
		super(
				loc, name,
				String.valueOf(ConfigFile.MULTILINE_BEG),
				formatIterable(values, loc.location.cfg),
				headOverhead);
		for(String str: values) {
			Line ln = new Line();
			ln.overhead = ConfigUtils.evalOverhead(new String[] {str});
			ln.str = Utils.trimSpaces(str);
			lines.add(ln);
		}
		mlcache = initCOMLCache(false);
	}


	private static String formatIterable(Iterable<String> iter, ConfigFile file) {
		StringBuilder retn = new StringBuilder();
		try {
			for(String str: iter) {
				retn.append(
						ConfigUtils.formatString(
								Utils.trimSpaces(str),
								file, -1)
						+'\n');
			}
		} catch (InvalidConfigException | NullPointerException ex) {
			throw new RuntimeException("this exception should never happen at this point", ex);
		}
		return retn.toString();
	}

	private String initCOMLCache(boolean formatted) {
		if(lines.isEmpty())
			return "null";

		int sz = lines.size();
		StringBuilder retn = new StringBuilder(lines.get(0).str);

		for(int i = 1; i < sz; i += 1) {
			String app = lines.get(i).str;
			if(!app.startsWith("//"))
				retn.append("\n"+app);
		}

		return retn.toString();
	}


	@Override
	public boolean isNull() { return lines.isEmpty(); }

	@Override
	public String getValue() { return mlcache; }

	/* FIXME THESE HAVE TO BE REWRITTEN
	 *
	@Override
	public String[] toStringArray() {
		if(lines.isEmpty())
			return new String[0];
		return lines.toArray(new String[0]);
	}
	@Override
	public String[] toStringArray(String separator) {
		return toStringArray();
	}
	*/

	@Override
	public String toString() {
		return "[" + location.fullname + "=multiline]";
	}

	@Override
	void printOnFile(PrintWriter pr) {
		 // this is basically a copy of super.printOnFile(), but it prints ConfigFile.MULTILINE_BEG
		pr.println(
				Utils.indentation(overhead[0], '\t') +
				name +
				Utils.indentation(overhead[1], ' ') +
				location.cfg.optionSeparator +
				Utils.indentation(overhead[2], ' ') +
				ConfigFile.MULTILINE_BEG);

		for(Line line: lines) {
			pr.println(
					Utils.indentation(line.overhead[0]+1, '\t') +
					line.str +
					Utils.indentation(line.overhead[1], ' '));
		}
		pr.println(
				Utils.indentation(overhead[0], '\t') +
				'}');
	}


	private static class Line {
		private String str;
		private byte[] overhead;
	}

}
