package abr.utils.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;

import abr.utils.Utils;

public class ConfigFile {

	public final static int configVersion = 3;
	public final static String META_PREFIX = "#";
	public final static String META_SEPARATOR = " ";
	public final static String COMMENT_PREFIX = "//";
	public final static String MULTILINE_BEG = "{";
	public final static String MULTILINE_END = "}";
	public final static String LITERAL = "\"";
	public File file;
	ConfigSection rootSection = new ConfigSection(this);
	String rootName = "~";
	String optionSeparator = ":";
	String sectionSuffix = ":";
	String tagPrefix = "-";
	byte[] defaultTagOverhead = new byte[] {1};
	byte[] defaultOptionOverhead = new byte[] {0,1};
	byte[] defaultSectionOverhead = new byte[] {0};
	HashMap<String, String> meta = new HashMap<>();
	LinkedList<ConfigWarning> warnings = new LinkedList<>();

	ConfigFile(File file) {
		this.file = file;
	}

	/** Copies the source's metadata into this ConfigFile.
	 * @deprecated Doesn't work as expected, use {@link #saveOnNewFile(String)}. */
	@Deprecated
	public final void cloneMeta(ConfigFile src) {
		for(Entry<String, String> othr: src.meta.entrySet()) {
			meta.put(othr.getKey(), othr.getValue());
		}
	}

	/** Copies the source's options and tags into this ConfigFile.
	 * @deprecated Doesn't work as expected, use {@link #saveOnNewFile(String)}. */
	@Deprecated
	public final void cloneData(ConfigFile src) {
		rootSection.cloneData(src.rootSection);
	}

	/** Copies the source's metadata into this ConfigFile, without
	 * replacing already existing values. */
	public final void defaultMeta(ConfigFile src) {
		for(Entry<String, String> othr: src.meta.entrySet())
			meta.putIfAbsent(othr.getKey(), othr.getValue());
	}

	/** Copies the source's options and tags into this ConfigFile, without
	 * replacing already existing values. */
	public final void defaultData(ConfigFile src) {
		rootSection.defaultData(src.rootSection);
	}

	/**
	 * Tries to read a configuration file, or creates a new one on
	 * FileNotFoundException.
	 * @return a populated ConfigFile (never null).
	 * @throws IOException - if file exists but cannot be read
	*/
	public static final ConfigFile getConfig(File file)
			throws IOException, InvalidConfigException
	{
		ConfigFile retn;
		try {
			retn = readConfig(file);
		} catch(FileNotFoundException ex) {
			file.createNewFile();
			retn = readConfig(file);
		}
		return Objects.requireNonNull(retn);
	}

	/**
	 * Tries to read a configuration file.
	 * @return a populated ConfigFile.
	*/
	public static ConfigFile readConfig(File file)
			throws FileNotFoundException, IOException, InvalidConfigException
	{
		ConfigFile cfg = new ConfigFile(file);
		ArrayList<String> lines = new ArrayList<>();

		BufferedReader br = new BufferedReader(new FileReader(file));
		String read;
		while((read = br.readLine()) != null)
			lines.add(read);
		br.close();

		cfg.parseMeta(lines);
		cfg.parseConfig(lines);

		return cfg;
	}

	/**
	 * Tries to create a new, empty configuration file.
	 * @return an empty ConfigFile.
	*/
	public static final ConfigFile newConfig(File file) throws IOException {
		file.delete();
		file.createNewFile();
		ConfigFile cfg = new ConfigFile(file);
		ArrayList<String> empty = new ArrayList<>();
		try {
			cfg.parseMeta(empty);
			cfg.parseConfig(empty);
		} catch (InvalidConfigException ex) {
			 // this should really never happen on an empty file
			ex.printStackTrace();
		}
		return cfg;
	}

