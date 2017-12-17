/*
 *
 *  * The MIT License
 *  *
 *  * Copyright 2016 Shekhar Gulati <shekhargulati84@gmail.com>.
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *
 */

package strman;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.*;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

public abstract class Strman {

    private static final Predicate<String> NULL_STRING_PREDICATE = Objects::isNull;
    private static final Supplier<String> NULL_STRING_MSG_SUPPLIER = () -> "'value' should be not null.";
    private static final String[] EMPTY_ARRAY = new String[0];

    private Strman() {
    }

    // 把 appends 加到 value 后面
    public static String append(final String value, final String... appends) {
        return appendArray(value, appends);
    }

    public static String appendArray(final String value, final String[] appends) {
        validateNotNull(value);
        if (appends == null || appends.length == 0) {
            return value;
        }

        return value + Arrays.stream(appends).collect(Collectors.joining(""));
    }

    // index is between -(length-1) to (length-1).
    public static Optional<String> at(final String value, int index) {
        if (isNullOrEmpty(value)) {
            return Optional.empty();
        }
        // 支持负数的 index
        int length = value.length();
        if (index < 0) {
            index = length + index;
        }
        return (index < length && index >= 0) ? Optional.of(String.valueOf(value.charAt(index))) : Optional.empty();
    }

    public static String[] between(final String value, final String start, final String end) {
        validateNotNull(value);
        validate(start, NULL_STRING_PREDICATE, () -> "'start' should be not null.");
        validate(end, NULL_STRING_PREDICATE, () -> "'end' should be not null.");

        // split with regex
        String[] parts = value.split(end);
        return Arrays.stream(parts)
                .filter(subPart -> subPart.contains(start))
                .map(subPart -> subPart.substring(subPart.indexOf(start) + start.length()))
                .toArray(String[]::new);
    }

    public static String[] chars(final String value) {
//      ArrayStoreException: return value.chars().mapToObj(i -> Integer.valueOf(i)).toArray(String[]::new);
        return split(value, "");
    }

    public static String collapseWhitespace(final String value) {
        validateNotNull(value);
        return value.trim().replaceAll("\\s\\s+", " ");
    }

    public static boolean contains(final String value, final String needle) {
        return contains(value, needle, false);
    }

    public static boolean contains(final String value, final String needle, final boolean caseSensitive) {
        validateNotNull(value);
        if (caseSensitive) {
            return value.contains(needle);
        }
        return value.toLowerCase().contains(needle.toLowerCase());
    }

    public static boolean containsAll(final String value, final String[] needles) {
        validateNotNull(value);
        return Arrays.stream(needles).allMatch(needle -> contains(value, needle, false));
    }

    public static boolean containsAll(final String value, final String[] needles, final boolean caseSensitive) {
        validateNotNull(value);
        return Arrays.stream(needles).allMatch(needle -> contains(value, needle, caseSensitive));
    }

    public static boolean containsAny(final String value, final String[] needles) {
        return containsAny(value, needles, false);
    }

    public static boolean containsAny(final String value, final String[] needles, final boolean caseSensitive) {
        validateNotNull(value);
        return Arrays.stream(needles).anyMatch(needle -> contains(value, needle, caseSensitive));
    }

    public static long countSubstr(final String value, final String subStr) {
        return countSubstr(value, subStr, true, false);
    }

    public static long countSubstr(final String value, final String subStr, final boolean caseSensitive,
                                   boolean allowOverlapping) {
        validateNotNull(value);
        return countSubstr(caseSensitive ? value : value.toLowerCase(), caseSensitive ? subStr : subStr.toLowerCase(),
                allowOverlapping, 0L);
    }

    private static long countSubstr(String value, String subStr, boolean allowOverlapping, long count) {
        int position = value.indexOf(subStr);
        if (position == -1) {
            return count;
        }
        int offset;
        if (!allowOverlapping) {
            offset = position + subStr.length();
        } else {
            offset = position + 1;
        }
        return countSubstr(value.substring(offset), subStr, allowOverlapping, ++count);
    }

    public static boolean endsWith(final String value, final String search) {
        validateNotNull(value);
        return endsWith(value, search, value.length(), true);
    }

