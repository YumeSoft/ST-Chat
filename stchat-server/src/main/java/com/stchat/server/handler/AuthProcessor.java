package com.stchat.server.handler;

import com.stchat.server.dao.PendingPasswordChangeDAO;
import com.stchat.server.dao.UserDAO;
import com.stchat.server.model.User;
import com.stchat.server.service.AuthService;
import com.stchat.server.service.PasswordService;
import org.json.JSONObject;
import com.stchat.server.util.JsonResponseUtil;

public class AuthProcessor {

    public static JSONObject handle(JSONObject request) {
        String type = request.optString("type");
        switch (type.toUpperCase()) {
            case "LOGIN": return handleLogin(request);
            case "REGISTER": return handleRegister(request);
            case "FORGOT_PASSWORD": return handleForgotPassword(request);
            case "CHANGE_PASSWORD": return handleChangePassword(request);
            default: return JsonResponseUtil.error("Unknown auth type.");
        }
    }

    private static JSONObject handleLogin(JSONObject request) {
        String username = request.optString("username");
        String password = request.optString("password");

        if (username.isEmpty() || password.isEmpty()) {
            return JsonResponseUtil.error("Username and password are required.");
        }

        if (AuthService.authenticateUser(username, password)) {
            UserDAO userDAO = new UserDAO();
            User user = userDAO.getUserByUsername(username);

            if (user == null) {
                return JsonResponseUtil.error("User not found.");
            }

            // Convert Java object to JSONObject
            JSONObject userJson = new JSONObject();
            userJson.put("id", user.getId());
            userJson.put("username", user.getUsername());
            userJson.put("email", user.getEmail());
            userJson.put("firstName", user.getFirstName());
            userJson.put("lastName", user.getLastName());
            userJson.put("avatarUrl", user.getAvatarUrl());
            userJson.put("phone", user.getPhone());
            userJson.put("bio", user.getBio());
            userJson.put("status", user.getStatus());
            userJson.put("lastSeen", user.getLastSeen() != null ? user.getLastSeen().toString() : null);
            userJson.put("isActive", user.isActive());
            userJson.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
            userJson.put("updatedAt", user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null);

            userJson.put("lastSeen", user.getLastSeen() != null
                    ? user.getLastSeen().toLocalDateTime().toString()
                    : null);
            userJson.put("createdAt", user.getCreatedAt() != null
                    ? user.getCreatedAt().toLocalDateTime().toString()
                    : null);
            userJson.put("updatedAt", user.getUpdatedAt() != null
                    ? user.getUpdatedAt().toLocalDateTime().toString()
                    : null);

            return new JSONObject()
                    .put("status", "success")
                    .put("message", "Login successful.")
                    .put("user", userJson);
        } else {
            return JsonResponseUtil.error("Invalid username or password.");
        }
    }


    private static JSONObject handleRegister(JSONObject request) {
        String username = request.optString("username");
        String email = request.optString("email");
        String password = request.optString("password");
        String firstName = request.optString("firstname");
        String lastName = request.optString("lastname");


        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            return JsonResponseUtil.error("Please fill all fields.");
        }

        UserDAO userDAO = new UserDAO();
        boolean registered = userDAO.registerUser(username, email, password, firstName, lastName);

        if (registered) {
            return JsonResponseUtil.success("Account created successfully.");
        } else {
            return JsonResponseUtil.error("Username or email already exists.");
        }
    }

    private static JSONObject handleForgotPassword(JSONObject request) {
        String email = request.optString("email");

        if (email.isEmpty()) {
            return JsonResponseUtil.error("Email is required.");
        }

        UserDAO userDAO = new UserDAO();
        String resetMessage = userDAO.resetPassword(email);

        if (resetMessage != null) {
            return JsonResponseUtil.success(resetMessage);
        } else {
            return JsonResponseUtil.error("Email not found.");
        }
    }

    private static JSONObject handleChangePassword(JSONObject request) {
        String email = request.optString("email");
        String currentPassword = request.optString("currentPassword");
        String newPassword = request.optString("newPassword");

        if (email.isEmpty() || currentPassword.isEmpty() || newPassword.isEmpty()) {
            return JsonResponseUtil.error("All fields are required.");
        }

        UserDAO userDAO = new UserDAO();
        PendingPasswordChangeDAO pendingDAO = new PendingPasswordChangeDAO();
        PasswordService passwordService = new PasswordService(userDAO, pendingDAO);

        boolean requestSent = passwordService.requestPasswordChange(email, currentPassword, newPassword);

        if (requestSent) {
            return JsonResponseUtil.success("Please check your email to confirm the password change.");
        } else {
            return JsonResponseUtil.error("Invalid current password or user not found.");
        }
    }

}

