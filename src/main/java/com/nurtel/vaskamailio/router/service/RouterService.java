package com.nurtel.vaskamailio.router.service;

import com.nurtel.vaskamailio.router.entity.RouterEntity;
import com.nurtel.vaskamailio.router.repository.RouterRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class RouterService {
    public static RouterEntity createRoute(
            RouterRepository routerRepository,
            String did,
            Integer setid,
            String description
    ) {
        did = did.strip();

        RouterEntity route = RouterEntity.builder()
                .did(did)
                .keyType(0)
                .valueType(1)
                .setid(String.valueOf(setid))
                .description(description)
                .build();

        if (routerRepository.findByDid(did).isPresent()) {
            throw new RuntimeException("Запись с таким DID уже существует");
        }

        route = routerRepository.save(route);
        return route;
    }

    public static Optional<RouterEntity> editRoute(
            RouterRepository routerRepository,
            Long id,
            String did,
            Integer setid,
            String description
    ) {
        did = did.strip();

        Optional<RouterEntity> optionalRoute = routerRepository.findById(id);
        if (optionalRoute.isEmpty()) return Optional.empty();
        RouterEntity route = optionalRoute.get();

        if (routerRepository.findByDid(did).isPresent() && !Objects.equals(routerRepository.findByDid(did).get().getId(), id)) {
            throw new RuntimeException("Запись с таким DID уже существует");
        }

        route.setDid(did);
        route.setKeyType(0);
        route.setValueType(1);
        route.setSetid(String.valueOf(setid));
        route.setDescription(description);
        route = routerRepository.save(route);
        return Optional.of(route);
    }

    public static void deleteRoute(
            RouterRepository routerRepository,
            Long id
    ) {
        Optional<RouterEntity> optionalRoute = routerRepository.findById(id);
        if (optionalRoute.isPresent()) routerRepository.deleteById(id);
    }
}
