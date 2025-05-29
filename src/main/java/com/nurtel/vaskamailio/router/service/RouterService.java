package com.nurtel.vaskamailio.router.service;

import com.nurtel.vaskamailio.router.entity.RouterEntity;
import com.nurtel.vaskamailio.router.repository.RouterRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RouterService {
    public static RouterEntity createRoute(
            RouterRepository routerRepository,
            String cid,
            String did,
            Integer setid,
            String description
    ) {
        RouterEntity route = RouterEntity.builder()
                .cid(cid)
                .did(did)
                .setid(setid)
                .description(description)
                .build();

        route = routerRepository.save(route);
        return route;
    }

    public static Optional<RouterEntity> editRoute(
            RouterRepository routerRepository,
            Long id,
            String cid,
            String did,
            Integer setid,
            String description
    ) {
        Optional<RouterEntity> optionalRoute = routerRepository.findById(id);
        if (optionalRoute.isEmpty()) return Optional.empty();
        RouterEntity route = optionalRoute.get();

        route.setCid(cid);
        route.setDid(did);
        route.setSetid(setid);
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