	final void parseMeta(ArrayList<String> lines) throws IOException, InvalidConfigException {
		String read;
		int linesCount = lines.size();

		 // 'line' could be changed when parsing multilines
		for(int line = 0; line < linesCount; line += 1) {
			read = lines.get(line);
			if(read.length() == 0) {
				continue;
			}
			String subst = Utils.trimSpaces(read);
			String[] spl = subst.split(META_SEPARATOR, 2);

			if(spl.length == 2) {
				if(spl[0].length() <= 0)
					continue;
				//spl[0] = Utils.trimSpaces(spl[0]);  // can't have spaces either way
				spl[1] = Utils.trimLeadingSpaces(spl[1]);
				if(spl[1].length() > 0) {
					if(spl[0].startsWith(META_PREFIX)) {
						spl[0] = spl[0].substring(META_PREFIX.length());
						//meta.put(spl[0], spl[1]);  // this happens in processMetadata
						processMetadata(spl[0], spl[1], line+1);
					}
				}
			}
			else
			if(spl.length == 1) {
				if(spl[0].length() <= 0)
					continue;
				if(spl[0].startsWith(META_PREFIX)) {
					spl[0] = spl[0].substring(META_PREFIX.length());
					//meta.put(spl[0], spl[1]);  // this happens in processMetadata
					processMetadata(spl[0], "", line+1);
				}
			}
		}

		// assign default values
		// REMOVED CUZ BLOAT meta.putIfAbsent("cfg.rootname", rootName);
		meta.putIfAbsent("cfg.version", String.valueOf(configVersion));
	}

	final void parseConfig(ArrayList<String> lines) throws IOException, InvalidConfigException {
		String read, trimmedRead;
		ConfigSection sec = new ConfigSection(this);
		ConfigSection newSec = null;
		boolean isEmpty;
		boolean isComment;
		int lineIndent = 0;
		int curIndent = 0;
		int linesCount = lines.size();

		for(int line = 0; line < linesCount; line += 1) {
			read = lines.get(line);
			trimmedRead = Utils.trimSpaces(read);

			isEmpty = false;
			isComment = false;

			// check if line is empty
			if(trimmedRead.length() == 0) {
				sec.put(new EmptyLine(sec, line, (byte) curIndent));
				isEmpty = true;
			}

			// check if line is comment
			if(trimmedRead.startsWith(COMMENT_PREFIX)) {
				sec.put(new EmptyLine(sec, line, read));
				isComment = true;
			}

			// determine indentation
			lineIndent = 0;
			while(lineIndent < read.length()) {
				if(read.charAt(lineIndent) == '\t') {
					lineIndent += 1;
				}
				else
				if((!isComment||isEmpty) && read.charAt(lineIndent) == ' ') {
					throw new InvalidConfigException(this, line+1, "spaces not allowed as indentation");
				}
				else break;
			}
			String subst = read.substring(lineIndent);
			String[] spl = read.split(optionSeparator, 2);
			String[] splTrimmed = subst.split(optionSeparator, 2);
			for(int i = 0; i < splTrimmed.length; i += 1)
				splTrimmed[i] = Utils.trimSpaces(splTrimmed[i]);

			 // if the line is metadata, it is meaningless here
			if(splTrimmed[0].startsWith(META_PREFIX))
				continue;

			// don't parse indentation if line is empty or comment
			if(!(isEmpty || isComment)) {
				// first check: relative indent is to the left;
				// pop sections
				while((lineIndent - curIndent) < 0) {
					 // null check, shouldn't happen but ynk
					if(sec == null)
						throw new InvalidConfigException(this, line+1, "invalid indentation");
					if(!sec.entries.isEmpty()) {
						//sections.put(sec.location.fullname, sec);
						sec.section.put(sec);
					}
					curIndent -= 1;
					sec = sec.section;
				}

				// second check: relative indent is to the right;
				// the difference can't be higher than 1
				if((lineIndent - curIndent) > 0) {
					if((lineIndent - curIndent) > 1) {
						throw new InvalidConfigException(this, line+1, "invalid indentation");
					}
					if(newSec != null) {
						sec = newSec;
						newSec = null;
						curIndent += 1;
					} else {
						throw new InvalidConfigException(this, line+1, "invalid indentation: not following a section name");
					}
				}
			}

			if(isEmpty) {
				sec.put(new EmptyLine(sec, line, (byte) curIndent));
			}
			else
			if(isComment) {
				sec.put(new EmptyLine(sec, line, read));
			}
			else
			if(splTrimmed.length == 1) {
				newSec = parseLineSingle(sec, line, ConfigUtils.evalOverhead(spl), splTrimmed[0]);
			}
			else
			if(splTrimmed.length == 2) {
				if(splTrimmed[1].equals(MULTILINE_BEG)) {
					line = parseMultiLine(sec, line, splTrimmed[0], ConfigUtils.evalOverhead(spl), lines);
				} else {
					newSec = parseLineSplit2(sec, line, ConfigUtils.evalOverhead(spl), optionSeparator, splTrimmed);
				}
			}
		}
		// pop all sections at EOF
		while(curIndent > 0) {
			 // null check, shouldn't happen but ynk
			if(sec == null)
				throw new InvalidConfigException(this, linesCount, "invalid indentation");
			if(!sec.entries.isEmpty()) {
				sec.section.put(sec);
			}
			curIndent -= 1;
			sec = sec.section;
		}
		rootSection = sec;
	}

