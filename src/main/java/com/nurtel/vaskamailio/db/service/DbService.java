package com.nurtel.vaskamailio.db.service;

import com.nurtel.vaskamailio.db.entity.DbEntity;
import com.nurtel.vaskamailio.db.repository.DbRepository;
import javassist.NotFoundException;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.Optional;

@Service
public class DbService {
    public DbEntity createDb(
            DbRepository dbRepository,
            String ip,
            String name,
            String mscSocket,
            String asteriskSocket,
            String login,
            String password
    ) {
        DbEntity db = DbEntity.builder()
                .ip(ip)
                .name(name)
                .mscSocket(mscSocket)
                .asteriskSocket(asteriskSocket)
                .login(login)
                .password(password)
                .build();

        db = dbRepository.save(db);
        return db;
    }

    public DbEntity editDb(
            DbRepository dbRepository,
            Long id,
            String ip,
            String name,
            String mscSocket,
            String asteriskSocket,
            String login,
            String password
    ) throws NotFoundException {
        Optional<DbEntity> optionalDb = dbRepository.findById(id);
        if (optionalDb.isEmpty()) throw new NotFoundException("Запись не найдена");
        DbEntity db = optionalDb.get();

        db.setIp(ip);
        db.setName(name);
        db.setMscSocket(mscSocket);
        db.setAsteriskSocket(asteriskSocket);
        db.setLogin(login);
        db.setPassword(password);

        db = dbRepository.save(db);
        return db;
    }

    public void deleteDb(DbRepository dbRepository, Long id) {
        dbRepository.deleteById(id);
    }

    public void copyDb(DataSource db1, DataSource db2) {
        return;
    }
}
