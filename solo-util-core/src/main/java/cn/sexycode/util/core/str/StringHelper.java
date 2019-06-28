package cn.sexycode.util.core.str;

import cn.sexycode.util.core.collection.ArrayHelper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.sexycode.util.core.str.StringPool.EMPTY;

/**
 * @author qinzaizhen
 */
public final class StringHelper {

    /**
     * Represents a failed index search.
     *
     * @since 2.1
     */
    public static final int INDEX_NOT_FOUND = -1;
    // ---------------------------------------------------------------- array

    String[] EMPTY_ARRAY = new String[0];

    byte[] BYTES_NEW_LINE = StringPool.NEWLINE.getBytes();

    private static Pattern UNDERLINE_TO_CAMELHUMP_PATTERN = Pattern.compile("_[a-z]");

    private static final int ALIAS_TRUNCATE_LENGTH = 10;

    public static final String WHITESPACE = " \n\r\f\t";

    public static final String[] EMPTY_STRINGS = new String[0];

    private StringHelper() { /* static methods only - hide constructor */
    }

    public static int lastIndexOfLetter(String string) {
        for (int i = 0; i < string.length(); i++) {
            char character = string.charAt(i);
            // Include "_".  See HHH-8073
            if (!Character.isLetter(character) && !('_' == character)) {
                return i - 1;
            }
        }
        return string.length() - 1;
    }

    public static String join(String seperator, String[] strings) {
        int length = strings.length;
        if (length == 0) {
            return "";
        }
        // Allocate space for length * firstStringLength;
        // If strings[0] is null, then its length is defined as 4, since that's the
        // length of "null".
        final int firstStringLength = strings[0] != null ? strings[0].length() : 4;
        StringBuilder buf = new StringBuilder(length * firstStringLength).append(strings[0]);
        for (int i = 1; i < length; i++) {
            buf.append(seperator).append(strings[i]);
        }
        return buf.toString();
    }

    public static String joinWithQualifierAndSuffix(String[] values, String qualifier, String suffix,
            String deliminator) {
        int length = values.length;
        if (length == 0) {
            return "";
        }
        StringBuilder buf = new StringBuilder(length * (values[0].length() + suffix.length()))
                .append(qualify(qualifier, values[0])).append(suffix);
        for (int i = 1; i < length; i++) {
            buf.append(deliminator).append(qualify(qualifier, values[i])).append(suffix);
        }
        return buf.toString();
    }

    public static String join(String seperator, Iterator objects) {
        StringBuilder buf = new StringBuilder();
        if (objects.hasNext()) {
            buf.append(objects.next());
        }
        while (objects.hasNext()) {
            buf.append(seperator).append(objects.next());
        }
        return buf.toString();
    }

    public static String join(String separator, Iterable objects) {
        return join(separator, objects.iterator());
    }

    public static String[] add(String[] x, String sep, String[] y) {
        final String[] result = new String[x.length];
        for (int i = 0; i < x.length; i++) {
            result[i] = x[i] + sep + y[i];
        }
        return result;
    }

    public static String repeat(String string, int times) {
        StringBuilder buf = new StringBuilder(string.length() * times);
        for (int i = 0; i < times; i++) {
            buf.append(string);
        }
        return buf.toString();
    }

    public static String repeat(String string, int times, String deliminator) {
        StringBuilder buf = new StringBuilder((string.length() * times) + (deliminator.length() * (times - 1)))
                .append(string);
        for (int i = 1; i < times; i++) {
            buf.append(deliminator).append(string);
        }
        return buf.toString();
    }

    public static String repeat(char character, int times) {
        char[] buffer = new char[times];
        Arrays.fill(buffer, character);
        return new String(buffer);
    }

    public static String replace(String template, String placeholder, String replacement) {
        return replace(template, placeholder, replacement, false);
    }

    public static String[] replace(String[] templates, String placeholder, String replacement) {
        String[] result = new String[templates.length];
        for (int i = 0; i < templates.length; i++) {
            result[i] = replace(templates[i], placeholder, replacement);
        }
        return result;
    }

    public static String replace(String template, String placeholder, String replacement, boolean wholeWords) {
        return replace(template, placeholder, replacement, wholeWords, false);
    }

    public static String replace(String template, String placeholder, String replacement, boolean wholeWords,
            boolean encloseInParensIfNecessary) {
        if (template == null) {
            return null;
        }
        int loc = template.indexOf(placeholder);
        if (loc < 0) {
            return template;
        } else {
            String beforePlaceholder = template.substring(0, loc);
            String afterPlaceholder = template.substring(loc + placeholder.length());
            return replace(beforePlaceholder, afterPlaceholder, placeholder, replacement, wholeWords,
                    encloseInParensIfNecessary);
        }
    }

