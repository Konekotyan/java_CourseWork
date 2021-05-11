package ru.mirea.estore.utils;

import javax.servlet.http.HttpServletRequest;

import ru.mirea.estore.model.StoreInfo;

public class Utils {

    // Products, stored in Session.
    public static StoreInfo getStoreInSession(HttpServletRequest request) {

        StoreInfo storeInfo = (StoreInfo) request.getSession().getAttribute("myStore");


        if (storeInfo == null) {
            storeInfo = new StoreInfo();

            request.getSession().setAttribute("myStore", storeInfo);
        }

        return storeInfo;
    }

    public static void removeStoreInSession(HttpServletRequest request) {
        request.getSession().removeAttribute("myStore");
    }

    public static void storeLastOrderedStoreInSession(HttpServletRequest request, StoreInfo storeInfo) {
        request.getSession().setAttribute("lastOrderedStore", storeInfo);
    }

    public static StoreInfo getLastOrderedStoreInSession(HttpServletRequest request) {
        return (StoreInfo) request.getSession().getAttribute("lastOrderedStore");
    }

}
