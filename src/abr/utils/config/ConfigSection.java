package abr.utils.config;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Map.Entry;

import abr.utils.Utils;

public final class ConfigSection extends ConfigEntry {

	HashMap<String, ConfigEntry> entries = new HashMap<>();
	LinkedList<ConfigEntry> orderedEntries = new LinkedList<>();

	ConfigSection(ConfigSection sec, String name, byte[] overhead) {
		super(Objects.requireNonNull(sec), name, overhead);
	}

	 // should only be used for root section
	ConfigSection(ConfigFile file) {
		super(file.rootName, file);
	}

	public boolean isRoot() {
		return
				this.overhead[0] == -1
				||
				(this.section == null && this.name.equals(location.cfg.rootName));
	}

	public ConfigEntry get(String subpath) {
		String[] spl = subpath.split("\\.", 2);
		if(spl.length == 1) {
			return entries.get(spl[0]);
		}
		else
		if(spl.length == 2) {
			ConfigEntry subsec = entries.get(spl[0]);
			if(subsec == null) {
				subsec = createSection(spl[0]);
			}
			else
			if(!(subsec instanceof ConfigSection))
				return null;
			// else
			return ((ConfigSection) subsec).get(spl[1]);
		}
		// else
		return null;
	}

	/** Behaves exactely like {@link HashMap#remove(Object)}. */
	ConfigOption removeOption(String option) {
		ConfigEntry got = entries.get(option);
		if(!(got instanceof ConfigOption))
			return null;
		got.invalidate();
		return (ConfigOption) entries.remove(option);
	}

	/** Behaves exactely like {@link HashMap#remove(Object)}. */
	ConfigSection removeSection(String section) {
		ConfigEntry got = entries.get(section);
		if(!(got instanceof ConfigSection))
			return null;
		((ConfigSection) got).clear();
		got.invalidate();
		return (ConfigSection) entries.remove(section);
	}

	/** Behaves exactely like {@link HashMap#remove(Object)}. */
	ConfigTag removeTag(String tag) {
		ConfigEntry got = entries.get(tag);
		if(!(got instanceof ConfigTag))
			return null;
		got.invalidate();
		return (ConfigTag) entries.remove(tag);
	}

	/** Empties all the entries. */
	void clear() {
		for(Entry<String, ConfigEntry> entry: entries.entrySet()) {
			if(entry.getValue() instanceof ConfigSection)
				((ConfigSection) entry.getValue()).clear();
		}
		orderedEntries.clear();
		entries.clear();
	}

	ConfigEntry put(ConfigEntry entry) {
		ConfigEntry prev = entries.put(entry.location.name, entry);
		 // add to linked list only if it's not a dupe
		if(prev == null)
			orderedEntries.addLast(entry);
		return prev;
	}

	/** Creates or sets an option in the section.
	 * @return a new option, or an existing one if found.
	*/
	public ConfigOption setOption(String name, String value) {
		if(entries.get(name) instanceof ConfigSection)
			throw new InvalidNameException(
					"tried to set option " + location.fullname + '.' + name +
					", but an entry with that name already exists");
		ConfigOption opt = new ConfigOption(
				this, name, value,
				ConfigUtils.encapsulateOverhead(
						(byte) (overhead[0]+1),
						location.cfg.defaultOptionOverhead,
						(byte) 0));
		ConfigEntry prev = entries.put(name, opt);
		 // add to linked list only if it's not a dupe
		if(prev == null)
			orderedEntries.addFirst(opt);
		return opt;
	}

	/** Creates or sets a multiline option in the section.
	 * @return a new option, or an existing one if found.
	*/
	public ConfigOption setOption(String name, Iterable<String> values) {
		if(entries.get(name) instanceof ConfigSection)
			throw new InvalidNameException(
					"tried to set option " + location.fullname + '.' + name +
					", but an entry with that name already exists");
		ConfigOptionMultiLine opt = new ConfigOptionMultiLine(
				this, name, values,
				ConfigUtils.encapsulateOverhead(
						(byte) (overhead[0]+1),
						location.cfg.defaultOptionOverhead,
						(byte) 0));
		ConfigEntry prev = entries.put(name, opt);
		 // add to linked list only if it's not a dupe
		if(prev == null)
			orderedEntries.addFirst(opt);
		return opt;
	}

