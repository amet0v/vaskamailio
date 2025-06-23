package com.nurtel.vaskamailio.router.service;

import com.nurtel.vaskamailio.router.entity.RouterEntity;
import com.nurtel.vaskamailio.router.repository.RouterRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RouterService {
    public static RouterEntity createRoute(
            RouterRepository routerRepository,
            String did,
            Integer setid,
            String description
    ) {
        RouterEntity route = RouterEntity.builder()
                .did(did)
                .keyType(0)
                .valueType(1)
                .setid(String.valueOf(setid))
                .description(description)
                .build();

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
        Optional<RouterEntity> optionalRoute = routerRepository.findById(id);
        if (optionalRoute.isEmpty()) return Optional.empty();
        RouterEntity route = optionalRoute.get();

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