	/** This is an internal function used by parseConfig(), barely stitched
	 * together to apply the Divide-Et-Impera strategy. Making sense with general
	 * function is impossible in this context, so whoever reads this will
	 * (probably?) have to deal with this bbq.
	 * @return the index of the first line out of the multiline block.
	 * @throws InvalidConfigException */
	private final int parseMultiLine(
			ConfigSection sec, int begLine,
			String optName,
			byte[] overhead,
			List<String> lines
	)
			throws InvalidConfigException
	{
		int curLine = begLine + 1;
		int sz = lines.size();
		String trimmed;
		String lineStr;
		ArrayList<String> valueLines = new ArrayList<>();

		while(curLine < sz) {
			lineStr = lines.get(curLine);
			trimmed = Utils.trimSpaces(lineStr);

			if(trimmed.equals(MULTILINE_END))
				break;
			if(trimmed.startsWith(COMMENT_PREFIX)) {
				warn("comments inside multiline options are not retained", curLine);
				continue;
			}
			valueLines.add(lineStr);

			curLine += 1;
		}

		if(curLine == sz)
			throw new InvalidConfigException(
					this,
					begLine+1,
					"control reached EOF (multiline option without terminator)");

		sec.put(new ConfigOptionMultiLine(sec, optName, valueLines, overhead));
		return curLine;
		//throw new InvalidConfigException(this, begLine+1, "stub function, should not be called");
	}

	/** This is an internal function used by parseConfig(), barely stitched
	 * together to apply the Divide-Et-Impera strategy. Making sense with general
	 * function is impossible in this context, so whoever reads this will
	 * (probably?) have to deal with this bbq.
	 * @return a new section, if one has been created; null otherwise.
	 * @throws InvalidConfigException */
	private final ConfigSection parseLineSplit2(ConfigSection sec, int line, byte[] overhead, String sep, String[] spl)
			throws InvalidConfigException
	{
		ConfigSection newSec = null;

		spl[1] = Utils.trimSpaces(spl[1]);

		if(spl[0].contains("."))
			throw new InvalidConfigException(this, line+1, "invalid option name: can't contain dots");
		if(spl[1].length() > 0) {
			//System.out.println("sp overhead:"+spl[0]+"[]"+overhead[0]);
			ConfigOption opt = new ConfigOption(sec, spl[0], spl[1], overhead);
			sec.put(opt);
		} else {
			newSec = parseLineSingle(sec, line, overhead, spl[0] + sep);
		}

		return newSec;
	}