	/** Creates a tag in the section.
	 * @return the new or existing tag. */
	public ConfigTag setTag(String name) {
		if(entries.get(name) instanceof ConfigSection)
			throw new InvalidNameException(
					"tried to add tag " + location.fullname + '.' + name +
					", but an entry with that name already exists");
		ConfigTag tag = new ConfigTag(
				this, name,
				ConfigUtils.encapsulateOverhead(
						(byte) (overhead[0]+1),
						location.cfg.defaultTagOverhead,
						(byte) 0));
		ConfigEntry prev = entries.put(name, tag);
		 // add to linked list only if it's not a dupe
		if(prev == null)
			orderedEntries.addFirst(tag);
		return tag;
	}

	public ConfigOption setDefaultOption(String name, String value) {
		ConfigOption opt = new ConfigOption(
				this, name, value,
				ConfigUtils.encapsulateOverhead(
						(byte) (overhead[0]+1),
						location.cfg.defaultOptionOverhead,
						(byte) 0));
		ConfigEntry old = entries.putIfAbsent(name, opt);
		if(old == null) {
			orderedEntries.addLast(opt);
			return opt;
		}
		if(!(old instanceof ConfigOption))
			throw new InvalidNameException(
					"tried to set option " + location.fullname + '.' + name +
					", but an option with that name already exists");
		else return (ConfigOption) old;
	}

	/** Creates a section with the given name.
	 * @return a new section, or an existing one if found */
	ConfigSection createSection(String name) {
		ConfigEntry sec = entries.get(name);
		if(sec == null) {
			sec = new ConfigSection(
					this, name,
					ConfigUtils.encapsulateOverhead((byte) (overhead[0]+1), location.cfg.defaultSectionOverhead, (byte) 0));
			ConfigEntry prev = entries.put(name, sec);
			 // add to linked list only if it's not a dupe
			if(prev == null)
				orderedEntries.addFirst(sec);
		}
		if(!(sec instanceof ConfigSection))
			throw new InvalidNameException(
					location.fullname + '.' + name +
					" exists, but is not a section");
		return (ConfigSection) sec;
	}

	ConfigSection navigate(String subpath) {
		String[] spl = subpath.split("\\.", 2);

		if(spl.length == 1)
			return this;
		if(spl.length > 1) {
			ConfigEntry subsec = entries.get(spl[0]);
			if(subsec == null) {
				subsec = createSection(spl[0]);
			}
			else
			if(!(subsec instanceof ConfigSection))
				throw new InvalidNameException(
						location.fullname + '.' + name +
						" exists, but is not a section");
			// else
			return ((ConfigSection) subsec).navigate(spl[1]);
		}
		else
			throw new RuntimeException("unknown error");
	}

	public ConfigOption getOption(String loc) {
		ConfigSection sec = navigate(loc);
		ConfigEntry entry = sec.get(loc);

		// trim loc in order to only have the simple name of the location
		{
			String[] split = loc.split(".");
			if(split.length > 0)
				loc = split[split.length - 1];
		}

		if(entry == null) {
			entry = new ConfigOption(
					this, loc, "null",
					ConfigUtils.encapsulateOverhead(
							(byte) (sec.overhead[0]+1),
							location.cfg.defaultOptionOverhead,
							(byte) 0));
			ConfigEntry prev = sec.entries.put(loc, entry);
			 // add to linked list only if it's not a dupe
			if(prev == null)
				sec.orderedEntries.addFirst(entry);
			return (ConfigOption) entry;
		}
		if(!(entry instanceof ConfigOption)) {
			throw new InvalidNameException(
					sec.location.fullname + '.' + name +
					" exists, but is not an option");
		}
		return (ConfigOption) entry;
	}
	public ConfigOption getOption(ConfigLocation loc) {
		ConfigSection sec = navigate(loc.fullname);
		return sec.getOption(loc.name);
	}

