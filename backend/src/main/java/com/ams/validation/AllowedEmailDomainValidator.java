package com.ams.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class AllowedEmailDomainValidator implements ConstraintValidator<AllowedEmailDomain, String> {
    private Set<String> allowedDomains = parse("gmail.com,yahoo.com,outlook.com,hotmail.com,icloud.com,protonmail.com");
    private static Set<String> parse(String domains) { return Arrays.stream(domains.split(",")).map(v -> v.trim().toLowerCase(Locale.ROOT)).collect(Collectors.toSet()); }
    @Override public void initialize(AllowedEmailDomain annotation) { allowedDomains = parse(annotation.domains()); }
    @Value("${ams.email.allowed-domains:gmail.com,yahoo.com,outlook.com,hotmail.com,icloud.com,protonmail.com}")
    public void setConfiguredDomains(String domains) { allowedDomains = parse(domains); }
    @Override public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isBlank()) return true;
        int at = email.lastIndexOf('@');
        return at > 0 && at < email.length() - 1 && allowedDomains.contains(email.substring(at + 1).toLowerCase(Locale.ROOT));
    }
}