	/** This is an internal function used by parseConfig(), barely stitched
	 * together to apply the Divide-Et-Impera strategy. Making sense with general
	 * function is impossible in this context, so whoever reads this will
	 * (probably?) have to man up and deal with this bbq.
	 * @return a new section, if one has been created; null otherwise.
	 * @throws InvalidConfigException */
	private final ConfigSection parseLineSingle(ConfigSection sec, int line, byte[] overhead, String spl0)
			throws InvalidConfigException
	{
		ConfigSection newSec = null;
		spl0 = Utils.trimSpaces(spl0);

		if(spl0.contains("."))
			throw new InvalidConfigException(this, line+1, "invalid name (can't contain dots)");
		if(spl0.endsWith(sectionSuffix)) {
			spl0 = spl0.substring(0, spl0.length()-sectionSuffix.length());
			overhead = ConfigUtils.evalOverhead(new String[] {spl0});
			spl0 = Utils.trimLeadingSpaces(spl0);
			//System.out.println("si overhead:"+spl0+"[]"+overhead[0]);
			newSec = new ConfigSection(
					sec,
					spl0,
					overhead);
		} else
		/*
		if(spl0.startsWith(tagPrefix)) {
			ConfigTag tag = new ConfigTag(sec, spl0.substring(tagPrefix.length()));
			sec.put(tag);
		} else
		*/
		if(spl0.startsWith(tagPrefix)) {
			String tagName = spl0.substring(tagPrefix.length());
			String tagNameTrimmed = Utils.trimPrecedingSpaces(tagName);
			if(tagNameTrimmed.length() == 0)
				throw new InvalidConfigException(this, line+1, "tag prefix without name");
			byte[] tagOverhead = new byte[3];
			// evaluate tag-specific overhead
			tagOverhead[0] = overhead[0];
			{
				byte[] suboh = ConfigUtils.evalOverhead(new String[] {tagName});
				tagOverhead[1] = suboh[0];
				tagOverhead[2] = suboh[1];
			}

			//System.out.println("tg overhead:"+tagNameTrimmed+"[]"+overhead[0]);
			ConfigTag tag = new ConfigTag(
					sec,
					tagNameTrimmed,
					tagOverhead);
			sec.put(tag);
		} else
			throw new InvalidConfigException(this, line+1, "invalid symbol: neither a section, an option nor a tag");

		return newSec;
	}

	public final LinkedList<ConfigWarning> getWarnings() { return warnings; }
	protected final void warn(Object obj, int line) {
		warnings.add(new ConfigWarning(String.valueOf(obj), line));
	}

	/** @return An Iterable of all meta variables (empty variables have value ""). */
	public final Set<Entry<String, String>> getMetadata() {
		return meta.entrySet();
	}

	public final ConfigSection getRootSection() {
		return rootSection;
	}

	public final ConfigOption removeOption(String location) {
		ConfigLocation loc = new ConfigLocation(location, this);
		return removeOption(loc);
	}

	public ConfigOption removeOption(ConfigLocation location) {
		ConfigSection sec = getSection(location.parent);
		return sec.removeOption(location.name);
	}

	public final ConfigTag removeTag(String location) {
		ConfigLocation loc = new ConfigLocation(location, this);
		return removeTag(loc);
	}

	public ConfigTag removeTag(ConfigLocation location) {
		ConfigSection sec = getSection(location.parent);
		return sec.removeTag(location.name);
	}

	public final ConfigSection removeSection(String location) {
		ConfigLocation loc = new ConfigLocation(location, this);
		return removeSection(loc);
	}

	public ConfigSection removeSection(ConfigLocation location) {
		ConfigSection sec = getSection(location);
		return sec.removeSection(location.name);
	}

	/**
	 * Looks for an option within a certain location, eventually
	 * creating a new one with null value.
	 * @return A new option, or an option with value==null if none has been found.
	*/
	public final ConfigOption getOption(String location) {
		ConfigLocation loc = new ConfigLocation(location, this);
		return getOption(loc);
	}
	/**
	 * Looks for an option within a certain location, eventually
	 * creating a new one with null value.
	 * @return A new option, or an option with value==null if none has been found.
	*/
	public ConfigOption getOption(ConfigLocation location) {
		return rootSection.getOption(location);
	}

