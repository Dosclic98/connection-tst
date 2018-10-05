package abr.utils.config;

import java.util.ArrayList;

import abr.utils.Utils;

class ConfigUtils {

	private ConfigUtils() { }

	static byte[] evalOverhead(String[] spl) {
		ArrayList<Byte> retn = new ArrayList<>();
		int sz;
		byte seqInit;
		byte seqEnd;
		int control;
		String substr;

		for(int i = 0; i < spl.length; i += 1) {
			substr = spl[i];
			seqInit = 0;
			sz = substr.length();
			 // increase seqInit until it's not a whitespace anymore
			while(seqInit < sz && Character.isWhitespace(substr.charAt(seqInit))) {
				seqInit += 1;
			}
			seqEnd = seqInit;
			control = seqInit;
			 // increase seqEnd until the non-WS sequence ends (may be 0-long)
			while(control < sz) {
				if(!Character.isWhitespace(substr.charAt(control)))
					seqEnd = (byte) (control+1);
				control += 1;
			}
			//System.out.println(spl[i]+","+seqInit+","+(sz-seqEnd));
			retn.add(seqInit);
			retn.add((byte) (sz - seqEnd));
		}

		int iterThru = retn.size();
		byte[] retnb = new byte[iterThru];
		for(int j = 0; j < iterThru; j += 1) {
			retnb[j] = retn.get(j);
		}

		return retnb;
	}

	static String formatString(String str, ConfigFile cfg, int line)
			throws InvalidConfigException
	{
		if(str.startsWith(ConfigFile.LITERAL)) {
			if(str.endsWith(ConfigFile.LITERAL) && str.length() > 1) {
				System.out.println(" >> " + str.substring(1, str.length()-1));
				return str.substring(1, str.length()-1);
			}
			// else
			throw new InvalidConfigException(
					cfg,
					line+1,
					"control reached EOL (literal value without terminator)");
		}
		return formatEscapes(str, cfg, line);
	}

	private static final String
			META_BEG_DELIM = ConfigFile.META_PREFIX+'{',
			META_END_DELIM = "}";
	static String formatEscapes(String str, ConfigFile cfg, int line)
			throws InvalidConfigException
	{
		boolean validState = true;
		String subDelim = Utils.firstDelim(str, META_BEG_DELIM, META_END_DELIM);

		// parse for metadata. may be optimized:
		//  Utils.firstDelim() calls indexOf, so the string
		//  is iterated twice up to the regex
		while(subDelim != null) {
			String meta = cfg.getMetaVar(subDelim);
			subDelim = META_BEG_DELIM + subDelim + META_END_DELIM;
			str = str.replaceFirst(
					java.util.regex.Pattern.quote(subDelim),
					(meta == null)? "" : meta);
			subDelim = Utils.firstDelim(str, META_BEG_DELIM, META_END_DELIM);
		}

		if(!validState)
			throw new InvalidConfigException(
					cfg,
					line+1,
					"control reached EOL (literal value without terminator)");
		 // temporary fast-coded return, can and should be heavily optimized
		return str
				.replace("\\n", "\n")
				.replace("\\\"", "\"")
				.replace("\\t", "\t");
	}

	static byte[] encapsulateOverhead(byte fst, byte[] oh, byte lst) {
		byte[] retn = new byte[oh.length + 2];
		retn[0] = fst;
		retn[oh.length+1] = lst;
		for(int i = 0; i < oh.length; i += 1)
			retn[i+1] = oh[i];
		return retn;
	}

}
