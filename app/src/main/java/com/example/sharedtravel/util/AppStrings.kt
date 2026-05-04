package com.example.sharedtravel.util

enum class StringKey {
    // Navigation
    NAV_FIND, NAV_OFFER, NAV_REQUESTS, NAV_BOOKINGS, NAV_PROFILE,
    
    // Auth
    LOGIN_TITLE, REGISTER_TITLE, EMAIL_LABEL, PASSWORD_LABEL, LOGIN_BUTTON,
    NO_ACCOUNT_TEXT, ALREADY_ACCOUNT_TEXT, LOGIN_SUCCESS, REGISTER_SUCCESS, LOGOUT_BUTTON,
    
    // Offer Ride
    OFFER_RIDE_TITLE, FROM_ORIGIN_LABEL, TO_DESTINATION_LABEL, DATE_LABEL, TIME_LABEL,
    PRICE_LABEL, SEATS_LABEL, PUBLISH_RIDE_BUTTON, RIDE_PUBLISHED_SUCCESS, FILL_ALL_FIELDS_ERROR,
    
    // Find Ride
    FIND_RIDE_TITLE, NO_RIDES_FOUND, CONFIRM_BOOKING_TITLE, CONFIRM_BOOKING_TEXT,
    CONFIRM_BUTTON, CANCEL_BUTTON, BOOK_NOW_BUTTON, YOUR_TRIP_LABEL, SEATS_LEFT_FORMAT,
    PER_SEAT_LABEL, DRIVING_SINCE_FORMAT, BOOKING_SUCCESS,
    
    // Requests / Dashboard
    DRIVER_DASHBOARD_TITLE, ACTIVE_TRIPS_LABEL, NO_TRIPS_CREATED, FINISH_TRIP_BUTTON,
    BOOKING_REQUESTS_LABEL, NO_PENDING_REQUESTS, PASSENGER_ID_LABEL, SEATS_REQUESTED_LABEL,
    STATUS_LABEL, APPROVE_BUTTON, REJECT_BUTTON,
    
    // Bookings
    MY_BOOKINGS_TITLE, NO_BOOKINGS_YET, RATE_DRIVER_BUTTON, ALREADY_RATED_LABEL,
    RATING_DIALOG_TITLE, RATING_DIALOG_TEXT, SUBMIT_BUTTON, RATING_SUCCESS,
    
    // Profile
    MY_PROFILE_TITLE, FIRST_NAME_LABEL, LAST_NAME_LABEL, DRIVING_SINCE_YEAR_LABEL,
    SAVE_PROFILE_BUTTON, PROFILE_SAVED_SUCCESS, NAMES_BLANK_ERROR, REVIEWS_COUNT_FORMAT,
    
    // Statuses
    STATUS_PENDING, STATUS_CONFIRMED, STATUS_REJECTED, STATUS_CANCELLED,
    STATUS_SCHEDULED, STATUS_COMPLETED,
    
    // Misc
    LANGUAGE_LABEL, TRIP_LABEL
}