	/**
	 * Looks for a tag within a certain location.
	 * @return An existing tag, or null if it hasn't been found.
	*/
	public final ConfigTag getTag(String location) {
		ConfigLocation loc = new ConfigLocation(location, this);
		return getTag(loc);
	}
	/**
	 * Looks for a tag within a certain location.
	 * @return An existing tag, or null if it hasn't been found.
	*/
	public ConfigTag getTag(ConfigLocation location) {
		return rootSection.getTag(location);
	}

	public ConfigTag setTag(String location, String name) {
		return setTag(new ConfigLocation(location, this), name);
	}
	public ConfigTag setTag(ConfigLocation location, String name) {
		ConfigSection sec = getSection(location.subLocation(name));
		return sec.setTag(name);
	}
	public ConfigTag setTag(ConfigLocation location) {
		ConfigSection sec = getSection(location.parent);
		return sec.setTag(location.name);
	}

	/**
	 * Looks for a section, or creates an empty one if none has been found.
	 * @return A new section, or an existing one if it has been found.
	*/
	public final ConfigSection getSection(String location) {
		ConfigLocation loc = new ConfigLocation(location, this);
		return getSection(loc);
	}

	/**
	 * Looks for a section, or creates an empty one if none has been found.
	 * @return A new section, or an existing one if it has been found.
	*/
	public ConfigSection getSection(ConfigLocation location) {
		return rootSection.getSection(location);
	}

	/** @return the number of metadata entries successfully processed */
	int reprocessMetadata() {
		int counter = 0;
		//boolean tmp;
		for(Entry<String, String> entry: this.meta.entrySet()) {
			String meta = entry.getKey();
			String value = entry.getValue();

			counter += 1;

			/*
			tmp = false;
			if(meta.startsWith("cfg.tmp.")) {
				meta = meta.replaceFirst("cfg.tmp.", "cfg.");
				tmp = true;
			}
			*/

			if(value.length() > 0) {
				switch(meta) {
				case "cfg.rootname":
					rootName = value;
					break;
				case "cfg.option.separator":
					optionSeparator = value;
					break;
				case "cfg.section.suffix":
					sectionSuffix = value;
					break;
				case "cfg.tag.prefix":
					tagPrefix = value;
					break;
				default:
					break;
				}
			}

			else {  // if value = ""
				switch(meta) {
				case "cfg.genmeta":
					this.meta.putIfAbsent("cfg.option.separator", optionSeparator);
					this.meta.putIfAbsent("cfg.section.suffix",   sectionSuffix);
					this.meta.putIfAbsent("cfg.tag.prefix",       tagPrefix);
					break;
				default:
					break;
				}
			}

			/*
			if(tmp) {
				this.meta.remove(entry.getKey());
			}
			*/
		}
		return counter;
	}

	void processMetadata(String meta, String value, int line) throws InvalidConfigException {
		boolean shouldKeep = true;
		if(!meta.startsWith("cfg.")) {
			this.meta.put(meta, value);
			return;
		}
		 // used to remember temporary metavars
		String originalMeta = meta;

		/*
		if(meta.startsWith("cfg.tmp.")) {
			meta = meta.replaceFirst("cfg.tmp.", "cfg.");
		}
		*/

		if(value.length() > 0) {
			switch(meta) {
			case "cfg.version":
				try {
					if(configVersion < Integer.parseInt(value))
						throw new InvalidConfigException(this, line+1, "incompatible configuration file version");
				} catch(NumberFormatException ex) {
					throw new InvalidConfigException(this, line+1, ex.getMessage());
				}
				break;
			case "cfg.rootname":
				rootName = value;
				break;
			case "cfg.option.separator":
				optionSeparator = value;
				break;
			case "cfg.option.overhead":
				try {
					String[] split = value.split(META_SEPARATOR);
					defaultTagOverhead = new byte[] {
							Byte.valueOf(Utils.trimSpaces(split[0])),
							Byte.valueOf(Utils.trimSpaces(split[1]))
					};
				} catch(NumberFormatException | ArrayIndexOutOfBoundsException ex) {
					throw new InvalidConfigException(this, line+1, ex.getMessage());
				}
				break;
			case "cfg.section.suffix":
				sectionSuffix = value;
				break;
			case "cfg.section.overhead":
				try {
					defaultSectionOverhead = new byte[] { Byte.valueOf(value) };
				} catch(NumberFormatException ex) {
					throw new InvalidConfigException(this, line+1, ex.getMessage());
				}
				break;
			case "cfg.tag.prefix":
				tagPrefix = value;
				break;
			case "cfg.tag.overhead":
				try {
					defaultTagOverhead = new byte[] { Byte.valueOf(value) };
				} catch(NumberFormatException ex) {
					throw new InvalidConfigException(this, line+1, ex.getMessage());
				}
				break;
			case "cfg.warn":
				shouldKeep = false;
				warn(value, line);
				break;
			default:
				break;
			}
		}

		else {  // if value = ""
			switch(meta) {
			case "cfg.genmeta":
				this.meta.putIfAbsent("cfg.option.separator", optionSeparator);
				this.meta.putIfAbsent("cfg.option.overhead", defaultOptionOverhead[0]+META_SEPARATOR+defaultOptionOverhead[1]);
				this.meta.putIfAbsent("cfg.section.suffix",   sectionSuffix);
				this.meta.putIfAbsent("cfg.section.overhead", String.valueOf(defaultOptionOverhead[0]));
				this.meta.putIfAbsent("cfg.tag.prefix",       tagPrefix);
				this.meta.putIfAbsent("cfg.tag.overhead", String.valueOf(defaultOptionOverhead[0]));
				break;
			default:
				break;
			}
		}

		if(shouldKeep)
			this.meta.put(originalMeta, value);
	}

