package abr.utils.config;

public class InvalidConfigException extends ConfigException {

	/**
	 *
	 */
	private static final long serialVersionUID = -1518850205195304596L;
	public final int line;

	public InvalidConfigException(ConfigFile cfg) {
		super(cfg);
		line = 0;
	}

	public InvalidConfigException(ConfigFile cfg, String str) {
		super(cfg, str);
		line = 0;
	}

	public InvalidConfigException(ConfigFile cfg, int line) {
		super(cfg);
		this.line = line;
	}

	public InvalidConfigException(ConfigFile cfg, int line, String str) {
		super(cfg, cfg.file.getName() + " at line " + line + ": " + str);
		this.line = line;
	}

	public InvalidConfigException(ConfigFile cfg,String str, Throwable arg1) {
		super(cfg, str, arg1);
		line = 0;
	}

	public InvalidConfigException(ConfigFile cfg, int line, String str, Throwable arg1) {
		super(cfg, cfg.file.getName() + " at line " + line + ": " + str, arg1);
		this.line = line;
	}

}