    public static boolean endsWith(final String value, final String search, final boolean caseSensitive) {
        validateNotNull(value);
        return endsWith(value, search, value.length(), caseSensitive);
    }

    public static boolean endsWith(final String value, final String search, final int position,
                                   final boolean caseSensitive) {
        validateNotNull(value);
        int remainingLength = position - search.length();
        if (caseSensitive) {
            return value.indexOf(search, remainingLength) > -1;
        }
        return value.toLowerCase().indexOf(search.toLowerCase(), remainingLength) > -1;
    }

    public static String ensureLeft(final String value, final String prefix) {
        return ensureLeft(value, prefix, true);
    }

    public static String ensureLeft(final String value, final String prefix, final boolean caseSensitive) {
        validateNotNull(value);
        validateNotNull(prefix);
        if (caseSensitive) {
            return value.startsWith(prefix) ? value : prefix + value;
        }
        String _value = value.toLowerCase();
        String _prefix = prefix.toLowerCase();
        // startsWith will throw nullPointerException
        return _value.startsWith(_prefix) ? value : prefix + value;
    }

    public static String base64Decode(final String value) {
        validateNotNull(value);
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }

    public static String base64Encode(final String value) {
        validateNotNull(value);
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Convert binary unicode (16 digits) string to string chars
     *
     * @param value The value to decode
     * @return The decoded String
     */
    public static String binDecode(final String value) {
        return decode(value, 16, 2);
    }

    /**
     * Convert string chars to binary unicode (16 digits)
     *
     * @param value The value to encode
     * @return String in binary format
     */
    public static String binEncode(final String value) {
        return encode(value, 16, 2);
    }

    /**
     * Convert decimal unicode (5 digits) string to string chars
     *
     * @param value The value to decode
     * @return decoded String
     */
    public static String decDecode(final String value) {
        return decode(value, 5, 10);
    }

    /**
     * Convert string chars to decimal unicode (5 digits)
     *
     * @param value The value to encode
     * @return Encoded value
     */
    public static String decEncode(final String value) {
        return encode(value, 5, 10);
    }

    public static String ensureRight(final String value, final String suffix) {
        return ensureRight(value, suffix, true);
    }

    public static String ensureRight(final String value, final String suffix, boolean caseSensitive) {
        validateNotNull(value);
        validateNotNull(suffix);
        return endsWith(value, suffix, caseSensitive) ? value : append(value, suffix);
    }

    public static Optional<String> first(final String value, final int n) {
        return Optional.ofNullable(value).filter(v -> !v.isEmpty()).map(v -> v.substring(0, n));
    }

    public static Optional<String> head(final String value) {
        return first(value, 1);
    }

    public static String format(final String value, String... params) {
        validateNotNull(value);
        // get matcher from compiled pattern, then use it to find matchings
        Pattern p = Pattern.compile("\\{(\\w+)}");
        Matcher m = p.matcher(value);
        String result = value;
        while (m.find()) {
            // 这是 regex capture 的数字, 比如 {5} 里面的 5
            int paramNumber = Integer.parseInt(m.group(1));
            if (params == null || paramNumber >= params.length) {
                throw new IllegalArgumentException("params does not have value for " + m.group());
            }
            // m.group() => m.group(0)
            result = result.replace(m.group(), params[paramNumber]);
        }
        return result;
    }

    public static String hexDecode(final String value) {
        return decode(value, 4, 16);
    }

    public static String hexEncode(final String value) {
        return encode(value, 4, 16);
    }

    public static int indexOf(final String value, final String needle, int offset, boolean caseSensitive) {
        validateNotNull(value);
        validateNotNull(needle);
        if (caseSensitive) {
            return value.indexOf(needle, offset);
        }
        return value.toLowerCase().indexOf(needle.toLowerCase(), offset);
    }

    // Objects.equals 实现不错: a == b || (a!=null && a.equals(b))
    public static boolean unequal(final String first, final String second) {
        return !Objects.equals(first, second);
    }

    // 应该用 unequal
    public static boolean inequal(final String first, final String second) {
        return !Objects.equals(first, second);
    }

    public static String insert(final String value, final String substr, final int index) {
        validateNotNull(value);
        validateNotNull(substr);
        if (index > value.length()) {
            return value;
        }
        return append(value.substring(0, index), substr, value.substring(index));
    }

    public static boolean isUpperCase(final String value) {
        validateNotNull(value);
        for (int i = 0; i < value.length(); i++) {
            if (Character.isLowerCase(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isLowerCase(final String value) {
        validateNotNull(value);
        for (int i = 0; i < value.length(); i++) {
            if (Character.isUpperCase(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static String last(final String value, int n) {
        validateNotNull(value);
        if (n > value.length()) {
            return value;
        }
        return value.substring(value.length() - n);
    }

    public static String leftPad(final String value, final String pad, final int length) {
        validateNotNull(value);
        validateNotNull(pad);
        if (value.length() > length) {
            return value;
        }
        return append(repeat(pad, length - value.length()), value);
    }

    public static String repeat(final String value, final int multiplier) {
        validateNotNull(value);
        return Stream.generate(() -> value).limit(multiplier).collect(joining());
    }

    public static boolean isString(final Object value) {
        if (Objects.isNull(value)) {
            throw new IllegalArgumentException("value can't be null");
        }
        return value instanceof String;
    }

    public static int lastIndexOf(final String value, final String needle) {
        validateNotNull(value);
        return lastIndexOf(value, needle, value.length(), true);
    }

    public static int lastIndexOf(final String value, final String needle, boolean caseSensitive) {
        validateNotNull(value);
        return lastIndexOf(value, needle, value.length(), caseSensitive);
    }

    // 扩充 string 自带的 lastIndexOf, 使得可以 caseInsensitive 地搜索
    public static int lastIndexOf(final String value, final String needle, final int offset,
                                  final boolean caseSensitive) {
        validateNotNull(value);
        validateNotNull(needle);
        if (caseSensitive) {
            return value.lastIndexOf(needle, offset);
        }
        return value.toLowerCase().lastIndexOf(needle.toLowerCase(), offset);
    }

    public static String leftTrim(final String value) {
        validateNotNull(value);
        return value.replaceAll("^\\s+", "");
    }

    public static int length(final String value) {
        validateNotNull(value);
        return value.length();
    }

    public static String prepend(final String value, final String... prepends) {
        return prependArray(value, prepends);
    }

    public static String prependArray(final String value, final String[] prepends) {
        validateNotNull(value);
        if (prepends == null || prepends.length == 0) {
            return value;
        }
        return Arrays.stream(prepends).collect(Collectors.joining("")) + value;
    }

    public static String[] removeEmptyStrings(String[] strings) {
        if (Objects.isNull(strings)) {
            throw new IllegalArgumentException("Input array should not be null");
        }
        return Arrays.stream(strings).filter(str -> str != null && !str.trim().isEmpty()).toArray(String[]::new);
    }

    public static String removeLeft(final String value, final String prefix) {
        return removeLeft(value, prefix, true);
    }

    public static String removeLeft(final String value, final String prefix, final boolean caseSensitive) {
        validateNotNull(value);
        validateNotNull(prefix);
        if (caseSensitive) {
            return value.startsWith(prefix) ? value.substring(prefix.length()) : value;
        }
        return value.toLowerCase().startsWith(prefix.toLowerCase()) ? value.substring(prefix.length()) : value;
    }

    /**
     * Remove all non word characters.
     *
     * @param value The input String
     * @return String without non-word characters
     */
    public static String removeNonWords(final String value) {
        validateNotNull(value);
        return value.replaceAll("[^\\w]+", "");
    }

    public static String removeRight(final String value, final String suffix) {
        return removeRight(value, suffix, true);
    }

    public static String removeRight(final String value, final String suffix, final boolean caseSensitive) {
        validateNotNull(value);
        validateNotNull(suffix);
        return !endsWith(value, suffix, caseSensitive) ? value :
                value.substring(0, value.toLowerCase().lastIndexOf(suffix.toLowerCase()));
    }

    public static String removeSpaces(final String value) {
        validateNotNull(value);
        return value.replaceAll("\\s", "");
    }

    public static String replace(final String value, final String search, final String newValue,
                                 final boolean caseSensitive) {
        validateNotNull(value);
        validateNotNull(search);
        if (caseSensitive) {
            return value.replace(search, newValue);
        }
        return Pattern.compile(search, Pattern.CASE_INSENSITIVE).matcher(value)
                .replaceAll(Matcher.quoteReplacement(newValue));
    }

    public static String reverse(final String value) {
        validateNotNull(value);
        return new StringBuilder(value).reverse().toString();
    }

    public static String rightPad(final String value, String pad, final int length) {
        validateNotNull(value);
        if (value.length() > length) {
            return value;
        }
        return append(value, repeat(pad, length - value.length()));
    }

    public static String rightTrim(final String value) {
        validateNotNull(value);
        return value.replaceAll("\\s+$", "");
    }

    /**
     * Truncate the string securely, not cutting a word in half. It always returns the last full word.
     *
     * @param value  The input String
     * @param length Max size of the truncated String
     * @param filler String that will be added to the end of the return string. Example: '...'
     * @return The truncated String
     */
    public static String safeTruncate(final String value, final int length, final String filler) {
        validateNotNull(value);
        if (length == 0) {
            return "";
        }
        if (length >= value.length()) {
            return value;
        }

        String[] words = words(value);
        StringJoiner result = new StringJoiner(" ");
        int spaceCount = 0;
        for (String word : words) {
            if (result.length() + word.length() + filler.length() + spaceCount > length) {
                break;
            } else {
                result.add(word);
                spaceCount++;
            }
        }
        return append(result.toString(), filler);
    }

    public static String[] split(final String value, final String regex) {
        validateNotNull(value);
        return value.split(regex);
    }

    public static String[] words(final String value) {
        return words(value, "\\s+");
    }

    public static String[] words(final String value, final String delimiter) {
        return split(value, delimiter);
    }

    public static String truncate(final String value, final int length, final String filler) {
        validateNotNull(value);
        if (length == 0) {
            return "";
        }
        if (length >= value.length()) {
            return value;
        }
        return append(value.substring(0, length - filler.length()), filler);
    }

    public static String htmlDecode(final String encodedHtml) {
        validateNotNull(encodedHtml);
        String[] entities = encodedHtml.split("&\\W+;");
        return Arrays.stream(entities)
                .map(e -> HtmlEntities.decodedEntities.getOrDefault(e, e))
                .collect(Collectors.joining());
    }

    public static String htmlEncode(final String html) {
        validateNotNull(html);
        return html.chars()
                .mapToObj(c -> "\\u" + String.format("%04x", c).toUpperCase())
                .map(HtmlEntities.encodedEntities::get).collect(joining());
    }

    public static String shuffle(final String value) {
        validateNotNull(value);
        List<String> strings = Arrays.asList(value.split(""));
        Collections.shuffle(strings);
        return strings.stream().collect(joining());
    }

    public static String slice(final String value, int begin, int end) {
        validateNotNull(value);
        return value.substring(begin, end);
    }

    public static String slugify(final String value) {
        validateNotNull(value);
        String transliterated = transliterate(collapseWhitespace(value.trim().toLowerCase()));
        return Arrays.stream(words(transliterated.replace("&", "-and-"), "\\W+")).collect(joining("-"));
    }

    public static String transliterate(final String value) {
        validateNotNull(value);
        String result = value;
        Set<Map.Entry<String, List<String>>> entries = Ascii.ascii.entrySet();
        for (Map.Entry<String, List<String>> entry : entries) {
            for (String ch : entry.getValue()) {
                result = result.replace(ch, entry.getKey());
            }
        }
        return result;
    }

    /**
     * Surrounds a 'value' with the given 'prefix' and 'suffix'.
     *
     * @param value  The input String
     * @param prefix prefix. If suffix is null then prefix is used
     * @param suffix suffix
     * @return The String with surround substrs!
     */
    public static String surround(final String value, final String prefix, final String suffix) {
        validateNotNull(value);
        String _prefix = Optional.ofNullable(prefix).orElse("");
        return append(_prefix, value, Optional.ofNullable(suffix).orElse(_prefix));
    }

    /**
     * Transform to camelCase
     *
     * @param value The input String
     * @return String in camelCase.
     */
    public static String toCamelCase(final String value) {
        if (value == null || value.length() == 0) {
            return "";
        }
        String str = toStudlyCase(value);
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    /**
     * Transform to StudlyCaps.
     *
     * @param value The input String
     * @return String in StudlyCaps.
     */
    public static String toStudlyCase(final String value) {
        validateNotNull(value);
        String[] words = collapseWhitespace(value.trim()).split("\\s*(_|-|\\s)\\s*");
        return Arrays.stream(words).filter(w -> !w.trim().isEmpty()).map(Strman::upperFirst).collect(joining());
    }

    /**
     * Return tail of the String
     *
     * @param value The input String
     * @return String tail
     */
    public static Optional<String> tail(final String value) {
        return Optional.ofNullable(value).filter(v -> !v.isEmpty()).map(v -> last(v, v.length() - 1));
    }

    /**
     * Decamelize String
     *
     * @param value The input String
     * @param chr   string to use
     * @return String decamelized.
     */
    public static String toDecamelize(final String value, final String chr) {
        String camelCasedString = toCamelCase(value);
        String[] words = camelCasedString.split("(?=\\p{Upper})");
        return Arrays.stream(words).map(String::toLowerCase).collect(joining(Optional.ofNullable(chr).orElse(" ")));
    }

    /**
     * Transform to kebab-case.
     *
     * @param value The input String
     * @return String in kebab-case.
     */
    public static String toKebabCase(final String value) {
        return toDecamelize(value, "-");
    }

    /**
     * Transform to snake_case.
     *
     * @param value The input String
     * @return String in snake_case.
     */
    public static String toSnakeCase(final String value) {
        return toDecamelize(value, "_");
    }

    public static String decode(final String value, final int digits, final int radix) {
        return Arrays.stream(split(value, "(?<=\\G.{" + digits + "})"))
                .map(data -> String.valueOf(Character.toChars(Integer.parseInt(data, radix))))
                .collect(joining());
    }

    public static String encode(final String value, final int digits, final int radix) {
        validateNotNull(value);
        return value.chars().mapToObj(ch -> leftPad(Integer.toString(ch, radix), "0", digits)).collect(joining());
    }

    /**
     * Join concatenates all the elements of the strings array into a single String. The separator string is placed between elements in the resulting string.
     *
     * @param strings   The input array to concatenate
     * @param separator The separator to use
     * @return Concatenated String
     */
    public static String join(final String[] strings, final String separator) throws IllegalArgumentException {
        if (strings == null) {
            throw new IllegalArgumentException("Input array 'strings' can't be null");
        }
        if (separator == null) {
            throw new IllegalArgumentException("separator can't be null");
        }
        StringJoiner joiner = new StringJoiner(separator);
        for (String el : strings) {
            joiner.add(el);
        }
        return joiner.toString();
    }

    /**
     * Converts the first character of string to upper case and the remaining to lower case.
     *
     * @param input The string to capitalize
     * @return The capitalized string
     */
    public static String capitalize(final String input) throws IllegalArgumentException {
        if (input == null) {
            throw new IllegalArgumentException("input can't be null");
        }
        if (input.length() == 0) {
            return "";
        }
        return head(input).map(String::toUpperCase).map(h -> tail(input).map(t -> h + t.toLowerCase()).orElse(h)).get();
    }

    /**
     * Converts the first character of string to lower case.
     *
     * @param input The string to convert
     * @return The converted string
     */
    public static String lowerFirst(final String input) throws IllegalArgumentException {
        if (input == null) {
            throw new IllegalArgumentException("input can't be null");
        }
        if (input.length() == 0) {
            return "";
        }

        return head(input).map(String::toLowerCase).map(h -> tail(input).map(t -> h + t).orElse(h)).get();
    }

    /**
     * Verifies whether String is enclosed by encloser
     *
     * @param input    The input String
     * @param encloser String which encloses input String
     * @return true if enclosed false otherwise
     */
    public static boolean isEnclosedBetween(final String input, final String encloser) {
        return isEnclosedBetween(input, encloser, encloser);
    }

    /**
     * Verifies whether String is enclosed by encloser
     *
     * @param input         The input String
     * @param leftEncloser  String which encloses input String at left start
     * @param rightEncloser String which encloses input String at the right end
     * @return true if enclosed false otherwise
     */
    public static boolean isEnclosedBetween(final String input, final String leftEncloser, String rightEncloser) {
        if (input == null) {
            throw new IllegalArgumentException("input can't be null");
        }
        if (leftEncloser == null) {
            throw new IllegalArgumentException("leftEncloser can't be null");
        }
        if (rightEncloser == null) {
            throw new IllegalArgumentException("rightEncloser can't be null");
        }
        return input.startsWith(leftEncloser) && input.endsWith(rightEncloser);
    }

    /**
     * Converts the first character of string to upper case.
     *
     * @param input The string to convert.
     * @return Returns the converted string.
     */
    public static String upperFirst(String input) {
        if (input == null) {
            throw new IllegalArgumentException("input can't be null");
        }
        return head(input).map(String::toUpperCase).map(h -> tail(input).map(t -> h + t).orElse(h)).get();
    }

    /**
     * Removes leading whitespace from string.
     *
     * @param input The string to trim.
     * @return Returns the trimmed string.
     */
    public static Optional<String> trimStart(final String input) {
        return Optional.ofNullable(input).filter(v -> !v.isEmpty()).map(Strman::leftTrim);
    }

    /**
     * Removes leading characters from string.
     *
     * @param input The string to trim.
     * @param chars The characters to trim.
     * @return Returns the trimmed string.
     */
    public static Optional<String> trimStart(final String input, String... chars) {
        return Optional.ofNullable(input).filter(v -> !v.isEmpty()).map(v -> {
            String pattern = String.format("^[%s]+", join(chars, "\\"));
            return v.replaceAll(pattern, "");
        });
    }

    /**
     * Removes trailing whitespace from string.
     *
     * @param input The string to trim.
     * @return Returns the trimmed string.
     */
    public static Optional<String> trimEnd(final String input) {
        return Optional.ofNullable(input).filter(v -> !v.isEmpty()).map(Strman::rightTrim);
    }

    /**
     * Removes trailing characters from string.
     *
     * @param input The string to trim.
     * @param chars The characters to trim.
     * @return Returns the trimmed string.
     */
    public static Optional<String> trimEnd(final String input, String... chars) {
        return Optional.ofNullable(input).filter(v -> !v.isEmpty()).map(v -> {
            String pattern = String.format("[%s]+$", join(chars, "\\"));
            return v.replaceAll(pattern, "");
        });
    }

    /**
     * Counts the number of occurrences of each character in the string
     *
     * @param input The input string
     * @return A map containing the number of occurrences of each character in the string
     */
    public static Map<Character, Long> charsCount(String input) {
        if (isNullOrEmpty(input)) {
            return Collections.emptyMap();
        }

        return input.chars().mapToObj(c -> (char) c).collect(groupingBy(identity(), counting()));
    }

    /**
     * Checks if string is empty.  This is a null safe check and will return true when string is null.
     *
     * @param input The input string
     * @return true if input string is null or empty
     */
    public static boolean isBlank(String input) {
        return input == null || input.isEmpty();
    }

    /**
     * Changes passed in string to all lower case and adds underscore between words.
     *
     * @param input The input string
     * @return the input string in all lower case with underscores between words
     */
    public static String underscored(final String input) {
        if (input == null || input.length() == 0) {
            return "";
        }

        return input.trim().replaceAll("([a-z\\d])([A-Z]+)", "$1_$2").replaceAll("[-\\s]+", "_").toLowerCase();
    }

    /**
     * Aggregates the contents of n strings into a single list of tuples.
     *
     * @param inputs A list of strings.
     * @return A list of strings if none of the strings in the input is null or empty.
     * An empty list otherwise.
     */
    public static List<String> zip(String... inputs) {
        if (inputs.length == 0) {
            return Collections.emptyList();
        }
        OptionalInt min = Arrays.stream(inputs).mapToInt(str -> str == null ? 0 : str.length()).min();
        if (!min.isPresent()) {
            return Collections.emptyList();
        }
        return IntStream.range(0, min.getAsInt())
                .mapToObj(elementIndex -> Arrays.stream(inputs)
                        .map(input -> String.valueOf(input.charAt(elementIndex)))
                        .collect(joining()))
                .collect(toList());
    }

    /**
     * Split lines to an array
     *
     * @param input The input String
     * @return lines in an array
     */
    public static String[] lines(String input) {
        if (input == null) {
            return EMPTY_ARRAY;
        }
        return input.split("\r\n?|\n");
    }

    /**
     * Converts a underscored or camelized string into an dasherized one.
     *
     * @param input The input String
     * @return dasherized String.
     */
    public static String dasherize(String input) {
        return toKebabCase(input);
    }

    /**
     * Converts an underscored, camelized, or dasherized string into a humanized one. Also removes beginning and ending whitespace.
     *
     * @param input The input String
     * @return humanized version of String
     */
    public static String humanize(final String input) {
        if (input == null || input.length() == 0) {
            return "";
        }
        return upperFirst(underscored(input).replaceAll("_", " "));
    }

    /**
     * Returns a copy of the string in which all the case-based characters have had their case swapped.
     *
     * @param input Input string
     * @return String with all the case swapped
     */
    public static String swapCase(String input) {
        if (input == null || input.length() == 0) {
            return "";
        }
        StringBuilder resultBuilder = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                resultBuilder.append(Character.toLowerCase(ch));
            } else {
                resultBuilder.append(Character.toUpperCase(ch));
            }
        }
        return resultBuilder.toString();
    }

    /**
     * Returns a string representation of the number passed in where groups of three digits are delimited by comma
     *
     * @param number Input number
     * @return formatted String
     */
    public static String formatNumber(long number) {
        String stringRepresentation = Long.toString(number);
        StringBuilder sb = new StringBuilder();
        int bound = stringRepresentation.length() - 1;
        String delimiter = ",";
        int counter = 0;
        for (int i = bound; i >= 0; i--) {
            char c = stringRepresentation.charAt(i);
            if (i != bound && counter % 3 == 0) {
                sb.append(delimiter);
            }
            sb.append(c);
            counter++;
        }
        return sb.reverse().toString();
    }

    public static String[] chop(String input, int step) {
        if (input == null || input.length() == 0) {
            return EMPTY_ARRAY;
        }
        if (step == 0) {
            return new String[]{input};
        }
        int strLength = input.length();
        int iterations = strLength % step == 0 ? strLength / step : strLength / step + 1;
        return IntStream.iterate(0, i -> i + step)
                .limit(iterations)
                .mapToObj(i -> input.substring(i, (i + step) < strLength ? i + step : strLength))
                .toArray(String[]::new);
    }

    /**
     * Converts a String into its Start Case version
     * https://en.wikipedia.org/wiki/Letter_case#Stylistic_or_specialised_usage
     *
     * @param input The input String
     * @return Start Case String
     */
    public static String startCase(final String input) {
        validate(input, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        // split into a word when we encounter a space, or an underscore, or a dash, or a switch from lower to upper case
        String[] words = words(input, "\\s|_|-|(?<=[a-z])(?=[A-Z])");
        return Arrays.stream(words).filter(w -> !w.trim().isEmpty())
            .map(w -> upperFirst(w.toLowerCase())).collect(joining(" "));
    }

    public static String escapeRegExp(final String input) {
        validate(input, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
        return input.replaceAll("[\\\\\\^\\$\\*\\+\\-\\?\\.\\|\\(\\)\\{\\}\\[\\]]", "\\\\$0");
    }

    private static void validate(String value, Predicate<String> predicate, final Supplier<String> supplier) {
        if (predicate.test(value)) {
            throw new IllegalArgumentException(supplier.get());
        }
    }
    
    private static void validateNotNull(String value) {
        validate(value, NULL_STRING_PREDICATE, NULL_STRING_MSG_SUPPLIER);
    }

    private static boolean isNullOrEmpty(String input) {
        return input == null || input.isEmpty();
    }

}