    public static String replace(String beforePlaceholder, String afterPlaceholder, String placeholder,
            String replacement, boolean wholeWords, boolean encloseInParensIfNecessary) {
        final boolean actuallyReplace = !wholeWords || afterPlaceholder.length() == 0 || !Character
                .isJavaIdentifierPart(afterPlaceholder.charAt(0));
        // We only need to check the left param to determine if the placeholder is already
        // enclosed in parentheses (HHH-10383)
        // Examples:
        // 1) "... IN (?1", we assume that "?1" does not need to be enclosed because there
        // there is already a right-parenthesis; we assume there will be a matching right-parenthesis.
        // 2) "... IN ?1", we assume that "?1" needs to be enclosed in parentheses, because there
        // is no left-parenthesis.

        // We need to check the placeholder is not used in `Order By FIELD(...)` (HHH-10502)
        // Examples:
        // " ... Order By FIELD(id,?1)",  after expand parameters, the sql is "... Order By FIELD(id,?,?,?)"
        boolean encloseInParens =
                actuallyReplace && encloseInParensIfNecessary && !(getLastNonWhitespaceCharacter(beforePlaceholder)
                        == '(') && !(getLastNonWhitespaceCharacter(beforePlaceholder) == ','
                        && getFirstNonWhitespaceCharacter(afterPlaceholder) == ')');
        StringBuilder buf = new StringBuilder(beforePlaceholder);
        if (encloseInParens) {
            buf.append('(');
        }
        buf.append(actuallyReplace ? replacement : placeholder);
        if (encloseInParens) {
            buf.append(')');
        }
        buf.append(replace(afterPlaceholder, placeholder, replacement, wholeWords, encloseInParensIfNecessary));
        return buf.toString();
    }

    public static char getLastNonWhitespaceCharacter(String str) {
        if (str != null && str.length() > 0) {
            for (int i = str.length() - 1; i >= 0; i--) {
                char ch = str.charAt(i);
                if (!Character.isWhitespace(ch)) {
                    return ch;
                }
            }
        }
        return '\0';
    }

    public static char getFirstNonWhitespaceCharacter(String str) {
        if (str != null && str.length() > 0) {
            for (int i = 0; i < str.length(); i++) {
                char ch = str.charAt(i);
                if (!Character.isWhitespace(ch)) {
                    return ch;
                }
            }
        }
        return '\0';
    }

    public static String replaceOnce(String template, String placeholder, String replacement) {
        if (template == null) {
            return null;
        }
        int loc = template.indexOf(placeholder);
        if (loc < 0) {
            return template;
        } else {
            return template.substring(0, loc) + replacement + template.substring(loc + placeholder.length());
        }
    }

    public static String[] split(String separators, String list) {
        return split(separators, list, false);
    }

    public static String[] split(String separators, String list, boolean include) {
        StringTokenizer tokens = new StringTokenizer(list, separators, include);
        String[] result = new String[tokens.countTokens()];
        int i = 0;
        while (tokens.hasMoreTokens()) {
            result[i++] = tokens.nextToken();
        }
        return result;
    }

    public static String[] splitTrimmingTokens(String separators, String list, boolean include) {
        StringTokenizer tokens = new StringTokenizer(list, separators, include);
        String[] result = new String[tokens.countTokens()];
        int i = 0;
        while (tokens.hasMoreTokens()) {
            result[i++] = tokens.nextToken().trim();
        }
        return result;
    }

    public static String unqualify(String qualifiedName) {
        int loc = qualifiedName.lastIndexOf('.');
        return (loc < 0) ? qualifiedName : qualifiedName.substring(loc + 1);
    }

    public static String qualifier(String qualifiedName) {
        int loc = qualifiedName.lastIndexOf('.');
        return (loc < 0) ? "" : qualifiedName.substring(0, loc);
    }

    /**
     * Collapses a name.  Mainly intended for use with classnames, where an example might serve best to explain.
     * Imagine you have a class named <samp>'org.hibernate.internal.util.StringHelper'</samp>; calling collapse on that
     * classname will result in <samp>'o.h.u.StringHelper'<samp>.
     *
     * @param name The name to collapse.
     * @return The collapsed name.
     */
    public static String collapse(String name) {
        if (name == null) {
            return null;
        }
        int breakPoint = name.lastIndexOf('.');
        if (breakPoint < 0) {
            return name;
        }
        return collapseQualifier(name.substring(0, breakPoint), true) + name.substring(breakPoint); // includes last '.'
    }

