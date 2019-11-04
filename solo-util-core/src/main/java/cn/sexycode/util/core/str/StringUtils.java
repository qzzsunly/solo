package cn.sexycode.util.core.str;

import cn.sexycode.util.core.collection.ArrayHelper;
import cn.sexycode.util.core.collection.CollectionUtils;
import cn.sexycode.util.core.lang.Assert;
import cn.sexycode.util.core.object.ObjectUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.sexycode.util.core.str.StringPool.EMPTY;

/**
 * @author qinzaizhen
 */
public final class StringUtils {

    private static final String FOLDER_SEPARATOR = "/";

    private static final String WINDOWS_FOLDER_SEPARATOR = "\\";

    private static final String TOP_PATH = "..";

    private static final String CURRENT_PATH = ".";

    private static final char EXTENSION_SEPARATOR = '.';

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

    private StringUtils() { /* static methods only - hide constructor */
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

    /**
     * Check whether the given {@code String} is empty.
     * <p>This method accepts any Object as an argument, comparing it to
     * {@code null} and the empty String. As a consequence, this method
     * will never return {@code true} for a non-null non-String object.
     * <p>The Object signature is useful for general attribute handling code
     * that commonly deals with Strings but generally has to iterate over
     * Objects since attributes may e.g. be primitive value objects as well.
     *
     * @param str the candidate String
     * @since 3.2.1
     */
    public static boolean isEmpty(Object str) {
        return (str == null || "".equals(str));
    }

    /**
     * Check that the given {@code CharSequence} is neither {@code null} nor
     * of length 0.
     * <p>Note: this method returns {@code true} for a {@code CharSequence}
     * that purely consists of whitespace.
     * <p><pre class="code">
     * StringUtils.hasLength(null) = false
     * StringUtils.hasLength("") = false
     * StringUtils.hasLength(" ") = true
     * StringUtils.hasLength("Hello") = true
     * </pre>
     *
     * @param str the {@code CharSequence} to check (may be {@code null})
     * @return {@code true} if the {@code CharSequence} is not {@code null} and has length
     * @see #hasText(String)
     */
    public static boolean hasLength(CharSequence str) {
        return (str != null && str.length() > 0);
    }

    /**
     * Check that the given {@code String} is neither {@code null} nor of length 0.
     * <p>Note: this method returns {@code true} for a {@code String} that
     * purely consists of whitespace.
     *
     * @param str the {@code String} to check (may be {@code null})
     * @return {@code true} if the {@code String} is not {@code null} and has length
     * @see #hasLength(CharSequence)
     * @see #hasText(String)
     */
    public static boolean hasLength(String str) {
        return (str != null && !str.isEmpty());
    }

    /**
     * Check whether the given {@code CharSequence} contains actual <em>text</em>.
     * <p>More specifically, this method returns {@code true} if the
     * {@code CharSequence} is not {@code null}, its length is greater than
     * 0, and it contains at least one non-whitespace character.
     * <p><pre class="code">
     * StringUtils.hasText(null) = false
     * StringUtils.hasText("") = false
     * StringUtils.hasText(" ") = false
     * StringUtils.hasText("12345") = true
     * StringUtils.hasText(" 12345 ") = true
     * </pre>
     *
     * @param str the {@code CharSequence} to check (may be {@code null})
     * @return {@code true} if the {@code CharSequence} is not {@code null},
     * its length is greater than 0, and it does not contain whitespace only
     * @see Character#isWhitespace
     */
    public static boolean hasText(CharSequence str) {
        return (str != null && str.length() > 0 && containsText(str));
    }

    /**
     * Check whether the given {@code String} contains actual <em>text</em>.
     * <p>More specifically, this method returns {@code true} if the
     * {@code String} is not {@code null}, its length is greater than 0,
     * and it contains at least one non-whitespace character.
     *
     * @param str the {@code String} to check (may be {@code null})
     * @return {@code true} if the {@code String} is not {@code null}, its
     * length is greater than 0, and it does not contain whitespace only
     * @see #hasText(CharSequence)
     */
    public static boolean hasText(String str) {
        return (str != null && !str.isEmpty() && containsText(str));
    }

    private static boolean containsText(CharSequence str) {
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the given {@code CharSequence} contains any whitespace characters.
     *
     * @param str the {@code CharSequence} to check (may be {@code null})
     * @return {@code true} if the {@code CharSequence} is not empty and
     * contains at least 1 whitespace character
     * @see Character#isWhitespace
     */
    public static boolean containsWhitespace(CharSequence str) {
        if (!hasLength(str)) {
            return false;
        }

        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the given {@code String} contains any whitespace characters.
     *
     * @param str the {@code String} to check (may be {@code null})
     * @return {@code true} if the {@code String} is not empty and
     * contains at least 1 whitespace character
     * @see #containsWhitespace(CharSequence)
     */
    public static boolean containsWhitespace(String str) {
        return containsWhitespace((CharSequence) str);
    }

    /**
     * Trim leading and trailing whitespace from the given {@code String}.
     *
     * @param str the {@code String} to check
     * @return the trimmed {@code String}
     * @see java.lang.Character#isWhitespace
     */
    public static String trimWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }

        int beginIndex = 0;
        int endIndex = str.length() - 1;

        while (beginIndex <= endIndex && Character.isWhitespace(str.charAt(beginIndex))) {
            beginIndex++;
        }

        while (endIndex > beginIndex && Character.isWhitespace(str.charAt(endIndex))) {
            endIndex--;
        }

        return str.substring(beginIndex, endIndex + 1);
    }

    /**
     * Trim <i>all</i> whitespace from the given {@code String}:
     * leading, trailing, and in between characters.
     *
     * @param str the {@code String} to check
     * @return the trimmed {@code String}
     * @see java.lang.Character#isWhitespace
     */
    public static String trimAllWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }

