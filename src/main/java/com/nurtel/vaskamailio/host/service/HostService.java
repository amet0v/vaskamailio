package com.nurtel.vaskamailio.host.service;

import com.nurtel.vaskamailio.host.entity.HostEntity;
import com.nurtel.vaskamailio.host.repository.HostRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HostService {
    public static HostEntity createHost(
            HostRepository hostRepository,
            String ip,
            Integer isAllow,
            String description
    ) {
        HostEntity host = HostEntity.builder()
                .ip(ip)
                .keyType(0)
                .valueType(1)
                .isActive(String.valueOf(isAllow))
                .description(description)
                .build();

        host = hostRepository.save(host);
        return host;
    }

    public static Optional<HostEntity> editHost(
            HostRepository hostRepository,
            Long id,
            String ip,
            Integer isAllow,
            String description
    ) {
        Optional<HostEntity> optionalHost = hostRepository.findById(id);
        if (optionalHost.isEmpty()) return Optional.empty();
        HostEntity host = optionalHost.get();

        host.setIp(ip);
        host.setKeyType(0);
        host.setValueType(1);
        host.setIsActive(String.valueOf(isAllow));
        host.setDescription(description);

        host = hostRepository.save(host);
        return Optional.of(host);
    }

    public static void deleteHost(
            HostRepository hostRepository,
            Long id
    ) {
        Optional<HostEntity> optionalHost = hostRepository.findById(id);
        if (optionalHost.isPresent()) hostRepository.deleteById(id);
    }

    public static String getIpFromDestination(String destination) {
        String[] parts = destination.split(":");
        if (parts.length >= 3) {
            return parts[1];
        } else {
            return destination;
        }
    }

    public static HostEntity getHostByIp(HostRepository hostRepository, String ip) {
        List<Optional<HostEntity>> optionalHostList = hostRepository.findByIp(ip);

        if (optionalHostList.isEmpty()) {
            throw new RuntimeException(String.format("Хост с айпи %s не найден в ht_hosts", ip));
        }

        return optionalHostList.getFirst()
                .orElseThrow(() -> new RuntimeException(
                        String.format("Хост с айпи %s не найден в ht_hosts", ip)
                ));
    }
}