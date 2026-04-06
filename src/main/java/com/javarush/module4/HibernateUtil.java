package com.javarush.module4;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    // SessionFactory — тяжёлый объект, создаётся один раз при старте приложения
    private static final SessionFactory SESSION_FACTORY;

    static {
        try {
            // Читает hibernate.cfg.xml из classpath и строит SessionFactory
            SESSION_FACTORY = new Configuration()
                    .configure()
                    .buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Ошибка создания SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    // Метод для получения SessionFactory в других классах
    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

    // Вызывается при завершении приложения для освобождения ресурсов
    public static void shutdown() {
        SESSION_FACTORY.close();
    }
}