    /**
     * Given a qualifier, collapse it.
     *
     * @param qualifier   The qualifier to collapse.
     * @param includeDots Should we include the dots in the collapsed form?
     * @return The collapsed form.
     */
    public static String collapseQualifier(String qualifier, boolean includeDots) {
        StringTokenizer tokenizer = new StringTokenizer(qualifier, ".");
        String collapsed = Character.toString(tokenizer.nextToken().charAt(0));
        while (tokenizer.hasMoreTokens()) {
            if (includeDots) {
                collapsed += '.';
            }
            collapsed += tokenizer.nextToken().charAt(0);
        }
        return collapsed;
    }

    /**
     * Partially unqualifies a qualified name.  For example, with a base of 'org.hibernate' the name
     * 'org.hibernate.internal.util.StringHelper' would become 'util.StringHelper'.
     *
     * @param name          The (potentially) qualified name.
     * @param qualifierBase The qualifier base.
     * @return The name itself, or the partially unqualified form if it begins with the qualifier base.
     */
    public static String partiallyUnqualify(String name, String qualifierBase) {
        if (name == null || !name.startsWith(qualifierBase)) {
            return name;
        }
        return name.substring(qualifierBase.length() + 1); // +1 to start afterQuery the following '.'
    }

    /**
     * Cross between {@link #collapse} and {@link #partiallyUnqualify}.  Functions much like {@link #collapse}
     * except that only the qualifierBase is collapsed.  For example, with a base of 'org.hibernate' the name
     * 'org.hibernate.internal.util.StringHelper' would become 'o.h.util.StringHelper'.
     *
     * @param name          The (potentially) qualified name.
     * @param qualifierBase The qualifier base.
     * @return The name itself if it does not begin with the qualifierBase, or the properly collapsed form otherwise.
     */
    public static String collapseQualifierBase(String name, String qualifierBase) {
        if (name == null || !name.startsWith(qualifierBase)) {
            return collapse(name);
        }
        return collapseQualifier(qualifierBase, true) + name.substring(qualifierBase.length());
    }

