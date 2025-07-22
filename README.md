# NeoProtect V2

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-Nukkit-brightgreen.svg" alt="Minecraft Nukkit">
  <img src="https://img.shields.io/badge/Версия-2.1.0-blue.svg" alt="Версия">
  <img src="https://img.shields.io/badge/Java-17-orange.svg" alt="Java 17">
</p>

<p align="center">
  <a href="https://t.me/ForgePlugins">
    <img src="https://img.shields.io/badge/Telegram-ForgePlugins-blue?logo=telegram" alt="Telegram">
  </a>
  <a href="https://github.com/SocialMoods/NeoProtectV2">
    <img src="https://img.shields.io/badge/GitHub-Оригинальный_репозиторий-gray?logo=github" alt="Оригинальный репозиторий">
  </a>
  <a href="https://github.com/Miroshka000/NeoProtectV2">
    <img src="https://img.shields.io/badge/GitHub-Форк_репозиторий-gray?logo=github" alt="Форк репозиторий">
  </a>
</p>

## 📋 Описание

**NeoProtect V2** - это мощный плагин для защиты территорий в Minecraft (Nukkit), который позволяет игрокам создавать приваты с помощью специальных блоков и получать уведомления о вторжениях через Telegram. 

> [!NOTE]
> Данный плагин был переработан специально для [ForgePlugins Studio](https://t.me/ForgePlugins). Оригинальный код принадлежит [SocialMoods](https://github.com/SocialMoods/NeoProtectV2).

## ✨ Особенности

- **🏠 Простая защита территорий** - используйте защитные блоки для создания приватов
- **🛡️ Гибкая настройка радиуса защиты** - разные блоки обеспечивают разный радиус защиты
- **👥 Управление доступом** - добавляйте других игроков в свои защищенные области
- **🔄 Передача владения** - возможность передать право собственности на регион другому игроку
- **📱 Интеграция с Telegram** - получайте уведомления о нарушениях в защищенной области
- **🌐 Удобные формы** - интуитивно понятный интерфейс для управления приватами

## 🛠️ Установка

1. Скачайте последнюю версию плагина из [релизов](https://github.com/Miroshka000/NeoProtectV2/releases)
2. Поместите JAR-файл в директорию `plugins` вашего сервера
3. Перезапустите сервер
4. Настройте конфигурацию в файле `plugins/NeoProtect/config.yml`
5. Готово!

## ⚙️ Настройка

### Основные параметры

В файле `config.yml` можно настроить следующие параметры:

- **protection-blocks** - ID блоков защиты и их радиус
- **maximum-protections** - максимальное количество приватов для одного игрока
- **particles-enabled** - включить/отключить частицы, показывающие границы привата

### Telegram-бот

1. Создайте бота через [@BotFather](https://t.me/BotFather)
2. Скопируйте токен бота
3. Вставьте токен и имя пользователя в файл конфигурации:
   ```yaml
   bot:
     token: "ВАШ_ТОКЕН_БОТА"
     username: "ИМЯ_ПОЛЬЗОВАТЕЛЯ_БОТА"
   ```

## 📌 Команды

| Команда | Описание |
|---------|----------|
| `/verify` | Получить код для привязки Telegram-аккаунта |
| `/protect` | Управление регионом, в котором вы находитесь |

## 📝 Разрешения

- `neoprotect.admin` - Доступ к административным функциям плагина
- `neoprotect.bypass` - Разрешение игнорировать защиту и взаимодействовать с чужими регионами

## ❓ Часто задаваемые вопросы

<details>
<summary><b>Как добавить других игроков в свой регион?</b></summary>
<p>
Находясь в своем регионе, используйте команду <code>/protect</code>, затем выберите "Добавить игрока" и введите его имя.
</p>
</details>

<details>
<summary><b>Как получать уведомления в Telegram?</b></summary>
<p>
1. Используйте команду <code>/verify</code> на сервере, чтобы получить код верификации<br>
2. Отправьте полученный код боту в Telegram<br>
3. В настройках региона включите уведомления
</p>
</details>

---

<p align="center">
Разработано с ❤️ <a href="https://github.com/Miroshka000">Miroshka000</a> для <a href="https://t.me/ForgePlugins">ForgePlugins</a>
</p> 