package com.example.bookstore.util;

import com.example.bookstore.entity.User;
import lombok.Data;

public class AuthContext {

    private static final ThreadLocal<User> USER_CONTEXT = new ThreadLocal<>();

    private AuthContext() {}

    public static void setCurrentUser(User user) {
        USER_CONTEXT.set(user);
    }

    public static User getCurrentUser() {
        return USER_CONTEXT.get();
    }

    public static Long getCurrentUserId() {
        User user = USER_CONTEXT.get();
        return user != null ? user.getId() : null;
    }

    public static String getCurrentUserRole() {
        User user = USER_CONTEXT.get();
        return user != null ? user.getRole() : null;
    }

    public static void remove() {
        USER_CONTEXT.remove();
    }
}