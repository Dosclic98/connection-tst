package abr.utils;

public final class Utils {

	private Utils() { }

	public static String trimLeadingSpaces(String src) {
		StringBuffer buf = new StringBuffer(src);
		int len = buf.length();
		char at;
		while(len > 0) {
			at = buf.charAt(len-1);
			if(Character.isWhitespace(at)) {
				len -= 1;
			}
			else break;
		}
		buf.setLength(len);
		return buf.toString();
	}

	public static String trimPrecedingSpaces(String src) {
		int len = src.length();
		int i = 0;
		while(i < len) {
			if(!Character.isWhitespace(src.charAt(i)))
				break;
			i += 1;
		}
		return src.substring(i);
	}

	public static String trimSpaces(String src) {
		StringBuffer buf = new StringBuffer(src);
		int len = buf.length();
		int a = 0;
		int nxt;
		char at;
		while(a < len) {
			if(!Character.isWhitespace(buf.charAt(a)))
				break;
			a += 1;
		}
		while(len > a) {
			nxt = len-1;
			at = buf.charAt(nxt);
			if(Character.isWhitespace(at)) {
				len = nxt;
			}
			else break;
		}
		return buf.substring(a,len);
	}

	public static String indentation(int indent, char padding) {
		StringBuilder b = new StringBuilder(indent);
		for(int i = 0; i < indent; i += 1)
			b.append(padding);
		return b.toString();
	}
	public static String indentation(int indent, String padding) {
		StringBuilder b = new StringBuilder(indent);
		for(int i = 0; i < indent; i += 1)
			b.append(padding);
		return b.toString();
	}

	public static String firstDelim(String src, String begDelim, String endDelim) {
		return firstDelim(src, begDelim, endDelim, 0);
	}

	public static String firstDelim(String src, String begDelim, String endDelim, int fromIndex) {
		int sz = src.length();
		int begDelimSz = begDelim.length();
		int i = fromIndex;
		int candidateBeg;
		int candidateEnd;

		while(i < sz) {
			candidateBeg = src.indexOf(begDelim, i);
			if(candidateBeg == -1)
				return null;
			candidateEnd = src.indexOf(endDelim, candidateBeg);
			if(candidateEnd == -1)
				return null;

			// if both delimitators are found, in that order:
			return src.substring(candidateBeg+begDelimSz, candidateEnd);
		}
		return null;
	}

}