object AppStrings {
    private val english = mapOf(
        StringKey.NAV_FIND to "Find",
        StringKey.NAV_OFFER to "Offer",
        StringKey.NAV_REQUESTS to "Requests",
        StringKey.NAV_BOOKINGS to "Bookings",
        StringKey.NAV_PROFILE to "Profile",
        StringKey.LOGIN_TITLE to "Login",
        StringKey.REGISTER_TITLE to "Register",
        StringKey.EMAIL_LABEL to "Email",
        StringKey.PASSWORD_LABEL to "Password",
        StringKey.LOGIN_BUTTON to "Login",
        StringKey.NO_ACCOUNT_TEXT to "Don't have an account? Register",
        StringKey.ALREADY_ACCOUNT_TEXT to "Already have an account? Login",
        StringKey.LOGIN_SUCCESS to "Login Successful!",
        StringKey.REGISTER_SUCCESS to "Registration Successful!",
        StringKey.LOGOUT_BUTTON to "Sign Out",
        StringKey.OFFER_RIDE_TITLE to "Offer a Ride",
        StringKey.FROM_ORIGIN_LABEL to "From (Origin)",
        StringKey.TO_DESTINATION_LABEL to "To (Destination)",
        StringKey.DATE_LABEL to "Date",
        StringKey.TIME_LABEL to "Time",
        StringKey.PRICE_LABEL to "Price",
        StringKey.SEATS_LABEL to "Seats",
        StringKey.PUBLISH_RIDE_BUTTON to "Publish Ride",
        StringKey.RIDE_PUBLISHED_SUCCESS to "Ride Published Successfully!",
        StringKey.FILL_ALL_FIELDS_ERROR to "Please fill all fields",
        StringKey.FIND_RIDE_TITLE to "Find a Ride",
        StringKey.NO_RIDES_FOUND to "No rides found for this route.",
        StringKey.CONFIRM_BOOKING_TITLE to "Confirm Booking",
        StringKey.CONFIRM_BOOKING_TEXT to "Are you sure you want to book a seat for the ride from %s to %s?",
        StringKey.CONFIRM_BUTTON to "Confirm",
        StringKey.CANCEL_BUTTON to "Cancel",
        StringKey.BOOK_NOW_BUTTON to "Book Now",
        StringKey.YOUR_TRIP_LABEL to "Your Trip",
        StringKey.SEATS_LEFT_FORMAT to "%d seats left",
        StringKey.PER_SEAT_LABEL to "per seat",
        StringKey.DRIVING_SINCE_FORMAT to "Driving since: %s",
        StringKey.BOOKING_SUCCESS to "Seat booked successfully!",
        StringKey.DRIVER_DASHBOARD_TITLE to "Driver Dashboard",
        StringKey.ACTIVE_TRIPS_LABEL to "Active Trips",
        StringKey.NO_TRIPS_CREATED to "No trips created yet.",
        StringKey.FINISH_TRIP_BUTTON to "Finish Trip",
        StringKey.BOOKING_REQUESTS_LABEL to "Booking Requests",
        StringKey.NO_PENDING_REQUESTS to "No pending requests",
        StringKey.PASSENGER_ID_LABEL to "Passenger ID: %s",
        StringKey.SEATS_REQUESTED_LABEL to "Seats requested: %d",
        StringKey.STATUS_LABEL to "Status: %s",
        StringKey.APPROVE_BUTTON to "Approve",
        StringKey.REJECT_BUTTON to "Reject",
        StringKey.MY_BOOKINGS_TITLE to "My Bookings",
        StringKey.NO_BOOKINGS_YET to "You haven't booked any rides yet",
        StringKey.RATE_DRIVER_BUTTON to "Rate Driver",
        StringKey.ALREADY_RATED_LABEL to "Already Rated",
        StringKey.RATING_DIALOG_TITLE to "Rate Driver",
        StringKey.RATING_DIALOG_TEXT to "How was your trip?",
        StringKey.SUBMIT_BUTTON to "Submit",
        StringKey.RATING_SUCCESS to "Thank you for your rating!",
        StringKey.MY_PROFILE_TITLE to "My Profile",
        StringKey.FIRST_NAME_LABEL to "First Name",
        StringKey.LAST_NAME_LABEL to "Last Name",
        StringKey.DRIVING_SINCE_YEAR_LABEL to "Driving Since (Year)",
        StringKey.SAVE_PROFILE_BUTTON to "Save Profile",
        StringKey.PROFILE_SAVED_SUCCESS to "Profile saved successfully!",
        StringKey.NAMES_BLANK_ERROR to "Names cannot be blank",
        StringKey.REVIEWS_COUNT_FORMAT to "(%d reviews)",
        StringKey.STATUS_PENDING to "PENDING",
        StringKey.STATUS_CONFIRMED to "CONFIRMED",
        StringKey.STATUS_REJECTED to "REJECTED",
        StringKey.STATUS_CANCELLED to "CANCELLED",
        StringKey.STATUS_SCHEDULED to "SCHEDULED",
        StringKey.STATUS_COMPLETED to "COMPLETED",
        StringKey.LANGUAGE_LABEL to "Language / Език",
        StringKey.TRIP_LABEL to "Trip: %s -> %s"
    )

