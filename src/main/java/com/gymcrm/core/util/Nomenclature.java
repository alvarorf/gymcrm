package com.gymcrm.core.util;

import org.slf4j.Logger;

public final class Nomenclature {

    private Nomenclature() {}

    // --- Action keys
    public enum Action {
        CREATE, UPDATE, DELETE, FETCH, AUTH, INITIALIZE, TOGGLE, UPDATE_SENSITIVE, SEED
    }

    // --- Message templates (centralized) ---
    private static final String OP_START = "[{}] Attempting to {} {}";
    private static final String OP_SUCCESS = "[{}] Successfully {} {}";
    private static final String CONSTRUCTOR_LOG = "[{}] Context initialized for {}";

    // --- Dictionary (Easily changeable/translatable) ---
    private static String getActionText(Action action, String methodName, Object... context) {
        return switch (action) {
            case CREATE -> "create";
            case UPDATE -> "update";
            case DELETE -> "delete";
            case SEED -> "seed database with initial data";
            case FETCH -> {
                if (methodName.startsWith("findAll") || (context.length > 0 && "ALL".equals(context[0]))) {
                    yield "retrieve all records for";
                }
                yield "retrieve context for " + (context.length > 0 ? context[0] : "record");
            }
            case AUTH -> "authenticate";
            case INITIALIZE -> "initialize";
            case TOGGLE -> "change status to " + (context.length > 0 ? context[0] : "alternate");
            case UPDATE_SENSITIVE -> "update sensitive data (password) for " + (context.length > 0 ? context[0] : "user");
        };
    }

    /**
     * Centralized Validation Messages
     * These must be 'public static final String' to be used in Annotations.
     */
    public static final class REQD {
        public static final String FIRST_NAME = "First Name is required";
        public static final String LAST_NAME = "Last Name is required";
        public static final String USERNAME = "Username is required";
        public static final String TRAINEE_USERNAME = "Trainee username is required";
        public static final String TRAINER_USERNAME = "Trainer username is required";
        public static final String PASSWORD = "Password is required";
        public static final String OLD_PASSWORD = "Old password is required";
        public static final String NEW_PASSWORD = "New password is required";
        public static final String IS_ACTIVE = "Activation status is required";
        public static final String SPECIALIZATION = "Specialization is required";
        public static final String TRAINING_TYPE = "Training Type name is required";
        public static final String TRAINING_NAME = "Training Name is required";
        public static final String TRAINING_DATE = "Training Date is required";
        public static final String TRAINING_DURATION = "Training Duration is required";
    }

    public static final class ERR {
        // Module & Field Headers
        public static final String MODULE_TRAINEE = "TRAINEE_SERVICE";
        public static final String MODULE_AUTH = "AUTH_SERVICE";
        public static final String KEY_DETAILS = "details";
        public static final String KEY_SUGGESTION = "suggestion";
        public static final String KEY_MESSAGE = "message";
        public static final String KEY_ERR_TYPE = "error_type";

        // Error Types
        public static final String TYPE_INTERNAL = "Internal Server Error";
        public static final String TYPE_REG_FAILED = "Registration Failed";
        public static final String TYPE_UPDATE_REFUSED = "Update Refused";
        public static final String TYPE_TOGGLE_FAILED = "Activation Toggle Failed";
        public static final String TYPE_NOT_FOUND = "Profile Not Found";
        public static final String TYPE_DEL_FAILED = "Deletion Impossible";

        // Auth Error Types
        public static final String TYPE_AUTH_FAILED = "Authentication Denied";
        public static final String TYPE_CREDENTIALS_INVALID = "Invalid Credentials";
        public static final String TYPE_USER_NOT_FOUND = "User Identity Unknown";
        public static final String TYPE_SAME_PASSWORD = "Password Change Rejected";

        // Detail Templates
        public static final String DETAIL_SAME_PASSWORD = "New password cannot be the same as the old password.";
        public static final String SUGGESTION_LOGIN = "Please check your username and password and try again.";

        // Detail Templates
        public static final String DETAIL_REG_MISSING = "Mandatory fields: %s and %s are missing or empty.";
        public static final String DETAIL_UPDATE_REQD = "Updating a trainee requires: %s and %s.";
        public static final String DETAIL_DEL_PREFIX = "Cannot delete trainee: ";
        public static final String SUGGESTION_VERIFY_NOMEN = "Please verify the Trainee nomenclature requirements.";
    }

    // --- Validation, response and server error messages ---
    public static final String MSG_LOGIN_SUCCESS = "Login successful";
    public static final String MSG_INVALID_CREDENTIALS = "Invalid credentials";
    public static final String MSG_AUTH_FAILED = "Authentication failed: ";
    public static final String MSG_AUTH_REQUIRED = "Username and password are required.";
    public static final String MSG_PASSWORD_CHANGED = "Password changed successfully";
    public static final String MSG_PASSWORD_CHANGE_FAILED = "Password change failed with errors"; // TODO: Use this in the AuthControllerExceptionHandler, add corresponding unit test (use @DisplayName and ARRANGE/ACT/ASSERT separately)


    // --- Dictionary and Error Messages ---
    public static final String MSG_INTERNAL_ERROR = "An unexpected error occurred. . Please try again later.";
    public static final String MSG_NOT_FOUND = "Record not found for: ";



    // It can handle optional context (IDs, Usernames, Booleans)
    public static void info(Logger logger, Action action, Object... context) {
        if (logger.isInfoEnabled()) {
            StackWalker.StackFrame caller = getCallerFrame();
            String methodName = caller.getMethodName();
            String subject = caller.getDeclaringClass().getSimpleName().replace("ServiceImpl", "").replace("DaoImpl", "").replace("Impl", "");

            if (methodName.equals("<init>")) {
                logger.info(CONSTRUCTOR_LOG, "CONSTRUCTOR", subject);
            } else {
                // Pass methodName to help detect "findAll"
                logger.info(OP_START, methodName, getActionText(action, methodName, context), subject);
            }
        }
    }

    // Overload for success messages where we want to show an ID/Username
    public static void success(Logger logger, Action action, Object identifier) {
        if (logger.isInfoEnabled()) {
            StackWalker.StackFrame caller = getCallerFrame();
            String methodName = caller.getMethodName();
            String subject = caller.getDeclaringClass().getSimpleName()
                    .replace("ServiceImpl", "").replace("DaoImpl", "").replace("Impl", "").replace("PostProcessor", "PostProc");
            // We pass identifier to getActionText as well in case the template needs it
            logger.info(OP_SUCCESS, methodName, getActionText(action, methodName, identifier), subject + " (" + identifier + ")");
        }
    }

    public static void warn(Logger logger, Action action, Object identifier) {
        if (logger.isWarnEnabled()) {
            StackWalker.StackFrame caller = getCallerFrame();
            String methodName = caller.getMethodName();
            String subject = caller.getDeclaringClass().getSimpleName().replace("ServiceImpl", "").replace("DaoImpl", "").replace("Impl", "");

            logger.warn("[{}] {} not found with identifier: {}", methodName, subject, identifier);
        }
    }

    private static StackWalker.StackFrame getCallerFrame() {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(frames -> frames.skip(2).findFirst().orElseThrow());
    }

    // Returns a standard error message for a class type.
    public static String getNotFoundMsg(Class<?> clazz) {
        return clazz.getSimpleName() + " not found";
    }

    public static String getNotFoundMsg(String identifier) { return MSG_NOT_FOUND + identifier;}

}