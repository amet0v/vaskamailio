package com.nurtel.vaskamailio.dispatcher.service;

import com.nurtel.vaskamailio.dispatcher.entity.DispatcherEntity;
import com.nurtel.vaskamailio.dispatcher.repository.DispatcherRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class DispatcherService {
    public static DispatcherEntity createDispatcherEntity(
            DispatcherRepository dispatcherRepository,
            Integer setid,
            String destination,
            Integer flags,
            Integer priority,
            String attrs,
            String description
    ) {
        destination = destination.strip();

        DispatcherEntity dispatcherEntity = DispatcherEntity.builder()
                .setid(setid)
                .destination(destination)
                .flags(flags)
                .priority(priority)
                .attrs(attrs)
                .description(description)
                .build();

//        if (dispatcherRepository.findByDestination(destination).isPresent()) {
//            throw new RuntimeException("Запись с таким destination уже существует");
//        }

        dispatcherEntity = dispatcherRepository.save(dispatcherEntity);
        return dispatcherEntity;
    }

    public static Optional<DispatcherEntity> editDispatcherEntity(
            DispatcherRepository dispatcherRepository,
            Integer id,
            Integer setid,
            String destination,
            Integer flags,
            Integer priority,
            String attrs,
            String description
    ) {
        destination = destination.strip();

        Optional<DispatcherEntity> optionalDispatcherEntity = dispatcherRepository.findById(id);
        if (optionalDispatcherEntity.isEmpty()) return Optional.empty();

//        if (dispatcherRepository.findByDestination(destination).isPresent() && !Objects.equals(dispatcherRepository.findByDestination(destination).get().getId(), id)) {
//            throw new RuntimeException("Запись с таким destination уже существует");
//        }

        DispatcherEntity dispatcherEntity = optionalDispatcherEntity.get();

        dispatcherEntity.setSetid(setid);
        dispatcherEntity.setDestination(destination);
        dispatcherEntity.setFlags(flags);
        dispatcherEntity.setPriority(priority);
        dispatcherEntity.setAttrs(attrs);
        dispatcherEntity.setDescription(description);

        dispatcherEntity = dispatcherRepository.save(dispatcherEntity);
        return Optional.of(dispatcherEntity);
    }

    public static void deleteDispatcherEntity(
            DispatcherRepository dispatcherRepository,
            Integer id
    ){
        Optional<DispatcherEntity> optionalDispatcherEntity = dispatcherRepository.findById(id);
        if (optionalDispatcherEntity.isPresent()) dispatcherRepository.deleteById(id);
    }
}