    private val bulgarian = mapOf(
        StringKey.NAV_FIND to "Търсене",
        StringKey.NAV_OFFER to "Предлагане",
        StringKey.NAV_REQUESTS to "Заявки",
        StringKey.NAV_BOOKINGS to "Резервации",
        StringKey.NAV_PROFILE to "Профил",
        StringKey.LOGIN_TITLE to "Вход",
        StringKey.REGISTER_TITLE to "Регистрация",
        StringKey.EMAIL_LABEL to "Имейл",
        StringKey.PASSWORD_LABEL to "Парола",
        StringKey.LOGIN_BUTTON to "Влез",
        StringKey.NO_ACCOUNT_TEXT to "Нямате акаунт? Регистрирайте се",
        StringKey.ALREADY_ACCOUNT_TEXT to "Вече имате акаунт? Влезте",
        StringKey.LOGIN_SUCCESS to "Успешен вход!",
        StringKey.REGISTER_SUCCESS to "Успешна регистрация!",
        StringKey.LOGOUT_BUTTON to "Изход",
        StringKey.OFFER_RIDE_TITLE to "Предложи Пътуване",
        StringKey.FROM_ORIGIN_LABEL to "От (Начална точка)",
        StringKey.TO_DESTINATION_LABEL to "До (Крайна точка)",
        StringKey.DATE_LABEL to "Дата",
        StringKey.TIME_LABEL to "Час",
        StringKey.PRICE_LABEL to "Цена",
        StringKey.SEATS_LABEL to "Места",
        StringKey.PUBLISH_RIDE_BUTTON to "Публикувай Пътуване",
        StringKey.RIDE_PUBLISHED_SUCCESS to "Пътуването е публикувано успешно!",
        StringKey.FILL_ALL_FIELDS_ERROR to "Моля, попълнете всички полета",
        StringKey.FIND_RIDE_TITLE to "Намери Пътуване",
        StringKey.NO_RIDES_FOUND to "Няма намерени пътувания за този маршрут.",
        StringKey.CONFIRM_BOOKING_TITLE to "Потвърди Резервация",
        StringKey.CONFIRM_BOOKING_TEXT to "Сигурни ли сте, че искате да резервирате място за пътуването от %s до %s?",
        StringKey.CONFIRM_BUTTON to "Потвърди",
        StringKey.CANCEL_BUTTON to "Отказ",
        StringKey.BOOK_NOW_BUTTON to "Резервирай",
        StringKey.YOUR_TRIP_LABEL to "Твое Пътуване",
        StringKey.SEATS_LEFT_FORMAT to "остават %d места",
        StringKey.PER_SEAT_LABEL to "на място",
        StringKey.DRIVING_SINCE_FORMAT to "Шофира от: %s",
        StringKey.BOOKING_SUCCESS to "Мястото е резервирано успешно!",
        StringKey.DRIVER_DASHBOARD_TITLE to "Табло на Шофьора",
        StringKey.ACTIVE_TRIPS_LABEL to "Активни Пътувания",
        StringKey.NO_TRIPS_CREATED to "Все още няма създадени пътувания.",
        StringKey.FINISH_TRIP_BUTTON to "Завърши Пътуването",
        StringKey.BOOKING_REQUESTS_LABEL to "Заявки за Резервация",
        StringKey.NO_PENDING_REQUESTS to "Няма чакащи заявки",
        StringKey.PASSENGER_ID_LABEL to "ID на Пътник: %s",
        StringKey.SEATS_REQUESTED_LABEL to "Заявени места: %d",
        StringKey.STATUS_LABEL to "Статус: %s",
        StringKey.APPROVE_BUTTON to "Одобри",
        StringKey.REJECT_BUTTON to "Откажи",
        StringKey.MY_BOOKINGS_TITLE to "Моите Резервации",
        StringKey.NO_BOOKINGS_YET to "Все още не сте резервирали пътувания",
        StringKey.RATE_DRIVER_BUTTON to "Оцени Шофьора",
        StringKey.ALREADY_RATED_LABEL to "Вече е оценено",
        StringKey.RATING_DIALOG_TITLE to "Оцени Шофьора",
        StringKey.RATING_DIALOG_TEXT to "Как беше твоето пътуване?",
        StringKey.SUBMIT_BUTTON to "Изпрати",
        StringKey.RATING_SUCCESS to "Благодарим за вашата оценка!",
        StringKey.MY_PROFILE_TITLE to "Моят Профил",
        StringKey.FIRST_NAME_LABEL to "Име",
        StringKey.LAST_NAME_LABEL to "Фамилия",
        StringKey.DRIVING_SINCE_YEAR_LABEL to "Шофира от (Година)",
        StringKey.SAVE_PROFILE_BUTTON to "Запази Профила",
        StringKey.PROFILE_SAVED_SUCCESS to "Профилът е запазен успешно!",
        StringKey.NAMES_BLANK_ERROR to "Имената не могат да бъдат празни",
        StringKey.REVIEWS_COUNT_FORMAT to "(%d отзива)",
        StringKey.STATUS_PENDING to "ЧАКАЩО",
        StringKey.STATUS_CONFIRMED to "ПОТВЪРДЕНО",
        StringKey.STATUS_REJECTED to "ОТКАЗАНО",
        StringKey.STATUS_CANCELLED to "ОТМЕНЕНО",
        StringKey.STATUS_SCHEDULED to "ПЛАНИРАНО",
        StringKey.STATUS_COMPLETED to "ЗАВЪРШЕНО",
        StringKey.LANGUAGE_LABEL to "Language / Език",
        StringKey.TRIP_LABEL to "Пътуване: %s -> %s"
    )

    fun get(key: StringKey, lang: String): String {
        return when (lang) {
            "bg" -> bulgarian[key] ?: english[key] ?: ""
            else -> english[key] ?: ""
        }
    }
}