	public String[] getMetaVarArray(String meta, int expectedSize)
			throws InvalidConfigException
	{
		String got = this.meta.get(meta);
		if(got == null)
			return null;
		String[] split = got.split(META_SEPARATOR, expectedSize);
		if(split.length != expectedSize)
			throw new InvalidConfigException(
					this,
					String.format(
							"meta variable \"%s\" expects %d arguments",
							meta, expectedSize));
		return split;
	}

	/** @return
	 * a string if the variable has a value;
	 * "" if the variable exists but has no value;
	 * null if the variable doesn't exist. */
;	public String getMetaVar(String meta) {
		return this.meta.get(meta);
	}

	public void setMetaVar(String meta, String value) {
		this.meta.put(meta, value);
	}
	public void setMetaVar(String meta, String... values) {
		String cat = "";
		if(values.length > 0)
			cat = values[0];
		for(int i = 1; i < values.length; i += 1)
			cat +=  META_SEPARATOR + values[i];
		setMetaVar(meta, cat);
	}

	/** Selects a new file location to use when saving,
	 * and performs an automatic save. */
	public void saveOnNewFile(String newname) throws FileNotFoundException {
		File oldFile = file;
		file = new File(newname);
		try(PrintWriter pr = new PrintWriter(file)) {
			write(pr);
		} catch(FileNotFoundException ex) {
			file = oldFile;
			throw ex;
		}
	}

	public void save() {
		try(PrintWriter pr = new PrintWriter(file)) {
			write(pr);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
	}

	public void write(PrintWriter writer) {
		/* int metaProcessed = */ reprocessMetadata();

		for(Entry<String, String> meta: meta.entrySet()) {
			String val = meta.getValue();
			if(val.length() > 0)
				writer.println(META_PREFIX + meta.getKey() + META_SEPARATOR + val);
			else
				writer.println(META_PREFIX + meta.getKey());
		}

		for(ConfigEntry entry: rootSection.orderedEntries) {
			if(entry.isValid())
				entry.printOnFile(writer);
		}
	}

	public static final void main(String[] args) throws Throwable {
		ConfigFile cfg = getConfig(new File("config.cfg"));
		cfg.getRootSection().setTag("neither");
		cfg.getSection("heh").setTag("dioporco");
		try(PrintWriter pw = new PrintWriter(System.out)) {
			cfg.write(pw);
		}
		File gay = new File("gay");
		ConfigFile othr1 = newConfig(gay);
		othr1.cloneData(cfg);
		othr1.defaultData(cfg);
		gay.delete();
	}

}
