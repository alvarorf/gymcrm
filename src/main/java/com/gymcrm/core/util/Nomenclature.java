package com.gymcrm.core.util;

import org.slf4j.Logger;

public final class Nomenclature {

    // --- Action keys
    public enum Action {
        CREATE, UPDATE, DELETE, FETCH, AUTH, INITIALIZE, TOGGLE, UPDATE_SENSITIVE, SEED, HEALTH_CHECK, METRICS_INIT
    }

    // --- Message templates (centralized) ---
    private static final String OP_START = "[{}] Attempting to {} {}";
    private static final String OP_SUCCESS = "[{}] Successfully {} {}";
    private static final String CONSTRUCTOR_LOG = "[{}] Context initialized for {}";

    // --- Dictionary ---
    private static String getActionText(Action action, String methodName, Object... context) {
        return switch (action) {
            case AUTH -> "authenticate";
            case CREATE -> "create";
            case DELETE -> "delete";
            case FETCH -> {
                if (methodName.startsWith("findAll") || (context.length > 0 && "ALL".equals(context[0]))) {
                    yield "retrieve all records for";
                }
                yield "retrieve context for " + (context.length > 0 ? context[0] : "record");
            }
            case HEALTH_CHECK -> "verify accessibility of " + (context.length > 0 ? context[0] : "resource");
            case INITIALIZE -> "initialize";
            case METRICS_INIT -> "initialize micrometer registry for environment: " + (context.length > 0 ? context[0] : "unknown");
            case SEED -> "seed database with initial data";
            case TOGGLE -> "change status to " + (context.length > 0 ? context[0] : "alternate");
            case UPDATE -> "update";
            case UPDATE_SENSITIVE -> "update sensitive data (password) for " + (context.length > 0 ? context[0] : "user");
        };
    }

    /**
     * Centralized Validation Messages
     * These must be 'public static final String' to be used in Annotations.
     */
    public static final class REQD {
        private REQD() {}
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
        public static final String TRAINING_NAME = "Training Name is required";
        public static final String TRAINING_DURATION = "Training Duration is required";
    }

    public static final class ERR {
        private ERR() {}
        // Module & field headers
        public static final String MODULE_TRAINING = "TRAINING_SERVICE";
        public static final String TYPE_TRAINING_DENIED = "Training Creation Denied";
        public static final String TYPE_INVALID_FORMAT = "Invalid Data Format";
        public static final String TYPE_PERSISTENCE = "Persistence Error";

        public static final String MODULE_TRAINEE = "TRAINEE_SERVICE";
        public static final String MODULE_AUTH = "AUTH_SERVICE";
        public static final String KEY_DETAILS = "details";
        public static final String KEY_SUGGESTION = "suggestion";
        public static final String KEY_MESSAGE = "message";
        public static final String KEY_ERR_TYPE = "error_type";

        // Error types
        public static final String TYPE_INTERNAL = "Internal Server Error";
        public static final String TYPE_REG_FAILED = "Registration Failed";
        public static final String TYPE_UPDATE_REFUSED = "Update Refused";
        public static final String TYPE_TOGGLE_FAILED = "Activation Toggle Failed";
        public static final String TYPE_NOT_FOUND = "Profile Not Found";
        public static final String TYPE_DEL_FAILED = "Deletion Impossible";
        public static final String TYPE_MALFORMED_JSON = "Malformed JSON";

        // Auth error types
        public static final String TYPE_AUTH_FAILED = "Authentication Denied";
        public static final String TYPE_CREDENTIALS_INVALID = "Invalid Credentials";
        public static final String TYPE_USER_NOT_FOUND = "User Identity Unknown";
        public static final String TYPE_SAME_PASSWORD = "Password Change Rejected";

        // Detail Templates
        public static final String DETAIL_SAME_PASSWORD = "New password cannot be the same as the old password.";
        public static final String DETAIL_TRAINING_MISSING = "Mandatory session details are missing or empty.";
        public static final String DETAIL_INCOMPATIBLE_TYPES = "The request body contains incompatible data types.";
        public static final String DETAIL_REG_MISSING = "Mandatory fields: %s and %s are missing or empty.";
        public static final String DETAIL_UPDATE_REQD = "Updating a trainee requires: %s and %s.";
        public static final String SUGGESTION_VERIFY_NOMEN = "Please verify the Trainee nomenclature requirements.";

        // Suggestions
        public static final String SUGGESTION_TRAINING_REQD = "Ensure 'traineeUsername', 'trainerUsername', and 'trainingName' are provided.";
        public static final String SUGGESTION_FORMAT = "Check if 'trainingDuration' is an Integer and 'trainingDate' follows 'YYYY-MM-DD'.";
        public static final String SUGGESTION_USER_VERIFY = "Verify that both the Trainee and Trainer usernames exist in the database.";

        // Trainer Specific Details/Suggestions
        public static final String DETAIL_TRAINER_SPEC = "Specialization (Training Type) is mandatory for Trainers.";
        public static final String SUGGESTION_TRAINER_SPEC = "Ensure 'specialization' object contains a valid 'trainingTypeName'.";
        public static final String SUGGESTION_TRAINER_SEARCH = "Verify that the trainer username and date range are correct.";

        // Generic technical error
        public static final String KEY_TECHNICAL_ERROR = "technical_error";

        // Account blocked due to brute force protection
        public static final String ACC_BLOCKED_BFORCE_PROTECTION= "Account is blocked for 5 minutes due to 3 failed attempts.";
    }

    public static final class MSG {
        private MSG(){}
        // --- Validation, response and server error messages ---
        public static final String LOGIN_SUCCESS = "Login successful";
        public static final String INVALID_CREDENTIALS = "Invalid credentials";
        public static final String AUTH_REQUIRED = "Username and password are required.";
        public static final String PASSWORD_CHANGED = "Password changed successfully";
        public static final String PASSWORD_CHANGE_FAILED = "Password change failed with errors";


        // --- Dictionary and error messages ---
        public static final String INTERNAL_ERROR = "An unexpected error occurred. . Please try again later.";
        public static final String NOT_FOUND = "Record not found for: ";
        public static final String SUGGESTION_MALFORMED = "Please ensure your JSON structure and data types are correct.";

        // Health details
        public static final String HEALTH_UP = "Seed data source is accessible";
        public static final String HEALTH_DOWN = "Seed data file missing!";

        public static final String LOGOUT_SUCCESS = "Logout successful";
        public static final String LOGOUT_FAILED = "Logout failed";

    }




    // It can handle optional context (IDs, usernames, booleans)
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

    public static String getNotFoundMsg(String identifier) { return MSG.NOT_FOUND + identifier;}

}