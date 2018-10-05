package abr.utils.config;

public abstract class ConfigException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 9094406331139533078L;
	public final ConfigFile config;

	public ConfigException(ConfigFile cfg) {
		config = cfg;
	}

	public ConfigException(ConfigFile cfg, String arg0) {
		super(arg0);
		config = cfg;
	}

	public ConfigException(ConfigFile cfg, Throwable arg0) {
		super(arg0);
		config = cfg;
	}

	public ConfigException(ConfigFile cfg, String arg0, Throwable arg1) {
		super(arg0, arg1);
		config = cfg;
	}

	public ConfigException(ConfigFile cfg, String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
		config = cfg;
	}

}
