package commons.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceReference {

    public static final String RR_PREFIX = "rr://";
    // rr://<env>/<app>/<customer>/<resource>/<id>
    public static final String RR_BASE_PATTERN = "^" + RR_PREFIX + "([A-Z]{2,})/([a-z]{2,})/([a-zA-Z0-9-_]{3,})";
    public static final Pattern RR_URI_PATTERN = Pattern.compile(RR_BASE_PATTERN + "((/[a-zA-Z0-9-]{2,}){0,6})$");

    private final String application;
    private final String environment;
    private final String customer;
    private final List<String> properties;

    public static boolean isValidSSUri(String ssUri) {
        return Objects.nonNull(ssUri) && RR_URI_PATTERN.matcher(ssUri).find();
    }

    protected ResourceReference(String application, String environment, String customer, List<String> properties) {
        this.application = Optional.ofNullable(application).map(String::toUpperCase).orElse(application);
        this.environment = Optional.ofNullable(environment).map(String::toLowerCase).orElse(environment);
        this.customer = customer;
        this.properties = properties;
    }

    protected ResourceReference(String application, String environment, String customer, String... properties) {
        this.application = Optional.ofNullable(application).map(String::toUpperCase).orElse(application);
        this.environment = Optional.ofNullable(environment).map(String::toLowerCase).orElse(environment);
        this.customer = customer;
        this.properties = Arrays.asList(properties);
    }

    public String getEnvironment() {
        return this.environment;
    }

    public String getApplication() {
        return this.application;
    }

    public String getCustomer() {
        return this.customer;
    }

    public List<String> getProperties() {
        return Collections.unmodifiableList(this.properties);
    }

    public String getValue() {
        StringJoiner joiner = new StringJoiner("/", RR_PREFIX, "");
        joiner.add(this.application);
        joiner.add(this.environment);
        joiner.add(this.customer);
        this.properties.forEach(joiner::add);
        return joiner.toString();
    }

    public static ResourceReference of(String rri) throws InvalidPropertiesFormatException {
        return matchPattern(rri).orElseThrow(() ->
            new InvalidPropertiesFormatException("Invalid RRI pattern")
        );
    }

    private static Optional<ResourceReference> matchPattern(String value) {
        Matcher matcher = RR_URI_PATTERN.matcher(value);
        if (matcher.find()) {
            Builder builder = new Builder(matcher.group(1), matcher.group(2), matcher.group(3));
            String restGroup = matcher.group(4).startsWith("/") ? matcher.group(4).substring(1) : matcher.group(4);
            builder.withProperties(restGroup.split("/"));
            return Optional.of(builder.build());
        } else {
            return Optional.empty();
        }
    }

    public String toString() {
        return this.getValue();
    }

    public static class Builder {
        private final String application;
        private final String environment;
        private final String customer;
        private final List<String> properties;

        public Builder(String application, String environment, String customer) {
            this.application = Optional.ofNullable(application).map(String::toUpperCase).orElse(application);
            this.environment = Optional.ofNullable(environment).map(String::toLowerCase).orElse(environment);
            this.customer = customer;
            this.properties = new ArrayList<>();
        }

        public Builder withProperties(String... properties) {
            this.properties.addAll(Arrays.asList(properties));
            return this;
        }

        public Builder addProperty(String property) {
            this.properties.add(property);
            return this;
        }

        public ResourceReference build() {
            return new ResourceReference(this.application, this.environment, this.customer, this.properties);
        }
    }
}