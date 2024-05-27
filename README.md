# Donation Management WEB-Application

Веб-приложение на основе Spring MVC для управления пожертвованиями через DonationAlerts. Оно позволяет получать пожертвования в реальном времени, отображать историю донатов, создавать заметки, составлять топ донатеров и устанавливать статусы для донатеров. Приложение также поддерживает конвертацию валют.

## Функциональные возможности

- Регистрация и авторизация пользователей
- Получение пожертвований в реальном времени с использованием DonationAlerts API
- Отображение и управление историей донатов
- Создание и управление заметками, связанными с пожертвованиями
- Составление топа донатеров
- Установка статусов для донатеров
- Конвертация валют с использованием Exchange Rate API

## Технологии

- **Spring MVC**: Архитектура Model-View-Controller
- **Thymeleaf**: Шаблонизатор для создания динамических веб-страниц
- **Bootstrap**: Фреймворк для создания адаптивного интерфейса
- **DonationAlerts API**: API для получения информации о пожертвованиях
- **Exchange Rate API**: Сервис для конвертации валют
- **MS SQL**: Реляционная база данных для хранения информации
- **Redis**: База данных ключ-значение для кэширования курсов валют

## Установка

1. Клонируйте репозиторий:
    ```bash
    git clone https://github.com/your-username/donation-management-app.git
    ```
2. Настройте файл `application.properties` в каталоге `src/main/resources`:
    ```properties
    spring.datasource.url=jdbc:sqlserver://your-database-url;databaseName=your-database-name
    spring.datasource.username=your-database-username
    spring.datasource.password=your-database-password
    spring.jpa.hibernate.ddl-auto=update

    spring.redis.host=your-redis-host
    spring.redis.port=your-redis-port
    ```
    
## Лицензия

Этот проект лицензирован под MIT License - подробности см. в файле [MIT License.txt](MIT License.txt).

## Контакты

Если у вас есть вопросы или предложения, пожалуйста, свяжитесь со мной по электронной почте: [kazankovruslankrk@gmail.com](mailto:kazankovruslankrk@gmail.com).
