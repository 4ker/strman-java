只有三个文件:

-   Ascii 是一个 abstract class, 里面有一个 map:

    ```java
    public static final Map<String, List<String>> ascii = new HashMap<String, List<String>>() {{
            put("0", Arrays.asList("°", "₀", "۰"));
            put("1", Arrays.asList("¹", "₁", "۱"));
            ...
    }};
    ```
-   HtmlEntities 是一个 abstract class, 里面有一个 map:

    ```java
    public static final Map<String, String> decodedEntities = new HashMap<String, String>() {{
            put("&AMP", "\u0026");
            put("&AMP;", "\u0026");
            ...
    }};
    ```
-   Strman.java

## MISC tricks

```java
@FunctionalInterface
public interface Supplier<T> {
    T get();
}

@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);
}

Objects.requireNonNull(value, "error message");

// string equals
public static boolean unequal(final String first, final String second) {
    return !Objects.equals(first, second);
}
// Objects.equals implementation
public static boolean equals(Object a, Object b) {
    return (a == b) || (a != null && a.equals(b));
}

// 检测有没有 lowercase char
public static boolean isUpperCase(final String value) { ... }

// value instanceof Type
public static boolean isString(final Object value) {
    if (Objects.isNull(value)) {
        throw new IllegalArgumentException("value can't be null");
    }
    return value instanceof String;
}

// string[] -> List<String> -> shuffled List of String
public static String shuffle(final String value) {
    validateNotNull(value);
    List<String> strings = Arrays.asList(value.split(""));
    Collections.shuffle(strings);
    return strings.stream().collect(joining());
}
```

## java.util.StringJoiner

(用 copy reference 拿到包路径 (⌘⇧⌃-C)

```java
StringJoiner sj = new StringJoiner(":", "[", "]");
sj.add("George").add("Sally").add("Fred");
String desiredString = sj.toString();
// "[George:Sally:Fred]"

List<Integer> numbers = Arrays.asList(1, 2, 3, 4);
String commaSeparatedNumbers = numbers.stream()
        .map(i -> i.toString())
        .collect(Collectors.joining(", "));
```

```java
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
```

## indexOf, lastIndexOf

```java
// String.lastIndexOf
public final class String
    implements java.io.Serializable, Comparable<String>, CharSequence {
    /** The value is used for character storage. */
    private final char value[];
    static int lastIndexOf(char[] source, int sourceOffset, int sourceCount,
                        char[] target, int targetOffset, int targetCount,
                        int fromIndex) { ... }
}
```

## toArray, toArray()

```java
Arrays.stream(parts)
        .filter(subPart -> subPart.contains(start))
        .map(subPart -> subPart.substring(subPart.indexOf(start) + start.length()))
        .toArray(String[]::new);

```

## stream

```java
// allMatch
public static boolean containsAll(final String value, final String[] needles) {
    validateNotNull(value);
    return Arrays.stream(needles).allMatch(needle -> contains(value, needle, false));
}

// anyMatch
public static boolean containsAny(final String value, final String[] needles, final boolean caseSensitive) {
    validateNotNull(value);
    return Arrays.stream(needles).anyMatch(needle -> contains(value, needle, caseSensitive));
}

// generate from supplier then limit and collect
public static String repeat(final String value, final int multiplier) {
    validateNotNull(value);
    return Stream.generate(() -> value).limit(multiplier).collect(joining());
}
// public static<T> Stream<T> generate(Supplier<T> s) {

// filter, toArray
public static String[] removeEmptyStrings(String[] strings) {
    if (Objects.isNull(strings)) {
        throw new IllegalArgumentException("Input array should not be null");
    }
    return Arrays.stream(strings).filter(str -> str != null && !str.trim().isEmpty()).toArray(String[]::new);
}
// stream: <A> A[] toArray(IntFunction<A[]> generator);

// stream boxing: intStream.mapToObj
public static String htmlEncode(final String html) {
    validateNotNull(html);
    return html.chars()
            .mapToObj(c -> "\\u" + String.format("%04x", c).toUpperCase())
            .map(HtmlEntities.encodedEntities::get).collect(joining());
}
// <U> Stream<U> mapToObj(IntFunction<? extends U> mapper);
```

## RegEx

```java
// remove extra whitespaces
value.trim().replaceAll("\\s\\s+", " ");

// public String replaceAll(String regex, String replacement) { ... }
public static String leftTrim(final String value) {
    validateNotNull(value);
    return value.replaceAll("^\\s+", "");
}
public static String rightTrim(final String value) {
    validateNotNull(value);
    return value.replaceAll("\\s+$", "");
}

// get matcher of from compiled pattern from regex string,
// then use it to find, replaceAll, etc
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
```

## indexOf, lastIndexOf

```java
public static boolean endsWith(final String value, final String search, final int position,
                               final boolean caseSensitive) {
    validateNotNull(value);
    int remainingLength = position - search.length();
    if (caseSensitive) {
        return value.indexOf(search, remainingLength) > -1;
    }
    return value.toLowerCase().indexOf(search.toLowerCase(), remainingLength) > -1;
}
```

## encoder/decoder

```java
// java.util.Base64.getDecoder
public static Decoder getDecoder() {
    return Decoder.RFC4648;
}

public static String base64Decode(final String value) {
    validateNotNull(value);
    //     public String(byte bytes[], Charset charset) {
    return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
}
```

## optional

```java
public static Optional<String> first(final String value, final int n) {
    // filter -> empty()
    return Optional.ofNullable(value).filter(v -> !v.isEmpty()).map(v -> v.substring(0, n));
}
```

---

strman-java [![Build Status](https://travis-ci.org/shekhargulati/strman-java.svg?branch=master)](https://travis-ci.org/shekhargulati/strman-java) [![codecov.io](https://codecov.io/github/shekhargulati/strman-java/coverage.svg?branch=master)](https://codecov.io/github/shekhargulati/strman-java?branch=master) [![License](https://img.shields.io/:license-mit-blue.svg)](./LICENSE.txt)
------

A Java 8 library for working with Strings.
You can learn about all the String utility functions implemented in `strman` library by reading the [documentation](https://github.com/shekhargulati/strman-java/wiki).

Getting Started
--------

To use strman in your application, you have to add `strman` to your classpath.
strman is available on [Maven Central](http://search.maven.org/) so you just need to add dependency in your favorite build tool as shown below.

For Apache Maven users, please add following to your `pom.xml`.

```xml
<dependencies>
    <dependency>
        <groupId>com.shekhargulati</groupId>
        <artifactId>strman</artifactId>
        <version>0.4.0</version>
    </dependency>
</dependencies>
```

Gradle users can add following to their `build.gradle` file.

```
compile(group: 'com.shekhargulati', name: 'strman', version: '0.4.0')
```

To learn what we added in the latest version please refer to `./changelog.md`.

You can refer to Javadocs online [http://shekhargulati.github.io/strman-java/](http://shekhargulati.github.io/strman-java/).

## Inspiration

This library is inspired by [dleitee/strman](https://github.com/dleitee/strman).

License
-------
strman is licensed under the MIT License - see the `LICENSE` file for details.