        int len = str.length();
        StringBuilder sb = new StringBuilder(str.length());
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (!Character.isWhitespace(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Trim leading whitespace from the given {@code String}.
     *
     * @param str the {@code String} to check
     * @return the trimmed {@code String}
     * @see java.lang.Character#isWhitespace
     */
    public static String trimLeadingWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }

        StringBuilder sb = new StringBuilder(str);
        while (sb.length() > 0 && Character.isWhitespace(sb.charAt(0))) {
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }

    /**
     * Trim trailing whitespace from the given {@code String}.
     *
     * @param str the {@code String} to check
     * @return the trimmed {@code String}
     * @see java.lang.Character#isWhitespace
     */
    public static String trimTrailingWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }

        StringBuilder sb = new StringBuilder(str);
        while (sb.length() > 0 && Character.isWhitespace(sb.charAt(sb.length() - 1))) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * Trim all occurrences of the supplied leading character from the given {@code String}.
     *
     * @param str              the {@code String} to check
     * @param leadingCharacter the leading character to be trimmed
     * @return the trimmed {@code String}
     */
    public static String trimLeadingCharacter(String str, char leadingCharacter) {
        if (!hasLength(str)) {
            return str;
        }

        StringBuilder sb = new StringBuilder(str);
        while (sb.length() > 0 && sb.charAt(0) == leadingCharacter) {
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }

    /**
     * Trim all occurrences of the supplied trailing character from the given {@code String}.
     *
     * @param str               the {@code String} to check
     * @param trailingCharacter the trailing character to be trimmed
     * @return the trimmed {@code String}
     */
    public static String trimTrailingCharacter(String str, char trailingCharacter) {
        if (!hasLength(str)) {
            return str;
        }

        StringBuilder sb = new StringBuilder(str);
        while (sb.length() > 0 && sb.charAt(sb.length() - 1) == trailingCharacter) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * Test if the given {@code String} starts with the specified prefix,
     * ignoring upper/lower case.
     *
     * @param str    the {@code String} to check
     * @param prefix the prefix to look for
     * @see java.lang.String#startsWith
     */
    public static boolean startsWithIgnoreCase(String str, String prefix) {
        return (str != null && prefix != null && str.length() >= prefix.length() && str
                .regionMatches(true, 0, prefix, 0, prefix.length()));
    }

    /**
     * Test if the given {@code String} ends with the specified suffix,
     * ignoring upper/lower case.
     *
     * @param str    the {@code String} to check
     * @param suffix the suffix to look for
     * @see java.lang.String#endsWith
     */
    public static boolean endsWithIgnoreCase(String str, String suffix) {
        return (str != null && suffix != null && str.length() >= suffix.length() && str
                .regionMatches(true, str.length() - suffix.length(), suffix, 0, suffix.length()));
    }

    /**
     * Test whether the given string matches the given substring
     * at the given index.
     *
     * @param str       the original string (or StringBuilder)
     * @param index     the index in the original string to start matching against
     * @param substring the substring to match at the given index
     */
    public static boolean substringMatch(CharSequence str, int index, CharSequence substring) {
        if (index + substring.length() > str.length()) {
            return false;
        }
        for (int i = 0; i < substring.length(); i++) {
            if (str.charAt(index + i) != substring.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Count the occurrences of the substring {@code sub} in string {@code str}.
     *
     * @param str string to search in
     * @param sub string to search for
     */
    public static int countOccurrencesOf(String str, String sub) {
        if (!hasLength(str) || !hasLength(sub)) {
            return 0;
        }

        int count = 0;
        int pos = 0;
        int idx;
        while ((idx = str.indexOf(sub, pos)) != -1) {
            ++count;
            pos = idx + sub.length();
        }
        return count;
    }

    /**
     * Delete all occurrences of the given substring.
     *
     * @param inString the original {@code String}
     * @param pattern  the pattern to delete all occurrences of
     * @return the resulting {@code String}
     */
    public static String delete(String inString, String pattern) {
        return replace(inString, pattern, "");
    }

    /**
     * Delete any character in a given {@code String}.
     *
     * @param inString      the original {@code String}
     * @param charsToDelete a set of characters to delete.
     *                      E.g. "az\n" will delete 'a's, 'z's and new lines.
     * @return the resulting {@code String}
     */
    public static String deleteAny(String inString, String charsToDelete) {
        if (!hasLength(inString) || !hasLength(charsToDelete)) {
            return inString;
        }

        StringBuilder sb = new StringBuilder(inString.length());
        for (int i = 0; i < inString.length(); i++) {
            char c = inString.charAt(i);
            if (charsToDelete.indexOf(c) == -1) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    //---------------------------------------------------------------------
    // Convenience methods for working with formatted Strings
    //---------------------------------------------------------------------

    /**
     * Turn the given Object into a {@code String} with single quotes
     * if it is a {@code String}; keeping the Object as-is else.
     *
     * @param obj the input Object (e.g. "myString")
     * @return the quoted {@code String} (e.g. "'myString'"),
     * or the input object as-is if not a {@code String}
     */

    public static Object quoteIfString(Object obj) {
        return (obj instanceof String ? quote((String) obj) : obj);
    }

    /**
     * Unqualify a string qualified by a separator character. For example,
     * "this:name:is:qualified" returns "qualified" if using a ':' separator.
     *
     * @param qualifiedName the qualified name
     * @param separator     the separator
     */
    public static String unqualify(String qualifiedName, char separator) {
        return qualifiedName.substring(qualifiedName.lastIndexOf(separator) + 1);
    }

    /**
     * Capitalize a {@code String}, changing the first letter to
     * upper case as per {@link Character#toUpperCase(char)}.
     * No other letters are changed.
     *
     * @param str the {@code String} to capitalize
     * @return the capitalized {@code String}
     */
    public static String capitalize(String str) {
        return changeFirstCharacterCase(str, true);
    }

    /**
     * Uncapitalize a {@code String}, changing the first letter to
     * lower case as per {@link Character#toLowerCase(char)}.
     * No other letters are changed.
     *
     * @param str the {@code String} to uncapitalize
     * @return the uncapitalized {@code String}
     */
    public static String uncapitalize(String str) {
        return changeFirstCharacterCase(str, false);
    }

    private static String changeFirstCharacterCase(String str, boolean capitalize) {
        if (!hasLength(str)) {
            return str;
        }

        char baseChar = str.charAt(0);
        char updatedChar;
        if (capitalize) {
            updatedChar = Character.toUpperCase(baseChar);
        } else {
            updatedChar = Character.toLowerCase(baseChar);
        }
        if (baseChar == updatedChar) {
            return str;
        }

        char[] chars = str.toCharArray();
        chars[0] = updatedChar;
        return new String(chars, 0, chars.length);
    }

    /**
     * Extract the filename from the given Java resource path,
     * e.g. {@code "mypath/myfile.txt" -> "myfile.txt"}.
     *
     * @param path the file path (may be {@code null})
     * @return the extracted filename, or {@code null} if none
     */

    public static String getFilename(String path) {
        if (path == null) {
            return null;
        }

        int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR);
        return (separatorIndex != -1 ? path.substring(separatorIndex + 1) : path);
    }

    /**
     * Extract the filename extension from the given Java resource path,
     * e.g. "mypath/myfile.txt" -> "txt".
     *
     * @param path the file path (may be {@code null})
     * @return the extracted filename extension, or {@code null} if none
     */

    public static String getFilenameExtension(String path) {
        if (path == null) {
            return null;
        }

        int extIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
        if (extIndex == -1) {
            return null;
        }

        int folderIndex = path.lastIndexOf(FOLDER_SEPARATOR);
        if (folderIndex > extIndex) {
            return null;
        }

        return path.substring(extIndex + 1);
    }

    /**
     * Strip the filename extension from the given Java resource path,
     * e.g. "mypath/myfile.txt" -> "mypath/myfile".
     *
     * @param path the file path
     * @return the path with stripped filename extension
     */
    public static String stripFilenameExtension(String path) {
        int extIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
        if (extIndex == -1) {
            return path;
        }

        int folderIndex = path.lastIndexOf(FOLDER_SEPARATOR);
        if (folderIndex > extIndex) {
            return path;
        }

        return path.substring(0, extIndex);
    }

    /**
     * Apply the given relative path to the given Java resource path,
     * assuming standard Java folder separation (i.e. "/" separators).
     *
     * @param path         the path to start from (usually a full file path)
     * @param relativePath the relative path to apply
     *                     (relative to the full file path above)
     * @return the full file path that results from applying the relative path
     */
    public static String applyRelativePath(String path, String relativePath) {
        int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR);
        if (separatorIndex != -1) {
            String newPath = path.substring(0, separatorIndex);
            if (!relativePath.startsWith(FOLDER_SEPARATOR)) {
                newPath += FOLDER_SEPARATOR;
            }
            return newPath + relativePath;
        } else {
            return relativePath;
        }
    }

    /**
     * Normalize the path by suppressing sequences like "path/.." and
     * inner simple dots.
     * <p>The result is convenient for path comparison. For other uses,
     * notice that Windows separators ("\") are replaced by simple slashes.
     *
     * @param path the original path
     * @return the normalized path
     */
    public static String cleanPath(String path) {
        if (!hasLength(path)) {
            return path;
        }
        String pathToUse = replace(path, WINDOWS_FOLDER_SEPARATOR, FOLDER_SEPARATOR);

        // Strip prefix from path to analyze, to not treat it as part of the
        // first path element. This is necessary to correctly parse paths like
        // "file:core/../core/io/Resource.class", where the ".." should just
        // strip the first "core" directory while keeping the "file:" prefix.
        int prefixIndex = pathToUse.indexOf(':');
        String prefix = "";
        if (prefixIndex != -1) {
            prefix = pathToUse.substring(0, prefixIndex + 1);
            if (prefix.contains(FOLDER_SEPARATOR)) {
                prefix = "";
            } else {
                pathToUse = pathToUse.substring(prefixIndex + 1);
            }
        }
        if (pathToUse.startsWith(FOLDER_SEPARATOR)) {
            prefix = prefix + FOLDER_SEPARATOR;
            pathToUse = pathToUse.substring(1);
        }

        String[] pathArray = delimitedListToStringArray(pathToUse, FOLDER_SEPARATOR);
        LinkedList<String> pathElements = new LinkedList<>();
        int tops = 0;

        for (int i = pathArray.length - 1; i >= 0; i--) {
            String element = pathArray[i];
            if (CURRENT_PATH.equals(element)) {
                // Points to current directory - drop it.
            } else if (TOP_PATH.equals(element)) {
                // Registering top path found.
                tops++;
            } else {
                if (tops > 0) {
                    // Merging path element with element corresponding to top path.
                    tops--;
                } else {
                    // Normal path element found.
                    pathElements.add(0, element);
                }
            }
        }

        // Remaining top paths need to be retained.
        for (int i = 0; i < tops; i++) {
            pathElements.add(0, TOP_PATH);
        }
        // If nothing else left, at least explicitly point to current path.
        if (pathElements.size() == 1 && "".equals(pathElements.getLast()) && !prefix.endsWith(FOLDER_SEPARATOR)) {
            pathElements.add(0, CURRENT_PATH);
        }

        return prefix + collectionToDelimitedString(pathElements, FOLDER_SEPARATOR);
    }

    /**
     * Compare two paths after normalization of them.
     *
     * @param path1 first path for comparison
     * @param path2 second path for comparison
     * @return whether the two paths are equivalent after normalization
     */
    public static boolean pathEquals(String path1, String path2) {
        return cleanPath(path1).equals(cleanPath(path2));
    }

    /**
     * Decode the given encoded URI component value. Based on the following rules:
     * <ul>
     * <li>Alphanumeric characters {@code "a"} through {@code "z"}, {@code "A"} through {@code "Z"},
     * and {@code "0"} through {@code "9"} stay the same.</li>
     * <li>Special characters {@code "-"}, {@code "_"}, {@code "."}, and {@code "*"} stay the same.</li>
     * <li>A sequence "{@code %<i>xy</i>}" is interpreted as a hexadecimal representation of the character.</li>
     * </ul>
     *
     * @param source  the encoded String
     * @param charset the character set
     * @return the decoded value
     * @throws IllegalArgumentException when the given source contains invalid encoded sequences
     * @see java.net.URLDecoder#decode(String, String)
     * @since 5.0
     */
    public static String uriDecode(String source, Charset charset) {
        int length = source.length();
        if (length == 0) {
            return source;
        }
        Assert.notNull(charset, "Charset must not be null");

        ByteArrayOutputStream bos = new ByteArrayOutputStream(length);
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            int ch = source.charAt(i);
            if (ch == '%') {
                if (i + 2 < length) {
                    char hex1 = source.charAt(i + 1);
                    char hex2 = source.charAt(i + 2);
                    int u = Character.digit(hex1, 16);
                    int l = Character.digit(hex2, 16);
                    if (u == -1 || l == -1) {
                        throw new IllegalArgumentException("Invalid encoded sequence \"" + source.substring(i) + "\"");
                    }
                    bos.write((char) ((u << 4) + l));
                    i += 2;
                    changed = true;
                } else {
                    throw new IllegalArgumentException("Invalid encoded sequence \"" + source.substring(i) + "\"");
                }
            } else {
                bos.write(ch);
            }
        }
        return (changed ? new String(bos.toByteArray(), charset) : source);
    }

    /**
     * Parse the given {@code String} value into a {@link Locale}, accepting
     * the {@link Locale#toString} format as well as BCP 47 language tags.
     *
     * @param localeValue the locale value: following either {@code Locale's}
     *                    {@code toString()} format ("en", "en_UK", etc), also accepting spaces as
     *                    separators (as an alternative to underscores), or BCP 47 (e.g. "en-UK")
     *                    as specified by {@link Locale#forLanguageTag} on Java 7+
     * @return a corresponding {@code Locale} instance, or {@code null} if none
     * @throws IllegalArgumentException in case of an invalid locale specification
     * @see #parseLocaleString
     * @see Locale#forLanguageTag
     * @since 5.0.4
     */

    public static Locale parseLocale(String localeValue) {
        String[] tokens = tokenizeLocaleSource(localeValue);
        if (tokens.length == 1) {
            validateLocalePart(localeValue);
            Locale resolved = Locale.forLanguageTag(localeValue);
            return (resolved.getLanguage().length() > 0 ? resolved : null);
        }
        return parseLocaleTokens(localeValue, tokens);
    }

    /**
     * Parse the given {@code String} representation into a {@link Locale}.
     * <p>For many parsing scenarios, this is an inverse operation of
     * {@link Locale#toString Locale's toString}, in a lenient sense.
     * This method does not aim for strict {@code Locale} design compliance;
     * it is rather specifically tailored for typical Spring parsing needs.
     * <p><b>Note: This delegate does not accept the BCP 47 language tag format.
     * Please use {@link #parseLocale} for lenient parsing of both formats.</b>
     *
     * @param localeString the locale {@code String}: following {@code Locale's}
     *                     {@code toString()} format ("en", "en_UK", etc), also accepting spaces as
     *                     separators (as an alternative to underscores)
     * @return a corresponding {@code Locale} instance, or {@code null} if none
     * @throws IllegalArgumentException in case of an invalid locale specification
     */

    public static Locale parseLocaleString(String localeString) {
        return parseLocaleTokens(localeString, tokenizeLocaleSource(localeString));
    }

    private static String[] tokenizeLocaleSource(String localeSource) {
        return tokenizeToStringArray(localeSource, "_ ", false, false);
    }

    private static Locale parseLocaleTokens(String localeString, String[] tokens) {
        String language = (tokens.length > 0 ? tokens[0] : "");
        String country = (tokens.length > 1 ? tokens[1] : "");
        validateLocalePart(language);
        validateLocalePart(country);

        String variant = "";
        if (tokens.length > 2) {
            // There is definitely a variant, and it is everything after the country
            // code sans the separator between the country code and the variant.
            int endIndexOfCountryCode = localeString.indexOf(country, language.length()) + country.length();
            // Strip off any leading '_' and whitespace, what's left is the variant.
            variant = trimLeadingWhitespace(localeString.substring(endIndexOfCountryCode));
            if (variant.startsWith("_")) {
                variant = trimLeadingCharacter(variant, '_');
            }
        }

        if (variant.isEmpty() && country.startsWith("#")) {
            variant = country;
            country = "";
        }

        return (language.length() > 0 ? new Locale(language, country, variant) : null);
    }

    private static void validateLocalePart(String localePart) {
        for (int i = 0; i < localePart.length(); i++) {
            char ch = localePart.charAt(i);
            if (ch != ' ' && ch != '_' && ch != '-' && ch != '#' && !Character.isLetterOrDigit(ch)) {
                throw new IllegalArgumentException("Locale part \"" + localePart + "\" contains invalid characters");
            }
        }
    }

    /**
     * Determine the RFC 3066 compliant language tag,
     * as used for the HTTP "Accept-Language" header.
     *
     * @param locale the Locale to transform to a language tag
     * @return the RFC 3066 compliant language tag as {@code String}
     * @deprecated as of 5.0.4, in favor of {@link Locale#toLanguageTag()}
     */
    @Deprecated
    public static String toLanguageTag(Locale locale) {
        return locale.getLanguage() + (hasText(locale.getCountry()) ? "-" + locale.getCountry() : "");
    }

    /**
     * Parse the given {@code timeZoneString} value into a {@link TimeZone}.
     *
     * @param timeZoneString the time zone {@code String}, following {@link TimeZone#getTimeZone(String)}
     *                       but throwing {@link IllegalArgumentException} in case of an invalid time zone specification
     * @return a corresponding {@link TimeZone} instance
     * @throws IllegalArgumentException in case of an invalid time zone specification
     */
    public static TimeZone parseTimeZoneString(String timeZoneString) {
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneString);
        if ("GMT".equals(timeZone.getID()) && !timeZoneString.startsWith("GMT")) {
            // We don't want that GMT fallback...
            throw new IllegalArgumentException("Invalid time zone specification '" + timeZoneString + "'");
        }
        return timeZone;
    }

    //---------------------------------------------------------------------
    // Convenience methods for working with String arrays
    //---------------------------------------------------------------------

    /**
     * Append the given {@code String} to the given {@code String} array,
     * returning a new array consisting of the input array contents plus
     * the given {@code String}.
     *
     * @param array the array to append to (can be {@code null})
     * @param str   the {@code String} to append
     * @return the new array (never {@code null})
     */
    public static String[] addStringToArray(String[] array, String str) {
        if (ObjectUtils.isEmpty(array)) {
            return new String[] { str };
        }

        String[] newArr = new String[array.length + 1];
        System.arraycopy(array, 0, newArr, 0, array.length);
        newArr[array.length] = str;
        return newArr;
    }

    /**
     * Concatenate the given {@code String} arrays into one,
     * with overlapping array elements included twice.
     * <p>The order of elements in the original arrays is preserved.
     *
     * @param array1 the first array (can be {@code null})
     * @param array2 the second array (can be {@code null})
     * @return the new array ({@code null} if both given arrays were {@code null})
     */

    public static String[] concatenateStringArrays(String[] array1, String[] array2) {
        if (ObjectUtils.isEmpty(array1)) {
            return array2;
        }
        if (ObjectUtils.isEmpty(array2)) {
            return array1;
        }

        String[] newArr = new String[array1.length + array2.length];
        System.arraycopy(array1, 0, newArr, 0, array1.length);
        System.arraycopy(array2, 0, newArr, array1.length, array2.length);
        return newArr;
    }

    /**
     * Merge the given {@code String} arrays into one, with overlapping
     * array elements only included once.
     * <p>The order of elements in the original arrays is preserved
     * (with the exception of overlapping elements, which are only
     * included on their first occurrence).
     *
     * @param array1 the first array (can be {@code null})
     * @param array2 the second array (can be {@code null})
     * @return the new array ({@code null} if both given arrays were {@code null})
     * @deprecated as of 4.3.15, in favor of manual merging via {@link LinkedHashSet}
     * (with every entry included at most once, even entries within the first array)
     */
    @Deprecated

    public static String[] mergeStringArrays(String[] array1, String[] array2) {
        if (ObjectUtils.isEmpty(array1)) {
            return array2;
        }
        if (ObjectUtils.isEmpty(array2)) {
            return array1;
        }

        List<String> result = new ArrayList<>();
        result.addAll(Arrays.asList(array1));
        for (String str : array2) {
            if (!result.contains(str)) {
                result.add(str);
            }
        }
        return toStringArray(result);
    }

    /**
     * Turn given source {@code String} array into sorted array.
     *
     * @param array the source array
     * @return the sorted array (never {@code null})
     */
    public static String[] sortStringArray(String[] array) {
        if (ObjectUtils.isEmpty(array)) {
            return new String[0];
        }

        Arrays.sort(array);
        return array;
    }

    /**
     * Copy the given {@code Collection} into a {@code String} array.
     * <p>The {@code Collection} must contain {@code String} elements only.
     *
     * @param collection the {@code Collection} to copy
     * @return the {@code String} array
     */
    public static String[] toStringArray(Collection<String> collection) {
        return collection.toArray(new String[0]);
    }

    /**
     * Copy the given Enumeration into a {@code String} array.
     * The Enumeration must contain {@code String} elements only.
     *
     * @param enumeration the Enumeration to copy
     * @return the {@code String} array
     */
    public static String[] toStringArray(Enumeration<String> enumeration) {
        return toStringArray(Collections.list(enumeration));
    }

    /**
     * Trim the elements of the given {@code String} array,
     * calling {@code String.trim()} on each of them.
     *
     * @param array the original {@code String} array (potentially empty)
     * @return the resulting array (of the same size) with trimmed elements
     */
    public static String[] trimArrayElements(String[] array) {
        if (ObjectUtils.isEmpty(array)) {
            return array;
        }

        String[] result = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            String element = array[i];
            result[i] = (element != null ? element.trim() : null);
        }
        return result;
    }

    /**
     * Remove duplicate strings from the given array.
     * <p>As of 4.2, it preserves the original order, as it uses a {@link LinkedHashSet}.
     *
     * @param array the {@code String} array (potentially empty)
     * @return an array without duplicates, in natural sort order
     */
    public static String[] removeDuplicateStrings(String[] array) {
        if (ObjectUtils.isEmpty(array)) {
            return array;
        }

        Set<String> set = new LinkedHashSet<>(Arrays.asList(array));
        return toStringArray(set);
    }

    /**
     * Split a {@code String} at the first occurrence of the delimiter.
     * Does not include the delimiter in the result.
     *
     * @param toSplit   the string to split (potentially {@code null} or empty)
     * @param delimiter to split the string up with (potentially {@code null} or empty)
     * @return a two element array with index 0 being before the delimiter, and
     * index 1 being after the delimiter (neither element includes the delimiter);
     * or {@code null} if the delimiter wasn't found in the given input {@code String}
     */

    public static String[] split(String toSplit, String delimiter) {
        if (!hasLength(toSplit) || !hasLength(delimiter)) {
            return null;
        }
        int offset = toSplit.indexOf(delimiter);
        if (offset < 0) {
            return null;
        }

        String beforeDelimiter = toSplit.substring(0, offset);
        String afterDelimiter = toSplit.substring(offset + delimiter.length());
        return new String[] { beforeDelimiter, afterDelimiter };
    }

    /**
     * Take an array of strings and split each element based on the given delimiter.
     * A {@code Properties} instance is then generated, with the left of the delimiter
     * providing the key, and the right of the delimiter providing the value.
     * <p>Will trim both the key and value before adding them to the {@code Properties}.
     *
     * @param array     the array to process
     * @param delimiter to split each element using (typically the equals symbol)
     * @return a {@code Properties} instance representing the array contents,
     * or {@code null} if the array to process was {@code null} or empty
     */

    public static Properties splitArrayElementsIntoProperties(String[] array, String delimiter) {
        return splitArrayElementsIntoProperties(array, delimiter, null);
    }

    /**
     * Take an array of strings and split each element based on the given delimiter.
     * A {@code Properties} instance is then generated, with the left of the
     * delimiter providing the key, and the right of the delimiter providing the value.
     * <p>Will trim both the key and value before adding them to the
     * {@code Properties} instance.
     *
     * @param array         the array to process
     * @param delimiter     to split each element using (typically the equals symbol)
     * @param charsToDelete one or more characters to remove from each element
     *                      prior to attempting the split operation (typically the quotation mark
     *                      symbol), or {@code null} if no removal should occur
     * @return a {@code Properties} instance representing the array contents,
     * or {@code null} if the array to process was {@code null} or empty
     */

    public static Properties splitArrayElementsIntoProperties(String[] array, String delimiter, String charsToDelete) {

        if (ObjectUtils.isEmpty(array)) {
            return null;
        }

        Properties result = new Properties();
        for (String element : array) {
            if (charsToDelete != null) {
                element = deleteAny(element, charsToDelete);
            }
            String[] splittedElement = split(element, delimiter);
            if (splittedElement == null) {
                continue;
            }
            result.setProperty(splittedElement[0].trim(), splittedElement[1].trim());
        }
        return result;
    }

    /**
     * Tokenize the given {@code String} into a {@code String} array via a
     * {@link StringTokenizer}.
     * <p>Trims tokens and omits empty tokens.
     * <p>The given {@code delimiters} string can consist of any number of
     * delimiter characters. Each of those characters can be used to separate
     * tokens. A delimiter is always a single character; for multi-character
     * delimiters, consider using {@link #delimitedListToStringArray}.
     *
     * @param str        the {@code String} to tokenize (potentially {@code null} or empty)
     * @param delimiters the delimiter characters, assembled as a {@code String}
     *                   (each of the characters is individually considered as a delimiter)
     * @return an array of the tokens
     * @see java.util.StringTokenizer
     * @see String#trim()
     * @see #delimitedListToStringArray
     */
    public static String[] tokenizeToStringArray(String str, String delimiters) {
        return tokenizeToStringArray(str, delimiters, true, true);
    }

    /**
     * Tokenize the given {@code String} into a {@code String} array via a
     * {@link StringTokenizer}.
     * <p>The given {@code delimiters} string can consist of any number of
     * delimiter characters. Each of those characters can be used to separate
     * tokens. A delimiter is always a single character; for multi-character
     * delimiters, consider using {@link #delimitedListToStringArray}.
     *
     * @param str               the {@code String} to tokenize (potentially {@code null} or empty)
     * @param delimiters        the delimiter characters, assembled as a {@code String}
     *                          (each of the characters is individually considered as a delimiter)
     * @param trimTokens        trim the tokens via {@link String#trim()}
     * @param ignoreEmptyTokens omit empty tokens from the result array
     *                          (only applies to tokens that are empty after trimming; StringTokenizer
     *                          will not consider subsequent delimiters as token in the first place).
     * @return an array of the tokens
     * @see java.util.StringTokenizer
     * @see String#trim()
     * @see #delimitedListToStringArray
     */
    public static String[] tokenizeToStringArray(String str, String delimiters, boolean trimTokens,
            boolean ignoreEmptyTokens) {

        if (str == null) {
            return new String[0];
        }

        StringTokenizer st = new StringTokenizer(str, delimiters);
        List<String> tokens = new ArrayList<>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (trimTokens) {
                token = token.trim();
            }
            if (!ignoreEmptyTokens || token.length() > 0) {
                tokens.add(token);
            }
        }
        return toStringArray(tokens);
    }

    /**
     * Take a {@code String} that is a delimited list and convert it into a
     * {@code String} array.
     * <p>A single {@code delimiter} may consist of more than one character,
     * but it will still be considered as a single delimiter string, rather
     * than as bunch of potential delimiter characters, in contrast to
     * {@link #tokenizeToStringArray}.
     *
     * @param str       the input {@code String} (potentially {@code null} or empty)
     * @param delimiter the delimiter between elements (this is a single delimiter,
     *                  rather than a bunch individual delimiter characters)
     * @return an array of the tokens in the list
     * @see #tokenizeToStringArray
     */
    public static String[] delimitedListToStringArray(String str, String delimiter) {
        return delimitedListToStringArray(str, delimiter, null);
    }

    /**
     * Take a {@code String} that is a delimited list and convert it into
     * a {@code String} array.
     * <p>A single {@code delimiter} may consist of more than one character,
     * but it will still be considered as a single delimiter string, rather
     * than as bunch of potential delimiter characters, in contrast to
     * {@link #tokenizeToStringArray}.
     *
     * @param str           the input {@code String} (potentially {@code null} or empty)
     * @param delimiter     the delimiter between elements (this is a single delimiter,
     *                      rather than a bunch individual delimiter characters)
     * @param charsToDelete a set of characters to delete; useful for deleting unwanted
     *                      line breaks: e.g. "\r\n\f" will delete all new lines and line feeds in a {@code String}
     * @return an array of the tokens in the list
     * @see #tokenizeToStringArray
     */
    public static String[] delimitedListToStringArray(String str, String delimiter, String charsToDelete) {

        if (str == null) {
            return new String[0];
        }
        if (delimiter == null) {
            return new String[] { str };
        }

        List<String> result = new ArrayList<>();
        if (delimiter.isEmpty()) {
            for (int i = 0; i < str.length(); i++) {
                result.add(deleteAny(str.substring(i, i + 1), charsToDelete));
            }
        } else {
            int pos = 0;
            int delPos;
            while ((delPos = str.indexOf(delimiter, pos)) != -1) {
                result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
                pos = delPos + delimiter.length();
            }
            if (str.length() > 0 && pos <= str.length()) {
                // Add rest of String, but not in case of empty input.
                result.add(deleteAny(str.substring(pos), charsToDelete));
            }
        }
        return toStringArray(result);
    }

    /**
     * Convert a comma delimited list (e.g., a row from a CSV file) into an
     * array of strings.
     *
     * @param str the input {@code String} (potentially {@code null} or empty)
     * @return an array of strings, or the empty array in case of empty input
     */
    public static String[] commaDelimitedListToStringArray(String str) {
        return delimitedListToStringArray(str, ",");
    }

    /**
     * Convert a comma delimited list (e.g., a row from a CSV file) into a set.
     * <p>Note that this will suppress duplicates, and as of 4.2, the elements in
     * the returned set will preserve the original order in a {@link LinkedHashSet}.
     *
     * @param str the input {@code String} (potentially {@code null} or empty)
     * @return a set of {@code String} entries in the list
     * @see #removeDuplicateStrings(String[])
     */
    public static Set<String> commaDelimitedListToSet(String str) {
        String[] tokens = commaDelimitedListToStringArray(str);
        return new LinkedHashSet<>(Arrays.asList(tokens));
    }

    /**
     * Convert a {@link Collection} to a delimited {@code String} (e.g. CSV).
     * <p>Useful for {@code toString()} implementations.
     *
     * @param coll   the {@code Collection} to convert (potentially {@code null} or empty)
     * @param delim  the delimiter to use (typically a ",")
     * @param prefix the {@code String} to start each element with
     * @param suffix the {@code String} to end each element with
     * @return the delimited {@code String}
     */
    public static String collectionToDelimitedString(Collection<?> coll, String delim, String prefix, String suffix) {

        if (CollectionUtils.isEmpty(coll)) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        Iterator<?> it = coll.iterator();
        while (it.hasNext()) {
            sb.append(prefix).append(it.next()).append(suffix);
            if (it.hasNext()) {
                sb.append(delim);
            }
        }
        return sb.toString();
    }

    /**
     * Convert a {@code Collection} into a delimited {@code String} (e.g. CSV).
     * <p>Useful for {@code toString()} implementations.
     *
     * @param coll  the {@code Collection} to convert (potentially {@code null} or empty)
     * @param delim the delimiter to use (typically a ",")
     * @return the delimited {@code String}
     */
    public static String collectionToDelimitedString(Collection<?> coll, String delim) {
        return collectionToDelimitedString(coll, delim, "", "");
    }

    /**
     * Convert a {@code Collection} into a delimited {@code String} (e.g., CSV).
     * <p>Useful for {@code toString()} implementations.
     *
     * @param coll the {@code Collection} to convert (potentially {@code null} or empty)
     * @return the delimited {@code String}
     */
    public static String collectionToCommaDelimitedString(Collection<?> coll) {
        return collectionToDelimitedString(coll, ",");
    }

    /**
     * Convert a {@code String} array into a delimited {@code String} (e.g. CSV).
     * <p>Useful for {@code toString()} implementations.
     *
     * @param arr   the array to display (potentially {@code null} or empty)
     * @param delim the delimiter to use (typically a ",")
     * @return the delimited {@code String}
     */
    public static String arrayToDelimitedString(Object[] arr, String delim) {
        if (ObjectUtils.isEmpty(arr)) {
            return "";
        }
        if (arr.length == 1) {
            return ObjectUtils.nullSafeToString(arr[0]);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) {
                sb.append(delim);
            }
            sb.append(arr[i]);
        }
        return sb.toString();
    }

    /**
     * Convert a {@code String} array into a comma delimited {@code String}
     * (i.e., CSV).
     * <p>Useful for {@code toString()} implementations.
     *
     * @param arr the array to display (potentially {@code null} or empty)
     * @return the delimited {@code String}
     */
    public static String arrayToCommaDelimitedString(Object[] arr) {
        return arrayToDelimitedString(arr, ",");
    }

}
