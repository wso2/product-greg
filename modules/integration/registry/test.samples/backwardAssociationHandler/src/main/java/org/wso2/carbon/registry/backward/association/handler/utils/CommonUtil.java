package org.wso2.carbon.registry.backward.association.handler.utils;

public class CommonUtil {

    private static ThreadLocal<Boolean> getAssociationInProgress = new ThreadLocal<Boolean>() {
        protected Boolean initialValue() {
            return false;
        }
    };

    public static boolean isGetAssociationLockAvailable() {
        return !getAssociationInProgress.get();
    }

    public static void acquireGetAssociationLock() {
        getAssociationInProgress.set(true);
    }

    public static void releaseGetAssociationLock() {
        getAssociationInProgress.set(false);
    }

    private static ThreadLocal<Boolean> getAllAssociationInProgress = new ThreadLocal<Boolean>() {
        protected Boolean initialValue() {
            return false;
        }
    };

    public static boolean isGetAllAssociationLockAvailable() {
        return !removeAssociationInProgress.get();
    }

    public static void acquireGetAllAssociationLock() {
        removeAssociationInProgress.set(true);
    }

    public static void releaseGetAllAssociationLock() {
        removeAssociationInProgress.set(false);
    }

    private static ThreadLocal<Boolean> removeAssociationInProgress = new ThreadLocal<Boolean>() {
        protected Boolean initialValue() {
            return false;
        }
    };

    public static boolean isRemoveAssociationLockAvailable() {
        return !removeAssociationInProgress.get();
    }

    public static void acquireRemoveAssociationLock() {
        removeAssociationInProgress.set(true);
    }

    public static void releaseRemoveAssociationLock() {
        removeAssociationInProgress.set(false);
    }
}
