package com.javarush.module4;

import com.javarush.module4.service.MovieService;
import java.math.BigDecimal;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        // Пробуем получить один фильм из БД
        /*try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Film film = session.get(Film.class, (short) 1);
            System.out.println("Фильм: " + film.getTitle());
        }*/

        MovieService service = new MovieService();

        // Тест 1: Создаём нового покупателя
        // storeId=1, cityId=300 (Alberta) — данные из дампа
        service.createCustomer(
                "IVAN", "PETROV",
                "ivan.petrov@example.com",
                (byte) 1,           // store_id
                "123 Main Street",  // адрес
                "Alberta",          // район
                (short) 300,        // city_id
                "555-0001"          // телефон
        );

        // Тест 2: Покупатель с customer_id=1 возвращает аренду rental_id=1
        //service.returnFilm(1);
        service.returnFilm(11496);

        // Тест 3: Покупатель customer_id=2 арендует inventory_id=10
        //         у сотрудника staff_id=1, платит 4.99
        service.rentInventory(
                (short) 2,
                10,
                (byte) 1,
                new BigDecimal("4.99")
        );

        // Тест 4: Добавляем новый фильм
        // language_id=1 (English), категории: 1=Action, 14=Sci-Fi
        // актёры: id=1, id=2, магазин: store_id=1
        service.addNewFilm(
                "Inception 2",
                "A mind-bending sequel about dreams within dreams",
                (short) 2024,
                (byte) 1,
                List.of((byte) 1, (byte) 14),
                List.of((short) 1, (short) 2),
                (byte) 1
        );

        HibernateUtil.shutdown();
    }
}