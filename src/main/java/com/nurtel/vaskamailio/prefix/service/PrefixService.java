package com.nurtel.vaskamailio.prefix.service;

import com.nurtel.vaskamailio.prefix.entity.PrefixEntity;
import com.nurtel.vaskamailio.prefix.repository.PrefixRepository;

import java.util.Objects;
import java.util.Optional;

public class PrefixService {
    public static PrefixEntity createPrefix(
            PrefixRepository prefixRepository,
            String regex,
            Integer setid,
            Boolean strip,
            Integer stripChars,
            String description
    ) {
        regex = regex.strip();

        if (prefixRepository.findByPattern(regex).isPresent()) {
            throw new RuntimeException("Запись с таким regex уже существует");
        }

        PrefixEntity prefix = PrefixEntity.builder()
                .pattern(regex)
                .setid(setid)
                .strip(strip)
                .stripChars(stripChars)
                .description(description)
                .build();

        prefix = prefixRepository.save(prefix);
        return prefix;
    }

    public static Optional<PrefixEntity> editPrefix(
            PrefixRepository prefixRepository,
            Long id,
            String regex,
            Integer setid,
            Boolean strip,
            Integer stripChars,
            String description
    ) {
        regex = regex.strip();

        if (prefixRepository.findByPattern(regex).isPresent() && !Objects.equals(prefixRepository.findByPattern(regex).get().getId(), id)) {
            throw new RuntimeException("Запись с таким regex уже существует");
        }

        Optional<PrefixEntity> optionalPrefix = prefixRepository.findById(id);
        if (optionalPrefix.isEmpty()) return Optional.empty();
        PrefixEntity prefix = optionalPrefix.get();

        prefix.setPattern(regex);
        prefix.setSetid(setid);
        prefix.setStrip(strip);
        prefix.setStripChars(stripChars);
        prefix.setDescription(description);

        prefix = prefixRepository.save(prefix);
        return Optional.of(prefix);
    }

    public static void deletePrefix(
            PrefixRepository prefixRepository,
            Long id
    ) {
        Optional<PrefixEntity> optionalPrefix = prefixRepository.findById(id);
        if (optionalPrefix.isPresent()) prefixRepository.deleteById(id);
    }
}
