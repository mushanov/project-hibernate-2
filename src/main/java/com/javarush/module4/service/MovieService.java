package com.javarush.module4.service;

import com.javarush.module4.HibernateUtil;
import com.javarush.module4.entity.*;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class MovieService {

    // ---------------------------------------------------------------
    // Задание 2: Создать нового покупателя со всеми зависимыми полями
    // ---------------------------------------------------------------
    public void createCustomer(String firstName, String lastName, String email,
                               Byte storeId, String addressStr, String district,
                               Short cityId, String phone) {

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                // Шаг 1 — создаём адрес (он нужен покупателю)
                Address address = new Address();
                address.setAddress(addressStr);
                address.setDistrict(district);
                address.setCity(session.get(City.class, cityId));
                address.setPhone(phone);
                address.setLastUpdate(LocalDateTime.now());
                session.persist(address);
                // После persist() у address появился address_id из БД

                // Шаг 2 — создаём покупателя и привязываем адрес
                Customer customer = new Customer();
                customer.setFirstName(firstName);
                customer.setLastName(lastName);
                customer.setEmail(email);
                customer.setAddress(address);
                customer.setStore(session.get(Store.class, storeId));
                customer.setActive(true);
                customer.setCreateDate(LocalDateTime.now());
                customer.setLastUpdate(LocalDateTime.now());
                session.persist(customer);

                tx.commit();
                System.out.println("Покупатель создан, id = " + customer.getId());

            } catch (Exception e) {
                // Если что-то пошло не так — откатываем ОБА insert.
                // Без транзакции address остался бы в БД без покупателя.
                tx.rollback();
                throw e;
            }
        }
    }

    // ---------------------------------------------------------------
    // Задание 3: Покупатель вернул ранее арендованный фильм
    // ---------------------------------------------------------------
    public void returnFilm(Integer rentalId) {

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Rental rental = session.get(Rental.class, rentalId);

                if (rental == null) {
                    throw new RuntimeException("Аренда не найдена: " + rentalId);
                }
                if (rental.getReturnDate() != null) {
                    throw new RuntimeException("Фильм уже был возвращён");
                }

                // Фиксируем дату и время возврата
                rental.setReturnDate(LocalDateTime.now());
                rental.setLastUpdate(LocalDateTime.now());
                // merge() сохраняет изменения существующего объекта в БД
                session.merge(rental);

                tx.commit();
                System.out.println("Фильм возвращён, rental_id = " + rentalId);

            } catch (Exception e) {
                tx.rollback();
                throw e;
            }
        }
    }

    // ---------------------------------------------------------------
    // Задание 4: Покупатель пришёл в магазин, арендовал инвентарь и оплатил
    // ---------------------------------------------------------------
    public void rentInventory(Short customerId, Integer inventoryId,
                              Byte staffId, BigDecimal amount) {

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Customer customer = session.get(Customer.class, customerId);
                Inventory inventory = session.get(Inventory.class, inventoryId);
                Staff staff = session.get(Staff.class, staffId);

                // Проверяем доступность инвентаря:
                // активная аренда = есть запись в rental с return_date IS NULL
                Long activeRentals = session.createQuery(
                                "SELECT COUNT(r) FROM Rental r " +
                                        "WHERE r.inventory = :inv AND r.returnDate IS NULL",
                                Long.class)
                        .setParameter("inv", inventory)
                        .getSingleResult();

                if (activeRentals > 0) {
                    throw new RuntimeException(
                            "Инвентарь уже арендован: " + inventoryId);
                }

                // Шаг 1 — создаём запись об аренде
                Rental rental = new Rental();
                rental.setRentalDate(LocalDateTime.now());
                rental.setInventory(inventory);
                rental.setCustomer(customer);
                rental.setStaff(staff);
                rental.setLastUpdate(LocalDateTime.now());
                session.persist(rental);

                // Шаг 2 — создаём платёж, привязанный к аренде
                Payment payment = new Payment();
                payment.setCustomer(customer);
                payment.setStaff(staff);
                payment.setRental(rental);
                payment.setAmount(amount);
                payment.setPaymentDate(LocalDateTime.now());
                payment.setLastUpdate(LocalDateTime.now());
                session.persist(payment);

                // Оба объекта сохраняются вместе — если один упадёт,
                // второй тоже не запишется (транзакция откатится)
                tx.commit();
                System.out.println("Аренда создана, rental_id = " + rental.getId()
                        + ", payment_id = " + payment.getId());

            } catch (Exception e) {
                tx.rollback();
                throw e;
            }
        }
    }

    // ---------------------------------------------------------------
    // Задание 5: Сняли новый фильм — добавляем его и делаем доступным
    // ---------------------------------------------------------------
    public void addNewFilm(String title, String description, Short releaseYear,
                           Byte languageId, List<Byte> categoryIds,
                           List<Short> actorIds, Byte storeId) {

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                // Шаг 1 — создаём запись Film
                Film film = new Film();
                film.setTitle(title);
                film.setDescription(description);
                film.setReleaseYear(releaseYear);
                film.setLanguage(session.get(Language.class, languageId));
                film.setRentalDuration((byte) 3);
                film.setRentalRate(new BigDecimal("4.99"));
                film.setReplacementCost(new BigDecimal("19.99"));
                film.setRating("G");
                film.setLastUpdate(LocalDateTime.now());

                // Подгружаем актёров и категории по их id
                List<Actor> actors = actorIds.stream()
                        .map(id -> session.get(Actor.class, id))
                        .collect(Collectors.toList());
                List<Category> categories = categoryIds.stream()
                        .map(id -> session.get(Category.class, id))
                        .collect(Collectors.toList());
                film.setActors(actors);
                film.setCategories(categories);

                session.persist(film);
                // После persist() у film появился film_id — он нужен для FilmText

                // Шаг 2 — создаём FilmText
                // @MapsId означает: id FilmText = id Film
                FilmText filmText = new FilmText();
                filmText.setFilm(film);
                filmText.setTitle(title);
                filmText.setDescription(description);
                session.persist(filmText);

                // Шаг 3 — добавляем инвентарь (один экземпляр в магазин)
                Inventory inventory = new Inventory();
                inventory.setFilm(film);
                inventory.setStore(session.get(Store.class, storeId));
                inventory.setLastUpdate(LocalDateTime.now());
                session.persist(inventory);

                tx.commit();
                System.out.println("Фильм добавлен, film_id = " + film.getId()
                        + ", inventory_id = " + inventory.getId());

            } catch (Exception e) {
                tx.rollback();
                throw e;
            }
        }
    }
}