	public ConfigSection getSection(String loc) {
		ConfigSection sec = navigate(loc);
		 // trim loc to only contain the simple name of the location
		String[] split = loc.split(".");
		if(split.length > 0)
			return sec.createSection(split[split.length - 1]);
		// else
		return sec.createSection(loc);
	}
	public ConfigSection getSection(ConfigLocation loc) {
		ConfigSection sec = navigate(loc.fullname);
		 // don't add the section itself
		if(this.location.equals(loc))
			return this;
		// else
		return sec.createSection(loc.name);
	}

	public ConfigTag getTag(ConfigLocation loc) {
		ConfigSection sec = navigate(loc.fullname);
		ConfigEntry entry = sec.get(loc.name);
		if(entry == null) {
			return null;
		}
		if(!(entry instanceof ConfigTag)) {
			throw new InvalidNameException(
					location.fullname + '.' + name +
					" exists, but is not a tag");
		}
		return (ConfigTag) entry;
	}

	public LinkedList<ConfigEntry> getAllEntries() {
		return orderedEntries;
	}

	public Collection<ConfigSection> getAllSections() {
		Collection<ConfigSection> sections = new ArrayList<>();
		for(ConfigEntry entry: orderedEntries) {
			if(entry instanceof ConfigSection)
				sections.add((ConfigSection) entry);
		}
		return sections;
	}

	public Collection<ConfigOption> getAllOptions() {
		Collection<ConfigOption> sections = new ArrayList<>();
		for(ConfigEntry entry: orderedEntries) {
			if(entry instanceof ConfigOption)
				sections.add((ConfigOption) entry);
		}
		return sections;
	}

	public Collection<ConfigTag> getAllTags() {
		Collection<ConfigTag> sections = new ArrayList<>();
		for(ConfigEntry entry: orderedEntries) {
			if(entry instanceof ConfigTag)
				sections.add((ConfigTag) entry);
		}
		return sections;
	}

	public void cloneData(ConfigSection src) {
		for(ConfigEntry entry: orderedEntries) {
			ConfigEntry newEntry;
			if(entry instanceof ConfigSection) {
				newEntry = createSection(entry.name);
				((ConfigSection) newEntry).cloneData((ConfigSection) entry);
			}
			else
			if(entry instanceof ConfigTag) {
				setTag(entry.name);
			}
			else
			if(entry instanceof ConfigOption) {
				setOption(entry.name, ((ConfigOption) entry).value);
			}
			else
			if(entry instanceof EmptyLine) {
				put(new EmptyLine(this, (EmptyLine) entry));
			}
		}
	}

	 // not updated to use orderedEntries yet
	public void defaultData(ConfigSection src) {
		for(Entry<String, ConfigEntry> mapEntry: src.entries.entrySet()) {
			ConfigEntry entry = mapEntry.getValue();
			ConfigEntry newEntry = null;
			if(entry instanceof ConfigSection) {
				newEntry = createSection(entry.name);
				((ConfigSection) newEntry).defaultData((ConfigSection) entry);
			}
			else
			if(entry instanceof ConfigTag) {
				setTag(entry.name);
			}
			else
			if(entry instanceof ConfigOption) {
				setDefaultOption(entry.name, ((ConfigOption) entry).value);
			}
			else
			if(entry instanceof EmptyLine) {
				put(entry);
			}
			else
				throw new RuntimeException("unknown error");
		}
	}

	@Override
	public boolean isValid() { return super.valid && !entries.isEmpty(); }

	@Override
	public String toString() {
		return String.valueOf('[') + location + ']';
	}

	@Override
	void printOnFile(PrintWriter pr) {
		if(entries.isEmpty() || location.isRoot()) {
			return;
		}
		pr.println(
				Utils.indentation(overhead[0], '\t') +
				name +
				Utils.indentation(overhead[1], ' ') +
				location.cfg.sectionSuffix);
		for(ConfigEntry entry: orderedEntries) {
			entry.printOnFile(pr);
		}
	}

}
