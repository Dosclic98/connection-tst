package abr.utils.config;

public final class ConfigLocation {

	public final ConfigLocation parent;
	public final ConfigFile cfg;
	public final String name;
	public final String fullname;

	/**
	 * @param parent - the location containing this one (null if the parent is
	 * the root location)
	 * @param name - the name of the location
	*/
	public ConfigLocation(ConfigLocation parent, String name) {
		this.parent = parent;
		this.name = name;
		this.cfg = parent.cfg;
		fullname = computeFullname();
	}

	public ConfigLocation(String name, ConfigFile file) {
		String[] sep = name.split("\\.");
		if(sep.length < 1) {
			this.parent = null;
			this.name = file.rootName;
			this.fullname = file.rootName;
			cfg = file;
			return;
		}

		ConfigLocation buffer = new ConfigLocation(file.rootSection.location, sep[0]);
		for(int i = 1; i < sep.length; i += 1) {
			buffer = new ConfigLocation(buffer, sep[i]);
		}
		this.parent = buffer.parent;
		this.name = buffer.name;
		this.fullname = buffer.computeFullname();
		cfg = file;
	}

	/** constructs a root location */
	public ConfigLocation(ConfigFile file) {
		parent = null;
		name = file.rootName;
		fullname = file.rootName;
		cfg = file;
	}

	/** @return the parent location relative to this instance. */
	public ConfigLocation getParent() { return parent; }
	/** @return the name of this location, excluding its parent. */
	public ConfigLocation getName() { return parent; }

	public boolean isRoot() {
		return parent == null;
	}

	public ConfigLocation subLocation(String name) {
		return new ConfigLocation(this, name);
	}

	private String computeFullname() {
		if(parent == null)
			return cfg.rootName;
		else
		if(parent.isRoot())
			return name;
		else
			return parent.toString() + '.' + name;
	}

	@Override
	public String toString() {
		return fullname;
	}

	@Override
	public boolean equals(Object othr) {
		if(othr instanceof String)
			return fullname.equals(othr);
		if(othr instanceof ConfigLocation)
			return fullname.equals(((ConfigLocation) othr).fullname);
		return false;
	}

	@Override
	public int hashCode() {
		return fullname.hashCode();
	}

}
