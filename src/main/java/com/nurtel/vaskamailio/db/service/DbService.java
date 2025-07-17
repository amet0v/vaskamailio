package com.nurtel.vaskamailio.db.service;

import com.nurtel.vaskamailio.db.config.DatabaseContextHolder;
import com.nurtel.vaskamailio.db.entity.DbEntity;
import com.nurtel.vaskamailio.db.repository.DbRepository;
import com.nurtel.vaskamailio.dispatcher.entity.DispatcherEntity;
import com.nurtel.vaskamailio.dispatcher.repository.DispatcherRepository;
import javassist.NotFoundException;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class DbService {

    public static DbEntity createDb(
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

    public static DbEntity editDb(
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

    public static void deleteDb(DbRepository dbRepository, Long id) {
        dbRepository.deleteById(id);
    }

    public static String copyDb(String sourceIp, String targetIp) {
        String dbName = "kamailiodb";
        String dbUser = "kamailio";

        String command = String.format(
                "pg_dump --clean --if-exists -h %s -U %s %s | psql -h %s -U %s %s",
                sourceIp, dbUser, dbName, targetIp, dbUser, dbName
        );

        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        pb.inheritIO(); // Показывает stdout и stderr в консоли приложения

        try {
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Ошибка при копировании базы данных, код: " + exitCode);
            }
            System.out.println("База успешно перенесена с " + sourceIp + " на " + targetIp);
            return ("База успешно перенесена с " + sourceIp + " на " + targetIp);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException("Ошибка при выполнении команды", e);
        }
    }

    public static String editAttrs(
            DbRepository dbRepository,
            DispatcherRepository dispatcherRepository,
            String dbIp
    ) throws NotFoundException {
        Optional<DbEntity> optionalDb = dbRepository.findByIp(dbIp);
        DbEntity db = null;

        if (optionalDb.isEmpty()) throw new NotFoundException("Запись не найдена");
        else {
            db = optionalDb.get();
        }

        DatabaseContextHolder.set(db.getName());

        List<DispatcherEntity> dispatcherEntityList = dispatcherRepository.findAll();
        for (DispatcherEntity dispatcherEntity : dispatcherEntityList) {
            if (dispatcherEntity.getDescription().contains("MSC"))
                dispatcherEntity.setAttrs("socket=udp:" + db.getMscSocket() + ":5060");
            else dispatcherEntity.setAttrs("socket=udp:" + db.getAsteriskSocket() + ":5060");
            dispatcherRepository.save(dispatcherEntity);
        }
        return "Аттрибуты dispatcher на сервере " + db.getName() + "успешно изменены";
    }
}