    public static String[] suffix(String[] columns, String suffix) {
        if (suffix == null) {
            return columns;
        }
        String[] qualified = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            qualified[i] = suffix(columns[i], suffix);
        }
        return qualified;
    }

    private static String suffix(String name, String suffix) {
        return (suffix == null) ? name : name + suffix;
    }

    public static String root(String qualifiedName) {
        int loc = qualifiedName.indexOf(".");
        return (loc < 0) ? qualifiedName : qualifiedName.substring(0, loc);
    }

    public static String unroot(String qualifiedName) {
        int loc = qualifiedName.indexOf(".");
        return (loc < 0) ? qualifiedName : qualifiedName.substring(loc + 1, qualifiedName.length());
    }

    public static boolean booleanValue(String tfString) {
        String trimmed = tfString.trim().toLowerCase(Locale.ROOT);
        return trimmed.equals("true") || trimmed.equals("t");
    }

    public static String toString(Object[] array) {
        int len = array.length;
        if (len == 0) {
            return "";
        }
        StringBuilder buf = new StringBuilder(len * 12);
        for (int i = 0; i < len - 1; i++) {
            buf.append(array[i]).append(", ");
        }
        return buf.append(array[len - 1]).toString();
    }

    public static String[] multiply(String string, Iterator placeholders, Iterator replacements) {
        String[] result = new String[] { string };
        while (placeholders.hasNext()) {
            result = multiply(result, (String) placeholders.next(), (String[]) replacements.next());
        }
        return result;
    }

    private static String[] multiply(String[] strings, String placeholder, String[] replacements) {
        String[] results = new String[replacements.length * strings.length];
        int n = 0;
        for (String replacement : replacements) {
            for (String string : strings) {
                results[n++] = replaceOnce(string, placeholder, replacement);
            }
        }
        return results;
    }

    public static int countUnquoted(String string, char character) {
        if ('\'' == character) {
            throw new IllegalArgumentException("Unquoted count of quotes is invalid");
        }
        if (string == null) {
            return 0;
        }
        // Impl note: takes advantage of the fact that an escpaed single quote
        // embedded within a quote-block can really be handled as two seperate
        // quote-blocks for the purposes of this method...
        int count = 0;
        int stringLength = string.length();
        boolean inQuote = false;
        for (int indx = 0; indx < stringLength; indx++) {
            char c = string.charAt(indx);
            if (inQuote) {
                if ('\'' == c) {
                    inQuote = false;
                }
            } else if ('\'' == c) {
                inQuote = true;
            } else if (c == character) {
                count++;
            }
        }
        return count;
    }

    public static int[] locateUnquoted(String string, char character) {
        if ('\'' == character) {
            throw new IllegalArgumentException("Unquoted count of quotes is invalid");
        }
        if (string == null) {
            return new int[0];
        }

        ArrayList locations = new ArrayList(20);

        // Impl note: takes advantage of the fact that an escpaed single quote
        // embedded within a quote-block can really be handled as two seperate
        // quote-blocks for the purposes of this method...
        int stringLength = string.length();
        boolean inQuote = false;
        for (int indx = 0; indx < stringLength; indx++) {
            char c = string.charAt(indx);
            if (inQuote) {
                if ('\'' == c) {
                    inQuote = false;
                }
            } else if ('\'' == c) {
                inQuote = true;
            } else if (c == character) {
                locations.add(indx);
            }
        }
        return ArrayHelper.toIntArray(locations);
    }

    public static boolean isNotEmpty(CharSequence string) {
        return string != null && string.length() > 0;
    }

    public static boolean isEmpty(CharSequence string) {
        return string == null || string.length() == 0;
    }

    public static boolean isEmptyOrWhiteSpace(String string) {
        return isEmpty(string) || isEmpty(string.trim());
    }

    public static String qualify(String prefix, String name) {
        if (name == null || prefix == null) {
            throw new NullPointerException("prefix or name were null attempting to build qualified name");
        }
        return prefix + '.' + name;
    }

    public static String qualifyConditionally(String prefix, String name) {
        if (name == null) {
            throw new NullPointerException("name was null attempting to build qualified name");
        }
        return isEmpty(prefix) ? name : prefix + '.' + name;
    }

    public static String[] qualify(String prefix, String[] names) {
        if (prefix == null) {
            return names;
        }
        int len = names.length;
        String[] qualified = new String[len];
        for (int i = 0; i < len; i++) {
            qualified[i] = qualify(prefix, names[i]);
        }
        return qualified;
    }

    public static String[] qualifyIfNot(String prefix, String[] names) {
        if (prefix == null) {
            return names;
        }
        int len = names.length;
        String[] qualified = new String[len];
        for (int i = 0; i < len; i++) {
            if (names[i].indexOf('.') < 0) {
                qualified[i] = qualify(prefix, names[i]);
            } else {
                qualified[i] = names[i];
            }
        }
        return qualified;
    }

    public static int firstIndexOfChar(String sqlString, BitSet keys, int startindex) {
        for (int i = startindex, size = sqlString.length(); i < size; i++) {
            if (keys.get(sqlString.charAt(i))) {
                return i;
            }
        }
        return -1;

    }

    public static int firstIndexOfChar(String sqlString, String string, int startindex) {
        BitSet keys = new BitSet();
        for (int i = 0, size = string.length(); i < size; i++) {
            keys.set(string.charAt(i));
        }
        return firstIndexOfChar(sqlString, keys, startindex);

    }

    public static String truncate(String string, int length) {
        if (string.length() <= length) {
            return string;
        } else {
            return string.substring(0, length);
        }
    }

    public static String generateAlias(String description) {
        return generateAliasRoot(description) + '_';
    }

    /**
     * Generate a nice alias for the given class name or collection role name and unique integer. Subclasses of
     * Loader do <em>not</em> have to use aliases of this form.
     *
     * @param description The base name (usually an entity-name or collection-role)
     * @param unique      A uniquing value
     * @return an alias of the form <samp>foo1_</samp>
     */
    public static String generateAlias(String description, int unique) {
        return generateAliasRoot(description) + Integer.toString(unique) + '_';
    }

    /**
     * Generates a root alias by truncating the "root name" defined by
     * the incoming decription and removing/modifying any non-valid
     * alias characters.
     *
     * @param description The root name from which to generate a root alias.
     * @return The generated root alias.
     */
    private static String generateAliasRoot(String description) {
        String result = truncate(unqualifyEntityName(description), ALIAS_TRUNCATE_LENGTH).toLowerCase(Locale.ROOT)
                .replace('/', '_') // entityNames may now include slashes for the representations
                .replace('$', '_'); //classname may be an inner class
        result = cleanAlias(result);
        if (Character.isDigit(result.charAt(result.length() - 1))) {
            return result + "x"; //ick!
        } else {
            return result;
        }
    }

    /**
     * Clean the generated alias by removing any non-alpha characters from the
     * beginning.
     *
     * @param alias The generated alias to be cleaned.
     * @return The cleaned alias, stripped of any leading non-alpha characters.
     */
    private static String cleanAlias(String alias) {
        char[] chars = alias.toCharArray();
        // short cut check...
        if (!Character.isLetter(chars[0])) {
            for (int i = 1; i < chars.length; i++) {
                // as soon as we encounter our first letter, return the substring
                // from that position
                if (Character.isLetter(chars[i])) {
                    return alias.substring(i);
                }
            }
        }
        return alias;
    }

    public static String unqualifyEntityName(String entityName) {
        String result = unqualify(entityName);
        int slashPos = result.indexOf('/');
        if (slashPos > 0) {
            result = result.substring(0, slashPos - 1);
        }
        return result;
    }

    public static String moveAndToBeginning(String filter) {
        if (filter.trim().length() > 0) {
            filter += " and ";
            if (filter.startsWith(" and ")) {
                filter = filter.substring(4);
            }
        }
        return filter;
    }

    /**
     * Determine if the given string is quoted (wrapped by '`' characters at beginning and end).
     *
     * @param name The name to check.
     * @return True if the given string starts and ends with '`'; false otherwise.
     */
    public static boolean isQuoted(String name) {
        return name != null && name.length() != 0 && ((name.charAt(0) == '`' && name.charAt(name.length() - 1) == '`')
                || (name.charAt(0) == '"' && name.charAt(name.length() - 1) == '"'));
    }

    /**
     * Return a representation of the given name ensuring quoting (wrapped with '`' characters).  If already wrapped
     * return name.
     *
     * @param name The name to quote.
     * @return The quoted version.
     */
    public static String quote(String name) {
        if (isEmpty(name) || isQuoted(name)) {
            return name;
        }
        // Convert the JPA2 specific quoting character (double quote) to Hibernate's (back tick)
        else if (name.startsWith("\"") && name.endsWith("\"")) {
            name = name.substring(1, name.length() - 1);
        }

        return "`" + name + '`';
    }

    /**
     * Return the unquoted version of name (stripping the start and end '`' characters if present).
     *
     * @param name The name to be unquoted.
     * @return The unquoted version.
     */
    public static String unquote(String name) {
        return isQuoted(name) ? name.substring(1, name.length() - 1) : name;
    }

    public static final String BATCH_ID_PLACEHOLDER = "$$BATCH_ID_PLACEHOLDER$$";

    /**
     * Takes a String s and returns a new String[1] with s as the only element.
     * If s is null or "", return String[0].
     *
     * @param s
     * @return String[]
     */
    public static String[] toArrayElement(String s) {
        return (s == null || s.length() == 0) ? new String[0] : new String[] { s };
    }

    public static String nullIfEmpty(String value) {
        return isEmpty(value) ? null : value;
    }

    public static List<String> parseCommaSeparatedString(String incomingString) {
        return Arrays.asList(incomingString.split("\\s*,\\s*"));
    }

    public static <T> String join(Collection<T> values, Renderer<T> renderer) {
        final StringBuilder buffer = new StringBuilder();
        boolean firstPass = true;
        for (T value : values) {
            if (firstPass) {
                firstPass = false;
            } else {
                buffer.append(", ");
            }

            buffer.append(renderer.render(value));
        }

        return buffer.toString();
    }

    public static <T> String join(T[] values, Renderer<T> renderer) {
        return join(Arrays.asList(values), renderer);
    }

    public interface Renderer<T> {
        String render(T value);
    }

    /**
     * 根据指定的样式进行转换
     *
     * @param str
     * @param style
     * @return
     */
    public static String convertByStyle(String str, Style style) {
        switch (style) {
            case camelhump:
                return camelhumpToUnderline(str);
            case uppercase:
                return str.toUpperCase();
            case lowercase:
                return str.toLowerCase();
            case camelhumpAndLowercase:
                return camelhumpToUnderline(str).toLowerCase();
            case camelhumpAndUppercase:
                return camelhumpToUnderline(str).toUpperCase();
            case normal:
            default:
                return str;
        }
    }

    /**
     * 将驼峰风格替换为下划线风格
     */
    public static String camelhumpToUnderline(String str) {
        final int size;
        final char[] chars;
        final StringBuilder sb = new StringBuilder((size = (chars = str.toCharArray()).length) * 3 / 2 + 1);
        char c;
        for (int i = 0; i < size; i++) {
            c = chars[i];
            if (isUppercaseAlpha(c)) {
                sb.append('_').append(toLowerAscii(c));
            } else {
                sb.append(c);
            }
        }
        return sb.charAt(0) == '_' ? sb.substring(1) : sb.toString();
    }

    /**
     * 将下划线风格替换为驼峰风格
     */
    public static String underlineToCamelhump(String str) {
        Matcher matcher = UNDERLINE_TO_CAMELHUMP_PATTERN.matcher(str);
        StringBuilder builder = new StringBuilder(str);
        for (int i = 0; matcher.find(); i++) {
            builder.replace(matcher.start() - i, matcher.end() - i, matcher.group().substring(1).toUpperCase());
        }
        if (Character.isUpperCase(builder.charAt(0))) {
            builder.replace(0, 1, String.valueOf(Character.toLowerCase(builder.charAt(0))));
        }
        return builder.toString();
    }

    public static boolean isUppercaseAlpha(char c) {
        return (c >= 'A') && (c <= 'Z');
    }

    public static boolean isLowercaseAlpha(char c) {
        return (c >= 'a') && (c <= 'z');
    }

    public static char toUpperAscii(char c) {
        if (isLowercaseAlpha(c)) {
            c -= (char) 0x20;
        }
        return c;
    }

    public static char toLowerAscii(char c) {
        if (isUppercaseAlpha(c)) {
            c += (char) 0x20;
        }
        return c;
    }

    /**
     * <p>Finds the first index within a CharSequence, handling {@code null}.
     * This method uses {@link String#indexOf(String, int)} if possible.</p>
     *
     * <p>A {@code null} CharSequence will return {@code -1}.
     * A negative start position is treated as zero.
     * An empty ("") search CharSequence always matches.
     * A start position greater than the string length only matches
     * an empty search CharSequence.</p>
     *
     * <pre>
     * StringUtils.indexOf(null, *, *)          = -1
     * StringUtils.indexOf(*, null, *)          = -1
     * StringUtils.indexOf("", "", 0)           = 0
     * StringUtils.indexOf("", *, 0)            = -1 (except when * = "")
     * StringUtils.indexOf("aabaabaa", "a", 0)  = 0
     * StringUtils.indexOf("aabaabaa", "b", 0)  = 2
     * StringUtils.indexOf("aabaabaa", "ab", 0) = 1
     * StringUtils.indexOf("aabaabaa", "b", 3)  = 5
     * StringUtils.indexOf("aabaabaa", "b", 9)  = -1
     * StringUtils.indexOf("aabaabaa", "b", -1) = 2
     * StringUtils.indexOf("aabaabaa", "", 2)   = 2
     * StringUtils.indexOf("abc", "", 9)        = 3
     * </pre>
     *
     * @param seq       the CharSequence to check, may be null
     * @param searchSeq the CharSequence to find, may be null
     * @param startPos  the start position, negative treated as zero
     * @return the first index of the search CharSequence (always &ge; startPos),
     * -1 if no match or {@code null} string input
     * @since 2.0
     * @since 3.0 Changed signature from indexOf(String, String, int) to indexOf(CharSequence, CharSequence, int)
     */
    public static int indexOf(final CharSequence seq, final CharSequence searchSeq, final int startPos) {
        if (seq == null || searchSeq == null) {
            return INDEX_NOT_FOUND;
        }
        return CharSequenceUtils.indexOf(seq, searchSeq, startPos);
    }

    /**
     * <p>Finds the first index within a CharSequence, handling {@code null}.
     * This method uses {@link String#indexOf(String, int)} if possible.</p>
     *
     * <p>A {@code null} CharSequence will return {@code -1}.</p>
     *
     * <pre>
     * StringUtils.indexOf(null, *)          = -1
     * StringUtils.indexOf(*, null)          = -1
     * StringUtils.indexOf("", "")           = 0
     * StringUtils.indexOf("", *)            = -1 (except when * = "")
     * StringUtils.indexOf("aabaabaa", "a")  = 0
     * StringUtils.indexOf("aabaabaa", "b")  = 2
     * StringUtils.indexOf("aabaabaa", "ab") = 1
     * StringUtils.indexOf("aabaabaa", "")   = 0
     * </pre>
     *
     * @param seq       the CharSequence to check, may be null
     * @param searchSeq the CharSequence to find, may be null
     * @return the first index of the search CharSequence,
     * -1 if no match or {@code null} string input
     * @since 2.0
     * @since 3.0 Changed signature from indexOf(String, String) to indexOf(CharSequence, CharSequence)
     */
    public static int indexOf(final CharSequence seq, final CharSequence searchSeq) {
        if (seq == null || searchSeq == null) {
            return INDEX_NOT_FOUND;
        }
        return CharSequenceUtils.indexOf(seq, searchSeq, 0);
    }

    /**
     * Returns the index within <code>seq</code> of the last occurrence of
     * the specified character. For values of <code>searchChar</code> in the
     * range from 0 to 0xFFFF (inclusive), the index (in Unicode code
     * units) returned is the largest value <i>k</i> such that:
     * <blockquote><pre>
     * this.charAt(<i>k</i>) == searchChar
     * </pre></blockquote>
     * is true. For other values of <code>searchChar</code>, it is the
     * largest value <i>k</i> such that:
     * <blockquote><pre>
     * this.codePointAt(<i>k</i>) == searchChar
     * </pre></blockquote>
     * is true.  In either case, if no such character occurs in this
     * string, then <code>-1</code> is returned. Furthermore, a {@code null} or empty ("")
     * <code>CharSequence</code> will return {@code -1}. The
     * <code>seq</code> <code>CharSequence</code> object is searched backwards
     * starting at the last character.
     *
     * <pre>
     * StringUtils.lastIndexOf(null, *)         = -1
     * StringUtils.lastIndexOf("", *)           = -1
     * StringUtils.lastIndexOf("aabaabaa", 'a') = 7
     * StringUtils.lastIndexOf("aabaabaa", 'b') = 5
     * </pre>
     *
     * @param seq        the <code>CharSequence</code> to check, may be null
     * @param searchChar the character to find
     * @return the last index of the search character,
     * -1 if no match or {@code null} string input
     * @since 2.0
     * @since 3.0 Changed signature from lastIndexOf(String, int) to lastIndexOf(CharSequence, int)
     * @since 3.6 Updated {@link CharSequenceUtils} call to behave more like <code>String</code>
     */
    public static int lastIndexOf(final CharSequence seq, final int searchChar) {
        if (isEmpty(seq)) {
            return INDEX_NOT_FOUND;
        }
        return CharSequenceUtils.lastIndexOf(seq, searchChar, seq.length());
    }

    /**
     * Returns the index within <code>seq</code> of the last occurrence of
     * the specified character, searching backward starting at the
     * specified index. For values of <code>searchChar</code> in the range
     * from 0 to 0xFFFF (inclusive), the index returned is the largest
     * value <i>k</i> such that:
     * <blockquote><pre>
     * (this.charAt(<i>k</i>) == searchChar) &amp;&amp; (<i>k</i> &lt;= startPos)
     * </pre></blockquote>
     * is true. For other values of <code>searchChar</code>, it is the
     * largest value <i>k</i> such that:
     * <blockquote><pre>
     * (this.codePointAt(<i>k</i>) == searchChar) &amp;&amp; (<i>k</i> &lt;= startPos)
     * </pre></blockquote>
     * is true. In either case, if no such character occurs in <code>seq</code>
     * at or before position <code>startPos</code>, then
     * <code>-1</code> is returned. Furthermore, a {@code null} or empty ("")
     * <code>CharSequence</code> will return {@code -1}. A start position greater
     * than the string length searches the whole string.
     * The search starts at the <code>startPos</code> and works backwards;
     * matches starting after the start position are ignored.
     *
     * <p>All indices are specified in <code>char</code> values
     * (Unicode code units).
     *
     * <pre>
     * StringUtils.lastIndexOf(null, *, *)          = -1
     * StringUtils.lastIndexOf("", *,  *)           = -1
     * StringUtils.lastIndexOf("aabaabaa", 'b', 8)  = 5
     * StringUtils.lastIndexOf("aabaabaa", 'b', 4)  = 2
     * StringUtils.lastIndexOf("aabaabaa", 'b', 0)  = -1
     * StringUtils.lastIndexOf("aabaabaa", 'b', 9)  = 5
     * StringUtils.lastIndexOf("aabaabaa", 'b', -1) = -1
     * StringUtils.lastIndexOf("aabaabaa", 'a', 0)  = 0
     * </pre>
     *
     * @param seq        the CharSequence to check, may be null
     * @param searchChar the character to find
     * @param startPos   the start position
     * @return the last index of the search character (always &le; startPos),
     * -1 if no match or {@code null} string input
     * @since 2.0
     * @since 3.0 Changed signature from lastIndexOf(String, int, int) to lastIndexOf(CharSequence, int, int)
     */
    public static int lastIndexOf(final CharSequence seq, final int searchChar, final int startPos) {
        if (isEmpty(seq)) {
            return INDEX_NOT_FOUND;
        }
        return CharSequenceUtils.lastIndexOf(seq, searchChar, startPos);
    }

    /**
     * <p>Finds the last index within a CharSequence, handling {@code null}.
     * This method uses {@link String#lastIndexOf(String)} if possible.</p>
     *
     * <p>A {@code null} CharSequence will return {@code -1}.</p>
     *
     * <pre>
     * StringUtils.lastIndexOf(null, *)          = -1
     * StringUtils.lastIndexOf(*, null)          = -1
     * StringUtils.lastIndexOf("", "")           = 0
     * StringUtils.lastIndexOf("aabaabaa", "a")  = 7
     * StringUtils.lastIndexOf("aabaabaa", "b")  = 5
     * StringUtils.lastIndexOf("aabaabaa", "ab") = 4
     * StringUtils.lastIndexOf("aabaabaa", "")   = 8
     * </pre>
     *
     * @param seq       the CharSequence to check, may be null
     * @param searchSeq the CharSequence to find, may be null
     * @return the last index of the search String,
     * -1 if no match or {@code null} string input
     * @since 2.0
     * @since 3.0 Changed signature from lastIndexOf(String, String) to lastIndexOf(CharSequence, CharSequence)
     */
    public static int lastIndexOf(final CharSequence seq, final CharSequence searchSeq) {
        if (seq == null || searchSeq == null) {
            return INDEX_NOT_FOUND;
        }
        return CharSequenceUtils.lastIndexOf(seq, searchSeq, seq.length());
    }

    /**
     * <p>Gets a substring from the specified String avoiding exceptions.</p>
     *
     * <p>A negative start position can be used to start {@code n}
     * characters from the end of the String.</p>
     *
     * <p>A {@code null} String will return {@code null}.
     * An empty ("") String will return "".</p>
     *
     * <pre>
     * StringUtils.substring(null, *)   = null
     * StringUtils.substring("", *)     = ""
     * StringUtils.substring("abc", 0)  = "abc"
     * StringUtils.substring("abc", 2)  = "c"
     * StringUtils.substring("abc", 4)  = ""
     * StringUtils.substring("abc", -2) = "bc"
     * StringUtils.substring("abc", -4) = "abc"
     * </pre>
     *
     * @param str   the String to get the substring from, may be null
     * @param start the position to start from, negative means
     *              count back from the end of the String by this many characters
     * @return substring from start position, {@code null} if null String input
     */
    public static String substring(final String str, int start) {
        if (str == null) {
            return null;
        }

        // handle negatives, which means last n characters
        if (start < 0) {
            start = str.length() + start; // remember start is negative
        }

        if (start < 0) {
            start = 0;
        }
        if (start > str.length()) {
            return EMPTY;
        }

        return str.substring(start);
    }

    /**
     * <p>Gets a substring from the specified String avoiding exceptions.</p>
     *
     * <p>A negative start position can be used to start/end {@code n}
     * characters from the end of the String.</p>
     *
     * <p>The returned substring starts with the character in the {@code start}
     * position and ends before the {@code end} position. All position counting is
     * zero-based -- i.e., to start at the beginning of the string use
     * {@code start = 0}. Negative start and end positions can be used to
     * specify offsets relative to the end of the String.</p>
     *
     * <p>If {@code start} is not strictly to the left of {@code end}, ""
     * is returned.</p>
     *
     * <pre>
     * StringUtils.substring(null, *, *)    = null
     * StringUtils.substring("", * ,  *)    = "";
     * StringUtils.substring("abc", 0, 2)   = "ab"
     * StringUtils.substring("abc", 2, 0)   = ""
     * StringUtils.substring("abc", 2, 4)   = "c"
     * StringUtils.substring("abc", 4, 6)   = ""
     * StringUtils.substring("abc", 2, 2)   = ""
     * StringUtils.substring("abc", -2, -1) = "b"
     * StringUtils.substring("abc", -4, 2)  = "ab"
     * </pre>
     *
     * @param str   the String to get the substring from, may be null
     * @param start the position to start from, negative means
     *              count back from the end of the String by this many characters
     * @param end   the position to end at (exclusive), negative means
     *              count back from the end of the String by this many characters
     * @return substring from start position to end position,
     * {@code null} if null String input
     */
    public static String substring(final String str, int start, int end) {
        if (str == null) {
            return null;
        }

        // handle negatives
        if (end < 0) {
            end = str.length() + end; // remember end is negative
        }
        if (start < 0) {
            start = str.length() + start; // remember start is negative
        }

        // check length next
        if (end > str.length()) {
            end = str.length();
        }

        // if start is greater than end, return ""
        if (start > end) {
            return EMPTY;
        }

        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }

        return str.substring(start, end);
    }
}
