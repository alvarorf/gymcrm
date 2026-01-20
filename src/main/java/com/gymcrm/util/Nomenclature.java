package com.gymcrm.util;

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

    // --- Validation messages ---
    public static final String MSG_REQUIRED = "First Name and Last Name are required.";

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

    public static String getNotFoundMsg(Class<?> clazz) {
        return clazz.getSimpleName() + " not found";
    }
}