package dos.connes;

import java.io.File;
import java.io.IOException;

import abr.utils.config.ConfigFile;
import abr.utils.config.InvalidConfigException;

public class ConfigExample {

	public static void main(String[] args) throws InvalidConfigException, IOException
	{
		File cfgFile = new File("config.cfg");
		// apri un config esistente, oppure creane uno nuovo
		ConfigFile cfg = ConfigFile.getConfig(cfgFile);

		// cerca l'opzione 'my-option' e stampala a schermo
		System.out.println(
				"my-option is \"" +
				cfg.getOption("my-option").getValueFormatted() +
				'"');
	}

}
