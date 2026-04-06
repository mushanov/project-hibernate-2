# Hibernate Movie Project

Проект по теме Hibernate #2. Маппинг Entity-классов на схему БД movie.

## Технологии
- Java 17
- Hibernate 6.4
- MySQL 8.0
- P6Spy (логирование SQL)
- Lombok
- Maven

## Запуск
1. Создать схему movie в MySQL
2. Импортировать dump-hibernate-2.sql
3. Указать пароль в hibernate.cfg.xml
4. Запустить Main.java

## Проблемы в структуре БД

### Проблема 1 — film_text.film_id не имеет Foreign Key

Поле film_id в таблице film_text является первичным ключом,
но не имеет ограничения FOREIGN KEY на таблицу film.

Последствия:
- можно вставить запись в film_text с несуществующим film_id
- можно удалить фильм из film, оставив "висячий" текст в film_text

Исправление:
ALTER TABLE film_text
MODIFY COLUMN film_id smallint unsigned NOT NULL,
ADD CONSTRAINT fk_film_text_film
FOREIGN KEY (film_id) REFERENCES film(film_id)
ON DELETE CASCADE ON UPDATE CASCADE;

### Проблема 2 — Несоответствие типов film_text.film_id

В таблице film поле film_id имеет тип smallint unsigned,
а в film_text — просто smallint (без unsigned).
MySQL не позволяет создать FK между разными типами —
это и есть причина отсутствия внешнего ключа.

Исправление:
ALTER TABLE film_text
MODIFY COLUMN film_id smallint unsigned NOT NULL;

### Проблема 3 — payment.rental_id не имеет Foreign Key

Поле rental_id в таблице payment не имеет ограничения FOREIGN KEY
на таблицу rental.

Последствия:
- можно создать платёж со ссылкой на несуществующую аренду
- при удалении аренды платёж останется с "мёртвой" ссылкой

Исправление:
ALTER TABLE payment
ADD CONSTRAINT fk_payment_rental
FOREIGN KEY (rental_id) REFERENCES rental(rental_id)
ON DELETE SET NULL ON UPDATE CASCADE;

### Проблема 4 — address.address2 без чёткой семантики

Поле address2 в части записей содержит пустую строку,
в других — NULL. Нет документации о назначении поля.

Предложение: переименовать в address_line2 и добавить комментарий,
либо удалить если поле нигде не используется.